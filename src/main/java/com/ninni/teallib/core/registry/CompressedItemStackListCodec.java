package com.ninni.teallib.core.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.nio.ByteBuffer;

public final class CompressedItemStackListCodec {
    private static final int MAX_DECOMPRESSED_BYTES = 32 * 1024 * 1024;
    private static final Codec<List<ItemStack>> ITEM_STACK_LIST_CODEC = ItemStack.CODEC.listOf();

    public static Codec<List<ItemStack>> create() {
        return Codec.of(CompressedItemStackListCodec::encode, CompressedItemStackListCodec::decode);
    }

    private static <T> DataResult<T> encode(List<ItemStack> value, DynamicOps<T> ops, T prefix) {
        return ITEM_STACK_LIST_CODEC.encodeStart(ops, value).flatMap(encoded -> {
            try {
                Tag tag = ops.convertTo(NbtOps.INSTANCE, encoded);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (GZIPOutputStream gzip = new GZIPOutputStream(bytes); DataOutputStream output = new DataOutputStream(gzip)) {
                    NbtIo.writeAnyTag(tag, output);
                }
                return ops.mergeToPrimitive(prefix, ops.createByteList(ByteBuffer.wrap(bytes.toByteArray())));
            } catch (IOException exception) {
                return DataResult.error(() -> "Could not compress item stack variants: " + exception.getMessage());
            }
        });
    }

    private static <T> DataResult<Pair<List<ItemStack>, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<ByteBuffer> encodedBytes = ops.getByteBuffer(input);
        if (encodedBytes.result().isEmpty()) return ITEM_STACK_LIST_CODEC.decode(ops, input);
        return encodedBytes.flatMap(buffer -> {
            try {
                ByteBuffer view = buffer.slice();
                byte[] compressed = new byte[view.remaining()];
                view.get(compressed);
                ByteArrayOutputStream expanded = new ByteArrayOutputStream();
                try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                    byte[] chunk = new byte[8192];
                    int read;
                    while ((read = gzip.read(chunk)) != -1) {
                        if (expanded.size() + read > MAX_DECOMPRESSED_BYTES) return DataResult.error(() -> "Compressed item stack variants exceed the decompression limit");
                        expanded.write(chunk, 0, read);
                    }
                }
                Tag tag;
                try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(expanded.toByteArray()))) {
                    tag = NbtIo.readAnyTag(inputStream, NbtAccounter.unlimitedHeap());
                }
                T converted = NbtOps.INSTANCE.convertTo(ops, tag);
                return ITEM_STACK_LIST_CODEC.parse(ops, converted).map(value -> Pair.of(value, input));
            } catch (IOException | RuntimeException exception) {
                return DataResult.error(() -> "Could not decompress item stack variants: " + exception.getMessage());
            }
        });
    }

    private CompressedItemStackListCodec() {
    }
}

package com.ninni.teallib.api.common.block.entity;

import com.ninni.teallib.api.common.data.blockvariant.BlockVariantManager;
import com.ninni.teallib.api.common.network.BlockEntitySyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class VariantHolderBlockEntity extends BlockEntity implements Nameable {
    private ResourceLocation variant;
    private Component customName;

    protected VariantHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract ResourceLocation getDefaultVariant();

    public void setVariant(ResourceLocation id) {
        this.variant = id;
        sync();
    }
    public boolean hasVariant() {
        return variant != null;
    }
    public ResourceLocation getVariant() {
        return variant != null ? variant : getDefaultVariant();
    }

    public void ensureVariantAssigned() {
        assert level != null;
        if (!level.isClientSide) {
            if (!BlockVariantManager.isValidVariant(this, variant)) {
                BlockVariantManager.assignNaturally(this);
                sync();
            }
        }
    }

    public BlockEntityType<?> getVariantType() {
        return super.getType();
    }

    @Override
    public @NotNull Component getName() {
        return customName != null ? customName : getDefaultName();
    }
    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }
    @Override
    public Component getCustomName() {
        return customName;
    }
    public void setCustomName(Component name) {
        this.customName = name;
        sync();
    }
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Variant", 8)) {
            this.variant = ResourceLocation.parse(tag.getString("Variant"));
        }
        if (tag.contains("CustomName", 8)) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (getVariantType() != getType()) tag.putString("VariantId", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(getVariantType()).toString());
        if (variant != null) tag.putString("Variant", variant.toString());
        if (customName != null) tag.putString("CustomName", Component.Serializer.toJson(customName, registries));
    }

    public void sync() {
        setChanged();
        if (level == null) return;
        if (!level.isClientSide()) {
            CompoundTag tag = new CompoundTag();
            saveAdditional(tag, level.registryAccess());
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), new BlockEntitySyncPacket(worldPosition, tag));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (shouldAssignVariantOnLoad()) {
            ensureVariantAssigned();
        }
    }

    public boolean shouldAssignVariantOnLoad() {
        return true;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        super.onDataPacket(net, pkt, registries);
        handleUpdateTag(pkt.getTag(), registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
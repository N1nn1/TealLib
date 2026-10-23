package com.ninni.teallib.api.common.item;

import com.ninni.teallib.core.registry.TealDataComponentTypes;
import com.ninni.teallib.core.registry.TealTags;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ComponentStacking {
    private static final ThreadLocal<MenuTransaction> ACTIVE_TRANSACTION = new ThreadLocal<>();
    private static final ThreadLocal<EntityMergeSnapshot> ACTIVE_ENTITY_MERGE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> SUPPRESS_COUNT_HOOK = ThreadLocal.withInitial(() -> 0);

    private static DataComponentType<List<ItemStack>> variantsType() {
        return TealDataComponentTypes.STACK_VARIANTS.get();
    }

    private static DataComponentType<Integer> selectedIndexType() {
        return TealDataComponentTypes.STACK_VARIANT_INDEX.get();
    }

    private static int selectedIndex(ItemStack stack, int size) {
        if (size <= 0 || stack == null || !stack.has(selectedIndexType())) return 0;
        Integer index = stack.get(selectedIndexType());
        if (index == null) return 0;
        return Math.floorMod(index, size);
    }

    public static boolean isConfigured(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(TealTags.COMPONENT_STACKABLE);
    }

    private static boolean hasVariants(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.has(variantsType());
    }

    public static boolean interceptComponentComparison(ItemStack first, ItemStack second) {
        return first != null && second != null && !first.isEmpty() && !second.isEmpty() && first.getItem() == second.getItem() && isConfigured(first) && isConfigured(second);
    }

    public static void beginMenuTransaction(AbstractContainerMenu menu) {
        List<StackAccess> accesses = new ArrayList<>(menu.slots.size() + 1);
        for (int i = 0; i < menu.slots.size(); i++) {
            int slotIndex = i;
            accesses.add(() -> menu.slots.get(slotIndex).getItem());
        }
        accesses.add(menu::getCarried);
        beginTransaction(accesses);
    }

    public static void beginInventoryTransaction(Inventory inventory, ItemStack source) {
        List<StackAccess> accesses = new ArrayList<>(inventory.getContainerSize() + 1);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int slotIndex = i;
            accesses.add(() -> inventory.getItem(slotIndex));
        }
        accesses.add(() -> source);
        beginTransaction(accesses);
    }

    private static void beginTransaction(List<StackAccess> accesses) {
        MenuTransaction active = ACTIVE_TRANSACTION.get();
        if (active != null) {
            active.depth++;
            return;
        }
        List<StackSnapshot> snapshots = new ArrayList<>(accesses.size());
        IdentityHashMap<ItemStack, Boolean> seen = new IdentityHashMap<>();
        for (StackAccess access : accesses) {
            ItemStack stack = access.get();
            if (stack != null && !stack.isEmpty() && seen.put(stack, Boolean.TRUE) != null) continue;
            snapshots.add(new StackSnapshot(access, stack));
        }
        ACTIVE_TRANSACTION.set(new MenuTransaction(snapshots));
    }

    public static void endTransaction() {
        MenuTransaction transaction = ACTIVE_TRANSACTION.get();
        if (transaction == null) return;

        transaction.depth--;
        if (transaction.depth > 0) return;

        ACTIVE_TRANSACTION.remove();

        Map<Item, List<ItemStack>> outgoing = new IdentityHashMap<>();
        List<FinalStack> finals = new ArrayList<>(transaction.snapshots.size());

        for (StackSnapshot snapshot : transaction.snapshots) {
            ItemStack current = snapshot.access.get();
            boolean currentTracked = isConfigured(current);
            Item currentItem = currentTracked ? current.getItem() : null;
            int currentCount = currentTracked ? current.getCount() : 0;

            boolean sameItem = snapshot.tracked && currentTracked && snapshot.item == currentItem;
            int remainingBeforeUnits = sameItem ? Math.min(snapshot.count, currentCount) : 0;
            int lost = snapshot.tracked ? snapshot.count - remainingBeforeUnits : 0;

            if (lost > 0) {
                outgoing.computeIfAbsent(snapshot.item, item -> new ArrayList<>()).addAll(copyRange(snapshot.variants, snapshot.count - lost, snapshot.count));
            }

            List<ItemStack> baseUnits = sameItem ? copyRange(snapshot.variants, 0, remainingBeforeUnits) : new ArrayList<>();
            int gained = currentTracked ? Math.max(0, currentCount - (sameItem ? snapshot.count : 0)) : 0;

            finals.add(new FinalStack(current, currentItem, currentCount, currentTracked, baseUnits, gained, sameItem ? snapshot.selectedIndex : 0));
        }

        Map<Item, Integer> cursors = new IdentityHashMap<>();

        for (FinalStack target : finals) {
            if (!target.tracked || target.stack == null || target.stack.isEmpty()) continue;
            if (target.count == 1 && !hasVariants(target.stack)) continue;

            List<ItemStack> assigned = new ArrayList<>(target.baseUnits);
            List<ItemStack> pool = outgoing.get(target.item);
            int cursor = cursors.getOrDefault(target.item, 0);

            while (target.gained > 0 && pool != null && cursor < pool.size()) {
                assigned.add(pool.get(cursor++).copy());
                target.gained--;
            }

            cursors.put(target.item, cursor);
            ItemStack fallback = assigned.isEmpty() ? visibleUnitSnapshot(target.stack) : assigned.getFirst();

            while (assigned.size() < target.count) {
                assigned.add(fallback.copy());
            }

            if (assigned.size() > target.count) assigned = new ArrayList<>(assigned.subList(0, target.count));
            writeVariants(target.stack, assigned, target.count, target.selectedIndex);
        }
    }

    public static boolean isTransactionActive() {
        return ACTIVE_TRANSACTION.get() != null;
    }

    public static void beginEntityMerge(ItemEntity destinationEntity, ItemStack destination, ItemEntity originEntity, ItemStack origin) {
        if (destinationEntity == null || originEntity == null || destination == null || origin == null || destination.isEmpty() || origin.isEmpty() || destination.getItem() != origin.getItem() || !isConfigured(destination) || !isConfigured(origin)) {
            ACTIVE_ENTITY_MERGE.remove();
            return;
        }
        ACTIVE_ENTITY_MERGE.set(new EntityMergeSnapshot(destinationEntity, originEntity, destination.getCount(), origin.getCount(), variantsForCount(destination, destination.getCount()), variantsForCount(origin, origin.getCount())));
    }

    public static void endEntityMerge(ItemEntity destinationEntity, ItemEntity originEntity) {
        EntityMergeSnapshot snapshot = ACTIVE_ENTITY_MERGE.get();
        ACTIVE_ENTITY_MERGE.remove();
        if (snapshot == null || snapshot.destinationEntity != destinationEntity || snapshot.originEntity != originEntity) return;
        ItemStack destination = destinationEntity.getItem();
        ItemStack origin = originEntity.getItem();
        int destinationCount = destination.isEmpty() ? 0 : destination.getCount();
        int originCount = origin.isEmpty() ? 0 : origin.getCount();
        int moved = Math.max(0, Math.min(snapshot.originCount, destinationCount - snapshot.destinationCount));
        if (moved == 0 || !isConfigured(destination)) return;
        List<ItemStack> destinationUnits = copyRange(snapshot.destinationVariants, 0, Math.min(snapshot.destinationCount, destinationCount));
        destinationUnits.addAll(copyRange(snapshot.originVariants, Math.max(0, snapshot.originCount - moved), snapshot.originCount));
        writeVariants(destination, destinationUnits, destinationCount, selectedIndex(destination, snapshot.destinationVariants.size()));
        destinationEntity.setItem(destination);
        if (originCount > 0 && isConfigured(origin)) {
            writeVariants(origin, copyRange(snapshot.originVariants, 0, Math.min(originCount, snapshot.originVariants.size())), originCount, selectedIndex(origin, snapshot.originVariants.size()));
            originEntity.setItem(origin);
        }
    }


    public static void beforeCountChange(ItemStack stack, int newCount) {
        if (isTransactionActive() || ACTIVE_ENTITY_MERGE.get() != null || SUPPRESS_COUNT_HOOK.get() > 0 || !hasVariants(stack)) return;

        int oldCount = stack.getCount();
        if (newCount >= oldCount) return;
        if (newCount <= 0) return;

        List<ItemStack> before = variantsForCount(stack, oldCount);
        int consumed = Math.min(oldCount - newCount, before.size());
        int selected = selectedIndex(stack, before.size());

        List<ItemStack> remaining = new ArrayList<>(before);

        for (int i = 0; i < consumed && !remaining.isEmpty(); i++) {
            remaining.remove(Math.floorMod(selected, remaining.size()));
        }
        writeVariants(stack, remaining, newCount, remaining.isEmpty() ? 0 : Math.min(selected, remaining.size() - 1));
    }


    public static boolean hasMultipleVariants(ItemStack stack) {
        return isConfigured(stack) && stack.getCount() > 1 && hasVariants(stack);
    }

    public static boolean cycleVariants(ItemStack stack, int direction, int step) {
        if (!hasMultipleVariants(stack) || direction == 0) return false;

        List<ItemStack> variants = variantsForCount(stack, stack.getCount());
        int selected = Math.floorMod(selectedIndex(stack, variants.size()) + Integer.signum(direction) * Math.max(1, step), variants.size());

        writeVariants(stack, variants, stack.getCount(), selected);
        return true;
    }

    public static boolean cycleMenuSlot(AbstractContainerMenu menu, int slotIndex, int direction, int step) {
        if (menu == null || slotIndex < 0 || slotIndex >= menu.slots.size()) return false;
        return cycleVariants(menu.slots.get(slotIndex).getItem(), direction, step);
    }


    public static ItemStack splitIfTracked(ItemStack stack, int requestedAmount) {
        if (isTransactionActive() || !hasVariants(stack)) return null;

        int oldCount = stack.getCount();
        int amount = Math.max(0, Math.min(requestedAmount, oldCount));

        if (amount == 0) return ItemStack.EMPTY;

        List<ItemStack> before = variantsForCount(stack, oldCount);
        int selected = selectedIndex(stack, before.size());

        List<ItemStack> remaining = new ArrayList<>(before);
        List<ItemStack> moved = new ArrayList<>();

        for (int i = 0; i < amount && !remaining.isEmpty(); i++) {
            moved.add(remaining.remove(Math.floorMod(selected, remaining.size())));
        }
        ItemStack result = new ItemStack(stack.getItem(), amount);
        writeVariants(result, moved, amount, 0);
        int remainingCount = oldCount - amount;
        setCountWithoutTracking(stack, remainingCount);
        writeVariants(stack, remaining, remainingCount, remaining.isEmpty() ? 0 : Math.min(selected, remaining.size() - 1));
        return result;
    }


    public static ItemStack copyWithCountIfTracked(ItemStack source, int requestedCount) {
        if (!hasVariants(source) || isTransactionActive()) return null;
        int count = Math.max(0, requestedCount);
        if (count == 0) return ItemStack.EMPTY;
        List<ItemStack> all = variantsForCount(source, source.getCount());
        int selected = selectedIndex(source, all.size());
        List<ItemStack> copied = copyRange(all, 0, Math.min(count, all.size()));
        ItemStack fallback = all.isEmpty() ? visibleUnitSnapshot(source) : all.getFirst();
        while (copied.size() < count) copied.add(fallback.copy());
        ItemStack result = new ItemStack(source.getItem(), count);
        writeVariants(result, copied, count, selected < count ? selected : 0);
        return result;
    }


    private static void setCountWithoutTracking(ItemStack stack, int count) {
        SUPPRESS_COUNT_HOOK.set(SUPPRESS_COUNT_HOOK.get() + 1);
        try {
            stack.setCount(count);
        } finally {
            int depth = SUPPRESS_COUNT_HOOK.get() - 1;
            if (depth <= 0) SUPPRESS_COUNT_HOOK.remove();
            else SUPPRESS_COUNT_HOOK.set(depth);
        }
    }

    private static List<ItemStack> variantsForCount(ItemStack stack, int count) {
        int safeCount = Math.max(0, count);
        List<ItemStack> stored = stack.get(variantsType());
        List<ItemStack> result = new ArrayList<>(safeCount);
        if (stored != null) {
            for (ItemStack variant : stored) result.add(visibleUnitSnapshot(variant));
        }
        ItemStack fallback = result.isEmpty() ? visibleUnitSnapshot(stack) : result.getFirst();
        while (result.size() < safeCount) result.add(fallback.copy());
        if (result.size() > safeCount) result = new ArrayList<>(result.subList(0, safeCount));
        return result;
    }

    private static ItemStack visibleUnitSnapshot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        copy.remove(variantsType());
        copy.remove(selectedIndexType());
        copy.setCount(1);
        return copy;
    }

    private static List<ItemStack> copyRange(List<ItemStack> source, int from, int to) {
        int start = Math.clamp(from, 0, source.size());
        int end = Math.clamp(to, start, source.size());
        List<ItemStack> result = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) result.add(visibleUnitSnapshot(source.get(i)));
        return result;
    }

    private static void writeVariants(ItemStack stack, List<ItemStack> variants, int resultingCount) {
        writeVariants(stack, variants, resultingCount, 0);
    }

    private static void writeVariants(ItemStack stack, List<ItemStack> variants, int resultingCount, int requestedSelectedIndex) {
        int count = Math.max(0, resultingCount);

        if (stack == null || count == 0 || variants.isEmpty()) {
            if (stack != null) {
                stack.remove(variantsType());
                stack.remove(selectedIndexType());
            }
            return;
        }

        List<ItemStack> fitted = copyRange(variants, 0, Math.min(count, variants.size()));
        ItemStack fallback = visibleUnitSnapshot(fitted.getFirst());

        while (fitted.size() < count) {
            fitted.add(fallback.copy());
        }

        int selected = Math.floorMod(requestedSelectedIndex, fitted.size());
        ItemStack representative = fitted.get(selected);

        Component customName = representative.get(DataComponents.CUSTOM_NAME);
        stack.applyComponents(representative.getComponents());

        if (customName == null) stack.remove(DataComponents.CUSTOM_NAME);
        else stack.set(DataComponents.CUSTOM_NAME, customName);

        if (count > 1) {
            stack.set(variantsType(), List.copyOf(fitted));
            stack.set(selectedIndexType(), selected);
        } else {
            stack.remove(variantsType());
            stack.remove(selectedIndexType());
        }
    }

    private interface StackAccess {
        ItemStack get();
    }

    private static final class StackSnapshot {
        private final StackAccess access;
        private final Item item;
        private final int count;
        private final boolean tracked;
        private final List<ItemStack> variants;
        private final int selectedIndex;

        private StackSnapshot(StackAccess access, ItemStack stack) {
            this.access = access;
            this.tracked = isConfigured(stack);
            this.item = this.tracked ? stack.getItem() : null;
            this.count = this.tracked ? stack.getCount() : 0;
            this.variants = this.tracked ? variantsForCount(stack, this.count) : new ArrayList<>();
            this.selectedIndex = this.tracked ? selectedIndex(stack, this.variants.size()) : 0;
        }
    }

    private static final class FinalStack {
        private final ItemStack stack;
        private final Item item;
        private final int count;
        private final boolean tracked;
        private final List<ItemStack> baseUnits;
        private int gained;
        private final int selectedIndex;

        private FinalStack(ItemStack stack, Item item, int count, boolean tracked, List<ItemStack> baseUnits, int gained, int selectedIndex) {
            this.stack = stack;
            this.item = item;
            this.count = count;
            this.tracked = tracked;
            this.baseUnits = baseUnits;
            this.gained = gained;
            this.selectedIndex = selectedIndex;
        }
    }

    private static final class MenuTransaction {
        private final List<StackSnapshot> snapshots;
        private int depth = 1;

        private MenuTransaction(List<StackSnapshot> snapshots) {
            this.snapshots = snapshots;
        }
    }

    private record EntityMergeSnapshot(ItemEntity destinationEntity, ItemEntity originEntity, int destinationCount, int originCount, List<ItemStack> destinationVariants, List<ItemStack> originVariants) { }
}

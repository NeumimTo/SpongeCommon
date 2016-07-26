/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Multimap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.phase.ItemDropData;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Similar to {@link Cause} except it can be built continuously
 * and retains no real side effects. Strictly speaking this object
 * exists to avoid confusion between what is suggested to be a
 * {@link Cause} for an {@link Event} versus the context of which
 * a {@link IPhaseState} is being completed with.
 */
public class PhaseContext {

    private boolean isCompleted = false;
    private final ArrayList<NamedCause> contextObjects = new ArrayList<>(10);
    @Nullable private Cause cause = null;

    @Nullable private CapturedBlocksSupplier blocksSupplier;
    @Nullable private BlockItemDropsSupplier blockItemDropsSupplier;
    @Nullable private BlockItemEntityDropsSupplier blockItemEntityDropsSupplier;
    @Nullable private CapturedItemsSupplier capturedItemsSupplier;
    @Nullable private CapturedEntitiesSupplier capturedEntitiesSupplier;
    @Nullable private CapturedItemStackSupplier capturedItemStackSupplier;
    @Nullable private EntityItemDropsSupplier entityItemDropsSupplier;
    @Nullable private EntityItemEntityDropsSupplier entityItemEntityDropsSupplier;
    private Object source;

    public static PhaseContext start() {
        return new PhaseContext();
    }

    public PhaseContext add(@Nullable NamedCause namedCause) {
        if (namedCause == null) {
            return this;
        }
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(namedCause);
        if (namedCause.getName().equals(NamedCause.SOURCE)) {
            this.source = namedCause.getCauseObject();
        }
        return this;
    }

    public PhaseContext addBlockCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        CapturedBlocksSupplier blocksSupplier = new CapturedBlocksSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, blocksSupplier));
        this.blocksSupplier = blocksSupplier;
        BlockItemEntityDropsSupplier blockItemEntityDropsSupplier = new BlockItemEntityDropsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_ITEM_DROPS, blockItemEntityDropsSupplier));
        this.blockItemEntityDropsSupplier = blockItemEntityDropsSupplier;
        BlockItemDropsSupplier blockItemDropsSupplier = new BlockItemDropsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, blockItemDropsSupplier));
        this.blockItemDropsSupplier = blockItemDropsSupplier;
        return this;
    }

    public PhaseContext addCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        CapturedBlocksSupplier blocksSupplier = new CapturedBlocksSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, blocksSupplier));
        this.blocksSupplier = blocksSupplier;
        BlockItemEntityDropsSupplier blockItemEntityDropsSupplier = new BlockItemEntityDropsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_ITEM_DROPS, blockItemEntityDropsSupplier));
        this.blockItemEntityDropsSupplier = blockItemEntityDropsSupplier;
        BlockItemDropsSupplier blockItemDropsSupplier = new BlockItemDropsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, blockItemDropsSupplier));
        this.blockItemDropsSupplier = blockItemDropsSupplier;
        CapturedItemsSupplier capturedItemsSupplier = new CapturedItemsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEMS, capturedItemsSupplier));
        this.capturedItemsSupplier = capturedItemsSupplier;
        CapturedEntitiesSupplier capturedEntitiesSupplier = new CapturedEntitiesSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, capturedEntitiesSupplier));
        this.capturedEntitiesSupplier = capturedEntitiesSupplier;
        CapturedItemStackSupplier capturedItemStackSupplier = new CapturedItemStackSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEM_STACKS, capturedItemStackSupplier));
        this.capturedItemStackSupplier = capturedItemStackSupplier;
        return this;
    }

    public PhaseContext addEntityCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        CapturedItemsSupplier capturedItemsSupplier = new CapturedItemsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEMS, capturedItemsSupplier));
        this.capturedItemsSupplier = capturedItemsSupplier;
        CapturedEntitiesSupplier capturedEntitiesSupplier = new CapturedEntitiesSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, capturedEntitiesSupplier));
        this.capturedEntitiesSupplier = capturedEntitiesSupplier;
        CapturedItemStackSupplier capturedItemStackSupplier = new CapturedItemStackSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEM_STACKS, capturedItemStackSupplier));
        this.capturedItemStackSupplier = capturedItemStackSupplier;
        return this;
    }

    public PhaseContext addEntityDropCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        EntityItemDropsSupplier entityItemDropsSupplier = new EntityItemDropsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITY_STACK_DROPS, entityItemDropsSupplier));
        this.entityItemDropsSupplier = entityItemDropsSupplier;
        EntityItemEntityDropsSupplier entityItemEntityDropsSupplier = new EntityItemEntityDropsSupplier();
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITY_ITEM_DROPS, entityItemEntityDropsSupplier));
        this.entityItemEntityDropsSupplier = entityItemEntityDropsSupplier;
        return this;
    }

    public PhaseContext player() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_PLAYER, new CapturePlayer()));
        return this;
    }

    public PhaseContext player(@Nullable Player player) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_PLAYER, new CapturePlayer(player)));
        return this;
    }

    public PhaseContext complete() {
        this.isCompleted = true;
        return this;
    }

    public boolean isComplete() {
        return this.isCompleted;
    }

    @Nullable private Class<?> cachedClass;
    @Nullable private Object cachedObject;
    @Nullable private String cachedName;

    @SuppressWarnings("unchecked")
    public <T> Optional<T> first(Class<T> tClass) {
        if (this.cachedClass != null && this.cachedClass == tClass) {
            if (this.cachedObject != null) {
                return Optional.of((T) this.cachedObject);
            }
        }
        for (NamedCause cause : this.contextObjects) {
            if (tClass.isInstance(cause.getCauseObject())) {
                Object causeObject = cause.getCauseObject();
                this.cachedClass = tClass;
                this.cachedObject = causeObject;
                this.cachedName = cause.getName();
                return Optional.of((T) causeObject);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> firstNamed(String name, Class<T> tClass) {
        if (name.equals(this.cachedName) && tClass == this.cachedClass) {
            if (this.cachedObject != null) {
                return Optional.of((T) this.cachedObject);
            }
        }
        for (NamedCause cause : this.contextObjects) {
            if (cause.getName().equalsIgnoreCase(name) && tClass.isInstance(cause.getCauseObject())) {
                this.cachedObject = cause.getCauseObject();
                this.cachedClass = tClass;
                this.cachedName = name;
                return Optional.of((T) cause.getCauseObject());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getSource(Class<T> sourceClass) {
        if (this.source == null) {
            return Optional.empty();
        }
        if (sourceClass.isInstance(this.source)) {
            return Optional.of((T) this.source);
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Entity> getCapturedEntities() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, CapturedEntitiesSupplier.class)
                .map(CapturedEntitiesSupplier::get)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture entity spawns!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<Entity> getCapturedEntitySupplier() throws IllegalStateException {
        if (this.capturedEntitiesSupplier == null) {
            throw PhaseUtil.throwWithContext("Intended to capture entity spawns!", this).get();
        }
        return this.capturedEntitiesSupplier;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<EntityItem> getCapturedItems() throws IllegalStateException {
        if (this.capturedItemsSupplier == null) {
            throw PhaseUtil.throwWithContext("Intended to capture dropped item entities!", this).get();
        }
        return this.capturedItemsSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<EntityItem> getCapturedItemsSupplier() throws IllegalStateException {
        if (this.capturedItemsSupplier == null) {
            throw PhaseUtil.throwWithContext("Intended to capture dropped item entities!", this).get();
        }
        return this.capturedItemsSupplier;
    }

    @SuppressWarnings("unchecked")
    public List<BlockSnapshot> getCapturedBlocks() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, CapturedBlocksSupplier.class)
                .map(CapturedBlocksSupplier::get)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture block changes, but there is no list available!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<BlockSnapshot> getCapturedBlockSupplier() throws IllegalStateException {
        if (this.blocksSupplier == null) {
            throw PhaseUtil.throwWithContext("Expected to be capturing blocks, but we're not capturing them!", this).get();
        }
        return this.blocksSupplier;
    }

    public Multimap<BlockPos, ItemDropData> getCapturedBlockDrops() throws IllegalStateException {
        if (this.blockItemDropsSupplier == null) {
            throw PhaseUtil.throwWithContext("Expected to be capturing block drops!", this).get();
        }
        return this.blockItemDropsSupplier.get();
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<BlockPos, ItemDropData> getBlockDropSupplier() throws IllegalStateException {
        if (this.blockItemDropsSupplier == null) {
            throw PhaseUtil.throwWithContext("Expected to be capturing block drops!", this).get();
        }
        return this.blockItemDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<BlockPos, EntityItem> getBlockItemDropSupplier() throws IllegalStateException {
        if (this.blockItemEntityDropsSupplier == null) {
            throw PhaseUtil.throwWithContext("Intended to track block item drops!", this).get();
        }
        return this.blockItemEntityDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<UUID, ItemDropData> getCapturedEntityDropSupplier() throws IllegalStateException {
        if (this.entityItemDropsSupplier == null) {
            throw PhaseUtil.throwWithContext("Intended to capture entity drops!", this).get();
        }
        return this.entityItemDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<UUID, EntityItem> getCapturedEntityItemDropSupplier() throws IllegalStateException {
        if (this.entityItemEntityDropsSupplier == null) {
            throw PhaseUtil.throwWithContext("Intended to capture entity drops!", this).get();
        }
        return this.entityItemEntityDropsSupplier;
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<ItemDropData> getCapturedItemStackSupplier() throws IllegalStateException {
        if (this.capturedItemStackSupplier == null) {
            throw PhaseUtil.throwWithContext("Expected to be capturing ItemStack drops from entities!", this).get();
        }
        return this.capturedItemStackSupplier;
    }

    public CapturePlayer getCapturedPlayerSupplier() throws IllegalStateException {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_PLAYER, CapturePlayer.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a Player from an event listener, but we're not capturing them!", this));
    }

    public Optional<Player> getCapturedPlayer() throws IllegalStateException {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_PLAYER, CapturePlayer.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a Player from an event listener, but we're not capturing them!", this))
                .getPlayer();
    }

    public void forEach(Consumer<NamedCause> consumer) {
        this.contextObjects.forEach(consumer);
    }

    PhaseContext() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isCompleted, this.contextObjects, this.cause);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PhaseContext other = (PhaseContext) obj;
        return Objects.equals(this.isCompleted, other.isCompleted)
               && Objects.equals(this.contextObjects, other.contextObjects)
               && Objects.equals(this.cause, other.cause);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("isCompleted", this.isCompleted)
                .add("contextObjects", this.contextObjects)
                .add("cause", this.cause)
                .toString();
    }

    static class BlockItemDropsSupplier extends CapturedMultiMapSupplier<BlockPos, ItemDropData> {

        BlockItemDropsSupplier() {
        }

    }

    static class EntityItemDropsSupplier extends CapturedMultiMapSupplier<UUID, ItemDropData> {

        EntityItemDropsSupplier() {
        }
    }

    static final class CapturedItemsSupplier extends CapturedSupplier<EntityItem> {

        CapturedItemsSupplier() {
        }
    }

    static final class CapturedItemStackSupplier extends CapturedSupplier<ItemDropData> {

        CapturedItemStackSupplier() {
        }
    }

    static final class CapturedBlocksSupplier extends CapturedSupplier<BlockSnapshot> {

        CapturedBlocksSupplier() {
        }
    }

    static final class CapturedEntitiesSupplier extends CapturedSupplier<Entity> {

        CapturedEntitiesSupplier() {
        }
    }

    static final class EntityItemEntityDropsSupplier extends CapturedMultiMapSupplier<UUID, EntityItem> {

        EntityItemEntityDropsSupplier() {
        }
    }

    static final class BlockItemEntityDropsSupplier extends CapturedMultiMapSupplier<BlockPos, EntityItem> {
        BlockItemEntityDropsSupplier() {
        }
    }

    public static final class CapturePlayer {

        @Nullable private Player player;

        CapturePlayer() {

        }

        CapturePlayer(@Nullable Player player) {
            this.player = player;
        }

        public Optional<Player> getPlayer() {
            return Optional.ofNullable(this.player);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CapturePlayer that = (CapturePlayer) o;
            return com.google.common.base.Objects.equal(this.player, that.player);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.player);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("player", this.player)
                    .toString();
        }

        public void addPlayer(EntityPlayerMP playerMP) {
            this.player = ((Player) playerMP);
        }
    }


    public static final class CaptureFlag {

        @Nullable private BlockChangeFlag flag;

        public CaptureFlag() {

        }

        public CaptureFlag(@Nullable BlockChangeFlag flag) {

        }

        public Optional<BlockChangeFlag> getFlag() {
            return Optional.ofNullable(this.flag);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CaptureFlag that = (CaptureFlag) o;
            return flag == that.flag;
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(flag);
        }

        public void addFlag(BlockChangeFlag flag) {
            this.flag = flag;
        }
    }
}
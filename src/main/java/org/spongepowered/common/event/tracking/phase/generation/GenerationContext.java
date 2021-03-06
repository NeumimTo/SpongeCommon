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
package org.spongepowered.common.event.tracking.phase.generation;

import net.minecraft.world.WorldServer;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

public class GenerationContext<G extends GenerationContext<G>> extends PhaseContext<G> {

    private World world;

    GenerationContext(IPhaseState<? extends G> state) {
        super(state);
    }

    @SuppressWarnings("unchecked")
    public G world(net.minecraft.world.World world) {
        this.world = (World) world;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G world(World world) {
        this.world = world;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G world(IMixinWorldServer world) {
        this.world = world.asSpongeWorld();
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G world(WorldServer worldServer) {
        this.world = (World) worldServer;
        return (G) this;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public PrettyPrinter printCustom(PrettyPrinter printer) {
        return super.printCustom(printer)
            .add("    - %s: %s", "World", this.world);
    }
}

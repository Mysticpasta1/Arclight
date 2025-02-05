package io.izzel.arclight.common.mixin.core.world.damagesource;

import io.izzel.arclight.common.bridge.core.util.DamageSourceBridge;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin implements DamageSourceBridge {

    // @formatter:off
    @Shadow @Nullable public Entity getEntity() { return null; }
    // @formatter:on

    private boolean sweep;
    private boolean melting;
    private boolean poison;

    public boolean isSweep() {
        return sweep;
    }

    @Override
    public boolean bridge$isSweep() {
        return isSweep();
    }

    public DamageSource sweep() {
        sweep = true;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource bridge$sweep() {
        return sweep();
    }

    public boolean isMelting() {
        return melting;
    }

    public DamageSource melting() {
        this.melting = true;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource bridge$melting() {
        return melting();
    }

    public boolean isPoison() {
        return poison;
    }

    public DamageSource poison() {
        this.poison = true;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource bridge$poison() {
        return poison();
    }
}

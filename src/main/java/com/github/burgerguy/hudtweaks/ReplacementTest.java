package com.github.burgerguy.hudtweaks;

import com.github.burgerguy.hudtweaks.api.HudTweaksApi;
import com.github.burgerguy.hudtweaks.api.PlaceholderName;
import com.github.burgerguy.hudtweaks.config.annotations.Configurable;
import com.github.burgerguy.hudtweaks.hud.HTIdentifier;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.MinecraftClient;

import java.util.Collection;

// temporary class
// todo: move to a test mod or something - remember to remove it from fabric.mod.json
public class ReplacementTest implements HudTweaksApi {
    @Override
    public Collection<PlaceholderName> getCustomElementEntries() {
        return ImmutableSet.of(new SomeReplacement());
    }
}

class SomeReplacement extends PlaceholderName {
    @Configurable
    public double prettiness = 1.0;

    protected SomeReplacement() {
        super(new HTIdentifier("test", "health", "square"), "onHealthRowsChange");
    }

    @Override
    protected double calculateWidth(MinecraftClient client) {
        return 20;
    }

    @Override
    protected double calculateHeight(MinecraftClient client) {
        return 20;
    }

    @Override
    protected double calculateDefaultX(MinecraftClient client) {
        return 100;
    }

    @Override
    protected double calculateDefaultY(MinecraftClient client) {
        return 100;
    }
}
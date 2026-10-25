package dev.sunflare.advancement;

import dev.sunflare.Sunflare;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTriggers {

    public static final DeferredRegister<net.minecraft.advancements.CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, Sunflare.MOD_ID);

    public static final DeferredHolder<net.minecraft.advancements.CriterionTrigger<?>, SunflareTrigger> EVENT =
            TRIGGERS.register("event", SunflareTrigger::new);

    private ModTriggers() {
    }
}

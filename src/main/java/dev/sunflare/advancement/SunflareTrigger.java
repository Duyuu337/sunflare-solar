package dev.sunflare.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

/**
 * 统一的自定义触发器：一个 code 字符串对应一个成就条件。
 */
public class SunflareTrigger extends SimpleCriterionTrigger<SunflareTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void fire(ServerPlayer player, String code) {
        this.trigger(player, instance -> instance.code().equals(code));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, String code) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.fieldOf("code").forGetter(TriggerInstance::code)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> of(String code) {
            return ModTriggers.EVENT.get().createCriterion(new TriggerInstance(Optional.empty(), code));
        }
    }

    /** 便于 JSON 与代码共用同一批字符串。 */
    public static final class Codes {
        public static final String GOVERNOR_ZERO = "governor_zero";
        public static final String GOVERNOR_ONLINE = "governor_online";
        public static final String BUS_CUT = "bus_cut";
        public static final String ANCHOR_THREE_END = "anchor_three_end";
        public static final String CROSS_DIM_CHARGE = "cross_dim_charge";

        private Codes() {
        }
    }
}

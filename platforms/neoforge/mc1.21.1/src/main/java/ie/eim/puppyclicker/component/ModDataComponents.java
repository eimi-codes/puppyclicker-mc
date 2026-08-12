package ie.eim.puppyclicker.component;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the per-stack data needed to distinguish clickers for different friends. */
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    PuppyClickerMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BoundFriend>> BOUND_FRIEND =
            COMPONENTS.registerComponentType(
                    "bound_friend",
                    // Persistent keeps the binding across saves; synchronized exposes it to clients.
                    builder -> builder
                            .persistent(BoundFriend.CODEC)
                            .networkSynchronized(BoundFriend.STREAM_CODEC));

    private ModDataComponents() {
    }
}

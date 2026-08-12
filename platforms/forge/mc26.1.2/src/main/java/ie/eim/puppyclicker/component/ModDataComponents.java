package ie.eim.puppyclicker.component;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Registers the per-stack data needed to distinguish clickers for different friends. */
public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PuppyClickerMod.MOD_ID);

    public static final RegistryObject<DataComponentType<BoundFriend>> BOUND_FRIEND =
            COMPONENTS.register(
                    "bound_friend",
                    // Persistent keeps the binding across saves; synchronized exposes it to clients.
                    () -> DataComponentType.<BoundFriend>builder()
                            .persistent(BoundFriend.CODEC)
                            .networkSynchronized(BoundFriend.STREAM_CODEC)
                            .build());

    private ModDataComponents() {
    }
}

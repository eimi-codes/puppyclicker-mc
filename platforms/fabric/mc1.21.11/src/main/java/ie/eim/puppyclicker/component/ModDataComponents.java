package ie.eim.puppyclicker.component;

import java.util.function.Supplier;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Registers the persistent, synchronized friend identity stored on each clicker. */
public final class ModDataComponents {
    private static final DataComponentType<BoundFriend> BOUND_FRIEND_TYPE =
            DataComponentType.<BoundFriend>builder()
                    .persistent(BoundFriend.CODEC)
                    .networkSynchronized(BoundFriend.STREAM_CODEC)
                    .build();
    public static final Supplier<DataComponentType<BoundFriend>> BOUND_FRIEND =
            () -> BOUND_FRIEND_TYPE;

    private ModDataComponents() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(PuppyClickerMod.MOD_ID, "bound_friend"),
                BOUND_FRIEND_TYPE);
    }
}

package ie.eim.puppyclicker.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Public friend identity persisted on a clicker stack.
 *
 * <p>This data is intentionally network-synchronized and server-visible. It never contains the
 * PuppyClicker API key.</p>
 */
public record BoundFriend(String id, String name) {
    /** Disk/world representation used by the item data component. */
    public static final Codec<BoundFriend> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(BoundFriend::id),
            Codec.STRING.fieldOf("name").forGetter(BoundFriend::name))
            .apply(instance, BoundFriend::new));

    /** Bounded network representation; both fields are capped before allocation. */
    public static final StreamCodec<ByteBuf, BoundFriend> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), BoundFriend::id,
            ByteBufCodecs.stringUtf8(64), BoundFriend::name,
            BoundFriend::new);
}

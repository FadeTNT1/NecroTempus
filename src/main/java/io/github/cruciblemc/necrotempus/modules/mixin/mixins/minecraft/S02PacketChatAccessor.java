package io.github.cruciblemc.necrotempus.modules.mixin.mixins.minecraft;

import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(S02PacketChat.class)
public interface S02PacketChatAccessor {

    @Accessor("field_148919_a")
    IChatComponent necrotempus$getComponent();
}

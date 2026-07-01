package io.github.cruciblemc.necrotempus.modules.features.chatheads.network;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.github.cruciblemc.necrotempus.modules.features.chatheads.client.render.ChatHeadRenderer;

public class ChatHeadSenderPacketHandler implements IMessageHandler<ChatHeadSenderPacket, IMessage> {

    @Override
    public IMessage onMessage(ChatHeadSenderPacket message, MessageContext ctx) {
        handleChatHeadSender(message);
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static void handleChatHeadSender(ChatHeadSenderPacket packet) {
        ChatHeadRenderer.rememberServerSender(
                new GameProfile(packet.getSenderUuid(), packet.getSenderName()),
                packet.getSenderDisplayName(),
                packet.getMessageText()
        );
    }
}

package io.github.cruciblemc.necrotempus.modules.features.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.github.cruciblemc.necrotempus.modules.features.chatheads.server.ChatHeadMetadataSender;

public class NTClientPacketHandler implements IMessageHandler<NTClientPacket, IMessage> {

    @Override
    public IMessage onMessage(NTClientPacket message, MessageContext ctx) {
        ChatHeadMetadataSender.markClientCapable(ctx.getServerHandler().playerEntity);
        return null;
    }

}

package io.github.cruciblemc.necrotempus.modules.mixin.mixins.minecraft;

import io.github.cruciblemc.necrotempus.modules.features.chatheads.server.ChatHeadMetadataSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {

    @Shadow
    public EntityPlayerMP playerEntity;

    @Inject(method = "processChatMessage", at = @At("HEAD"))
    private void necrotempus$beginChatHeadSender(C01PacketChatMessage packetIn, CallbackInfo ci) {
        ChatHeadMetadataSender.beginChat(playerEntity, packetIn.func_149439_c());
    }

    @Inject(method = "processChatMessage", at = @At("RETURN"))
    private void necrotempus$endChatHeadSender(C01PacketChatMessage packetIn, CallbackInfo ci) {
        ChatHeadMetadataSender.endChat();
    }

    @Inject(
            method = "sendPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkManager;scheduleOutboundPacket(Lnet/minecraft/network/Packet;[Lio/netty/util/concurrent/GenericFutureListener;)V"))
    private void necrotempus$sendChatHeadSender(Packet packetIn, CallbackInfo ci) {
        ChatHeadMetadataSender.sendMetadataBeforeChatPacket(playerEntity, packetIn);
    }
}

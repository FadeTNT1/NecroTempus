package io.github.cruciblemc.necrotempus.modules.features.chatheads.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class ChatHeadSenderPacket implements IMessage {

    private UUID senderUuid;
    private String senderName;
    private String senderDisplayName;
    private String messageText;

    public ChatHeadSenderPacket() {
    }

    public ChatHeadSenderPacket(UUID senderUuid, String senderName, String senderDisplayName, String messageText) {
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.senderDisplayName = senderDisplayName;
        this.messageText = messageText;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tagCompound = ByteBufUtils.readTag(buf);
        senderUuid = UUID.fromString(tagCompound.getString("senderUuid"));
        senderName = tagCompound.getString("senderName");
        senderDisplayName = tagCompound.getString("senderDisplayName");
        messageText = tagCompound.getString("messageText");
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setString("senderUuid", senderUuid.toString());
        tagCompound.setString("senderName", senderName);
        tagCompound.setString("senderDisplayName", senderDisplayName == null ? "" : senderDisplayName);
        tagCompound.setString("messageText", messageText);
        ByteBufUtils.writeTag(buf, tagCompound);
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getMessageText() {
        return messageText;
    }
}

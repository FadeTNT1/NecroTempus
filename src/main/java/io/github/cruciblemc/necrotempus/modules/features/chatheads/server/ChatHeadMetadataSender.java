package io.github.cruciblemc.necrotempus.modules.features.chatheads.server;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import io.github.cruciblemc.necrotempus.NecroTempus;
import io.github.cruciblemc.necrotempus.modules.features.chatheads.network.ChatHeadSenderPacket;
import io.github.cruciblemc.necrotempus.modules.mixin.mixins.minecraft.S02PacketChatAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class ChatHeadMetadataSender {

    private static final ChatHeadMetadataSender INSTANCE = new ChatHeadMetadataSender();
    private static final long RECENT_CHAT_TTL_MILLIS = 5_000L;
    private static final Set<UUID> CLIENTS_WITH_NECROTEMPUS = new HashSet<>();
    private static final Queue<RecentChat> RECENT_CHATS = new ArrayDeque<>();
    private static final ThreadLocal<EntityPlayerMP> CHAT_SENDER = new ThreadLocal<>();
    private static final ThreadLocal<String> CHAT_MESSAGE = new ThreadLocal<>();
    private static boolean registered;

    private ChatHeadMetadataSender() {
    }

    public static void init() {
        if (registered)
            return;

        FMLCommonHandler.instance().bus().register(INSTANCE);
        registered = true;
    }

    public static void markClientCapable(EntityPlayerMP player) {
        if (player != null && player.getUniqueID() != null)
            CLIENTS_WITH_NECROTEMPUS.add(player.getUniqueID());
    }

    public static void beginChat(EntityPlayerMP sender, String message) {
        String normalizedMessage = StringUtils.normalizeSpace(message);

        if (normalizedMessage == null || normalizedMessage.startsWith("/"))
            return;

        CHAT_SENDER.set(sender);
        CHAT_MESSAGE.set(normalizedMessage);
        rememberRecentChat(sender, normalizedMessage);
    }

    public static void endChat() {
        CHAT_SENDER.remove();
        CHAT_MESSAGE.remove();
    }

    public static void sendMetadataBeforeChatPacket(EntityPlayerMP recipient, Packet packet) {
        EntityPlayerMP sender = CHAT_SENDER.get();
        String rawMessage = CHAT_MESSAGE.get();

        if (recipient == null || !(packet instanceof S02PacketChat))
            return;

        if (!CLIENTS_WITH_NECROTEMPUS.contains(recipient.getUniqueID()))
            return;

        IChatComponent component = ((S02PacketChatAccessor) packet).necrotempus$getComponent();

        if (component == null)
            return;

        String packetText = component.getUnformattedText();
        RecentChat recentChat = null;

        if (packetText == null)
            return;

        if (sender != null && rawMessage != null && packetText.contains(rawMessage)) {
            recentChat = new RecentChat(sender, rawMessage, System.currentTimeMillis());
        }

        if (recentChat == null)
            recentChat = findRecentChat(packetText);

        if (recentChat == null)
            return;

        NecroTempus.DISPATCHER.sendTo(new ChatHeadSenderPacket(
                recentChat.sender.getUniqueID(),
                recentChat.sender.getGameProfile().getName(),
                extractVisibleSenderName(packetText, recentChat),
                packetText
        ), recipient);
    }

    private static String extractVisibleSenderName(String packetText, RecentChat recentChat) {
        String fallbackName = recentChat.sender.getGameProfile().getName();
        String message = recentChat.message;

        if (packetText == null || message == null)
            return fallbackName;

        int messageIndex = packetText.indexOf(message);

        if (messageIndex <= 0)
            return fallbackName;

        String prefix = packetText.substring(0, messageIndex).trim();
        prefix = trimTrailingSeparators(prefix);
        prefix = stripBracketedPrefixes(prefix).trim();

        if (prefix.startsWith("<") && prefix.endsWith(">"))
            prefix = prefix.substring(1, prefix.length() - 1).trim();

        if (prefix.isEmpty())
            return fallbackName;

        int lastSpace = prefix.lastIndexOf(' ');
        String candidate = lastSpace >= 0 ? prefix.substring(lastSpace + 1) : prefix;
        candidate = stripWrappingPunctuation(candidate).trim();

        return candidate.isEmpty() ? fallbackName : candidate;
    }

    private static String trimTrailingSeparators(String text) {
        int end = text.length();

        while (end > 0) {
            char character = text.charAt(end - 1);

            if (!Character.isLetterOrDigit(character) && character != '_')
                end--;
            else
                break;
        }

        return text.substring(0, end);
    }

    private static String stripBracketedPrefixes(String text) {
        String result = text;

        while (result.startsWith("[")) {
            int end = result.indexOf(']');

            if (end < 0)
                break;

            result = result.substring(end + 1).trim();
        }

        return result;
    }

    private static String stripWrappingPunctuation(String text) {
        int start = 0;
        int end = text.length();

        while (start < end && !Character.isLetterOrDigit(text.charAt(start)) && text.charAt(start) != '_')
            start++;

        while (end > start && !Character.isLetterOrDigit(text.charAt(end - 1)) && text.charAt(end - 1) != '_')
            end--;

        return text.substring(start, end);
    }

    private static void rememberRecentChat(EntityPlayerMP sender, String message) {
        if (sender == null || message == null)
            return;

        synchronized (RECENT_CHATS) {
            RECENT_CHATS.add(new RecentChat(sender, message, System.currentTimeMillis()));
            pruneRecentChats();
        }
    }

    private static RecentChat findRecentChat(String packetText) {
        synchronized (RECENT_CHATS) {
            pruneRecentChats();

            for (RecentChat recentChat : RECENT_CHATS) {
                if (packetText.contains(recentChat.message))
                    return recentChat;
            }
        }

        return null;
    }

    private static void pruneRecentChats() {
        long now = System.currentTimeMillis();
        Iterator<RecentChat> iterator = RECENT_CHATS.iterator();

        while (iterator.hasNext()) {
            if (now - iterator.next().createdAt > RECENT_CHAT_TTL_MILLIS)
                iterator.remove();
        }
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
        if (event.handler != null && event.handler instanceof net.minecraft.network.NetHandlerPlayServer) {
            EntityPlayerMP player = ((net.minecraft.network.NetHandlerPlayServer) event.handler).playerEntity;

            if (player != null)
                CLIENTS_WITH_NECROTEMPUS.remove(player.getUniqueID());
        }
    }

    private static class RecentChat {

        private final EntityPlayerMP sender;
        private final String message;
        private final long createdAt;

        private RecentChat(EntityPlayerMP sender, String message, long createdAt) {
            this.sender = sender;
            this.message = message;
            this.createdAt = createdAt;
        }
    }
}

/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.message
 *
 * @author tassu
 */

package me.tassu.queue.message;

import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Message {

    private List<String> message;
    private boolean isCloned = false;

    private Message copy() {
        return new Message(message);
    }

    Message(List<String> message) {
        this.message = message;
    }

    private Map<String, String> placeholders = Maps.newHashMap();

    public Message addPlaceholder(String key, String value) {
        if (isCloned) {
            placeholders.put(String.format("%%%s%%", key), value);
            return this;
        }

        Message cloned = copy();
        cloned.placeholders.put(String.format("%%%s%%", key), value);
        cloned.isCloned = true;
        return cloned;
    }

    public Message broadcast() {
        for (String tempMessage : message) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                tempMessage = tempMessage.replace(entry.getKey(), entry.getValue());
            }
            ProxyServer.getInstance().broadcast(new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', tempMessage))));
        }

        return this;
    }

    public Message send(ProxiedPlayer... players) {
        for (String tempMessage : message) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                tempMessage = tempMessage.replace(entry.getKey(), entry.getValue());
            }

            final String finalTempMessage = tempMessage;
            Arrays.stream(players).forEach(player ->
                    player.sendMessage(new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', finalTempMessage)))));
        }

        return this;
    }


}

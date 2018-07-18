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
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

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
        Message cloned;

        if (isCloned) {
            cloned = this;
        } else {
            cloned = copy();
        }

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

    public Message send(CommandSender... senders) {
        for (String tempMessage : message) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                tempMessage = tempMessage.replace(entry.getKey(), entry.getValue());
            }

            final String finalTempMessage = tempMessage;
            Arrays.stream(senders).forEach(player ->
                    player.sendMessage(new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', finalTempMessage)))));
        }

        return this;
    }


}

/*
 * MIT License
 *
 * Copyright (c) 2018 Tassu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tassu.queue.message;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.val;
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

    private List<String> build() {
        val list = Lists.<String>newArrayList();
        for (String tempMessage : message) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                tempMessage = tempMessage.replace(entry.getKey(), entry.getValue());
            }

            list.add(ChatColor.translateAlternateColorCodes('&', tempMessage));
        }

        return list;
    }

    public Message broadcast() {
        for (String tempMessage : build()) {
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(tempMessage));
        }

        return this;
    }

    public Message send(CommandSender... senders) {
        for (String tempMessage : build()) {
            Arrays.stream(senders).forEach(player -> player.sendMessage(TextComponent.fromLegacyText(tempMessage)));
        }

        return this;
    }


    public List<String> getMessage() {
        return build();
    }
}

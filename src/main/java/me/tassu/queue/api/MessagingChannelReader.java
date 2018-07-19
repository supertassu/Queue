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

package me.tassu.queue.api;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import lombok.val;
import me.tassu.queue.queue.QueueManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class MessagingChannelReader implements Listener {

    @Inject private ProxyServer proxyServer;
    @Inject private QueueManager queueManager;

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());
        String sub = input.readUTF();
        if (sub.equals("JoinQueue")) {
            val uuid = UUID.fromString(input.readUTF());
            val optQueue = queueManager.getQueueById(input.readUTF());

            if (proxyServer.getPlayer(uuid) == null) return;
            if (!optQueue.isPresent()) return;

            val player = proxyServer.getPlayer(uuid);

            queueManager.getQueueForPlayer(player).ifPresent(it -> it.removePlayer(player));

            val queue = optQueue.get();

            queue.addPlayer(player);
        } else if (sub.equals("LeaveQueue")) {
            val uuid = UUID.fromString(input.readUTF());
            if (proxyServer.getPlayer(uuid) == null) return;
            val player = proxyServer.getPlayer(uuid);

            val optQueue = queueManager.getQueueForPlayer(player);
            if (!optQueue.isPresent()) return;
            val queue = optQueue.get();

            queue.removePlayer(player);
        }
    }

}

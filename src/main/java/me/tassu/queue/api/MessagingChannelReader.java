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
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import lombok.val;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.QueueManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class MessagingChannelReader implements Listener {

    @Inject private ProxyServer proxy;
    @Inject private QueueManager queueManager;

    private Message joinedMessage, leftMessage;

    @Inject
    public MessagingChannelReader(MessageManager messageManager) {
        this.joinedMessage = messageManager.getMessage("JOINED");
        this.leftMessage = messageManager.getMessage("LEFT");
    }

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

            if (proxy.getPlayer(uuid) == null) return;
            if (!optQueue.isPresent()) return;

            val player = proxy.getPlayer(uuid);

            queueManager.getQueueForPlayer(player).ifPresent(it -> it.removePlayer(player));

            val queue = optQueue.get();

            joinedMessage.addPlaceholder("QUEUE_NAME", queue.getName()).send(player);
            queue.addPlayer(player);
        } else if (sub.equals("LeaveQueue")) {
            val uuid = UUID.fromString(input.readUTF());
            if (proxy.getPlayer(uuid) == null) return;
            val player = proxy.getPlayer(uuid);

            val optQueue = queueManager.getQueueForPlayer(player);
            if (!optQueue.isPresent()) return;
            val queue = optQueue.get();

            leftMessage.addPlaceholder("QUEUE_NAME", queue.getName()).send(player);
            queue.removePlayer(player);
        }
    }

    public void updatePlayers() {
        for (ServerInfo info : proxy.getServers().values()) {
            if (info.getPlayers().isEmpty()) continue;

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("QueueUpdate");

            for (ProxiedPlayer player : info.getPlayers()) {
                out.writeUTF(player.getUniqueId().toString());
                val queue = queueManager.getQueueForPlayer(player);

                if (queue.isPresent()) {
                    out.writeUTF(queue.get().getName());
                    out.writeUTF(String.valueOf(queue.get().getPosition(player)));
                    out.writeUTF(String.valueOf(queue.get().getQueueLength()));
                } else {
                    out.writeUTF("");
                    out.writeUTF("");
                    out.writeUTF("");
                }
            }

            info.sendData("BungeeCord", out.toByteArray());
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        queueManager.getQueueForPlayer(event.getPlayer()).ifPresent(q -> q.removePlayer(event.getPlayer()));
    }

}

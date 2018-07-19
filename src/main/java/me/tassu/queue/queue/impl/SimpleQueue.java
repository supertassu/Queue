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

/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.queue
 *
 * @author tassu
 */
package me.tassu.queue.queue.impl;

import com.google.common.collect.Lists;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.IQueue;
import me.tassu.queue.queue.QueueMessagingProperties;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class SimpleQueue implements IQueue {

    private MessageManager messageManager = QueuePlugin.getInjector().getInstance(MessageManager.class);

    private Message msgSendingTo = messageManager.getMessage("SENDING");
    private Message msgSent = messageManager.getMessage("SENT");
    private Message msgCouldNotSend = messageManager.getMessage("SENDING_ERROR");

    private List<UUID> players = Lists.newArrayList();

    private String id;
    private String name;

    // as defined on bungeecord configuration
    private ServerInfo server;

    // in seconds
    private int sendDelay;

    private QueueMessagingProperties queueMessagingProperties;

    private SimpleQueue(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.server = builder.server;
        this.sendDelay = builder.sendDelay;
        this.queueMessagingProperties = builder.messager;
    }

    @Override
    public List<ProxiedPlayer> getPlayers() {
        return players.stream()
                .map(ProxyServer.getInstance()::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void addPlayer(ProxiedPlayer player) {
        if (player == null) return;
        if (isQueued(player)) return;
        players.add(player.getUniqueId());
    }

    @Override
    public void removePlayer(ProxiedPlayer player) {
        if (player == null) return;
        players.remove(player.getUniqueId());
    }

    @Override
    public boolean isQueued(ProxiedPlayer player) {
        if (player == null) return false;
        return players.contains(player.getUniqueId());
    }

    @Override
    public int getQueueLength() {
        return players.size();
    }

    @Override
    public void sendFirstPlayer() {
        if (players.size() == 0) return;

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(players.get(0));

        if (player == null) {
            players.remove(0);
            return;
        }

        msgSendingTo
                .addPlaceholder("SERVER_NAME", server.getName())
                .addPlaceholder("QUEUE_NAME", getName())
                .send(player);

        player.connect(server, (result, error) -> {
            if (result) {
                players.remove(0);
                msgSent
                        .addPlaceholder("SERVER_NAME", server.getName())
                        .addPlaceholder("QUEUE_NAME", getName())
                        .send(player);
            } else {
                msgCouldNotSend
                        .addPlaceholder("SERVER_NAME", server.getName())
                        .addPlaceholder("QUEUE_NAME", getName())
                        .addPlaceholder("ERROR_MESSAGE", getErrorMessage(error))
                        .send(player);
                if (error != null) error.printStackTrace();
            }
        });

    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public int getQueueLengthAhead(ProxiedPlayer player) {
        return getPosition(player) - 1;
    }

    @Override
    public int getPosition(ProxiedPlayer player) {
        if (player == null) return -1;
        return players.indexOf(player.getUniqueId()) + 1;
    }

    @Override
    public QueueMessagingProperties messagingProperties() {
        return queueMessagingProperties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    public ServerInfo getServer() {
        return server;
    }

    @Override
    public int getSendDelay() {
        return sendDelay;
    }

    @SuppressWarnings("UnusedReturnValue") // tassu
    public static final class Builder {
        private String id;
        private String name;
        private ServerInfo server;
        private int sendDelay;
        private QueueMessagingProperties messager = new QueueMessagingProperties();

        private Builder() {
        }

        public SimpleQueue build() {
            SimpleQueue queue = new SimpleQueue(this);
            QueuePlugin.getInjector().injectMembers(queue);
            return queue;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder server(ServerInfo server) {
            this.server = server;
            return this;
        }

        public Builder sendDelay(int sendDelay) {
            this.sendDelay = sendDelay;
            return this;
        }

        public Builder messager(QueueMessagingProperties queueMessagingProperties) {
            this.messager = queueMessagingProperties;
            return this;
        }
    }

    private String getErrorMessage(Throwable ex) {
        if (ex == null) return "null";
        return ex.getLocalizedMessage();
    }

}

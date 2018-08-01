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

package me.tassu.queue.queue.impl;

import com.google.common.collect.Lists;
import lombok.val;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.api.ex.QueueJoinException;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.IQueue;
import me.tassu.queue.queue.QueueManager;
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
    private QueueManager queueManager = QueuePlugin.getInjector().getInstance(QueueManager.class);

    private Message msgSendingTo = messageManager.getMessage("SENDING");
    private Message msgSent = messageManager.getMessage("SENT");
    private Message msgCouldNotSend = messageManager.getMessage("SENDING_ERROR");

    private Message msgInvalidProtocol = messageManager.getMessage("JOIN_ERROR_INVALID_PROTOCOL");
    private Message msgAlreadyQueued = messageManager.getMessage("JOIN_ERROR_ALREADY_QUEUED");

    private List<UUID> players = Lists.newArrayList();

    private String id;
    private String name;

    // as defined on bungeecord configuration
    private ServerInfo server;

    // in seconds
    private int sendDelay;

    // protocol stuff
    private int requiredExactProtocol, requiredMinProtocol, requiredMaxProtocol;

    private QueueMessagingProperties queueMessagingProperties;

    private SimpleQueue(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.server = builder.server;
        this.sendDelay = builder.sendDelay;
        this.requiredExactProtocol = builder.requiredExactProtocol;
        this.requiredMinProtocol = builder.requiredMinProtocol;
        this.requiredMaxProtocol = builder.requiredMaxProtocol;
        this.queueMessagingProperties = builder.queueMessagingProperties;
    }

    @Override
    public List<ProxiedPlayer> getPlayers() {
        return players.stream()
                .map(ProxyServer.getInstance()::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void addPlayer(ProxiedPlayer player) throws QueueJoinException {
        if (player == null) return;

        val current = queueManager.getQueueForPlayer(player);

        if (current.isPresent()) throw new QueueJoinException(
                "Already queued to " + current.get().getName(),
                msgAlreadyQueued.addPlaceholder("QUEUE", current.get().getName()));

        val protocol = player.getPendingConnection().getVersion();

        if (requiredExactProtocol != -1) {
            if (protocol != requiredExactProtocol) {
                throw new QueueJoinException("Invalid protocol version (req: " + requiredExactProtocol + " != actual: "
                        + protocol + ")", msgInvalidProtocol);
            }
        } else if (requiredMaxProtocol != -1) {
            if (requiredMaxProtocol > protocol) {
                throw new QueueJoinException("Invalid protocol version (req: " + requiredMinProtocol + " > actual: "
                        + protocol + ")", msgInvalidProtocol);
            }
        } else if (requiredMinProtocol != -1) {
            if (requiredMinProtocol < protocol) {
                throw new QueueJoinException("Invalid protocol version (req: " + requiredMinProtocol + " < actual: "
                        + protocol + ")", msgInvalidProtocol);
            }
        }

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
        if (player == null) throw new IllegalArgumentException("no such player");
        if (!isQueued(player)) throw new IllegalArgumentException("not queued");
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

    private String getErrorMessage(Throwable ex) {
        if (ex == null) return "null";
        return ex.getLocalizedMessage();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {
        private String id;
        private String name;
        private ServerInfo server;
        private int sendDelay;
        private int requiredExactProtocol = -1;
        private int requiredMinProtocol = -1;
        private int requiredMaxProtocol = -1;
        private QueueMessagingProperties queueMessagingProperties;

        private Builder() {
        }

        public SimpleQueue build() {
            return new SimpleQueue(this);
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

        public Builder requiredExactProtocol(int requiredExactProtocol) {
            this.requiredExactProtocol = requiredExactProtocol;
            return this;
        }

        public Builder requiredMinProtocol(int requiredMinProtocol) {
            this.requiredMinProtocol = requiredMinProtocol;
            return this;
        }

        public Builder requiredMaxProtocol(int requiredMaxProtocol) {
            this.requiredMaxProtocol = requiredMaxProtocol;
            return this;
        }

        public Builder messager(QueueMessagingProperties queueMessagingProperties) {
            this.queueMessagingProperties = queueMessagingProperties;
            return this;
        }
    }
}

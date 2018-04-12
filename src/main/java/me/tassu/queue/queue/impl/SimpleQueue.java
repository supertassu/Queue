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
import com.google.inject.Inject;
import com.google.inject.name.Named;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.message.Message;
import me.tassu.queue.queue.IQueue;
import me.tassu.queue.queue.QueueMessagingProperties;
import me.tassu.queue.queue.QueuePauser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SimpleQueue implements IQueue {

    @Inject
    @Named("SENDING")
    private Message msgSendingTo;
    @Inject
    @Named("SENT")
    private Message msgSent;
    @Inject
    @Named("SEND_ERROR")
    private Message msgCouldNotSend;

    private List<UUID> players = Lists.newArrayList();

    private String id;
    private String name;

    // as defined on bungeecord configuration
    private ServerInfo server;

    // in ticks
    private int sendDelay;
    private QueuePauser pauser;

    private QueueMessagingProperties queueMessagingProperties;

    private SimpleQueue(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.server = builder.server;
        this.sendDelay = builder.sendDelay;
        this.pauser = builder.pauser;
        this.queueMessagingProperties = builder.messager;
    }

    @Override
    public List<ProxiedPlayer> getPlayers() {
        return players.stream().map(ProxyServer.getInstance()::getPlayer).collect(Collectors.toList());
    }

    @Override
    public void addPlayer(ProxiedPlayer player) {
        players.add(player.getUniqueId());
    }

    @Override
    public void removePlayer(ProxiedPlayer player) {
        players.remove(player.getUniqueId());
    }

    @Override
    public boolean isQueued(ProxiedPlayer player) {
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
                error.printStackTrace();
                msgCouldNotSend
                        .addPlaceholder("SERVER_NAME", server.getName())
                        .addPlaceholder("QUEUE_NAME", getName())
                        .addPlaceholder("ERROR_MESSAGE", error.getLocalizedMessage())
                        .send(player);
            }
        });


    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public int getQueueLengthAhead(ProxiedPlayer player) {
        return 0;
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

    public int getSendDelay() {
        return sendDelay;
    }

    public QueuePauser getPauser() {
        return pauser;
    }

    public static final class Builder {
        private String id;
        private String name;
        private ServerInfo server;
        private int sendDelay;
        private QueuePauser pauser = new QueuePauser();
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

        public Builder pauser(QueuePauser pauser) {
            this.pauser = pauser;
            return this;
        }

        public Builder messager(QueueMessagingProperties queueMessagingProperties) {
            this.messager = queueMessagingProperties;
            return this;
        }
    }
}

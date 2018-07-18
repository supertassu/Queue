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

package me.tassu.queue.queue;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public interface IQueue {

    String getId();
    String getName();

    List<ProxiedPlayer> getPlayers();
    void addPlayer(ProxiedPlayer player);
    void removePlayer(ProxiedPlayer player);
    void sendFirstPlayer();
    boolean isQueued(ProxiedPlayer player);
    int getQueueLength();
    int getQueueLengthAhead(ProxiedPlayer player);
    int getPosition(ProxiedPlayer player);
    int getSendDelay();

    QueueMessagingProperties messagingProperties();
    QueuePauser pauser();
}

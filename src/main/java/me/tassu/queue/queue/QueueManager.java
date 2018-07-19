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
package me.tassu.queue.queue;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.file.ConfigFile;
import me.tassu.queue.file.FileManager;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.impl.SimpleQueue;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class QueueManager {

    private QueuePlugin plugin;

    @Inject
    public QueueManager(QueuePlugin plugin, FileManager fileManager, MessageManager messageManager) {
        this.plugin = plugin;
        queueConfig = fileManager.getQueueFile();
        queueResettingMsg = messageManager.getMessage("QUEUE_RESET");
    }

    private ConfigFile queueConfig;
    private Message queueResettingMsg;

    private Set<IQueue> queueSet = Sets.newHashSet();

    /**
     * Reloads and clears all queues.
     */
    public void refreshQueues() {
        queueSet.forEach(queue -> queue.getPlayers().forEach(player -> queueResettingMsg
                .addPlaceholder("QUEUE_NAME", queue.getName())
                .send(player)));

        queueSet.clear();

        queueConfig.getKeys().forEach(key ->
                queueSet.add(loadQueueFromConfig(key))
        );
    }

    /**
     * loads a queue from configuration
     *
     * @param id queue id
     * @return the queue
     */
    private IQueue loadQueueFromConfig(String id) {
        Configuration config = queueConfig.getSection(id);
        if (config == null) return null;

        if (config.getString("type").equalsIgnoreCase("SimpleQueue")) {
            SimpleQueue.Builder queue = SimpleQueue.builder()
                    .id(id)
                    .server(ProxyServer.getInstance().getServerInfo(config.getString("server")))
                    .name(config.getString("display"))
                    .messager(QueueMessagingProperties.fromConfig(config));

            if (config.getKeys().contains("sendDelay")) {
                queue.sendDelay(config.getInt("sendDelay"));
            }

            return queue.build();
        } else {
            plugin.getLogger().info("unknown queue type " + config.getString("type"));
        }

        return null;
    }

    /**
     * refreshes & clears a queue
     *
     * @param queue specific queue
     */
    public void refreshQueue(String queue) {
        IQueue tempQueue = queueSet.stream().filter(it -> it.getId().equalsIgnoreCase(queue)).findFirst().orElseThrow(() -> new IllegalArgumentException("queue not found"));
        queueResettingMsg.send(tempQueue.getPlayers().toArray(new ProxiedPlayer[0]));
        queueSet.removeIf(it -> it.getId().equalsIgnoreCase(queue));
        queueSet.add(loadQueueFromConfig(queue));
    }

    /**
     * @return All queues on the system.
     */
    public Set<IQueue> getAllQueues() {
        return Sets.newHashSet(queueSet);
    }

    public Optional<IQueue> getQueueById(String name) {
        return getAllQueues().stream().filter(it -> it.getId().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<IQueue> getQueueForPlayer(ProxiedPlayer player) {
        return getAllQueues().stream().filter(it -> it.isQueued(player)).findFirst();
    }
}

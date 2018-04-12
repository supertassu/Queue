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
import com.google.inject.name.Named;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.file.ConfigFile;
import me.tassu.queue.message.Message;
import me.tassu.queue.queue.impl.SimpleQueue;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.Set;

public class QueueManager {

    @Inject
    @Named("queues.yml")
    private ConfigFile queueConfig;
    @Inject
    @Named("QUEUE_RESET")
    private Message queueResettingMsg;

    @Inject
    private QueuePlugin plugin;

    private Set<IQueue> queueSet = Sets.newHashSet();

    public QueueManager() {

    }

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
                    .pauser(new QueuePauser())
                    .messager(QueueMessagingProperties.fromConfig(config));

            if (config.getKeys().contains("sendDelay")) {
                queue.sendDelay(config.getInt("sendDelay"));
            }

        } else {
            plugin.getLogger().info("unknown queue type "+ config.getString("type"));
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


}

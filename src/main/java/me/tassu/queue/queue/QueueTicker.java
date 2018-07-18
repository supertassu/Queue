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

import com.google.inject.Inject;
import lombok.val;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;

import java.util.Calendar;

public class QueueTicker implements Runnable {

    @Inject
    public QueueTicker(QueueManager queueManager, MessageManager messageManager) {
        this.queueManager = queueManager;
        this.msgStatusUpdate = messageManager.getMessage("STATUS");
    }

    private QueueManager queueManager;
    private Message msgStatusUpdate;

    @Override
    public void run() {
        queueManager
                .getAllQueues()
                .forEach(queue -> {
                    if (queue.pauser().isPaused()) {
                        if (queue.pauser().getUnpauseDate() < System.currentTimeMillis()) queue.pauser().unpause();
                        else return;
                    }

                    val second = Calendar.getInstance().get(Calendar.SECOND);
                    boolean sendStatus = false;

                    if (second % queue.getSendDelay() == 0) {
                        queue.sendFirstPlayer();

                        if (queue.messagingProperties().shouldMessageOnUpdate()) {
                            sendStatus = true;
                        }
                    }

                    if (second % queue.messagingProperties().getMessageRepeatSeconds() == 0
                            && !sendStatus) {
                        sendStatus = true;
                    }

                    if (sendStatus) {
                        sendStatusUpdate(queue);
                    }
                });
    }

    private void sendStatusUpdate(IQueue queue) {
        queue.getPlayers().forEach(it -> {
            val message = msgStatusUpdate
                    .addPlaceholder("QUEUE_NAME", queue.getName())
                    .addPlaceholder("QUEUE_LENGTH", String.valueOf(queue.getQueueLength()))
                    .addPlaceholder("POSITION", String.valueOf(queue.getPosition(it)))
                    .addPlaceholder("PLAYERS_AHEAD", String.valueOf(queue.getQueueLengthAhead(it)));

            message.send(it);
        });
    }
}

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

import com.google.inject.Inject;
import lombok.val;
import me.tassu.queue.api.MessagingChannelReader;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;

import java.util.Calendar;

public class QueueTicker implements Runnable {

    @Inject
    public QueueTicker(MessageManager messageManager) {
        this.msgStatusUpdate = messageManager.getMessage("STATUS");
    }

    @Inject private QueueManager queueManager;
    @Inject private PauseManager pauser;
    @Inject private MessagingChannelReader api;

    private Message msgStatusUpdate;

    @Override
    public void run() {
        queueManager
                .getAllQueues()
                .forEach(queue -> {
                    val second = Calendar.getInstance().get(Calendar.SECOND);

                    if (second % 5 == 0) {
                        api.updatePlayers();
                    }

                    boolean sendStatus = false;

                    if (second % queue.getSendDelay() == 0 && !pauser.isPaused(queue)) {
                        queue.sendFirstPlayer();

                        if (queue.messagingProperties().shouldMessageOnUpdate()) {
                            sendStatus = true;
                        }
                    }

                    if (second % queue.messagingProperties().getMessageRepeatSeconds() == 0) {
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

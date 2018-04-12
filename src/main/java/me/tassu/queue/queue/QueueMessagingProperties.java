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

import net.md_5.bungee.config.Configuration;

public class QueueMessagingProperties {

    private boolean messageOnUpdate = true;

    // set to -1 to disable
    private int messageRepeatSeconds = 20;

    static QueueMessagingProperties fromConfig(Configuration configuration) {
        QueueMessagingProperties properties = new QueueMessagingProperties();

        if (configuration.getKeys().contains("statusMessageDelay")) {
            properties.messageRepeatSeconds = configuration.getInt("statusMessageDelay");
        }
        if (configuration.getKeys().contains("sendPositionUpdates")) {
            properties.messageOnUpdate = configuration.getBoolean("sendPositionUpdates");
        }

        return properties;
    }


    public int getMessageRepeatSeconds() {
        return messageRepeatSeconds;
    }

    public boolean shouldMessageOnUpdate() {
        return messageOnUpdate;
    }
}


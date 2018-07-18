/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.message
 *
 * @author tassu
 */

package me.tassu.queue.message;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.file.ConfigFile;
import me.tassu.queue.file.FileManager;

@Singleton
public class MessageManager {

    private ConfigFile messages;

    @Inject
    private QueuePlugin plugin;
    @Inject
    private FileManager fileManager;

    public Message getMessage(String identifier) {
        if (messages == null) messages = fileManager.getMessagesFile();

        if (!messages.getKeys().contains(identifier)) {
            plugin.getLogger().info("A message with id " + identifier + " was not found from configuration.");
            messages.set(identifier, "Unknown message");
        }

        if (messages.get(identifier) instanceof String) {
            return new Message(Lists.newArrayList(messages.getString(identifier)));
        }

        return new Message(messages.getStringList(identifier));
    }

}

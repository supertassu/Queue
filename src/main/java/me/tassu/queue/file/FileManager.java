/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.file
 *
 * @author tassu
 */

package me.tassu.queue.file;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import me.tassu.queue.QueuePlugin;

import java.io.*;
import java.util.Map;


public class FileManager {

    @Inject
    private QueuePlugin plugin;

    @Inject
    @Named("config.yml")
    private ConfigFile configFile;
    @Inject
    @Named("messages.yml")
    private ConfigFile messagesFile;
    @Inject
    @Named("queues.yml")
    private ConfigFile queueFile;

    public ConfigFile getConfigFile() {
        return configFile;
    }

    public ConfigFile getMessagesFile() {
        return messagesFile;
    }

    public ConfigFile getQueueFile() {
        return queueFile;
    }

    private Map<String, ConfigFile> files = Maps.newHashMap();

    public ConfigFile getFile(String name) {
        if (!files.containsKey(name)) files.put(name, from(name));
        return files.get(name);
    }

    public void reload() throws IOException {
        getConfigFile().reloadConfig();
        getMessagesFile().reloadConfig();
        getQueueFile().reloadConfig();
    }

    private ConfigFile from(String name) {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) throw new RuntimeException("could not create data folder");
        }

        File file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) throw new RuntimeException("could not create a file named " + name);
                try (InputStream is = QueuePlugin.class.getResourceAsStream(name);
                     OutputStream os = new FileOutputStream(file)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }


        try {
            return new ConfigFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

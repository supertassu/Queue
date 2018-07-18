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
import com.google.inject.Singleton;
import me.tassu.queue.QueuePlugin;

import java.io.*;
import java.util.Map;

@Singleton
public class FileManager {

    private QueuePlugin plugin;

    @Inject
    public FileManager(QueuePlugin plugin) {
        this.plugin = plugin;
        configFile = getFile("config.yml");
        messagesFile = getFile("messages.yml");
        queueFile = getFile("queues.yml");
    }

    private ConfigFile configFile;
    private ConfigFile messagesFile;
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
        if (plugin == null) throw new IllegalStateException("plugin is null, wtf?");

        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) throw new RuntimeException("could not create data folder");
        }

        File file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) throw new RuntimeException("could not create a file named " + name);
                try (InputStream is = plugin.getResourceAsStream(name);
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

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

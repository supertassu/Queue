/*
 * This file is part of a project by Tassu_.
 * Usage of this file (or parts of it) is not allowed
 * without a permission from Tassu_.
 *
 * You may contact Tassu_ by e-mailing to <tassu@tassu.me>.
 *
 * Current Package: me.tassu.queue.util
 *
 * @author tassu
 */

package me.tassu.queue.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import me.tassu.queue.QueuePlugin;
import me.tassu.queue.file.ConfigFile;
import me.tassu.queue.message.Message;

public class QueueModule extends AbstractModule {

    private final QueuePlugin plugin;

    public QueueModule(QueuePlugin plugin) {
        this.plugin = plugin;
    }


    public Injector createInjector() {
        return Guice.createInjector(this);
    }

    @Override
    protected void configure() {
        this.bind(QueuePlugin.class).toInstance(this.plugin);
    }

    @Provides
    ConfigFile getConfigFile(@Named("File Name") String fileName) {
        return plugin.getFileManager().getFile(fileName);
    }

    @Provides
    Message getMessage(@Named("Message Key") String key) {
        return plugin.getMessageManager().getMessage(key);
    }

}

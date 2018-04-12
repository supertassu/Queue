package me.tassu.queue;

import com.google.inject.Inject;
import com.google.inject.Injector;
import me.tassu.queue.file.FileManager;
import me.tassu.queue.inject.QueueModule;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.QueueManager;
import me.tassu.queue.queue.QueueTicker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public final class QueuePlugin extends Plugin {

    @Inject
    private FileManager fileManager;
    @Inject
    private MessageManager messageManager;
    @Inject
    private QueueManager queueManager;
    @Inject
    private ProxyServer proxy;

    private static Injector injector;

    @Override
    public void onEnable() {
        QueueModule module = new QueueModule(this);
        injector = module.createInjector();
        injector.injectMembers(this);

        queueManager.refreshQueues();

        proxy.getScheduler().schedule(this, new QueueTicker(), 1, TimeUnit.SECONDS);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    /**
     * @return Google Guice injector
     */
    public static Injector getInjector() {
        return injector;
    }
}

package me.tassu.queue;

import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.val;
import me.tassu.queue.command.QueueCommand;
import me.tassu.queue.inject.QueueModule;
import me.tassu.queue.queue.QueueManager;
import me.tassu.queue.queue.QueueTicker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public final class QueuePlugin extends Plugin {

    @Inject private QueueManager queueManager;
    @Inject private ProxyServer proxy;
    @Inject private QueueTicker ticker;
    @Inject private QueueCommand command;

    private static Injector injector;

    @Override
    public void onEnable() {
        val module = new QueueModule(this);
        injector = module.createInjector();
        injector.injectMembers(this);

        queueManager.refreshQueues();

        proxy.getPluginManager().registerCommand(this, command);
        proxy.getScheduler().schedule(this, ticker, 1, 1, TimeUnit.SECONDS);

    }

    /**
     * @return Google Guice injector
     */
    public static Injector getInjector() {
        return injector;
    }
}

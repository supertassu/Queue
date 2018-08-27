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

package me.tassu.queue;

import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.val;
import me.tassu.queue.api.MessagingChannelReader;
import me.tassu.queue.command.QueueCommand;
import me.tassu.queue.inject.QueueModule;
import me.tassu.queue.listener.PlayerListener;
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
    @Inject private PlayerListener listener;
    @Inject private MessagingChannelReader api;

    private static Injector injector;

    @Override
    public void onEnable() {
        val module = new QueueModule(this);
        injector = module.createInjector();
        injector.injectMembers(this);

        queueManager.refreshQueues();

        proxy.getPluginManager().registerListener(this, listener);
        proxy.getPluginManager().registerListener(this, api);
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

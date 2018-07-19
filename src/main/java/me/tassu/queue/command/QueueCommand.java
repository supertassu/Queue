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

package me.tassu.queue.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.val;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.PauseManager;
import me.tassu.queue.queue.QueueManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

@Singleton
public class QueueCommand extends Command {

    @Inject
    private QueueManager manager;
    @Inject
    private PauseManager pauser;

    private Message queueListHeaderMessage,
            queueListEntryMessage,
            queueListFooterMessage,
            usageMessage,
            notQueuedMessage,
            noSuchQueueMessage,
            noPermissionsMessage,
            joinedMessage,
            leftMessage;


    @Inject
    public QueueCommand(MessageManager messageManager) {
        super("queue", "queue.command.execute", "queue", "q", "que");

        this.queueListHeaderMessage = messageManager.getMessage("COMMAND_HELP_HEADER");
        this.queueListEntryMessage = messageManager.getMessage("COMMAND_HELP_QUEUE_ENTRY");
        this.queueListFooterMessage = messageManager.getMessage("COMMAND_HELP_FOOTER");
        this.noSuchQueueMessage = messageManager.getMessage("COMMAND_NO_SUCH_QUEUE");
        this.noPermissionsMessage = messageManager.getMessage("COMMAND_NO_PERMISSIONS");
        this.notQueuedMessage = messageManager.getMessage("NOT_QUEUED");
        this.usageMessage = messageManager.getMessage("COMMAND_USAGE");
        this.joinedMessage = messageManager.getMessage("JOINED");
        this.leftMessage = messageManager.getMessage("LEFT");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            queueListHeaderMessage
                    .addPlaceholder("AMOUNT", String.valueOf(manager.getAllQueues().size()))
                    .send(sender);

            manager.getAllQueues().forEach(queue -> queueListEntryMessage
                    .addPlaceholder("STATUS_COLOR", pauser.isPaused(queue) ? "e" : "a")
                    .addPlaceholder("NAME", queue.getName())
                    .addPlaceholder("ID", queue.getId())
                    .send(sender));

            queueListFooterMessage.send(sender);
        } else if (args[0].equalsIgnoreCase("join")) {
            if (args.length != 2) {
                usageMessage.addPlaceholder("USAGE", "/queue join <id>").send(sender);
                return;
            }

            if (!(sender instanceof ProxiedPlayer)) return;

            val queue = manager.getQueueById(args[1]);
            val player = (ProxiedPlayer) sender;

            if (!queue.isPresent()) {
                noSuchQueueMessage.addPlaceholder("QUEUE_NAME", args[1]).send(sender);
                return;
            }

            queue.get().addPlayer(player);
            joinedMessage.addPlaceholder("QUEUE_NAME", queue.get().getName()).send(sender);
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (!(sender instanceof ProxiedPlayer)) return;
            val player = (ProxiedPlayer) sender;
            val queue = manager.getQueueForPlayer(player);

            if (!queue.isPresent()) {
                notQueuedMessage.send(sender);
                return;
            }

            queue.get().removePlayer(player);
            leftMessage.addPlaceholder("QUEUE_NAME", queue.get().getName()).send(sender);
        } else if (args[0].equalsIgnoreCase("pause")) {
            if (!sender.hasPermission("queue.command.pause")) {
                noPermissionsMessage.send(sender);
                return;
            }

            if (args.length != 2) {
                usageMessage.addPlaceholder("USAGE", "/queue pause <id>").send(sender);
                return;
            }

            val queue = manager.getQueueById(args[1]);

            if (!queue.isPresent()) {
                noSuchQueueMessage.addPlaceholder("QUEUE_NAME", args[1]).send(sender);
                return;
            }

            pauser.pause(queue.get());
            sender.sendMessage(TextComponent.fromLegacyText("&cPaused queue " + queue.get().getName() + ": "
                    + pauser.isPaused(queue.get())));
        } else if (args[0].equalsIgnoreCase("resume")) {
            if (!sender.hasPermission("queue.command.resume")) {
                noPermissionsMessage.send(sender);
                return;
            }

            if (args.length != 2) {
                usageMessage.addPlaceholder("USAGE", "/queue resume <id>").send(sender);
                return;
            }

            val queue = manager.getQueueById(args[1]);

            if (!queue.isPresent()) {
                noSuchQueueMessage.addPlaceholder("QUEUE_NAME", args[1]).send(sender);
                return;
            }

            pauser.unpause(queue.get());
            sender.sendMessage(TextComponent.fromLegacyText("&aResumed queue " + queue.get().getName() + ": "
                    + pauser.isPaused(queue.get())));
        }
    }
}

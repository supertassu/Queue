package me.tassu.queue.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.val;
import me.tassu.queue.message.Message;
import me.tassu.queue.message.MessageManager;
import me.tassu.queue.queue.QueueManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

@Singleton
public class QueueCommand extends Command {

    private QueueManager manager;

    private Message queueListHeaderMessage, queueListEntryMessage, queueListFooterMessage;

    @Inject
    public QueueCommand(QueueManager manager, MessageManager messageManager) {
        super("queue", "queue.command.execute", "queue", "q", "que");
        this.manager = manager;

        this.queueListHeaderMessage = messageManager.getMessage("COMMAND_HELP_HEADER");
        this.queueListEntryMessage = messageManager.getMessage("COMMAND_HELP_QUEUE_ENTRY");
        this.queueListFooterMessage = messageManager.getMessage("COMMAND_HELP_FOOTER");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            queueListHeaderMessage
                    .addPlaceholder("AMOUNT", String.valueOf(manager.getAllQueues().size()))
                    .send(sender);

            manager.getAllQueues().forEach(queue -> queueListEntryMessage
                    .addPlaceholder("STATUS_COLOR", queue.pauser().isPaused() ? "e" : "a")
                    .addPlaceholder("NAME", queue.getName())
                    .addPlaceholder("ID", queue.getId())
                    .send(sender));

            queueListFooterMessage.send(sender);
        } else if (args[0].equalsIgnoreCase("join")) {
            if (args.length != 2) {
                sender.sendMessage(TextComponent.fromLegacyText("/queue join <id>"));
                return;
            }

            if (!(sender instanceof ProxiedPlayer)) return;

            val queue = manager.getQueueById(args[1]);
            val player = (ProxiedPlayer) sender;

            if (!queue.isPresent()) {
                sender.sendMessage(TextComponent.fromLegacyText("no such queue"));
                return;
            }

            queue.get().addPlayer(player);
            sender.sendMessage(TextComponent.fromLegacyText("Joined queue " + queue.get().getName()));
        }
    }
}

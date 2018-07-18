package me.tassu.queue.command;

import com.google.inject.Inject;
import lombok.val;
import me.tassu.queue.queue.QueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class QueueCommand extends Command {

    public QueueCommand() {
        super("queue", "queue.command.execute", "queue", "q", "que");
    }

    @Inject
    private QueueManager manager;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new ComponentBuilder("Available queues")
                    .append(" [Legend: ")
                    .color(ChatColor.GRAY)
                    .append("ACTIVE")
                    .color(ChatColor.GREEN)
                    .append(" ")
                    .append("PAUSED")
                    .color(ChatColor.YELLOW)
                    .append("]")
                    .color(ChatColor.GRAY)
                    .create());

            manager.getAllQueues().forEach(queue -> {
                val builder = new ComponentBuilder("*");

                if (queue.pauser().isPaused()) {
                    builder.color(ChatColor.YELLOW);
                } else {
                    builder.color(ChatColor.GREEN);
                }

                builder
                        .append(" ")
                        .append(queue.getName(), ComponentBuilder.FormatRetention.NONE)
                        .append(" ")
                        .append(queue.getId(), ComponentBuilder.FormatRetention.NONE)
                        .color(ChatColor.GRAY);

                sender.sendMessage(builder.create());

                sender.sendMessage(TextComponent.fromLegacyText("Use /queue join <id> to join a queue."));
            });
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

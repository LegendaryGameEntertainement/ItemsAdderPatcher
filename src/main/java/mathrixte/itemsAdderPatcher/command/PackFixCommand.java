package mathrixte.itemsAdderPatcher.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PackFixCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Runnable patchTask;
    private final AtomicBoolean patchRunning;

    public PackFixCommand(
            JavaPlugin plugin,
            Runnable patchTask,
            AtomicBoolean patchRunning
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.patchTask = Objects.requireNonNull(patchTask, "patchTask");
        this.patchRunning = Objects.requireNonNull(patchRunning, "patchRunning");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("itemsadderpatcher.admin")) {
            sender.sendMessage(ChatColor.RED + "Tu n'as pas la permission.");
            return true;
        }

        if (args.length > 0 && !args[0].equalsIgnoreCase("patch")) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /" + label + " patch");
            return true;
        }

        if (!patchRunning.compareAndSet(false, true)) {
            sender.sendMessage(ChatColor.YELLOW + "Un patch est déjà en cours.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Correction du resource pack en cours...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = false;
            String errorMessage = null;

            try {
                patchTask.run();
                success = true;
            } catch (Exception exception) {
                errorMessage = exception.getMessage();
                plugin.getLogger().severe(
                        "Échec du patch manuel : " + exception
                );
                exception.printStackTrace();
            } finally {
                patchRunning.set(false);
            }

            boolean finalSuccess = success;
            String finalErrorMessage = errorMessage;

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (finalSuccess) {
                    sender.sendMessage(
                            ChatColor.GREEN + "Resource pack corrigé."
                    );
                } else {
                    sender.sendMessage(
                            ChatColor.RED + "Échec du patch. Consulte la console."
                    );

                    if (finalErrorMessage != null) {
                        sender.sendMessage(
                                ChatColor.GRAY + finalErrorMessage
                        );
                    }
                }
            });
        });

        return true;
    }
}
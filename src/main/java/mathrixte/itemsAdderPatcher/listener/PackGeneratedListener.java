package mathrixte.itemsAdderPatcher.listener;

import dev.lone.itemsadder.api.Events.ItemsAdderPackCompressedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PackGeneratedListener implements Listener {

    private final JavaPlugin plugin;
    private final Runnable patchTask;
    private final AtomicBoolean patchRunning;

    public PackGeneratedListener(
            JavaPlugin plugin,
            Runnable patchTask,
            AtomicBoolean patchRunning
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.patchTask = Objects.requireNonNull(patchTask, "patchTask");
        this.patchRunning = Objects.requireNonNull(patchRunning, "patchRunning");
    }

    @EventHandler
    public void onPackCompressed(ItemsAdderPackCompressedEvent event) {
        if (!patchRunning.compareAndSet(false, true)) {
            plugin.getLogger().warning(
                    "Pack généré, mais un patch est déjà en cours."
            );
            return;
        }

        plugin.getLogger().info(
                "ItemsAdder a terminé la compression du pack. "
                        + "Lancement du patch..."
        );

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                patchTask.run();

                plugin.getLogger().info(
                        "Patch automatique du resource pack terminé."
                );
            } catch (Exception exception) {
                plugin.getLogger().severe(
                        "Échec du patch automatique : " + exception
                );
                exception.printStackTrace();
            } finally {
                patchRunning.set(false);
            }
        });
    }
}
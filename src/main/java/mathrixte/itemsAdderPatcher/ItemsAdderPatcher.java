package mathrixte.itemsAdderPatcher;

import mathrixte.itemsAdderPatcher.command.PackFixCommand;
import mathrixte.itemsAdderPatcher.listener.PackGeneratedListener;
import mathrixte.itemsAdderPatcher.pack.FontPatcher;
import mathrixte.itemsAdderPatcher.pack.PackLocator;
import mathrixte.itemsAdderPatcher.pack.ZipPatcher;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ItemsAdderPatcher extends JavaPlugin {

    private final AtomicBoolean patchRunning = new AtomicBoolean(false);

    private PackLocator packLocator;
    private ZipPatcher zipPatcher;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("ItemsAdder") == null) {
            getLogger().severe("ItemsAdder n'est pas installé ou pas chargé. Le plugin s'arrête.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            initializePackPipeline();
        } catch (IllegalStateException exception) {
            getLogger().severe("Configuration invalide : " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Runnable patchTask = this::runPatchOrThrow;

        getServer().getPluginManager().registerEvents(
                new PackGeneratedListener(this, patchTask, patchRunning),
                this
        );

        PluginCommand command = getCommand("iapatch");
        if (command == null) {
            getLogger().severe("La commande /iapatch n'est pas déclarée dans plugin.yml.");
        } else {
            command.setExecutor(new PackFixCommand(this, patchTask, patchRunning));
        }

        getLogger().info("ItemsAdderPatcher activé.");
        getLogger().info("ZIP cible (patché sur place) : " + packLocator.targetZip());
        getLogger().info("Sauvegarde automatique : " + packLocator.backupZip());
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemsAdderPatcher désactivé.");
    }

    private void initializePackPipeline() {
        packLocator = new PackLocator(this);
        FontPatcher fontPatcher = new FontPatcher(packLocator.blockedTextures());
        zipPatcher = new ZipPatcher(packLocator, fontPatcher, getLogger());
    }

    private void runPatchOrThrow() {
        try {
            zipPatcher.patch();
        } catch (IOException exception) {
            throw new RuntimeException("Échec du patch du resource pack", exception);
        }
    }
}
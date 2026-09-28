package mathrixte.itemsAdderPatcher.pack;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class PackLocator {

    private final Path targetZip;
    private final List<String> fontFiles;
    private final List<String> blockedTextures;

    public PackLocator(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        FileConfiguration config = plugin.getConfig();
        Path serverRoot = plugin.getServer().getWorldContainer().toPath();

        String targetPath = config.getString(
                "pack.target",
                "plugins/ItemsAdder/output/generated.zip"
        );

        this.targetZip = serverRoot.resolve(targetPath).normalize();

        this.fontFiles = List.copyOf(
                config.getStringList("font-patch.files")
        );
        this.blockedTextures = List.copyOf(
                config.getStringList("font-patch.blocked-textures")
        );

        if (this.fontFiles.isEmpty()) {
            throw new IllegalStateException(
                    "font-patch.files est vide dans config.yml"
            );
        }

        if (this.blockedTextures.isEmpty()) {
            throw new IllegalStateException(
                    "font-patch.blocked-textures est vide dans config.yml"
            );
        }
    }

    public Path targetZip() {
        return targetZip;
    }

    public Path backupZip() {
        return targetZip.resolveSibling(targetZip.getFileName() + ".bak");
    }

    public Path temporaryZip() {
        return targetZip.resolveSibling(targetZip.getFileName() + ".tmp");
    }

    public Path hashFile() {
        return targetZip.resolveSibling(targetZip.getFileName() + ".sha1");
    }

    public List<String> fontFiles() {
        return fontFiles;
    }

    public List<String> blockedTextures() {
        return blockedTextures;
    }
}
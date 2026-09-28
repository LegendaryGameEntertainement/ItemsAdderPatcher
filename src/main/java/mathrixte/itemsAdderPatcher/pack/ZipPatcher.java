package mathrixte.itemsAdderPatcher.pack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipPatcher {

    private final PackLocator locator;
    private final FontPatcher fontPatcher;
    private final Logger logger;

    public ZipPatcher(PackLocator locator, FontPatcher fontPatcher, Logger logger) {
        this.locator = Objects.requireNonNull(locator, "locator");
        this.fontPatcher = Objects.requireNonNull(fontPatcher, "fontPatcher");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public PatchResult patch() throws IOException {
        Path target = locator.targetZip();
        Path backup = locator.backupZip();
        Path temporary = locator.temporaryZip();

        if (!Files.isRegularFile(target)) {
            throw new IOException("ZIP source introuvable : " + target);
        }

        Files.deleteIfExists(temporary);

        List<String> fontFiles = locator.fontFiles();
        boolean changed = false;

        try (ZipFile source = new ZipFile(target.toFile());
             OutputStream fileOutput = Files.newOutputStream(temporary);
             ZipOutputStream zipOutput = new ZipOutputStream(fileOutput)) {

            Enumeration<? extends ZipEntry> entries = source.entries();

            while (entries.hasMoreElements()) {
                ZipEntry sourceEntry = entries.nextElement();
                String normalizedName = sourceEntry.getName().replace('\\', '/');

                ZipEntry outputEntry = new ZipEntry(sourceEntry.getName());
                outputEntry.setTime(sourceEntry.getTime());
                zipOutput.putNextEntry(outputEntry);

                byte[] data;
                try (InputStream entryInput = source.getInputStream(sourceEntry)) {
                    data = entryInput.readAllBytes();
                }

                if (fontFiles.contains(normalizedName)) {
                    String original = new String(data, StandardCharsets.UTF_8);
                    String patched = fontPatcher.patch(original);

                    if (!patched.equals(original)) {
                        changed = true;
                        data = patched.getBytes(StandardCharsets.UTF_8);
                        logger.info("Fichier patché : " + normalizedName);
                    }
                }

                zipOutput.write(data);
                zipOutput.closeEntry();
            }
        }

        if (!changed) {
            Files.deleteIfExists(temporary);
            logger.info("Aucune entrée à corriger, ZIP original conservé.");

            String sha1 = PackHash.sha1(target);
            Files.writeString(locator.hashFile(), sha1, StandardCharsets.US_ASCII);

            return new PatchResult(target, sha1, false);
        }

        Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Sauvegarde créée : " + backup);

        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("ZIP corrigé écrasé sur : " + target);

        String sha1 = PackHash.sha1(target);
        Files.writeString(locator.hashFile(), sha1, StandardCharsets.US_ASCII);

        logger.info("SHA-1 : " + sha1);

        return new PatchResult(target, sha1, true);
    }

    public record PatchResult(Path zipFile, String sha1, boolean changed) {
    }
}
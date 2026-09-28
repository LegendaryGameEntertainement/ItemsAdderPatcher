package mathrixte.itemsAdderPatcher.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class PackHash {

    private PackHash() {
    }

    public static String sha1(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;

                while ((read = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }

            StringBuilder builder = new StringBuilder();
            for (byte value : digest.digest()) {
                builder.append(String.format(Locale.ROOT, "%02x", value));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 indisponible sur cette JVM", exception);
        }
    }
}
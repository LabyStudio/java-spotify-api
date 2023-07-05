import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class SpotifyEnableDevMode {

    public static void main(String[] args) throws IOException {
        enableDevTools(Paths.get(System.getenv("LOCALAPPDATA"), "Spotify"));
        enableEmployeeMode(Paths.get(System.getenv("APPDATA"), "Spotify"));
    }

    private static void enableDevTools(Path spotifyLocal) throws IOException {
        Path file = spotifyLocal.resolve("offline.bnk");
        TextPatcher patcher = (fileName, content) -> {
            return content.replaceAll("(?<=app-developer..|app-developer>)0", "2");
        };
        patchFile(file, patcher);
    }

    private static void enableEmployeeMode(Path spotifyAppData) throws IOException {
        Path appsDir = spotifyAppData.resolve("Apps");
        Path xpuiSpa = appsDir.resolve("xpui.spa");

        patchArchive(xpuiSpa, (TextPatcher) (fileName, content) -> {
            if (!fileName.equals("xpui.js")) {
                return content;
            }

            content = content.replaceAll("\"1\"===e.employee", " true"); // Minified
            content = content.replaceAll("\"1\" === e.employee", " true"); // Formatted

            return content;
        });
    }

    private static void patchFile(Path archive, Patcher patcher) throws IOException {
        Files.write(archive, patcher.patch(archive.getFileName().toString(), Files.readAllBytes(archive)));
    }

    private static void patchArchive(Path archive, Patcher patcher) throws IOException {
        // Move archive to temp location
        Path tempArchive = archive.getParent().resolve(archive.getFileName() + ".tmp");
        if (!Files.exists(archive)) {
            Files.move(archive, tempArchive);
        }

        // Write patched archive to original location
        try (
                ZipFile zipFile = new ZipFile(tempArchive.toFile());
                ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(archive))
        ) {
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entryIn = entries.nextElement();

                // Read bytes of temp archive entry
                try (InputStream is = zipFile.getInputStream(entryIn)) {
                    ByteArrayOutputStream inputBytes = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        inputBytes.write(buf, 0, len);
                    }

                    // Patch
                    byte[] outputBytes = patcher.patch(entryIn.getName(), inputBytes.toByteArray());

                    // Write patched bytes to new archive
                    zos.putNextEntry(entryIn);
                    zos.write(outputBytes);
                }
            }
        }
    }

    private interface TextPatcher extends Patcher {

        @Override
        default byte[] patch(String fileName, byte[] payload) throws IOException {
            String content = new String(payload, StandardCharsets.ISO_8859_1);
            String patched = this.patch(fileName, content);
            return patched.getBytes(StandardCharsets.ISO_8859_1);
        }

        String patch(String fileName, String content) throws IOException;

    }

    private interface Patcher {
        byte[] patch(String fileName, byte[] payload) throws IOException;
    }
}

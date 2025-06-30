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
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        Path spotifyApp = isWindows
                ? Paths.get(System.getenv("APPDATA"), "Spotify")
                : Paths.get("/opt/spotify");
        if (!Files.exists(spotifyApp)) {
            System.err.println("Spotify application directory not found: " + spotifyApp);
            return;
        }
        enableEmployeeMode(spotifyApp);

        Path spotifyCache = isWindows
                ? Paths.get(System.getenv("LOCALAPPDATA"), "Spotify")
                : Paths.get("/home", System.getProperty("user.name"), ".cache", "spotify");
        if (!Files.exists(spotifyCache)) {
            System.err.println("Spotify cache directory not found: " + spotifyCache);
            return;
        }

        enableDevTools(spotifyCache);
    }

    private static void enableDevTools(Path spotifyLocal) throws IOException {
        Path file = spotifyLocal.resolve("offline.bnk");
        TextPatcher patcher = (fileName, content) -> {
            content = content.replaceAll("(?<=app-developer..|app-developer>)0", "2");
            System.out.println("Patched " + fileName + " to enable developer tools.");
            return content;
        };
        patchFile(file, patcher);
    }

    private static void enableEmployeeMode(Path spotifyAppData) throws IOException {
        Path appsDir = spotifyAppData.resolve("Apps");
        Path xpuiSpa = appsDir.resolve("xpui.spa");
        patchArchive(xpuiSpa, (TextPatcher) (fileName, content) -> {
            if (!fileName.endsWith(".js")) {
                return content; // Only patch JavaScript files
            }
            if (content.contains(".employee.isEmployee")) {
                content = content.replace(
                        ".employee.isEmployee",
                        ".autoPlay"
                );
                System.out.println("Patched " + fileName + " to enable employee mode.");
            }
            return content;
        });
    }

    private static void patchFile(Path archive, Patcher patcher) throws IOException {
        Files.write(archive, patcher.patch(archive.getFileName().toString(), Files.readAllBytes(archive)));
    }

    private static void patchArchive(Path archive, Patcher patcher) throws IOException {
        if (!Files.exists(archive)) {
            System.err.println("Archive not found: " + archive);
            return;
        }

        if (!Files.isWritable(archive)) {
            System.err.println("No write permission for archive: " + archive);
            return;
        }

        // Move archive to temp location
        Path tempArchive = Paths.get(System.getProperty("java.io.tmpdir"), "spotify-temp-" + archive.getFileName());
        Files.copy(archive, tempArchive);

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
        } finally {
            // Delete temp archive
            Files.deleteIfExists(tempArchive);
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

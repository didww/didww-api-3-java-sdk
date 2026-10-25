package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.didww.sdk.prices.PriceFile;
import com.didww.sdk.prices.PriceFileReader;
import com.didww.sdk.prices.PricesService;
import com.didww.sdk.prices.VoiceInPrice;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Downloads the Voice IN price file (2026-10-22), checks it again with If-Modified-Since, and reads a few rows.
 *
 * The file is written exactly as served, still gzipped, and dated with the response's Last-Modified.
 * fetch(path) sends the modification time of the file at path as If-Modified-Since, so while the price file
 * is unchanged the second call, and any later run against the same file, is answered 304 and keeps it.
 *
 * The file goes to the directory given as an argument, or to didww-prices in the user cache directory
 * (%LOCALAPPDATA% on Windows, ~/Library/Caches on macOS, otherwise $XDG_CACHE_HOME or ~/.cache).
 * A missing directory is created for the current user only (rwx------) where the file system supports
 * POSIX permissions.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.PricesDownloadExample
 *            --args="[dir]"
 */
public class PricesDownloadExample {

    private static final int FIRST_ROWS = 5;

    public static void main(String[] args) throws IOException {
        DidwwClient client = ExampleClientFactory.fromEnv();
        Path dir = createDirectory(args.length > 0 ? Paths.get(args[0]) : defaultPriceDir());
        Path path = dir.resolve("voice_in_prices.csv.gz");
        System.out.println("Price files go to " + dir);

        try {
            run(client.voiceInPrices(), path);
        } catch (DidwwPricesException e) {
            if (e.getKind() == PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED) {
                System.out.println("No Voice IN price plan on this account: " + e.getMessage());
            } else if (e.getKind() == PricesErrorKind.SERVICE_UNAVAILABLE) {
                System.out.println("The price file is not available yet, retry in " + e.getRetryAfter()
                        + ": " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private static void run(PricesService<VoiceInPrice> prices, Path path) throws IOException {
        System.out.println("=== HEAD ===");
        PriceFile head = prices.head();
        System.out.println("Last-Modified: " + head.getLastModified() + ", " + head.getContentLength() + " bytes, "
                + head.getFileName());

        System.out.println("\n=== Fetch " + path + " ===");
        fetch(prices, path);

        System.out.println("\n=== Fetch again ===");
        fetch(prices, path);

        System.out.println("\n=== First rows ===");
        try (Stream<Map<String, String>> rows = PriceFileReader.rows(path)) {
            rows.limit(FIRST_ROWS).forEach(row -> System.out.println("  " + row.get("Country") + " "
                    + row.get("Destination") + " " + row.get("Source type") + ": " + row.get("Rate (USD, excl. VAT)")));
        }
    }

    private static void fetch(PricesService<VoiceInPrice> prices, Path path) {
        PriceFile file = prices.fetch(path);
        System.out.println(file.isNotModified() ? "Not modified, file kept" : "Downloaded " + file.getBytesWritten() + " bytes");
    }

    private static Path defaultPriceDir() {
        Path cache = userCacheDir();
        if (cache == null) {
            throw new IllegalStateException("No user cache directory; pass a directory: --args=\"<dir>\"");
        }
        return cache.resolve("didww-prices");
    }

    // Creates the directory when missing, for the current user only where POSIX permissions are supported;
    // an existing directory is left as it is.
    private static Path createDirectory(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            return dir;
        }
        Path parent = dir.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try {
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                Files.createDirectory(dir, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            } else {
                Files.createDirectory(dir);
            }
        } catch (FileAlreadyExistsException e) {
            if (!Files.isDirectory(dir)) {
                throw e;
            }
        }
        return dir;
    }

    // %LOCALAPPDATA% on Windows, ~/Library/Caches on macOS, otherwise $XDG_CACHE_HOME or ~/.cache.
    // Null when it cannot be determined.
    private static Path userCacheDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.startsWith("windows")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            return isBlank(localAppData) ? null : Paths.get(localAppData);
        }
        boolean mac = os.startsWith("mac");
        if (!mac) {
            String xdgCache = System.getenv("XDG_CACHE_HOME");
            if (!isBlank(xdgCache)) {
                return Paths.get(xdgCache);
            }
        }
        String home = System.getProperty("user.home");
        if (isBlank(home)) {
            return null;
        }
        return mac ? Paths.get(home, "Library", "Caches") : Paths.get(home, ".cache");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.didww.sdk.prices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * A downloaded price file gets the permissions of any new file (the umask), not
 * the owner-only mode of a temporary file, also when it replaces such a file.
 * Java cannot read the umask, so a file created by the test is the reference.
 */
@DisabledOnOs(OS.WINDOWS)
class PriceFileUmaskTest extends PricesTestBase {
    private static final Set<PosixFilePermission> OWNER_ONLY = PosixFilePermissions.fromString("rw-------");

    @TempDir
    Path dir;

    @Test
    void testPriceFileDownloadToFileFollowsUmask() throws IOException {
        byte[] fixture = voiceInFixture();
        wireMock.stubFor(any(urlPathEqualTo(VOICE_IN_FILE)).willReturn(priceFile(fixture, LAST_MODIFIED_HEADER)));
        Set<PosixFilePermission> newFileMode = Files.getPosixFilePermissions(Files.createFile(dir.resolve("reference")));
        assumeFalse(newFileMode.equals(OWNER_ONLY), "a umask of 077 cannot tell the modes apart");
        Path newPath = dir.resolve("voice_in_prices.csv.gz");
        Path oldPath = dir.resolve("voice_out_prices.csv.gz");
        Files.write(oldPath, "old copy".getBytes(StandardCharsets.US_ASCII));
        Files.setPosixFilePermissions(oldPath, OWNER_ONLY);

        client.voiceInPrices().downloadToFile(newPath);
        client.voiceInPrices().downloadToFile(oldPath);

        assertThat(Files.getPosixFilePermissions(newPath)).isEqualTo(newFileMode);
        assertThat(Files.getPosixFilePermissions(oldPath)).as("the replaced owner-only file gets the mode of a new file")
                .isEqualTo(newFileMode);
        assertThat(Files.readAllBytes(oldPath)).isEqualTo(fixture);
    }
}

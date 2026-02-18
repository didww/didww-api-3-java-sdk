package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.Encrypt;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UploadFileExample {

    public static void main(String[] args) throws Exception {
        // FILE_PATH=/path/to/file.jpeg DIDWW_API_KEY=YOUR_KEY ./gradlew runExample -PexampleClass=com.didww.examples.UploadFileExample
        String filePath = System.getenv("FILE_PATH");
        Path path;
        if (filePath == null || filePath.isBlank()) {
            path = Files.createTempFile("didww-example-", ".pdf");
            createSamplePdf(path, "Example");
            System.out.println("FILE_PATH is not set, created sample PDF: " + path);
        } else {
            path = Paths.get(filePath);
        }

        DidwwClient client = ExampleClientFactory.fromEnv();

        byte[] fileContent = Files.readAllBytes(path);
        Encrypt enc = new Encrypt(client);

        String fingerprint = enc.getFingerprint();
        System.out.println("Fingerprint: " + fingerprint);

        byte[] encryptedData = enc.encrypt(fileContent);
        System.out.println("Encrypted size: " + encryptedData.length + " bytes");

        String originalName = path.getFileName() != null ? path.getFileName().toString() : "example.pdf";
        List<String> uploadedIds = client.uploadEncryptedFile(
                encryptedData,
                originalName + ".enc",
                fingerprint,
                originalName
        );
        System.out.println("Uploaded encrypted file IDs: " + uploadedIds);
    }

    private static void createSamplePdf(Path path, String text) throws Exception {
        String escapedText = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        String stream = "BT /F1 24 Tf 72 720 Td (" + escapedText + ") Tj ET";

        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n");
        objects.add("4 0 obj\n<< /Length " + stream.length() + " >>\nstream\n" + stream + "\nendstream\nendobj\n");
        objects.add("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

        List<Integer> offsets = new ArrayList<>();
        for (String obj : objects) {
            offsets.add(out.size());
            out.write(obj.getBytes(StandardCharsets.US_ASCII));
        }

        int xrefOffset = out.size();
        out.write(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
        out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (Integer offset : offsets) {
            out.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.US_ASCII));
        }

        out.write(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n").getBytes(StandardCharsets.US_ASCII));
        out.write(("startxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));

        Files.write(path, out.toByteArray());
    }
}

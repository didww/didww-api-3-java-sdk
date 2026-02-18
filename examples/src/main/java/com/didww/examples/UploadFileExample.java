package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.Encrypt;

import java.nio.file.Files;
import java.nio.file.Paths;

public class UploadFileExample {

    public static void main(String[] args) throws Exception {
        // FILE_PATH=/path/to/file.jpeg DIDWW_API_KEY=YOUR_KEY ./gradlew runExample -PexampleClass=com.didww.examples.UploadFileExample
        String filePath = System.getenv("FILE_PATH");
        if (filePath == null) {
            System.err.println("Please provide FILE_PATH env variable");
            System.exit(1);
        }

        DidwwClient client = ExampleClientFactory.fromEnv();

        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        Encrypt enc = new Encrypt(client);

        String fingerprint = enc.getFingerprint();
        System.out.println("Fingerprint: " + fingerprint);

        byte[] encryptedData = enc.encrypt(fileContent);
        System.out.println("Encrypted size: " + encryptedData.length + " bytes");

        // Use the encrypted data with the encrypted_files endpoint
        // to upload the file to DIDWW
    }
}

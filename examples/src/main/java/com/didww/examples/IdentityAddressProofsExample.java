package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.Encrypt;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.resource.*;
import com.didww.sdk.resource.enums.IdentityType;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates identity and address, encrypts and uploads files,
 * attaches proofs to both identity and address.
 *
 * Demonstrates:
 * 1. Create an identity with a country
 * 2. Create an address linked to the identity
 * 3. Fetch proof types for identity and address
 * 4. Encrypt and upload a PDF file
 * 5. Create proofs attached to identity and address
 * 6. Clean up created resources
 */
public class IdentityAddressProofsExample {

    public static void main(String[] args) throws Exception {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // --- Step 1: Get a country ---
        List<Country> countries = client.countries().list().getData();
        Country country = countries.get(0);
        System.out.println("Using country: " + country.getName() + " (" + country.getId() + ")");

        // --- Step 2: Create an identity ---
        Identity identity = new Identity();
        identity.setFirstName("John");
        identity.setLastName("Doe");
        identity.setPhoneNumber("12125551234");
        identity.setIdentityType(IdentityType.PERSONAL);
        identity.setCountry(country);
        identity = client.identities().create(identity).getData();
        System.out.println("Created identity: " + identity.getId()
                + " (" + identity.getFirstName() + " " + identity.getLastName() + ")");

        // --- Step 3: Create an address linked to the identity ---
        Address address = new Address();
        address.setCityName("New York");
        address.setPostalCode("10001");
        address.setAddress("123 Main St");
        address.setIdentity(Identity.build(identity.getId()));
        address.setCountry(Country.build(country.getId()));
        address = client.addresses().create(address).getData();
        System.out.println("Created address: " + address.getId() + " (" + address.getAddress() + ")");

        // --- Step 4: Fetch proof types ---
        List<ProofType> proofTypes = client.proofTypes().list().getData();
        ProofType identityProofType = null;
        ProofType addressProofType = null;
        for (ProofType pt : proofTypes) {
            if ("Personal".equals(pt.getEntityType()) && identityProofType == null) {
                identityProofType = pt;
            } else if ("Address".equals(pt.getEntityType()) && addressProofType == null) {
                addressProofType = pt;
            }
            if (identityProofType != null && addressProofType != null) break;
        }

        if (identityProofType != null) {
            System.out.println("Identity proof type: " + identityProofType.getName()
                    + " (" + identityProofType.getId() + ")");
        } else {
            System.out.println("No identity proof type found");
        }
        if (addressProofType != null) {
            System.out.println("Address proof type: " + addressProofType.getName()
                    + " (" + addressProofType.getId() + ")");
        } else {
            System.out.println("No address proof type found");
        }

        // --- Step 5: Encrypt and upload PDF files ---
        Path samplePdf = Files.createTempFile("didww-example-", ".pdf");
        createSamplePdf(samplePdf, "Proof Document");
        byte[] fileContent = Files.readAllBytes(samplePdf);

        Encrypt enc = new Encrypt(client);
        String fingerprint = enc.getFingerprint();

        // Upload for identity proof
        byte[] encrypted1 = enc.encrypt(fileContent);
        List<String> fileIds1 = client.uploadEncryptedFile(
                encrypted1, "identity_proof.pdf.enc", fingerprint, "identity_proof.pdf");

        // Upload for address proof
        byte[] encrypted2 = enc.encrypt(fileContent);
        List<String> fileIds2 = client.uploadEncryptedFile(
                encrypted2, "address_proof.pdf.enc", fingerprint, "address_proof.pdf");

        System.out.println("Uploaded encrypted files: " + fileIds1 + ", " + fileIds2);

        // --- Step 6: Create proof for identity ---
        if (identityProofType != null) {
            Proof identityProof = new Proof();
            identityProof.setEntity(Identity.build(identity.getId()));
            identityProof.setProofType(ProofType.build(identityProofType.getId()));
            identityProof.setFiles(Arrays.asList(EncryptedFile.build(fileIds1.get(0))));

            QueryParams params = QueryParams.builder().include("proof_type").build();
            Proof createdProof = client.proofs().create(identityProof, params).getData();
            System.out.println("Created identity proof: " + createdProof.getId()
                    + " (type=" + createdProof.getProofType().getName() + ")");
        }

        // --- Step 7: Create proof for address ---
        if (addressProofType != null) {
            Proof addressProof = new Proof();
            addressProof.setEntity(Address.build(address.getId()));
            addressProof.setProofType(ProofType.build(addressProofType.getId()));
            addressProof.setFiles(Arrays.asList(EncryptedFile.build(fileIds2.get(0))));

            QueryParams params = QueryParams.builder().include("proof_type").build();
            Proof createdProof = client.proofs().create(addressProof, params).getData();
            System.out.println("Created address proof: " + createdProof.getId()
                    + " (type=" + createdProof.getProofType().getName() + ")");
        }

        // --- Step 8: Verify ---
        System.out.println("\nIdentity " + identity.getId() + ": verified=" + identity.getVerified());
        System.out.println("Address " + address.getId() + ": verified=" + address.getVerified());

        // --- Step 9: Clean up ---
        System.out.println("\nCleaning up...");
        client.addresses().delete(address.getId());
        System.out.println("  Deleted address: " + address.getId());
        client.identities().delete(identity.getId());
        System.out.println("  Deleted identity: " + identity.getId());
        for (String fid : fileIds1) {
            client.encryptedFiles().delete(fid);
            System.out.println("  Deleted encrypted file: " + fid);
        }
        for (String fid : fileIds2) {
            client.encryptedFiles().delete(fid);
            System.out.println("  Deleted encrypted file: " + fid);
        }

        System.out.println("\nDone!");
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

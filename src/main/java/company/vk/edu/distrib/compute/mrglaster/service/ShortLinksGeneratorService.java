package company.vk.edu.distrib.compute.mrglaster.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShortLinksGeneratorService {
    private final URI baseUrl;

    public ShortLinksGeneratorService(String baseUrl) {
        this.baseUrl = URI.create(baseUrl);
    }

    public String generateLinkID(String longUrl) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(longUrl.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String shortHash = hexString.substring(0, 10);
        return shortHash;
    }

    public String generateShortURL(String linkId) throws NoSuchAlgorithmException {
        return baseUrl.resolve(linkId).toString();
    }
}

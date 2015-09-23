package de.codewave.utils.registration;

import de.codewave.utils.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.utils.registration.RegistrationUtils
 */
public class RegistrationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationUtils.class);

    public static String getRegistrationData(URL signedRegistrationUrl, URL publicKeyUrl) {
        try {
            List<String> lines = IOUtils.readTextLines(signedRegistrationUrl, false);
            List<String> dataLines = new ArrayList<String>();
            for (String line = lines.remove(0); line.length() != 0; line = lines.remove(0)) {
                dataLines.add(line);
            }
            byte[] bytes = Base64.decodeBase64(StringUtils.trim(StringUtils.join(IOUtils.readTextLines(publicKeyUrl, false), "")).getBytes("UTF-8"));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            String registrationData = new String(Base64.decodeBase64(StringUtils.trim(StringUtils.join(dataLines, "")).getBytes()), "UTF-8");
            signature.update(registrationData.getBytes("UTF-8"));
            if (signature.verify(Base64.decodeBase64(StringUtils.trim(StringUtils.join(lines, "")).getBytes("UTF-8")))) {
                return registrationData;
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not read registration data.", e);
            }
        }
        return null;
    }

    public static String createSignedRegistration(String registrationData, URL privateKeyUrl)
            throws SignatureException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        byte[] dataBytes = registrationData.getBytes("UTF-8");
        byte[] privateKeyBytes = Base64.decodeBase64(StringUtils.trim(StringUtils.join(IOUtils.readTextLines(privateKeyUrl, false), "")).getBytes(
                "UTF-8"));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(dataBytes);
        byte[] signatureBytes = signature.sign();
        String lines = new String(Base64.encodeBase64Chunked(registrationData.getBytes("UTF-8")), "UTF-8");
        lines += SystemUtils.LINE_SEPARATOR + SystemUtils.LINE_SEPARATOR;
        lines += new String(Base64.encodeBase64Chunked(signatureBytes), "UTF-8");
        return lines;
    }

    public static KeyPair generateKeyPair(String applicationName) throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(applicationName.getBytes("UTF-8"));
        generator.initialize(512, random);
        return generator.generateKeyPair();
    }
}

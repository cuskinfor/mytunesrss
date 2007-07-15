import sun.misc.*;

import java.io.*;
import java.security.*;
import java.security.spec.*;

public class P300165105 {
    public static final int ERC_SUCCESS = 0;
    public static final int ERC_SUCCESS_BIN = 1;
    public static final int ERC_ERROR = 10;
    public static final int ERC_MEMORY = 11;
    public static final int ERC_FILE_IO = 12;
    public static final int ERC_BAD_ARGS = 13;
    public static final int ERC_BAD_INPUT = 14;
    public static final int ERC_EXPIRED = 15;
    public static final int ERC_INTERNAL = 16;
    public static int ExitCode = ERC_INTERNAL;
    public static String fileEncoding = "ISO-8859-1";
    private static final String expectedEncoding = "UTF8";
    public static String REG_NAME;
    public static String EXPIRATION;
    public static int PRODUCT_ID;
    public static String KeyMIMEType;
    public static String KeyDisplayFileName;
    public static String KeyData;

    private static void GenerateKey() throws Exception {
        String privateKeyText =
            "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAmXSPI3gvxsNNfnl9FZzNRaEoUoly" +
                "i3XCN8BY5pE/zw0uJAPPpiP7oHXjROE47ripa7UHwYEDSLCjmmqjdM1mXQIDAQABAkB4a9yr4/Py" +
                "EW8tsd6z6CG543bGFnBZ+mYX5Ayfmxosux8EJO0/p86J66QzLvMpeFrkWTTU1a+IpZUG1UCN48xN" +
                "AiEAx/4hIwgUhX2NtLu80M1vnOMCyQVyK5vB8uY7A2oU6CcCIQDEbhKnGWS77jjBe/2DlIRyTN+T" +
                "j1w9TebfMZ+kaBnr2wIhAMI1vMQiIpuHU0cBUNiLxylZIelISpiihvN0NDaam3bdAiEAvZD44Vhp" +
                "VdCF53wQYb6fv2sezVoqC2O17ioGxfRNJ8cCICtujU0LS0jPMF/No6P+CsfTSm5HMAaHFN0R5JzE" +
                "O21P";
        String regData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<registration>\n" +
            "    <name>" + REG_NAME + "</name>\n" +
            "    <registered>true</registered>\n";
        if (EXPIRATION != null && EXPIRATION.length() > 0) {
            regData += "    <expiration>" + EXPIRATION + "</expiration>\n";
        }
        regData += "</registration>";
        byte[] privateKeyBytes = new BASE64Decoder().decodeBuffer(privateKeyText);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(regData.getBytes());
        byte[] signatureBytes = signature.sign();
        KeyData = new String(new BASE64Encoder().encode(regData.getBytes("UTF-8")));
        KeyData += System.getProperty("line.separator") + System.getProperty("line.separator");
        KeyData += new String(new BASE64Encoder().encode(signatureBytes));
        KeyDisplayFileName = "MyTunesRSS-3.0.key";
        KeyMIMEType = "text/plain";
    }

    private static void ParseInputLine(String line) {
        int p = line.indexOf('=');
        if (p != -1) {
            String key = line.substring(0, p);
            String value = line.substring(p + 1);
            if (key.indexOf("ENCODING") >= 0) {
                fileEncoding = value;
            } else if (key.compareTo("REG_NAME") == 0) {
                REG_NAME = value;
            } else if (key.compareTo("EXPIRATION") == 0) {
                EXPIRATION = value;
            } else if (key.compareTo("PRODUCT_ID") == 0) {
                PRODUCT_ID = Integer.valueOf(value).intValue();
            }
        }
    }

    private static void ReadInput(String pathname) throws Exception {
        String s;
        BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(pathname), expectedEncoding));
        while ((s = fin.readLine()) != null) {
            ParseInputLine(s);
        }
        fin.close();
        if (!fileEncoding.equals(expectedEncoding)) {
            ExitCode = ERC_BAD_INPUT;
            throw new Exception("bad input encoding, expected "
                + expectedEncoding + " but found: " + fileEncoding);
        }
        if (PRODUCT_ID != 300165105) {
            ExitCode = ERC_BAD_INPUT;
            throw new Exception("Bad product ID: " + PRODUCT_ID);
        }

    }

    public static int KeyMain(String[] args) {
        try {
            try {
                if (args.length == 3) {
                    ReadInput(args[0]);
                    GenerateKey();
                    WriteText(args[1], KeyMIMEType + ":" + KeyDisplayFileName);
                    WriteText(args[2], KeyData);
                    ExitCode = ERC_SUCCESS_BIN;
                } else {
                    System.out.println("Usage: <input><output1><output2>");
                    ExitCode = ERC_BAD_ARGS;
                }
            } catch (IOException eio) {
                ExitCode = ERC_FILE_IO;
                throw eio;
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            try {
                WriteText(args[1], "Error #" + ExitCode + ": " + e.getMessage());
            } catch (IOException eio) {
                System.err.println("could not write error file: " + eio.getMessage());
            }
        }
        return ExitCode;
    }

    private static void WriteText(String pathname, String value) throws IOException {
        BufferedWriter fout;
        fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
            pathname), fileEncoding));
        fout.write(value);
        fout.flush();
    }

    public static final void main(String[] args) {
        int erc;

        System.out.println("JAVA Example Key Generator");
        erc = KeyMain(args);

        switch (erc) {
            case ERC_SUCCESS:
                System.out.println("ERC_SUCCESS");
                break;
            case ERC_SUCCESS_BIN:
                System.out.println("ERC_SUCCESS_BIN");
                break;
            case ERC_ERROR:
                System.out.println("ERC_ERROR");
                break;
            case ERC_MEMORY:
                System.out.println("ERC_MEMORY");
                break;
            case ERC_FILE_IO:
                System.out.println("ERC_FILE_IO");
                break;
            case ERC_BAD_ARGS:
                System.out.println("ERC_BAD_ARGS");
                break;
            case ERC_BAD_INPUT:
                System.out.println("ERC_BAD_INPUT");
                break;
            case ERC_EXPIRED:
                System.out.println("ERC_EXPIRED");
                break;
            case ERC_INTERNAL:
                System.out.println("ERC_INTERNAL");
                break;
        }

        System.exit(erc);
    }
}
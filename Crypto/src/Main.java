import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        LWJAlgorithm lwj = new LWJAlgorithm();

        // 1. 1,000,000자 문자열 생성
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1_000_000; i++) sb.append((char) ('A' + (i % 26)));
        String originalText = sb.toString();
        byte[] originalBytes = originalText.getBytes(StandardCharsets.UTF_8);

        // 2. 고정 16바이트 키
        byte[] key = new byte[]{
                (byte) 0x1F, (byte) 0xA2, (byte) 0x3B, (byte) 0x4C,
                (byte) 0x55, (byte) 0x60, (byte) 0x7D, (byte) 0x8E,
                (byte) 0x9F, (byte) 0xAA, (byte) 0xBD, (byte) 0xCE,
                (byte) 0xD1, (byte) 0xE2, (byte) 0xF3, (byte) 0x04
        };

        // ================================
        // LWJ 암호화
        // ================================
        long lwjEncStart = System.nanoTime();
        byte[] lwjEncrypted = encryptWithLWJ(originalBytes, lwj, key);
        long lwjEncEnd = System.nanoTime();

        long lwjDecStart = System.nanoTime();
        byte[] lwjDecrypted = decryptWithLWJ(lwjEncrypted, lwj, key, originalBytes.length);
        long lwjDecEnd = System.nanoTime();

        // ================================
        // AES 암호화
        // ================================
        long aesEncStart = System.nanoTime();
        byte[] aesEncrypted = encryptWithAES(originalBytes, key);
        long aesEncEnd = System.nanoTime();

        long aesDecStart = System.nanoTime();
        byte[] aesDecrypted = decryptWithAES(aesEncrypted, key);
        long aesDecEnd = System.nanoTime();

        // ================================
        // 결과 출력
        // ================================
        System.out.printf("LWJ 암호화 시간: %.3f초%n", (lwjEncEnd - lwjEncStart) / 1e9);
        System.out.printf("LWJ 복호화 시간: %.3f초%n", (lwjDecEnd - lwjDecStart) / 1e9);
        System.out.println("LWJ 복호화 성공 여부: " +
                Arrays.equals(originalBytes, lwjDecrypted));

        System.out.printf("AES 암호화 시간: %.3f초%n", (aesEncEnd - aesEncStart) / 1e9);
        System.out.printf("AES 복호화 시간: %.3f초%n", (aesDecEnd - aesDecStart) / 1e9);
        System.out.println("AES 복호화 성공 여부: " +
                Arrays.equals(originalBytes, aesDecrypted));
    }

    public static byte[] encryptWithLWJ(byte[] data, LWJAlgorithm alg, byte[] key) {
        int numBlocks = (int) Math.ceil(data.length / 16.0);
        byte[] out = new byte[numBlocks * 16];

        for (int i = 0; i < numBlocks; i++) {
            int start = i * 16;
            int end = Math.min(start + 16, data.length);
            byte[] block = Arrays.copyOfRange(data, start, end);
            byte[] enc = alg.encrypt(block, key);
            System.arraycopy(enc, 0, out, start, 16);
        }
        return out;
    }

    public static byte[] decryptWithLWJ(byte[] data, LWJAlgorithm alg, byte[] key, int originalLength) {
        int numBlocks = data.length / 16;
        byte[] out = new byte[data.length];

        for (int i = 0; i < numBlocks; i++) {
            int start = i * 16;
            byte[] block = Arrays.copyOfRange(data, start, start + 16);
            byte[] dec = alg.decrypt(block, key);
            System.arraycopy(dec, 0, out, start, 16);
        }
        return Arrays.copyOf(out, originalLength);
    }

    public static byte[] encryptWithAES(byte[] data, byte[] keyBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

        // Zero padding
        int paddedLen = ((data.length + 15) / 16) * 16;
        byte[] padded = Arrays.copyOf(data, paddedLen);

        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(padded);
    }

    public static byte[] decryptWithAES(byte[] encrypted, byte[] keyBytes) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encrypted);
    }
}
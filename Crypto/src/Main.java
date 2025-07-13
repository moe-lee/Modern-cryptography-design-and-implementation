import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        LWJAlgorithm algorithm = new LWJAlgorithm();

        // 1. 1,000,000자 원문 생성
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1_000_000; i++) sb.append((char) ('A' + (i % 26)));
        String originalText = sb.toString();
        byte[] originalBytes = originalText.getBytes(StandardCharsets.UTF_8);

        // 2. 16바이트 고정 키
        byte[] key = new byte[] {
                (byte) 0x1F, (byte) 0xA2, (byte) 0x3B, (byte) 0x4C,
                (byte) 0x55, (byte) 0x60, (byte) 0x7D, (byte) 0x8E,
                (byte) 0x9F, (byte) 0xAA, (byte) 0xBD, (byte) 0xCE,
                (byte) 0xD1, (byte) 0xE2, (byte) 0xF3, (byte) 0x04
        };

        // 3. 암호화
        long encStart = System.nanoTime();
        byte[] encrypted = encryptLargeText(originalBytes, algorithm, key);
        long encEnd = System.nanoTime();
        System.out.printf("🔐 암호화 시간: %.3f초%n", (encEnd - encStart) / 1e9);

        // 4. 복호화
        long decStart = System.nanoTime();
        byte[] decrypted = decryptLargeText(encrypted, algorithm, key);
        long decEnd = System.nanoTime();
        System.out.printf("🔓 복호화 시간: %.3f초%n", (decEnd - decStart) / 1e9);

        // 5. 정확성 확인
        String recoveredText = new String(decrypted, StandardCharsets.UTF_8);
        if (originalText.equals(recoveredText)) {
            System.out.println("✅ 복호화 결과가 원문과 일치합니다.");
        } else {
            System.out.println("❌ 복호화 실패: 결과가 원문과 다릅니다.");
        }
    }

    // 16바이트 단위 블록 암호화 + 패딩 처리
    public static byte[] encryptLargeText(byte[] data, LWJAlgorithm algorithm, byte[] key) {
        int totalLen = data.length;
        int numBlocks = (int) Math.ceil(data.length / 16.0);
        byte[] encrypted = new byte[numBlocks * 16];

        for (int i = 0; i < numBlocks; i++) {
            int start = i * 16;
            int end = Math.min(start + 16, totalLen);
            byte[] block = Arrays.copyOfRange(data, start, end);
            byte[] cipherBlock = algorithm.encrypt(block, key);
            System.arraycopy(cipherBlock, 0, encrypted, start, 16);
        }

        return encrypted;
    }

    public static byte[] decryptLargeText(byte[] encrypted, LWJAlgorithm algorithm, byte[] key) {
        int numBlocks = encrypted.length / 16;
        byte[] decrypted = new byte[numBlocks * 16];

        for (int i = 0; i < numBlocks; i++) {
            int start = i * 16;
            byte[] block = Arrays.copyOfRange(encrypted, start, start + 16);
            byte[] plainBlock = algorithm.decrypt(block, key);
            System.arraycopy(plainBlock, 0, decrypted, start, 16);
        }

        // 원래 길이만큼 자르기 (패딩 없음)
        return Arrays.copyOf(decrypted, decrypted.length); // 필요시 truncate
    }
}
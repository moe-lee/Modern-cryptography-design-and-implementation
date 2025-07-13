import java.util.HashMap;
import java.util.Map;

public class LWJAlgorithm {
    private static final Map<Integer, Integer> S_BOX = new HashMap<>();
    private static final int BYTE = 8;
    private static final byte[] XOR_MAST_BYTES = {
            (byte) 0x9F, (byte) 0xDE, (byte) 0x08, (byte) 0x2E,
            (byte) 0x54, (byte) 0x91, (byte) 0xFF, (byte) 0x9F,
            (byte) 0x7C, (byte) 0xE1, (byte) 0x4D, (byte) 0x6D,
            (byte) 0x2C, (byte) 0x38, (byte) 0xE9, (byte) 0x54
    };

    static {
        String sBoxHex = "5F C7 41 C0 4C 80 81 2B 56 32 01 90 CD 69 98 45 " +
                "AC 3C AA C3 CC E2 21 4E 9B 6F 1C 99 FF 1F 44 2E " +
                "59 CB 78 15 55 D1 49 AF 57 1B 0B 4A 42 EE 52 73 " +
                "37 29 10 28 F6 7D 33 FC 31 74 3E 0C 88 11 5C 43 " +
                "B2 38 97 F4 F0 85 E4 1E 64 D0 6D 58 92 08 91 3F " +
                "60 6A 36 8B 16 CF 5A E0 84 A8 DD D2 A4 A7 E6 A9 " +
                "6E 89 9C C8 20 A3 50 3B 23 CA 34 C1 66 D7 F9 9E " +
                "62 18 26 B9 7C FB D6 4B DF 5D EC 12 76 C9 48 6C " +
                "AB B7 70 8F E1 79 27 71 2F 19 C5 D3 07 E3 F2 40 " +
                "06 3A A1 EF 14 94 7E BE 25 8D DE 77 ED E5 B0 E8 " +
                "0E 54 D4 B4 A2 35 17 E7 2C 68 9F 04 7A 7B 0F C2 " +
                "09 AE 51 83 BB 6B A5 9A 87 65 4F 8C 03 C4 9D B6 " +
                "DC C6 13 95 F7 1D 00 2D 8E EB 47 F1 A0 DB B8 BC " +
                "46 D5 5B 05 A6 30 4D B5 02 53 61 DA 3D 2A F3 FD " +
                "0A CE FE 72 82 EA BD 7F F8 F5 39 8A AD 63 96 E9 " +
                "BF 1A BA B3 D9 0D 24 86 22 FA 93 B1 5E 67 D8 75";
        String[] hexUnits = sBoxHex.split(" ");
        for (int i = 0; i < 256; i++) {
            S_BOX.put(i, Integer.parseInt(hexUnits[i], 16));
        }
    }

    public int substitute(int plain) {
        int res = 0;
        for (int shamt = 0; shamt < 16; shamt++) {
            int b = (plain >> (shamt * BYTE)) & 0xFF;
            res |= (S_BOX.get(b) << (shamt * BYTE));
        }
        return res;
    }

    public byte[] permutate(byte[] plain) {
        byte[] chunk = shakeAlongColumns(plain);
        chunk = swapBytes(chunk);
        chunk = boilBits(chunk);
        chunk = doXor(chunk);
        chunk = shakeAlongRows(chunk);
        chunk = swapBytes(chunk);
        chunk = boilBits(chunk);
        return chunk;
    }

    public byte[] shakeAlongColumns(byte[] plain) {
        byte[] res = new byte[16];
        for (int i = 0; i < 16; i++) res[i] = plain[(i + 8) % 16];
        return res;
    }

    public byte[] shakeAlongRows(byte[] plain) {
        byte[] res = new byte[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res[i * 4 + j] = plain[i * 4 + ((j + 2) % 4)];
            }
        }
        return res;
    }

    public byte[] swapBytes(byte[] plain) {
        byte[] res = plain.clone();
        swap(res, 0, 6); swap(res, 3, 10);
        swap(res, 5, 12); swap(res, 9, 15);
        swap(res, 1, 7); swap(res, 2, 4);
        swap(res, 8, 14); swap(res, 11, 13);
        return res;
    }

    private void swap(byte[] arr, int i, int j) {
        byte tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public byte[] doXor(byte[] plain) {
        byte[] res = new byte[16];
        for (int i = 0; i < 16; i++) res[i] = (byte)(plain[i] ^ XOR_MAST_BYTES[i]);
        return res;
    }

    public byte[] boilBits(byte[] plain) {
        byte[] res = plain.clone();
        for (int i = 0; i < 15; i++) {
            byte[] r = bitShake(res[i], res[i + 1]);
            res[i] = r[0];
            res[i + 1] = r[1];
        }
        return res;
    }

    public byte[] bitShake(byte upper, byte lower) {
        int tempUpper = 0, tempLower = 0;
        int u = Byte.toUnsignedInt(upper), l = Byte.toUnsignedInt(lower);

        for (int i = 0; i < 4; i++) {
            int upperBit = ((u & (0x80 >> i)) >> (i * 2 - i));
            int lowerBit = ((l & (0x80 >> i)) >> (i * 2 - i + 1));
            tempUpper |= upperBit | lowerBit;
        }
        for (int i = 0; i < 4; i++) {
            tempLower |= (((u & (0x80 >> (i + 4))) << 4) >> (i * 2 - i));
            tempLower |= (((l & (0x80 >> (i + 4))) << 4) >> (i * 2 - i + 1));
        }

        int buff = (tempUpper & 0xF0) >> 4;
        tempUpper = (tempUpper << 4) | ((tempLower & 0xF0) >> 4);
        tempLower = ((tempLower & 0x0F) << 4) | buff;

        return new byte[]{(byte) tempUpper, (byte) tempLower};
    }

    public byte[] bitUnshake(byte upper, byte lower) {
        int u = Byte.toUnsignedInt(upper), l = Byte.toUnsignedInt(lower);
        int buff = (l & 0x0F) << 4;
        l = (l >> 4) | ((u & 0x0F) << 4);
        u = (u >> 4) | buff;

        int recoveredUpper = 0, recoveredLower = 0;
        for (int i = 0; i < 8; i++) {
            int shift = 7 - i;
            int bitU = (u >> shift) & 1;
            int bitL = (l >> shift) & 1;

            if (i % 2 == 0) {
                recoveredUpper |= bitU << (7 - i / 2);
                recoveredUpper |= bitL << (3 - i / 2);
            } else {
                recoveredLower |= bitU << (7 - i / 2);
                recoveredLower |= bitL << (3 - i / 2);
            }
        }
        return new byte[]{(byte) recoveredUpper, (byte) recoveredLower};
    }

    public byte[] encrypt(byte[] plain, byte[] key) {
        byte[] state = new byte[16];
        System.arraycopy(plain, 0, state, 0, Math.min(plain.length, 16));

        for (int round = 0; round < 16; round++) {
            for (int i = 0; i < 16; i++) state[i] ^= key[i];
            for (int i = 0; i < 16; i++) state[i] = (byte)(int) S_BOX.get(Byte.toUnsignedInt(state[i]));
            state = permutate(state);
        }

        for (int i = 0; i < 16; i++) state[i] ^= key[i];
        return state;
    }

    public byte[] decrypt(byte[] cipher, byte[] key) {
        byte[] state = cipher.clone();
        for (int i = 0; i < 16; i++) state[i] ^= key[i];

        for (int round = 0; round < 16; round++) {
            state = inversePermutate(state);
            for (int i = 0; i < 16; i++) state[i] = inverseSubstitute(state[i]);
            for (int i = 0; i < 16; i++) state[i] ^= key[i];
        }
        return state;
    }

    private byte inverseSubstitute(byte b) {
        int val = Byte.toUnsignedInt(b);
        for (Map.Entry<Integer, Integer> entry : S_BOX.entrySet()) {
            if (entry.getValue() == val) return entry.getKey().byteValue();
        }
        throw new IllegalArgumentException("No inverse S-BOX mapping found.");
    }

    private byte[] inversePermutate(byte[] plain) {
        byte[] chunk = plain.clone();
        for (int i = 14; i >= 0; i--) {
            byte[] res = bitUnshake(chunk[i], chunk[i + 1]);
            chunk[i] = res[0];
            chunk[i + 1] = res[1];
        }
        chunk = swapBytes(chunk);
        chunk = inverseShakeAlongRows(chunk);
        chunk = doXor(chunk);
        for (int i = 14; i >= 0; i--) {
            byte[] res = bitUnshake(chunk[i], chunk[i + 1]);
            chunk[i] = res[0];
            chunk[i + 1] = res[1];
        }
        chunk = swapBytes(chunk);
        chunk = inverseShakeAlongColumns(chunk);
        return chunk;
    }

    private byte[] inverseShakeAlongRows(byte[] plain) {
        byte[] res = new byte[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res[i * 4 + ((j + 2) % 4)] = plain[i * 4 + j];
            }
        }
        return res;
    }

    private byte[] inverseShakeAlongColumns(byte[] plain) {
        byte[] res = new byte[16];
        for (int i = 0; i < 16; i++) {
            res[(i + 8) % 16] = plain[i];
        }
        return res;
    }
}
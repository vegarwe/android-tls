package no.raiom.tls;

public class Utils {

    public static String toByteString(byte[] bytes) {
        StringBuilder byteString = new StringBuilder();
        for (byte b : bytes) {
            if (b >= 32 && b <= 127) {
                byteString.append((char) b);
            } else {
                byteString.append(String.format("\\x%02x", b));
            }
        }
        return byteString.toString();
    }

    public static String toByteString(byte[] bytes, int start, int length) {
        StringBuilder byteString = new StringBuilder();
        int maxlength = bytes.length < start + length ? bytes.length : start + length;
        for (int i = start; i < maxlength; i++) {
            if (bytes[i] >= 32 && bytes[i] <= 127) {
                byteString.append((char) bytes[i]);
            } else {
                byteString.append(String.format("\\x%02x", bytes[i]));
            }
        }
        return byteString.toString();
    }
}

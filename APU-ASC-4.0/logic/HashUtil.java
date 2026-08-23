package logic;

public class HashUtil {
    public static String hash(String input) {
        if (input == null) return null;
        return Integer.toHexString(input.trim().hashCode());
    }
}

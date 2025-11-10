package app.query;

public class TariffUtils {
    public static double parseTariffValue(Object value) {
        if (value == null) return Double.NEGATIVE_INFINITY;
        String str = value.toString().replaceAll("[^0-9.]+", "");
        if (str.isEmpty()) return Double.NEGATIVE_INFINITY;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public static int countDots(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '.')
                count++;
        }
        return count;
    }
}

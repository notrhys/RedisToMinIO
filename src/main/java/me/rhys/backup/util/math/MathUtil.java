package me.rhys.backup.util.math;

import java.text.DecimalFormat;

public class MathUtil {
    public static float trimFloat(int degree, float d) {
        String format = "#.#";
        for (int i = 1; i < degree; ++i) {
            format = format + "#";
        }
        DecimalFormat twoDForm = new DecimalFormat(format);
        return Float.parseFloat(twoDForm.format(d).replaceAll(",", "."));
    }
}

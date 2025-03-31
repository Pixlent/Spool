package me.pixlent.demo.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathApi {
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }
}

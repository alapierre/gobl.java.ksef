package io.alapierre.ksef.fa.converter;

import org.junit.jupiter.api.Test;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2024.01.25
 */
public class SimpleTest {

    @Test
    void roundToString() {

        String value = "23.0%";

        System.out.println(value.replace(".0%", ""));

    }

}

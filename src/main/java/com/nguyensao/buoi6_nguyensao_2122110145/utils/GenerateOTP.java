package com.nguyensao.buoi6_nguyensao_2122110145.utils;

import java.security.SecureRandom;

public class GenerateOTP {
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        return String.format("%06d", random.nextInt(1000000));
    }
}

package com.hotelbooking.util;

import java.util.UUID;

public final class Ids {
    private Ids() { }
    public static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

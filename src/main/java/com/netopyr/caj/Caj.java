package com.netopyr.caj;

import java.util.EnumSet;

public class Caj {

    public static Assertion expect(Object object) {
        return new Assertion(object, EnumSet.noneOf(Assertion.Flags.class));
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

}

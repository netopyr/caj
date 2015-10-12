package com.netopyr.caj;

import org.testng.annotations.Test;

import static com.netopyr.caj.Caj.expect;

public class ExampleTests {

    @Test
    public void testsShouldBeSimple() {
        final String yourTest = "simple";

        expect(yourTest).to.be("simple");
    }
}

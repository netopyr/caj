package com.netopyr.caj;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static com.netopyr.caj.Caj.expect;

public class DocumentationTest {

    @Test
    public void testReadme() {
        final String yourTest = "simple";
        expect(yourTest).to.be("simple");

        final int javasAge = 20;
        expect(javasAge).to.be.within(15, 25);

        final Object aSpecificValue = new Object();
        final Object[] collection = new Object[] {aSpecificValue};
        expect(collection).to.include(aSpecificValue);

        final Java java = new Java();
        java.age = 20;
        expect(java).to.have.a.property("age").which.is.at.least(20);

        expect(yourTest).not.to.be("hard");

        expect("Hello").to.have.a.length.of.at.most(5);

        final Tea tea = new Tea();
        expect(tea).to.have.property("extras").which.contains("smile");

        final Runnable badFunction = () -> {throw new Error("testing");};
        expect(badFunction).to.cause(Error.class).with.property("message", "testing");
    }

    private class Java {
        public int age;
    }

    private class Tea {
        public List<String> extras = Collections.singletonList("smile");
    }

}

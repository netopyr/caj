package com.netopyr.caj;

import org.testng.annotations.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.netopyr.caj.Caj.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.fail;

public class AssertionTest {

    private static void expectAssertionError(Runnable runnable, String message) {
        try {
            runnable.run();
        } catch (AssertionError err) {
            assertThat(err.getMessage(), is(message));
            return;
        }
        fail("Should have thrown a java.lang.AssertionError");
    }

    @Test
    public void shouldDoSimpleTypeAssertion() {
        expect("test").to.be.a(String.class);
    }

    @Test
    public void shouldDoSimpleEqualAssertion() {
        expect("foo").to.equal("foo");
    }

    @Test
    public void shouldFail() {
        expectAssertionError(
                () -> Caj.fail("This has failed"),
                "This has failed"
        );
    }

    @Test
    public void shouldExpectTrue() {
        expect(true).to.be(true);
        expect(false).to.not.be(true);
        expect(1).to.not.be(true);
        expectAssertionError(
                () -> expect("test").to.be(true),
                "expected \"test\" to be true"
        );
    }

    @Test
    public void shouldExpectFalse() {
        expect(false).to.be(false);
        expect(true).to.not.be(false);
        expect(0).to.not.be(false);
        expectAssertionError(
                () -> expect("").to.be(false),
                "expected \"\" to be false"
        );
    }

    @Test
    public void shouldExpectNull() {
        expect(null).to.be(null);
        expect(false).to.not.be(null);
        expect(0).to.not.be(false);
        expectAssertionError(
                () -> expect("").to.be(null),
                "expected \"\" to be null"
        );
    }

    @Test
    public void shouldDoTypeAssertion() {
        expect("test").to.be.a(String.class);
        expectAssertionError(
                () -> expect("test").not.to.be.a(String.class),
                "expected \"test\" not to be an instance of class java.lang.String"
        );

        expect(5).to.be.a(Number.class);
        expect(true).to.be.a(Boolean.class);
        expect(new Object[0]).to.be.an(Object[].class);
        expect(new Object()).to.be.an(Object.class);
        expect(null).to.be.a(null);

        expectAssertionError(
                () -> expect(5).not.to.be.a(Number.class, "blah"),
                "blah: expected 5 not to be an instance of class java.lang.Number"
        );

        expect(new Date()).to.be.an.instanceOf(Date.class);
        expectAssertionError(
                () -> expect(3).to.be.an.instanceOf(Date.class),
                "expected 3 to be an instance of class java.util.Date"
        );
    }

    @Test
    public void shouldCheckWithin() {
        expect(5).to.be.within(5, 10);
        expect(5).to.be.within(3, 6);
        expect(5).to.be.within(3, 5);
        expect(5).to.not.be.within(1, 3);
        expect("foo").to.have.length.within(2, 4);
        expect(new Integer[]{1, 2, 3}).to.have.length.within(2, 4);

        expectAssertionError(
                () -> expect(5).to.not.be.within(4, 6, "blah"),
                "blah: expected 5 to not be within 4..6"
        );

        expectAssertionError(
                () -> expect(10).to.be.within(50, 100, "blah"),
                "blah: expected 10 to be within 50..100"
        );

        expectAssertionError(
                () -> expect("foo").to.have.length.within(5, 7, "blah"),
                "blah: expected \"foo\" to have a length within 5..7"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3}).to.have.length.within(5, 7, "blah"),
                "blah: expected [1, 2, 3] to have a length within 5..7"
        );
    }

    @Test
    public void shouldCheckAbove() {
        expect(5).to.be.above(2);
        expect(5).to.be.greaterThan(2);
        expect(5).to.not.be.above(5);
        expect(5).to.not.be.above(6);
        expect("foo").to.have.length.above(2);
        expect(new Integer[]{1, 2, 3}).to.have.length.above(2);

        expectAssertionError(
                () -> expect(5).to.be.above(6, "blah"),
                "blah: expected 5 to be above 6"
        );

        expectAssertionError(
                () -> expect(10).to.not.be.above(6, "blah"),
                "blah: expected 10 to be at most 6"
        );

        expectAssertionError(
                () -> expect("foo").to.have.length.above(4, "blah"),
                "blah: expected \"foo\" to have a length above 4 but got 3"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3}).to.have.length.above(4, "blah"),
                "blah: expected [1, 2, 3] to have a length above 4 but got 3"
        );
    }

    @Test
    public void shouldCheckAtLeast() {
        expect(5).to.be.at.least(2);
        expect(5).to.be.at.least(5);
        expect(5).to.not.be.at.least(6);
        expect("foo").to.have.length.of.at.least(2);
        expect(new Integer[]{1, 2, 3}).to.have.length.of.at.least(2);

        expectAssertionError(
                () -> expect(5).to.be.at.least(6, "blah"),
                "blah: expected 5 to be at least 6"
        );

        expectAssertionError(
                () -> expect(10).to.not.be.at.least(6, "blah"),
                "blah: expected 10 to be below 6"
        );

        expectAssertionError(
                () -> expect("foo").to.have.length.of.at.least(4, "blah"),
                "blah: expected \"foo\" to have a length at least 4 but got 3"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3}).to.have.length.of.at.least(4, "blah"),
                "blah: expected [1, 2, 3] to have a length at least 4 but got 3"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3, 4}).to.not.have.length.of.at.least(4, "blah"),
                "blah: expected [1, 2, 3, 4] to have a length below 4"
        );
    }

    @Test
    public void shouldCheckBelow() {
        expect(2).to.be.below(5);
        expect(2).to.be.lessThan(5);
        expect(2).to.not.be.below(2);
        expect(2).to.not.be.below(1);
        expect("foo").to.have.length.below(4);
        expect(new Integer[]{1, 2, 3}).to.have.length.below(4);

        expectAssertionError(
                () -> expect(6).to.be.below(5, "blah"),
                "blah: expected 6 to be below 5"
        );

        expectAssertionError(
                () -> expect(6).to.not.be.below(10, "blah"),
                "blah: expected 6 to be at least 10"
        );

        expectAssertionError(
                () -> expect("foo").to.have.length.below(2, "blah"),
                "blah: expected \"foo\" to have a length below 2 but got 3"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3}).to.have.length.below(2, "blah"),
                "blah: expected [1, 2, 3] to have a length below 2 but got 3"
        );
    }

    @Test
    public void shouldCheckAtMost() {
        expect(2).to.be.at.most(5);
        expect(2).to.be.at.most(2);
        expect(2).to.not.be.at.most(1);
        expect(2).to.not.be.at.most(1);
        expect("foo").to.have.length.of.at.most(4);
        expect(new Integer[]{1, 2, 3}).to.have.length.of.at.most(4);

        expectAssertionError(
                () -> expect(6).to.be.at.most(5, "blah"),
                "blah: expected 6 to be at most 5"
        );

        expectAssertionError(
                () -> expect(6).to.not.be.at.most(10, "blah"),
                "blah: expected 6 to be above 10"
        );

        expectAssertionError(
                () -> expect("foo").to.have.length.of.at.most(2, "blah"),
                "blah: expected \"foo\" to have a length at most 2 but got 3"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3}).to.have.length.of.at.most(2, "blah"),
                "blah: expected [1, 2, 3] to have a length at most 2 but got 3"
        );

        expectAssertionError(
                () -> expect(new Integer[]{1, 2}).to.not.have.length.of.at.most(2, "blah"),
                "blah: expected [1, 2] to have a length above 2"
        );
    }

    @Test
    public void shouldCheckMatch() {
        expect("foobar").to.match("^foo");
        expect("foobar").to.matches("^foo");
        expect("foobar").to.not.match("^bar");

        expectAssertionError(
                () -> expect("foobar").to.match(Pattern.compile("^bar", Pattern.CASE_INSENSITIVE), "blah"),
                "blah: expected \"foobar\" to match /^bar/i"
        );

        expectAssertionError(
                () -> expect("foobar").to.matches(Pattern.compile("^bar", Pattern.CASE_INSENSITIVE), "blah"),
                "blah: expected \"foobar\" to match /^bar/i"
        );

        expectAssertionError(
                () -> expect("foobar").to.not.match(Pattern.compile("^foo", Pattern.CASE_INSENSITIVE), "blah"),
                "blah: expected \"foobar\" not to match /^foo/i"
        );
    }

    @Test
    public void shouldCheckLength() {
        expect("test").to.have.length(4);
        expect("test").to.not.have.length(3);
        expect(new Integer[]{1, 2, 3}).to.have.length(3);

        expectAssertionError(
                () -> expect(4).to.have.length(3, "blah"),
                "blah: expected 4 to be an array, Collection, Map or String"
        );

        expectAssertionError(
                () -> expect("asd").to.not.have.length(3, "blah"),
                "blah: expected \"asd\" to not have a length of 3"
        );
    }

    @Test
    public void shouldCheckDeepEqual() {
        expect("test").to.eql("test");
        expect(1).to.eql(1);
        expect("4").to.not.eql(4);

        expectAssertionError(
                () -> expect(4).to.eql(3, "blah"),
                "blah: expected 4 to deeply equal 3"
        );
    }

    @Test
    public void shouldCheckEqual() {
        expect("test").to.equal("test");
        expect(1).to.equal(1);

        expectAssertionError(
                () -> expect(4).to.equal(3, "blah"),
                "blah: expected 4 to equal 3"
        );

        expectAssertionError(
                () -> expect("4").to.equal(4, "blah"),
                "blah: expected \"4\" to equal 4"
        );
    }

    @Test
    public void shouldCheckEmpty() {
        expect("").to.be.empty();
        expect("foo").not.to.be.empty();
        expect(new Object[0]).to.be.empty();
        expect(new String[]{"foo"}).not.to.be.empty();
        expect(new HashMap<>()).to.be.empty();

        expectAssertionError(
                () -> expect("").not.to.be.empty(),
                "expected \"\" not to be empty"
        );

        expectAssertionError(
                () -> expect("foo").to.be.empty(),
                "expected \"foo\" to be empty"
        );

        expectAssertionError(
                () -> expect(new Object[0]).not.to.be.empty(),
                "expected [] not to be empty"
        );

        expectAssertionError(
                () -> expect(new String[]{"foo"}).to.be.empty(),
                "expected [\"foo\"] to be empty"
        );

        expectAssertionError(
                () -> expect(new HashMap<>()).not.to.be.empty(),
                "expected {} not to be empty"
        );
    }

    @Test
    public void checkPropertyName() {
        expect("test").to.have.property("length");
        expect(4).to.not.have.property("length");

        expectAssertionError(
                () -> expect("asd").to.have.property("foo"),
                "expected \"asd\" to have a property \"foo\"");

        class Foo {
            public String bar;
        }

        class FooBar {
            public Foo foo = new Foo();

            @Override
            public String toString() {
                return "class FooBar";
            }
        }

        expect(new FooBar()).to.have.property("foo.bar");

        class FooArray {
            public int[] foo = new int[]{1, 2, 3};
        }

        expect(new FooArray()).to.have.property("foo[1]");
    }

    @Test
    public void checkPropertyNameAndValue() {
        expect("test").to.have.property("length", 4);

        class Tea {
            public String tea;

            public Tea(String tea) {
                this.tea = tea;
            }
        }
        class DeepObj {
            public Tea green = new Tea("matcha");
            public Object[] teas = new Object[]{"chai", "matcha", new Tea("konacha")};
        }
        final DeepObj deepObj = new DeepObj();
        expect(deepObj).to.have.property("green.tea", "matcha");
        expect(deepObj).to.have.property("teas[1]", "matcha");
        expect(deepObj).to.have.property("teas[2].tea", "konacha");

        expect(deepObj).to.have.property("teas")
                .that.is.an(Object[].class)
                .with.property("[2]")
                .that.deep.equals(new Tea("konacha"));

        expectAssertionError(
                () -> expect(deepObj).to.have.property("teas[3]"),
                "expected " + deepObj.toString() + " to have a property \"teas[3]\""
        );
        expectAssertionError(
                () -> expect(deepObj).to.have.property("teas[3]", "bar"),
                "expected " + deepObj.toString() + " to have a property \"teas[3]\""
        );
        expectAssertionError(
                () -> expect(deepObj).to.have.property("teas[3].tea", "bar"),
                "expected " + deepObj.toString() + " to have a property \"teas[3].tea\""
        );

        final Object[] arr = new Object[]{
                new String[]{"chai", "matcha", "konacha"},
                new Tea[]{new Tea("chai"), new Tea("matcha"), new Tea("konacha")}
        };
        final String arrToString = "[[\"chai\", \"matcha\", \"konacha\"], " + Arrays.toString((Tea[]) arr[1]) + "]";
        expect(arr).to.have.property("[0][1]", "matcha");
        expect(arr).to.have.property("[1][2].tea", "konacha");
        expectAssertionError(
                () -> expect(arr).to.have.property("[2][1]"),
                "expected " + arrToString + " to have a property \"[2][1]\""
        );
        expectAssertionError(
                () -> expect(arr).to.have.property("[2][1]", "none"),
                "expected " + arrToString + " to have a property \"[2][1]\""
        );
        expectAssertionError(
                () -> expect(arr).to.have.property("[0][3]", "none"),
                "expected " + arrToString + " to have a property \"[0][3]\""
        );

        expectAssertionError(
                () -> expect("asd").to.have.property("length", 4, "blah"),
                "blah: expected \"asd\" to have a property \"length\" of 4, but got 3"
        );

        expectAssertionError(
                () -> expect("asd").to.not.have.property("length", 3, "blah"),
                "blah: expected \"asd\" to not have a property \"length\" of 3"
        );

        expectAssertionError(
                () -> expect("asd").to.not.have.property("foo", 3, "blah"),
                "blah: \"asd\" has no property \"foo\""
        );
    }

    @Test
    public void checkDeepPropertyNameAndValue() {
        class Foo {
            public String bar = "baz";
        }

        class FooBar {
            public Foo foo = new Foo();

            @Override
            public String toString() {
                return "class FooBar";
            }
        }
        final FooBar fooBar = new FooBar();

        expect(fooBar).to.have.property("foo.bar", "baz");

        expectAssertionError(
                () -> expect(fooBar).to.have.property("foo.bar", "quux", "blah"),
                "blah: expected " + fooBar + " to have a property \"foo.bar\" of \"quux\", but got \"baz\""
        );

        expectAssertionError(
                () -> expect(fooBar).to.not.have.property("foo.bar", "baz", "blah"),
                "blah: expected " + fooBar + " to not have a property \"foo.bar\" of \"baz\""
        );

        class Foo5 {
            public int foo = 5;

            @Override
            public String toString() {
                return "class Foo5";
            }
        }
        final Foo5 foo5 = new Foo5();

        expectAssertionError(
                () -> expect(foo5).to.not.have.property("foo.bar", "baz", "blah"),
                "blah: " + foo5 + " has no property \"foo.bar\""
        );
    }

    @Test
    public void checkString() {
        expect("foobar").to.have.string("bar");
        expect("foobar").to.have.string("foo");
        expect("foobar").to.not.have.string("baz");

        expectAssertionError(
                () -> expect(3).to.have.string("baz"),
                "expected 3 to be an instance of class java.lang.String"
        );

        expectAssertionError(
                () -> expect("foobar").to.have.string("baz", "blah"),
                "blah: expected \"foobar\" to contain \"baz\""
        );

        expectAssertionError(
                () -> expect("foobar").to.not.have.string("bar", "blah"),
                "blah: expected \"foobar\" to not contain \"bar\""
        );
    }

    @Test
    public void checkInclude() {
        expect(new String[]{"foo", "bar"}).to.include("foo");
        expect(new String[]{"foo", "bar"}).to.include("foo");
        expect(new String[]{"foo", "bar"}).to.include("bar");
        expect(new int[]{1, 2}).to.include(1);
        expect(new String[]{"foo", "bar"}).to.not.include("baz");
        expect(new String[]{"foo", "bar"}).to.not.include(1);

        final Map<String, Integer> a1 = new HashMap<>();
        a1.put("a", 1);
        final Map<String, Integer> b1 = new HashMap<>();
        b1.put("b", 1);
        final Map<String, Integer> b2 = new HashMap<>();
        b2.put("b", 2);
        final Map<String, Integer> b3 = new HashMap<>();
        b3.put("b", 3);
        final Map<String, Integer> a1b2 = new HashMap<>(a1);
        a1b2.put("b", 2);
        final Map<String, Integer> a1c2 = new HashMap<>(a1);
        a1c2.put("c", 2);
        
        expect(a1b2).to.include(a1);
        expect(a1b2).to.not.include(b3);
        expect(a1b2).to.include(a1b2);
        expect(a1b2).to.not.include(a1c2);

        expect(new Map[]{a1, b2}).to.include(a1);
        expect(new Map[]{a1}).to.include(a1);
        expect(new Map[]{a1}).to.not.include(b1);

        expectAssertionError(
                () -> expect(new String[]{"foo"}).to.include("bar", "blah"),
                "blah: expected [\"foo\"] to include \"bar\""
        );

        expectAssertionError(
                () -> expect(new String[]{"bar", "foo"}).to.not.include("foo", "blah"),
                "blah: expected [\"bar\", \"foo\"] to not include \"foo\""
        );

        expectAssertionError(
                () -> expect(a1).to.include(b2),
                "expected {a=1} to include {b=2}"
        );

        expectAssertionError(
                () -> expect(a1b2).to.not.include(b2),
                "expected {a=1, b=2} to not include {b=2}"
        );

        expectAssertionError(
                () -> expect(new Map[]{a1, b2}).to.not.include(b2),
                "expected [{a=1}, {b=2}] to not include {b=2}"
        );
    }

    @Test
    public void checkKeys() {
        final Map<String, Integer> foo = new HashMap<>();
        foo.put("foo", 1);
        final Map<String, Integer> fooBar = new LinkedHashMap<>();
        fooBar.put("foo", 1);
        fooBar.put("bar", 2);
        final Map<String, Integer> fooBarBaz = new LinkedHashMap<>();
        fooBarBaz.put("foo", 1);
        fooBarBaz.put("bar", 2);
        fooBarBaz.put("baz", 3);
        final HashMap<String, Integer> foo6 = new HashMap<>();
        foo6.put("foo", 6);
        final HashMap<String, Integer> bar7 = new HashMap<>();
        bar7.put("bar", 7);
        final HashMap<String, Integer> foo6bar7 = new LinkedHashMap<>();
        foo6bar7.put("foo", 6);
        foo6bar7.put("bar", 7);
        final HashMap<String, Integer> foo7baz8 = new LinkedHashMap<>();
        foo7baz8.put("foo", 7);
        foo7baz8.put("baz", 8);

        expect(foo).to.have.keys("foo");
        expect(foo).to.have.keys(Collections.singleton("foo"));
        expect(foo).have.keys(foo6);
        expect(fooBar).to.have.keys("foo", "bar");
        expect(fooBar).to.have.keys(Arrays.asList("foo", "bar"));
        expect(fooBar).have.keys(foo6bar7);
        expect(fooBarBaz).to.contain.keys("foo", "bar");
        expect(fooBarBaz).to.contain.keys("bar", "foo");
        expect(fooBarBaz).to.contain.keys("baz");
        expect(fooBar).contain.keys(foo6);
        expect(fooBar).contain.keys(bar7);
        expect(fooBar).contain.keys(foo6);

        expect(fooBar).to.contain.keys("foo");
        expect(fooBar).to.contain.keys("bar");
        expect(fooBar).to.contain.keys("bar", "foo");
        expect(fooBar).to.contain.keys(Collections.singleton("foo"));
        expect(fooBar).to.contain.keys(Collections.singleton("bar"));
        expect(fooBar).to.contain.keys(Arrays.asList("bar", "foo"));
        expect(fooBarBaz).to.contain.all.keys("bar", "foo");

        expect(fooBar).to.not.have.keys("baz");
        expect(fooBar).to.not.have.keys("foo", "baz");
        expect(fooBar).to.not.contain.keys("baz");
        expect(fooBar).to.not.contain.keys("foo", "baz");
        expect(fooBar).to.not.contain.keys("baz", "foo");

        expect(fooBar).to.have.any.keys("foo", "baz");
        expect(fooBar).to.have.any.keys("foo");
        expect(fooBar).to.contain.any.keys("bar", "baz");
        expect(fooBar).to.contain.any.keys("foo");
        expect(fooBar).to.have.all.keys("bar", "foo");
        expect(fooBar).to.contain.all.keys("bar", "foo");
        expect(fooBar).contain.any.keys(foo6);
        expect(fooBar).have.all.keys(foo6bar7);
        expect(fooBar).contain.all.keys(foo6bar7);

        expect(fooBar).to.not.have.any.keys("baz", "abc", "def");
        expect(fooBar).to.not.have.any.keys("baz");
        expect(fooBar).to.not.contain.any.keys("baz");
        expect(fooBar).to.not.have.all.keys("baz", "foo");
        expect(fooBar).to.not.contain.all.keys("baz", "foo");
        expect(fooBar).not.have.all.keys(foo7baz8);
        expect(fooBar).not.contain.all.keys(foo7baz8);

        try {
            expect(foo).to.have.keys();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            expect(foo).to.have.keys((String[])null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        expectAssertionError(
                () -> expect(foo).to.have.keys("bar"),
                "expected {foo=1} to have key \"bar\""
        );

        expectAssertionError(
                () -> expect(foo).to.have.keys("bar", "baz"),
                "expected {foo=1} to have keys \"bar\", and \"baz\""
        );

        expectAssertionError(
                () -> expect(foo).to.have.keys("foo", "bar", "baz"),
                "expected {foo=1} to have keys \"foo\", \"bar\", and \"baz\""
        );

        expectAssertionError(
                () -> expect(foo).to.not.have.keys("foo"),
                "expected {foo=1} to not have key \"foo\""
        );

        expectAssertionError(
                () -> expect(fooBar).to.not.have.keys("foo", "bar"),
                "expected {foo=1, bar=2} to not have keys \"foo\", and \"bar\""
        );

        expectAssertionError(
                () -> expect(fooBar).to.have.all.keys("foo"),
                "expected {foo=1, bar=2} to have key \"foo\""
        );

        expectAssertionError(
                () -> expect(foo).to.not.contain.keys("foo"),
                "expected {foo=1} to not contain key \"foo\""
        );

        expectAssertionError(
                () -> expect(foo).to.contain.keys("foo", "bar"),
                "expected {foo=1} to contain keys \"foo\", and \"bar\""
        );

        expectAssertionError(
                () -> expect(foo).to.have.any.keys("baz"),
                "expected {foo=1} to have key \"baz\"");

        expectAssertionError(
                () -> expect(fooBar).to.not.have.all.keys("foo", "bar"),
                "expected {foo=1, bar=2} to not have keys \"foo\", and \"bar\"");

        expectAssertionError(
                () -> expect(fooBar).to.not.have.any.keys("foo", "baz"),
                "expected {foo=1, bar=2} to not have keys \"foo\", or \"baz\""
        );
    }
    
    @Test
    public void checkChaining() {
        class Tea {
            public String name = "chai";
            public String[] extras = new String[] {"milk", "sugar", "smile"};
        }
        final Tea tea = new Tea();
        
        expect(tea).to.have.property("extras").with.lengthOf(3);
        
        expect(tea).to.have.property("extras").which.contains("smile");

        expectAssertionError(
                () -> expect(tea).to.have.property("extras").with.lengthOf(4),
                "expected [\"milk\", \"sugar\", \"smile\"] to have a length of 4 but got 3"
        );

        expect(tea).to.be.a(Tea.class).and.have.property("name", "chai");

        final Runnable badFn = () -> { throw new Error("testing"); };

        expect(badFn).to.cause(Error.class).with.property("message", "testing");
    }
    
    @Test
    public void checkCauses() {
        class CustomError extends Error {
            CustomError(String message) {
                super(message);
            }
        }
        
        final Error specificError = new Error("boo");

        final Runnable goodFn = new Runnable() {
            @Override public void run() {}
            @Override public String toString() { return "[Function: goodFn]"; }
        };
        final Runnable badFn = new Runnable() {
            @Override public void run() { throw new Error("testing"); }
            @Override public String toString() { return "[Function: badFn]"; }
        };
        final Runnable npeFn = new Runnable() {
            @Override public void run() { throw new NullPointerException("hello"); }
            @Override public String toString() { return "[Function: npeFn]"; }
        };
        final Runnable specificErrFn = new Runnable() {
            @Override public void run() { throw specificError; }
            @Override public String toString() { return "[Function: specificErrFn]"; }
        };
        final Runnable customErrFn = new Runnable() {
            @Override public void run() { throw new CustomError("foo"); }
            @Override public String toString() { return "[Function: customErrFn]"; }
        };

        expect(goodFn).to.not.cause();
        expect(goodFn).to.not.cause(Error.class);
        expect(goodFn).to.not.cause(specificError);
        expect(badFn).to.cause();
        expect(badFn).to.cause(Error.class);
        expect(badFn).to.not.cause(NullPointerException.class);
        expect(badFn).to.not.cause(specificError);
        expect(npeFn).to.cause();
        expect(npeFn).to.cause(NullPointerException.class);
        expect(npeFn).to.cause(Exception.class);
        expect(npeFn).to.not.cause(IllegalArgumentException.class);
        expect(npeFn).to.not.cause(specificError);
        expect(specificErrFn).to.cause(specificError);

        expect(badFn).to.cause(Pattern.compile("testing"));
        expect(badFn).to.not.cause(Pattern.compile("hello"));
        expect(badFn).to.cause("testing");
        expect(badFn).to.not.cause("hello");

        expect(badFn).to.cause(Error.class, Pattern.compile("testing"));
        expect(badFn).to.cause(Error.class, "testing");

        expectAssertionError(
                expect(goodFn).to::cause,
                "expected " + goodFn + " to cause an Error or Exception"
        );

        expectAssertionError(
                () -> expect(goodFn).to.cause(NullPointerException.class),
                "expected " + goodFn + " to cause NullPointerException"
        );

        expectAssertionError(
                () -> expect(goodFn).to.cause(specificError),
                "expected " + goodFn + " to cause " + specificError
        );

        expectAssertionError(
                expect(badFn).to.not::cause,
                "expected " + badFn + " to not cause an Error or Exception but " + new Error("testing") + " was thrown"
        );

        expectAssertionError(
                () -> expect(badFn).to.cause(NullPointerException.class),
                "expected " + badFn + " to cause " + NullPointerException.class + " but " + new Error("testing") + " was thrown");

        expectAssertionError(
                () -> expect(badFn).to.cause(specificError),
                "expected " + badFn + " to cause " + specificError + " but " + new Error("testing") + " was thrown"
        );

        expectAssertionError(
                () -> expect(badFn).to.not.cause(Error.class),
                "expected " + badFn + " to not cause " + Error.class + " but " + new Error("testing") + " was thrown"
        );

        expectAssertionError(
                () -> expect(npeFn).to.not.cause(NullPointerException.class),
                "expected " + npeFn + " to not cause " + NullPointerException.class + " but " + new NullPointerException("hello") + " was thrown"
        );

        expectAssertionError(
                () -> expect(specificErrFn).to.cause(new NullPointerException("eek")),
                "expected " + specificErrFn + " to cause " + new NullPointerException("eek") + " but " + specificError + " was thrown");

        expectAssertionError(
                () -> expect(specificErrFn).to.not.cause(specificError),
                "expected " + specificErrFn + " to not cause " + specificError
        );

        expectAssertionError(
                () -> expect(badFn).to.not.cause(Pattern.compile("testing")),
                "expected " + badFn + " to cause an Error or Exception not matching /testing/"
        );

        expectAssertionError(
                () -> expect(badFn).to.cause(Pattern.compile("hello")),
                "expected " + badFn + " to cause an Error or Exception matching /hello/ but got \"testing\"");

        expectAssertionError(
                () -> expect(badFn).to.cause(Error.class, Pattern.compile("hello"), "blah"),
                "blah: expected " + badFn + " to cause an Error or Exception matching /hello/ but got \"testing\""
        );

        expectAssertionError(
                () -> expect(badFn).to.cause(Error.class, "hello", "blah"),
                "blah: expected " + badFn + " to cause an Error or Exception including \"hello\" but got \"testing\""
        );

        expectAssertionError(
                expect(customErrFn).to.not::cause,
                "expected " + customErrFn + " to not cause an Error or Exception but " + new CustomError("foo") + " was thrown"
        );
    }

    @Test
    public void checkSatisfy() {
        final Predicate<Object> predicate = new Predicate<Object>() {
            @Override
            public boolean test(Object num) {
                return Integer.valueOf(1).equals(num);
            }

            @Override
            public String toString() {
                return "Test Predicate";
            }
        };

        expect(1).to.satisfy(predicate);

        expectAssertionError(
                () -> expect(2).to.satisfy(predicate, "blah"),
                "blah: expected 2 to satisfy Test Predicate"
        );
    }
    
    @Test
    public void checkCloseTo() {
        expect(1.5).to.be.closeTo(1.0, 0.5);
        expect(10).to.be.closeTo(20, 20);
        expect(-10).to.be.closeTo(20, 30);

        expectAssertionError(
                () -> expect(2).to.be.closeTo(1.0, 0.5, "blah"),
                "blah: expected 2 to be close to 1.0 +/- 0.5"
        );

        expectAssertionError(
                () -> expect(-10).to.be.closeTo(20, 29, "blah"),
                "blah: expected -10 to be close to 20 +/- 29"
        );

        expectAssertionError(
                () -> expect(new Double[] {1.5}).to.be.closeTo(1.0, 0.5),
                "expected [1.5] to be an instance of class java.lang.Number"
        );
    }

    @Test
    public void checkIncludeMembers() {
        expect(new Integer[] {1, 2, 3}).to.include.members(new Integer[0]);
        expect(new Integer[] {1, 2, 3}).to.include.members(new Integer[] {3, 2});
        expect(new Integer[] {1, 2, 3}).to.not.include.members(new Integer[]{8, 4});
        expect(new Integer[] {1, 2, 3}).to.not.include.members(new Integer[]{1, 2, 3, 4});

        expectAssertionError(
                () -> expect(new Integer[]{1, 2, 3}).to.not.include.members(new Integer[0]),
                "expected [1, 2, 3] to not be a superset of []"
        );
        expectAssertionError(
                () -> expect(new Integer[] {1, 2, 3}).to.not.include.members(new Integer[]{3, 2}),
                "expected [1, 2, 3] to not be a superset of [3, 2]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {1, 2, 3}).to.include.members(new Integer[]{8, 4}),
                "expected [1, 2, 3] to be a superset of [8, 4]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {1, 2, 3}).to.include.members(new Integer[]{1, 2, 3, 4}),
                "expected [1, 2, 3] to be a superset of [1, 2, 3, 4]"
        );
    }

    @Test
    public void checkSameMembers() {
        expect(new Integer[] {5, 4}).to.have.same.members(new Integer[] {4, 5});
        expect(new Integer[] {5, 4}).to.have.same.members(new Integer[] {5, 4});
        expect(new Integer[] {5, 4}).to.not.have.same.members(new Integer[0]);
        expect(new Integer[] {5, 4}).to.not.have.same.members(new Integer[]{6, 5});
        expect(new Integer[] {5, 4}).to.not.have.same.members(new Integer[]{5, 4, 2});

        expectAssertionError(
                () -> expect(new Integer[]{5, 4}).to.not.have.same.members(new Integer[]{4, 5}),
                "expected [5, 4] to not have the same members as [4, 5]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.not.have.same.members(new Integer[] {5, 4}),
                "expected [5, 4] to not have the same members as [5, 4]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.have.same.members(new Integer[0]),
                "expected [5, 4] to have the same members as []"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.have.same.members(new Integer[]{6, 5}),
                "expected [5, 4] to have the same members as [6, 5]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.have.same.members(new Integer[]{5, 4, 2}),
                "expected [5, 4] to have the same members as [5, 4, 2]"
        );
    }

    @Test
    public void checkMembers() {
        expect(new Integer[] {5, 4}).to.have.members(new Integer[] {4, 5});
        expect(new Integer[] {5, 4}).to.have.members(new Integer[] {5, 4});
        expect(new Integer[] {5, 4}).to.not.have.members(new Integer[0]);
        expect(new Integer[] {5, 4}).to.not.have.members(new Integer[]{6, 5});
        expect(new Integer[] {5, 4}).to.not.have.members(new Integer[]{5, 4, 2});

        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.not.have.members(new Integer[] {4, 5}),
                "expected [5, 4] to not have the same members as [4, 5]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.not.have.members(new Integer[] {5, 4}),
                "expected [5, 4] to not have the same members as [5, 4]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.have.members(new Integer[0]),
                "expected [5, 4] to have the same members as []"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.have.members(new Integer[]{6, 5}),
                "expected [5, 4] to have the same members as [6, 5]"
        );
        expectAssertionError(
                () -> expect(new Integer[] {5, 4}).to.have.members(new Integer[]{5, 4, 2}),
                "expected [5, 4] to have the same members as [5, 4, 2]"
        );
    }

    @Test
    public void checkChange() {
        class Obj {
            public int value = 10;
            public String str = "foo";
            @Override
            public String toString() {
                return "obj";
            }
        }
        final Obj obj = new Obj();
        final Runnable fn = () -> obj.value += 5;
        final Runnable sameFn = () -> {};
        final Runnable bangFn = () -> obj.str += "!";

        expect(fn).to.change(obj, "value");
        expect(sameFn).to.not.change(obj, "value");
        expect(sameFn).to.not.change(obj, "str");
        expect(bangFn).to.change(obj, "str");

        expectAssertionError(
                () -> expect(fn).to.not.change(obj, "value"),
                "expected .value to not change"
        );
        expectAssertionError(
                () -> expect(sameFn).to.change(obj, "value"),
                "expected .value to change"
        );
        expectAssertionError(
                () -> expect(sameFn).to.change(obj, "str"),
                "expected .str to change"
        );
        expectAssertionError(
                () -> expect(bangFn).to.not.change(obj, "str"),
                "expected .str to not change"
        );
    }
    
    @Test
    public void checkIncreaseDecrease() {
        class Obj {
            public int value = 10;
        }
        final Obj obj = new Obj();
        final Runnable incFn = () -> obj.value += 2;
        final Runnable decFn = () -> obj.value -= 3;
        final Runnable smFn = () -> obj.value += 0;

        expect(smFn).to.not.increase(obj, "value");
        expect(decFn).to.not.increase(obj, "value");
        expect(incFn).to.increase(obj, "value");

        expectAssertionError(
                () -> expect(smFn).to.increase(obj, "value"),
                "expected .value to increase"
        );
        expectAssertionError(
                () -> expect(decFn).to.increase(obj, "value"),
                "expected .value to increase"
        );
        expectAssertionError(
                () -> expect(incFn).to.not.increase(obj, "value"),
                "expected .value to not increase"
        );

        expect(smFn).to.not.decrease(obj, "value");
        expect(incFn).to.not.decrease(obj, "value");
        expect(decFn).to.decrease(obj, "value");

        expectAssertionError(
                () -> expect(smFn).to.decrease(obj, "value"),
                "expected .value to decrease"
        );
        expectAssertionError(
                () -> expect(incFn).to.decrease(obj, "value"),
                "expected .value to decrease"
        );
        expectAssertionError(
                () -> expect(decFn).to.not.decrease(obj, "value"),
                "expected .value to not decrease"
        );
    }
}

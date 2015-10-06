package com.netopyr.caj;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assertion {

    private static final Pattern ARRAY_PATH_ELEMENT = Pattern.compile("^(.*)\\[(.*)]$");
    private static final Pattern ARRAY_INDEX = Pattern.compile("\\[(.*?)]");

    private final Object object;
    private final EnumSet<Flags> flags;
    enum Flags {NOT, DEEP, ANY, CONTAINS, DO_LENGTH}

    private String prefix;
    private Object actual;
    private Object expected;

    private Callable<Object> lateObjectBinding = null;


    Assertion(Object object, EnumSet<Flags> flags) {
        this.object = object;
        this.flags = flags;

        final EnumSet<Flags> notFlags = flags.clone();
        notFlags.add(Flags.NOT);
        not = flags.contains(Flags.NOT)? this : new Assertion(object, notFlags);

        final EnumSet<Flags> deepFlags = flags.clone();
        deepFlags.add(Flags.DEEP);
        deep = flags.contains(Flags.DEEP)? this : new Assertion(object, deepFlags);

        final EnumSet<Flags> anyFlags = flags.clone();
        anyFlags.add(Flags.ANY);
        any = flags.contains(Flags.ANY)? this : new Assertion(object, anyFlags);

        final EnumSet<Flags> containsFlags = flags.clone();
        containsFlags.add(Flags.CONTAINS);
        contains = flags.contains(Flags.CONTAINS)? this : new Assertion(object, containsFlags);
        include = contains;
        includes = contains;
        contain = contains;

        final EnumSet<Flags> doLengthFlags = flags.clone();
        doLengthFlags.add(Flags.DO_LENGTH);
        length = flags.contains(Flags.DO_LENGTH)? this : new Assertion(object, doLengthFlags);
        size = length;
    }



    private boolean getNot() {
        return flags.contains(Flags.NOT);
    }
    private boolean getDeep() {
        return flags.contains(Flags.DEEP);
    }
    private boolean getAny() {
        return flags.contains(Flags.ANY);
    }
    private boolean getContains() { return flags.contains(Flags.CONTAINS); }
    private boolean getDoLength() { return flags.contains(Flags.DO_LENGTH); }


    /**
     * The field {@code to} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion to    = this;

    /**
     * The field {@code be} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion be    = this;

    /**
     * The field {@code been} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion been  = this;

    /**
     * The field {@code is} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion is    = this;

    /**
     * The field {@code and} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion and   = this;

    /**
     * The field {@code has} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion has   = this;

    /**
     * The field {@code have} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion have  = this;

    /**
     * The field {@code with} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion with  = this;

    /**
     * The field {@code that} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion that  = this;

    /**
     * The field {@code which} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion which = this;

    /**
     * The field {@code at} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion at    = this;

    /**
     * The field {@code of} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion of    = this;

    /**
     * The field {@code same} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion same  = this;

    /**
     * The field {@code a} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion a     = this;

    /**
     * The field {@code an} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion an    = this;

    /**
     * The field {@code all} is provided as a chainable element to improve the readability of your assertion.
     * It does not provide any testing capabilities.
     */
    public final Assertion all   = this;

    /**
     * Negates any of assertions following in the chain.
     *
     * <pre>
     *     expect(foo).to.not.equal("bar");
     *     expect(goodFn).to.not.throw(Error.class);
     *     expect(obj).to.have.property("foo")
     *       .and.not.equal("bar");
     * </pre>
     */
    public final Assertion not;

    /**
     * Sets the `deep` flag, later used by the {@link #equal(Object)} and
     * {@link #property(String)} assertions.
     *
     * <pre>
     *     expect(foo).to.deep.equal(bar);
     *     expect(obj)
     *       .to.have.deep.property("foo.bar.baz", "quux");
     * </pre>
     *
     * WARNING: There is a good chance that this element will be removed in the near future!
     */
    public final Assertion deep;

    /**
     * Sets the `any` flag later used in the {@link #keys(Collection)} assertion.
     *
     * <pre>
     *     expect(foo).to.have.any.keys("bar", "baz");
     * </pre>
     */
    public final Assertion any;

    /**
     * Sets the 'include' flag later used in the {@link #keys(Collection)} assertion.
     *
     * <pre>
     *     expect(map).to.include.keys("foo");
     * </pre>
     */
    public final Assertion include;

    /** An alias of {@link #include} */
    public final Assertion contain;

    /** An alias of {@link #include} */
    public final Assertion includes;

    /** An alias of {@link #include} */
    public final Assertion contains;

    /**
     * Sets the `doLength` flag later used as a chain precursor to a value
     * comparison for the `length` property.
     *
     * <pre>
     *     expect("foo").to.have.length.above(2);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.above(2);
     *     expect("foo").to.have.length.below(4);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.below(4);
     *     expect("foo").to.have.length.within(2,4);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.within(2,4);
     * </pre>
     */
    public final Assertion length;

    /** An alias of {@link #length} */
    public final Assertion size;



    /**
     * The method {@code instanceOf} asserts the class of a value.
     *
     * <pre>
     *     expect(foo).to.be.an.instanceOf(Foo.class)
     *     expect("test").to.be.an.instanceOf(String.class)
     * </pre>
     *
     * @param clazz The class to check the value against.
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     * @return The root of a new Assertion chain on the value.
     */
    public Assertion instanceOf(Class<?> clazz, String prefix) {
        this.prefix = prefix;
        if (clazz == null) {
            assertEqual(null, prefix);
        } else {
            doAssert(
                    () -> clazz.isInstance(object),
                    "expected #{this} to be an instance of " + clazz,
                    "expected #{this} not to be an instance of " + clazz
            );
        }
        return Caj.expect(object);
    }

    /**
     * Same as {@link #instanceOf(Class, String)} with no prefix set.
     *
     * @param clazz The class to check the value against.
     * @return The root of a new Assertion chain on the value.
     */
    public Assertion instanceOf(Class<?> clazz) {
        return instanceOf(clazz, null);
    }

    /** An alias of {@link #instanceOf(Class)} */
    public Assertion a(Class<?> clazz) {
        return instanceOf(clazz);
    }
    /** An alias of {@link #instanceOf(Class, String)} */
    public Assertion a(Class<?> clazz, String prefix) {
        return instanceOf(clazz, prefix);
    }
    /** An alias of {@link #instanceOf(Class)} */
    public Assertion an(Class<?> clazz) {
        return instanceOf(clazz);
    }
    /** An alias of {@link #instanceOf(Class, String)} */
    public Assertion an(Class<?> clazz, String prefix) {
        return instanceOf(clazz, prefix);
    }



    private void doEqual(Object expected, String prefix, String op) {
        this.prefix = prefix;
        if (getDeep()) {
            eql(expected, prefix);
        } else {
            assertEqual(expected, prefix, op);
        }
    }

    /**
     * Asserts that the target is equal to the given value.
     *
     * <pre>
     *     expect('hello').to.equal('hello');
     *     expect(42).to.equal(42);
     *     expect(1).to.not.equal(true);
     *     expect({ foo: 'bar' }).to.not.equal({ foo: 'bar' });
     *     expect({ foo: 'bar' }).to.deep.equal({ foo: 'bar' });
     * </pre>
     *
     * @param expected the expected value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void equal(Object expected, String prefix) {
        doEqual(expected, prefix, null);
    }
    /**
     * Same as {@link #equal(Object, String)} with no prefix set.
     *
     * @param expected the expected value the target should be compared with
     */
    public void equal(Object expected) {
        doEqual(expected, null, null);
    }
    /** An alias of {@link #equal(Object, String)} */
    public void eq(Object expected, String prefix) {
        doEqual(expected, prefix, null);
    }
    /** An alias of {@link #equal(Object)} */
    public void eq(Object expected) {
        doEqual(expected, null, null);
    }
    /** An alias of {@link #equal(Object, String)} */
    public void be(Object expected, String prefix) {
        doEqual(expected, prefix, "be");
    }
    /** An alias of {@link #equal(Object)} */
    public void be(Object expected) {
        doEqual(expected, null, "be");
    }



    public void eql(Object expected, String prefix) {
        this.prefix = prefix;
        if ((object instanceof Collection && expected instanceof Collection)
                || (object instanceof Map && expected instanceof Map)) {
            doAssert(
                    () -> object.equals(expected),
                    "expected #{this} to not deeply equal #{exp}",
                    "expected #{this} to not deeply equal #{exp}"
            );
        } else if (object.getClass().isArray() && expected.getClass().isArray()){
            final int n = getLength();
            doAssert(
                    () -> n == Array.getLength(expected),
                    "expected #{this} to deeply equal #{exp}",
                    "expected #{this} to not deeply equal #{exp}"
            );
            for (int i = 0; i < n; i++) {
                final Object elem1 = Array.get(object, i);
                final Object elem2 = Array.get(expected, i);
                doAssert(
                        () -> elem1 == null? elem2 == null : elem1.equals(elem2),
                        "expected #{this} to deeply equal #{exp}",
                        "expected #{this} to not deeply equal #{exp}"
                );
            }
        } else {
            doEqual(expected, prefix, "deeply equal");
        }
    }
    public void eql(Object expected) {
        eql(expected, null);
    }
    public void eqls(Object expected, String prefix) {
        eql(expected, prefix);
    }
    public void eqls(Object expected) {
        eql(expected, null);
    }



    private void assertLengthWithin(int start, int finish) {
        final int length = getLength();
        final String range = start + ".." + finish;
        doAssert(
                () -> start <= length && length <= finish,
                "expected #{this} to have a length within " + range,
                "expected #{this} to not have a length within " + range
        );
    }

    /**
     * Asserts that the target is within a range.
     *
     * <pre>
     *     expect(7).to.be.within(5, 10);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a length range. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.within(2, 4);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.within(2, 4);
     * </pre>
     *
     * @param start the lower bound (inclusive)
     * @param finish the upper bound (inclusive)
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void within(double start, double finish, String prefix) {
        this.prefix = prefix;
        final String range = start + ".." + finish;
        if (getDoLength()) {
            assertLengthWithin((int) start, (int) finish);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> start <= number.doubleValue() && number.doubleValue() <= finish,
                    "expected #{this} to be within " + range,
                    "expected #{this} to not be within " + range
            );
        }
    }

    /**
     * Same as {@link #within(double, double, String)} with no prefix set.
     *
     * @param start the lower bound (inclusive)
     * @param finish the upper bound (inclusive)
     */
    public void within(double start, double finish) {
        within(start, finish, null);
    }

    /**
     * Asserts that the target is within a range.
     *
     * <pre>
     *     expect(7L).to.be.within(5L, 10L);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a length range. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.within(2L, 4L);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.within(2L, 4L);
     * </pre>
     *
     * @param start the lower bound (inclusive)
     * @param finish the upper bound (inclusive)
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void within(long start, long finish, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthWithin((int)start, (int)finish);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            final String range = start + ".." + finish;
            doAssert(
                    () -> start <= number.longValue() && number.longValue() <= finish,
                    "expected #{this} to be within " + range,
                    "expected #{this} to not be within " + range
            );
        }
    }

    /**
     * Same as {@link #within(long, long, String)} with no prefix set.
     *
     * @param start the lower bound (inclusive)
     * @param finish the upper bound (inclusive)
     */
    public void within(long start, long finish) {
        within(start, finish, null);
    }


    /**
     * Asserts that the target is greater than `value`.
     *
     * <pre>
     *     expect(10).to.be.above(5);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a minimum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.above(2);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.above(2);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void above(double n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthAbove((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.doubleValue() > n,
                    "expected #{this} to be above " + n,
                    "expected #{this} to be at most " + n
            );
        }
    }
    /** Alias of {@link #above(double, String)} */
    public void greaterThan(double n, String prefix) {
        above(n, prefix);
    }
    /** Alias of {@link #above(double, String)} */
    public void gt(double n, String prefix) {
        above(n, prefix);
    }

    /**
     * Same as {@link #above(double, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void above(double n) {
        above(n, null);
    }
    /** Alias of {@link #above(double)} */
    public void greaterThan(double n) {
        above(n);
    }
    /** Alias of {@link #above(double)} */
    public void gt(double n) {
        above(n);
    }

    /**
     * Asserts that the target is greater than `value`.
     *
     * <pre>
     *     expect(10L).to.be.above(5L);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a minimum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.above(2L);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.above(2L);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void above(long n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthAbove((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.longValue() > n,
                    "expected #{this} to be above " + n,
                    "expected #{this} to be at most " + n
            );
        }
    }
    /** Alias of {@link #above(long, String)} */
    public void greaterThan(long n, String prefix) {
        above(n, prefix);
    }
    /** Alias of {@link #above(long, String)} */
    public void gt(long n, String prefix) {
        above(n, prefix);
    }

    /**
     * Same as {@link #above(long, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void above(long n) {
        above(n, null);
    }
    /** Alias of {@link #above(long)} */
    public void greaterThan(long n) {
        above(n);
    }
    /** Alias of {@link #above(long)} */
    public void gt(long n) {
        above(n);
    }

    private void assertLengthAbove(int n) {
        final int length = getLength();
        doAssert(
                () -> length > n,
                "expected #{this} to have a length above " + n + " but got " + length,
                "expected #{this} to not have a length above" + n
        );
    }



    /**
     * Asserts that the target is greater than or equal to `value`.
     *
     * <pre>
     *     expect(10).to.be.at.least(10);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a minimum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.of.at.least(2);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.of.at.least(3);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void least(double n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthAtLeast((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.doubleValue() >= n,
                    "expected #{this} to be at least " + n,
                    "expected #{this} to be below " + n
            );
        }
    }
    /** Alias of {@link #least(double, String)} */
    public void gte(double n, String prefix) {
        least(n, prefix);
    }

    /**
     * Same as {@link #least(double, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void least(double n) {
        least(n, null);
    }
    /** Alias of {@link #least(double)} */
    public void gte(double n) {
        least(n);
    }

    /**
     * Asserts that the target is greater than or equal to `value`.
     *
     * <pre>
     *     expect(10L).to.be.at.least(10L);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a minimum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.of.at.least(2L);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.of.at.least(3L);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void least(long n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthAtLeast((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.longValue() >= n,
                    "expected #{this} to be at least " + n,
                    "expected #{this} to be below " + n
            );
        }
    }
    /** Alias of {@link #least(long, String)} */
    public void gte(long n, String prefix) {
        least(n, prefix);
    }

    /**
     * Same as {@link #least(long, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void least(long n) {
        least(n, null);
    }
    /** Alias of {@link #least(long)} */
    public void gte(long n) {
        least(n);
    }

    private void assertLengthAtLeast(int n) {
        final int length = getLength();
        doAssert(
                () -> length >= n,
                "expected #{this} to have a length at least " + n + " but got " + length,
                "expected #{this} to have a length below " + n
        );
    }


    /**
     * Asserts that the target is less than `value`.
     *
     * <pre>
     *     expect(5).to.be.below(10);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a maximum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.below(4);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.below(4);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void below(double n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthBelow((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.doubleValue() < n,
                    "expected #{this} to be below " + n,
                    "expected #{this} to be at least " + n
            );
        }
    }
    /** Alias of {@link #below(double, String)} */
    public void lessThan(double n, String prefix) {
        below(n, prefix);
    }
    /** Alias of {@link #below(double, String)} */
    public void lt(double n, String prefix) {
        below(n, prefix);
    }

    /**
     * Same as {@link #below(double, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void below(double n) {
        below(n, null);
    }
    /** Alias of {@link #below(double)} */
    public void lessThan(double n) {
        below(n);
    }
    /** Alias of {@link #below(double)} */
    public void lt(double n) {
        below(n);
    }

    /**
     * Asserts that the target is less than `value`.
     *
     * <pre>
     *     expect(5L).to.be.below(10L);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a maximum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.below(4L);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.below(4L);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void below(long n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthBelow((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.longValue() < n,
                    "expected #{this} to be below " + n,
                    "expected #{this} to be at least " + n
            );
        }
    }
    /** Alias of {@link #below(long, String)} */
    public void lessThan(long n, String prefix) {
        below(n, prefix);
    }
    /** Alias of {@link #below(long, String)} */
    public void lt(long n, String prefix) {
        below(n, prefix);
    }

    /**
     * Same as {@link #below(long, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void below(long n) {
        below(n, null);
    }
    /** Alias of {@link #below(long)} */
    public void lessThan(long n) {
        below(n);
    }
    /** Alias of {@link #below(long)} */
    public void lt(long n) {
        below(n);
    }

    private void assertLengthBelow(int n) {
        final int length = getLength();
        doAssert(
                () -> length < n,
                "expected #{this} to have a length below " + n + " but got " + length,
                "expected #{this} to not have a length below" + n
        );
    }


    /**
     * Asserts that the target is less than or equal to `value`.
     *
     * <pre>
     *     expect(5).to.be.at.most(5);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a maximum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.of.at.most(4);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.of.at.most(3);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void most(double n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthAtMost((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.doubleValue() <= n,
                    "expected #{this} to be at most " + n,
                    "expected #{this} to be above " + n
            );
        }
    }
    /** Alias of {@link #most(double, String)} */
    public void lte(double n, String prefix) {
        most(n, prefix);
    }

    /**
     * Same as {@link #most(double, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void most(double n) {
        most(n, null);
    }
    /** Alias of {@link #most(double)} */
    public void lte(double n) {
        most(n);
    }

    /**
     * Asserts that the target is less than or equal to `value`.
     *
     * <pre>
     *     expect(5L).to.be.at.most(5L);
     * </pre>
     *
     * Can also be used in conjunction with `length` to
     * assert a maximum length. The benefit being a
     * more informative error message than if the length
     * was supplied directly.
     *
     * <pre>
     *     expect("foo").to.have.length.of.at.most(4L);
     *     expect(new Integer[] {1, 2, 3}).to.have.length.of.at.most(3L);
     * </pre>
     *
     * @param n the value the target should be compared with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void most(long n, String prefix) {
        this.prefix = prefix;
        if (getDoLength()) {
            assertLengthAtMost((int) n);
        } else {
            if (!(object instanceof Number)) {
                throw new AssertionError(getMessage("expected #{this} to be an instance of " + Number.class));
            }
            final Number number = (Number)object;
            doAssert(
                    () -> number.longValue() <= n,
                    "expected #{this} to be at most " + n,
                    "expected #{this} to be above " + n
            );
        }
    }
    /** Alias of {@link #most(long, String)} */
    public void lte(long n, String prefix) {
        most(n, prefix);
    }

    /**
     * Same as {@link #most(long, String)} with no prefix set.
     *
     * @param n the value the target should be compared with
     */
    public void most(long n) {
        most(n, null);
    }
    /** Alias of {@link #most(long)} */
    public void lte(long n) {
        most(n);
    }

    private void assertLengthAtMost(int n) {
        final int length = getLength();
        doAssert(
                () -> length <= n,
                "expected #{this} to have a length at most " + n + " but got " + length,
                "expected #{this} to have a length above " + n
        );
    }


    /**
     * Asserts that the target matches a regular expression.
     *
     * <pre>
     *     expect("foobar").to.match(Pattern.compile("^foo"));
     * </pre>
     *
     * @param pattern the {@code Pattern} to match the target with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void match(Pattern pattern, String prefix) {
        this.prefix = prefix;
        if (!(object instanceof CharSequence)) {
            throw new AssertionError(getMessage("expected #{this} to be an instance of " + CharSequence.class));
        }
        doAssert(
                () -> pattern.matcher((CharSequence) object).find(),
                "expected #{this} to match " + formatValue(pattern),
                "expected #{this} not to match " + formatValue(pattern)
        );
    }
    /** Alias of {@link #match(Pattern, String)} */
    public void matches(Pattern pattern, String prefix) {
        match(pattern, prefix);
    }

    /**
     * Asserts that the target matches a regular expression.
     *
     * <pre>
     *     expect("foobar").to.match("^foo");
     * </pre>
     *
     * @param regex the regular expression to match the target with
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void match(String regex, String prefix) {
        match(Pattern.compile(regex), prefix);
    }
    /** Alias of {@link #match(String, String)} */
    public void matches(String regex, String prefix) {
        match(regex, prefix);
    }

    /**
     /**
     * Same as {@link #match(Pattern, String)} with no prefix set.
     *
     * @param pattern the {@code Pattern} to match the target with
     */
    public void match(Pattern pattern) {
        match(pattern, null);
    }
    /** Alias of {@link #match(Pattern)} */
    public void matches(Pattern regex) {
        match(regex);
    }

    /**
     /**
     * Same as {@link #match(String, String)} with no prefix set.
     *
     * @param regex the regular expression to match the target with
     */
    public void match(String regex) {
        match(regex, null);
    }
    /** Alias of {@link #match(String)} */
    public void matches(String regex) {
        match(regex);
    }


    /**
     * Asserts that the target's `length` has the expected value.
     *
     * <pre>
     *     expect(new Integer[] {1, 2, 3}).to.have.length(3);
     *     expect("foobar").to.have.length(6);
     * </pre>
     * 
     * @param length the expected length
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     * @return an {@code Assertion} on the length
     */
    public Assertion length(int length, String prefix) {
        this.prefix = prefix;
        final int n = getLength();
        this.expected = length;
        this.actual = n;
        doAssert(
                () -> n == length,
                "expected #{this} to have a length of #{exp} but got #{act}",
                "expected #{this} to not have a length of #{act}"
        );
        return new Assertion(object, EnumSet.of(Flags.DO_LENGTH));
    }
    /** Alias of {@link #length(int, String)} */
    public Assertion size(int length, String prefix) {
        return length(length, prefix);
    }
    /** Alias of {@link #length(int, String)} */
    public void lengthOf(int length, String prefix) {
        length(length, prefix);
    }
    /** Alias of {@link #length(int, String)} */
    public void sizeOf(int length, String prefix) {
        length(length, prefix);
    }

    /**
     * Same as {@link #length(int, String)} with no prefix set.
     *
     * @param length the expected length
     * @return an {@code Assertion} on the length
     */
    public Assertion length(int length) {
        return length(length, null);
    }
    /** Alias as {@link #length(int)} */
    public Assertion size(int length) {
        return length(length, null);
    }
    /** Alias as {@link #length(int)} */
    public void lengthOf(int length) {
        length(length, null);
    }
    /** Alias as {@link #length(int)} */
    public void sizeOf(int length) {
        length(length, null);
    }


    /**
     * Asserts that the target's length is `0`. 
     *
     * <pre>
     *     expect(new Object[]).to.be.empty;
     *     expect("").to.be.empty;
     *     expect(Collections.emptyList()).to.be.empty;
     * </pre>
     *
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     */
    public void empty(String prefix) {
        this.prefix = prefix;
        doAssert(
                () -> getLength() == 0,
                "expected #{this} to be empty",
                "expected #{this} not to be empty"
        );
    }

    /**
     * Same as {@link #empty(String)} with no prefix set.
     */
    public void empty() {
        empty(null);
    }


    /**
     * Asserts the inclusion of an object in an array/collection or a substring in a string.
     *
     * <pre>
     *     expect(new Integer[] {1, 2, 3}).to.include(2);
     *     expect("foobar").to.contain("foo");
     * </pre>
     *
     * @param value the element/substring that is expected to be included in the target
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     * @return an Assertion with the contains-flag set
     */
    public Assertion include(Object value, String prefix) {
        this.prefix = prefix;
        final boolean expected;
        if (object instanceof Collection) {
            expected = ((Collection)object).contains(value);
        } else if (object.getClass().isArray()) {
            final int n = Array.getLength(object);
            boolean foundElement = false;
            for (int i = 0; i < n; i++) {
                final Object element = Array.get(object, i);
                if (value == null? element == null : value.equals(element)) {
                    foundElement = true;
                    break;
                }
            }
            expected = foundElement;
        } else if (object instanceof String && value instanceof CharSequence) {
            expected = ((String)object).contains((CharSequence)value);
        } else if (object instanceof Map && value instanceof Map) {
            final Map actualMap = (Map)object;
            final Map expectedMap = (Map)value;
            boolean allContained = true;
            for (final Object entry : expectedMap.entrySet()) {
                final Object key = ((Map.Entry)entry).getKey();
                allContained = actualMap.containsKey(key);
                allContained &= Objects.equals(((Map.Entry)entry).getValue(), actualMap.get(key));
                if (!allContained) {
                    break;
                }
            }
            expected = allContained;
        } else {
            expected = false;
        }
        doAssert(
                () -> expected,
                "expected #{this} to include " + formatValue(value),
                "expected #{this} to not include " + formatValue(value)
        );
        return new Assertion(object, EnumSet.of(Flags.CONTAINS));
    }
    /** Alias of {@link #include(Object, String)} */
    public Assertion includes(Object value, String prefix) {
        return include(value, prefix);
    }
    /** Alias of {@link #include(Object, String)} */
    public Assertion contain(Object value, String prefix) {
        return include(value, prefix);
    }
    /** Alias of {@link #include(Object, String)} */
    public Assertion contains(Object value, String prefix) {
        return include(value, prefix);
    }

    /**
     * Same as {@link #include(Object, String)} with no prefix set.
     *
     * @param value the element/substring that is expected to be included in the target
     * @return an Assertion with the contains-flag set
     */
    public Assertion include(Object value) {
        return include(value, null);
    }
    /** Alias of {@link #include(Object)} */
    public Assertion includes(Object value) {
        return include(value);
    }
    /** Alias of {@link #include(Object)} */
    public Assertion contain(Object value) {
        return include(value);
    }
    /** Alias of {@link #include(Object)} */
    public Assertion contains(Object value) {
        return include(value);
    }



    private Object getPropertyValue(String path) {
        final PathInfo pathInfo = getPathInfo(path, object);
        doAssert(
                pathInfo::exists,
                "expected #{this} to have a property " + formatValue(path),
                "expected #{this} to not have a property " + formatValue(path)
        );
        return pathInfo.getValue();
    }

    private PathInfo getPathInfo(String path, Object object) {
        final String[] pathElements = path.split("\\.");
        Object value = object;
        for (final String pathElement : pathElements) {
            final int arrayPos = pathElement.indexOf('[');
            if (arrayPos != 0) {
                final String propertyName = arrayPos < 0? pathElement : pathElement.substring(0, arrayPos);
                try {
                    value = nextProperty(value, propertyName);
                } catch (IllegalStateException ex) {
                    return PathInfo.NOT_FOUND;
                }
            }
            if (arrayPos >= 0) {
                final Matcher matcher = ARRAY_INDEX.matcher(pathElement.substring(arrayPos));
                while (matcher.find()) {
                    try {
                        final String index = matcher.group(1);
                        if (value.getClass().isArray()) {
                            value = Array.get(value, Integer.parseInt(index));
                        } else if (value instanceof List<?>) {
                            value = ((List<?>) value).get(Integer.parseInt(index));
                        } else if (value instanceof Map<?, ?>) {
                            final Map<?, ?> map = (Map<?, ?>) value;
                            final TypeVariable[] type = map.getClass().getTypeParameters();
                            final Object key = type.length > 0 && "java.lang.Integer".equals(type[0].getBounds()[0].getTypeName()) ?
                                    Integer.parseInt(index) : index;
                            value = map.get(key);
                        } else {
                            return PathInfo.NOT_FOUND;
                        }
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                        return PathInfo.NOT_FOUND;
                    }
                }
            }
        }
        return new PathInfo(value);
    }

    private Object nextProperty(Object bean, String propertyName) {
        final Class<?> clazz = bean.getClass();
        final String propertyGetterBase = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        try {
            final Method getter = clazz.getMethod("get" + propertyGetterBase);
            if (! void.class.equals(getter.getReturnType())) {
                return getter.invoke(bean);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore
        }
        try {
            final Method booleanGetter = clazz.getMethod("is" + propertyGetterBase);
            final Class<?> returnType = booleanGetter.getReturnType();
            if (boolean.class.equals(returnType) || Boolean.class.equals(returnType)) {
                return booleanGetter.invoke(bean);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore
        }
        try {
            // we also want to support old-style getters like size() and length()
            final Method getter = clazz.getMethod(propertyName);
            if (! void.class.equals(getter.getReturnType())) {
                return getter.invoke(bean);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore
        }
        try {
            return clazz.getField(propertyName).get(bean);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // ignore
        }
        throw new IllegalStateException("Object " + formatValue(object) + " does not have a property " + formatValue(propertyName));
    }

    /**
     * Asserts that the target has a property `name`, and that
     * the value of that property is equal to  `value`.
     * You can use dot- and bracket-notation for deep references into objects and arrays.
     *
     *     // simple referencing
     *     class Foo {
     *         public String getFoo();
     *         [...]
     *     }
     *     expect(new Foo()).to.have.property("foo");
     *     expect(new Foo()).to.have.property("foo", "bar");
     *
     *     // deep referencing
     *     class Tea {
     *         public String getTea();
     *         [...]
     *     }
     *     class Teas {
     *         public Tea getGreen();
     *         public Object[] getTeas();
     *         [...]
     *     }
     *
     *     expect(new Teas()).to.have.property('green.tea', 'matcha');
     *     expect(new Teas()).to.have.property('teas[1]', 'matcha');
     *     expect(new Teas()).to.have.property('teas[2].tea', 'konacha');
     *
     * You can also use an array as the starting point of a property
     * assertion, or traverse nested arrays.
     *
     *     final Object[] arr = new Object[] {
     *         new String[] {"chai", "matcha", "konacha"},
     *         new Tea[] { new Tea(), new Tea(), new Tea() }
     *     };
     *
     *     expect(arr).to.have.deep.property("[0][1]", "matcha");
     *     expect(arr).to.have.deep.property("[1][2].tea", "konacha");
     *
     * Furthermore, the method property() changes the subject of the assertion
     * to be the value of that property from the original object. This
     * permits for further chainable assertions on that property.
     *
     *     expect(obj).to.have.property("foo")
     *         .that.is.a(String.class);
     *     expect(deepObj).to.have.property("green")
     *         .that.is.an(Tea.class)
     *         .that.deep.equals(new Tea());
     *     expect(deepObj).to.have.property("teas")
     *         .that.is.an(Array.class)
     *         .with.property("[2]")
     *         .that.deep.equals(new Tea());
     *
     * @param path path to the property that should be compared to the value
     * @param expected the expected value of the property
     * @param prefix A prefix that will be prepended to any error message generated by this assertion.
     * @return an Assertion on the property
     */
    public Assertion property(String path, Object expected, String prefix) {
        this.prefix = prefix;

        final boolean notFlagIsSet = this.getNot();
        flags.remove(Flags.NOT);
        final PathInfo pathInfo = getPathInfo(path, object);
        final Object value = pathInfo.getValue();
        if (notFlagIsSet) {
            flags.add(Flags.NOT);
        }

        doAssert(
                () -> pathInfo.exists() != getNot(),
                "expected #{this} to have a property " + formatValue(path),
                "#{this} has no property " + formatValue(path)
        );

        this.expected = expected;
        this.actual = value;
        doAssert(
                () -> expected == null? value == null : expected.equals(value),
                "expected #{this} to have a property " + formatValue(path) + " of #{exp}, but got #{act}",
                "expected #{this} to not have a property " + formatValue(path) + " of #{act}"

        );

        return Caj.expect(value);
    }
    public Assertion property(String path, Object expected) {
        return property(path, expected, null);
    }
    public Assertion property(String path) {
        final PathInfo pathInfo = getPathInfo(path, object);
        final String descriptor = getDeep()? "deep property" : "property";
        doAssert(
                pathInfo::exists,
                "expected #{this} to have a " + descriptor + " " + formatValue(path),
                "expected #{this} to not have " + descriptor + " " + formatValue(path)
        );
        final Object value = pathInfo.getValue();
        return Caj.expect(value);
    }



    public void string(CharSequence expected) {
        string(expected, null);
    }
    public void string(CharSequence expected, String prefix) {
        this.prefix = prefix;
        Caj.expect(object).is.a(String.class, prefix);
        doAssert(
                () -> ((String) object).contains(expected),
                "expected #{this} to contain " + formatValue(expected),
                "expected #{this} to not contain " + formatValue(expected)
        );
    }



    public void keys(Collection<String> keys) {
        this.keys(keys, null);
    }
    public void keys(Collection<String> keys, String prefix) {
        this.prefix = prefix;
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys are required");
        }
        if (! (object instanceof Map)) {
            final String message = getMessage("expected #{this} to be a Map");
            throw new AssertionError(message);
        }
        final Map map = (Map)object;

        final boolean ok;
        if (getAny()) {
            ok = keys.stream().anyMatch(key -> map.keySet().contains(key));
        } else {
            boolean tmpOk = keys.stream().allMatch(key -> map.keySet().contains(key));
            if (!getNot() && !getContains()) {
                tmpOk &= keys.size() == map.size();
            }
            ok = tmpOk;
        }

        final StringBuilder builder = new StringBuilder(getContains()? "contain " : "have ");
        if (keys.size() == 1) {
            builder.append("key ").append(formatValue(keys.iterator().next()));
        } else {
            builder.append("keys ");
            final List<String> keyLabels = new ArrayList<>(keys);
            final String last = keyLabels.remove(keys.size() - 1);
            for (final String keyLabel : keyLabels) {
                builder.append(formatValue(keyLabel)).append(", ");
            }
            builder.append(getAny()? "or " : "and ");
            builder.append(formatValue(last));
        }

        doAssert(
                () -> ok,
                "expected #{this} to " + builder.toString(),
                "expected #{this} to not " + builder.toString()
        );
    }
    public void keys(String... keys) {
        if (keys == null) {
            throw new IllegalArgumentException("keys are required");
        }
        this.keys(Arrays.asList(keys), null);
    }
    public void keys(Map<String, ?> keys) {
        this.keys(keys, null);
    }
    public void keys(Map<String, ?> keys, String prefix) {
        if (keys == null) {
            throw new IllegalArgumentException("keys are required");
        }
        this.keys(keys.keySet(), prefix);
    }



    private Assertion assertCause(Class<? extends Throwable> throwableClass, Throwable expectedThrowable, String errorMessage, Pattern errorPattern, String prefix) {
        this.prefix = prefix;
        Assertion chainedAssertion = null;

        try {
            if (object instanceof Runnable) {
                ((Runnable)object).run();
            } else if (object instanceof Callable) {
                ((Callable)object).call();
            } else if (object instanceof Supplier) {
                ((Supplier)object).get();
            } else {
                throw new AssertionError(getMessage("expected #{this} to be a Runnable, Callable, or Supplier"));
            }
        } catch (Throwable actualThrowable) {
            actual = actualThrowable;
            chainedAssertion = Caj.expect(actualThrowable);
            if (throwableClass != null) {
                expected = throwableClass;
                doAssert(
                        () -> throwableClass.isAssignableFrom(actualThrowable.getClass()),
                        "expected #{this} to cause #{exp} but #{act} was thrown",
                        "expected #{this} to not cause #{exp} but #{act} was thrown"
                );

                if (errorMessage == null && errorPattern == null) {
                    return chainedAssertion;
                }
            } else if (expectedThrowable != null) {
                expected = expectedThrowable;
                doAssert(
                        () -> expectedThrowable.equals(actualThrowable),
                        "expected #{this} to cause #{exp} but #{act} was thrown",
                        "expected #{this} to not cause #{exp}"
                );

                if (errorMessage == null && errorPattern == null) {
                    return chainedAssertion;
                }
            }

            if (errorMessage != null) {
                final String message = actualThrowable.getMessage();
                expected = errorMessage;
                actual = message;
                doAssert(
                        () -> message != null && message.contains(errorMessage),
                        "expected #{this} to cause an Error or Exception including #{exp} but got #{act}",
                        "expected #{this} to cause an Error or Exception not including #{exp}"
                );
                return chainedAssertion;
            } else if (errorPattern != null) {
                final String message = actualThrowable.getMessage();
                expected = errorPattern;
                actual = message;
                doAssert(
                        () -> message != null && errorPattern.matcher(message).find(),
                        "expected #{this} to cause an Error or Exception matching #{exp} but got #{act}",
                        "expected #{this} to cause an Error or Exception not matching #{exp}"
                );
                return chainedAssertion;
            }
        }

        final boolean thrown = actual != null;
        final String actuallyGot = thrown? " but #{act} was thrown" : "";

        final String expectedThrown =
                throwableClass != null? throwableClass.getSimpleName()
                        : expectedThrowable != null? expectedThrowable.toString()
                        :"an Error or Exception";
        doAssert(
                () -> thrown,
                "expected #{this} to cause " + expectedThrown + actuallyGot,
                "expected #{this} to not cause " + expectedThrown + actuallyGot
        );

        return chainedAssertion != null? chainedAssertion : Caj.expect(null);
    }
    public Assertion cause(Class<? extends Throwable> throwableClass, String errorMessage, String prefix) {
        return assertCause(throwableClass, null, errorMessage, null, prefix);
    }
    public Assertion cause(Class<? extends Throwable> throwableClass, Pattern errorPattern, String prefix) {
        return assertCause(throwableClass, null, null, errorPattern, prefix);
    }
    public Assertion cause(Class<? extends Throwable> throwableClass, String errorMessage) {
        return cause(throwableClass, errorMessage, null);
    }
    public Assertion cause(Class<? extends Throwable> throwableClass, Pattern errorPattern) {
        return cause(throwableClass, errorPattern, null);
    }
    public Assertion cause(Class<? extends Throwable> throwableClass) {
        return cause(throwableClass, (String) null, null);
    }
    public Assertion cause(String errorMessage) {
        return cause(null, errorMessage, null);
    }
    public Assertion cause(Pattern errorPattern) {
        return cause(null, errorPattern, null);
    }
    public Assertion cause() {
        return cause(null, (String) null, null);
    }
    public Assertion cause(Throwable throwable, String prefix) {
        return assertCause(null, throwable, null, null, prefix);
    }
    public Assertion cause(Throwable throwable) {
        return cause(throwable, null);
    }

    public Assertion causes(Class<? extends Throwable> throwableClass, Pattern errorMessage, String prefix) {
        return cause(throwableClass, errorMessage, prefix);
    }
    public Assertion causes(Class<? extends Throwable> throwableClass, String errorMessage) {
        return cause(throwableClass, errorMessage, null);
    }
    public Assertion causes(Class<? extends Throwable> throwableClass, Pattern errorMessage) {
        return cause(throwableClass, errorMessage, null);
    }
    public Assertion causes(Class<? extends Throwable> throwableClass) {
        return cause(throwableClass, (String)null, null);
    }
    public Assertion causes(String errorMessage) {
        return cause(null, errorMessage, null);
    }
    public Assertion causes(Pattern errorMessage) {
        return cause(null, errorMessage, null);
    }
    public Assertion causes() {
        return cause(null, (String) null, null);
    }
    public Assertion causes(Throwable throwable, String prefix) {
        return cause(throwable, prefix);
    }
    public Assertion causes(Throwable throwable) {
        return cause(throwable, null);
    }



    public void satisfy(Predicate<Object> predicate, String prefix) {
        this.prefix = prefix;
        doAssert(
                () -> predicate.test(object),
                "expected #{this} to satisfy " + formatValue(predicate),
                "expected #{this} to not satisfy " + formatValue(predicate)
        );
    }
    public void satisfy(Predicate<Object> predicate) {
        satisfy(predicate, null);
    }



    public void closeTo(double expected, double delta, String prefix) {
        this.prefix = prefix;
        if (! (object instanceof Number)) {
            final String message = getMessage("expected #{this} to be an instance of class java.lang.Number");
            throw new AssertionError(message);
        }
        doAssert(
                () -> Math.abs(expected - ((Number) object).doubleValue()) <= delta,
                "expected #{this} to be close to " + expected + " +/- " + delta,
                "expected #{this} not to be close to " + expected + " +/- " + delta
        );
    }
    public void closeTo(double expected, double delta) {
        closeTo(expected, delta, null);
    }
    public void closeTo(long expected, long delta, String prefix) {
        this.prefix = prefix;
        if (! (object instanceof Number)) {
            final String message = getMessage("expected #{this} to be an instance of class java.lang.Number");
            throw new AssertionError(message);
        }
        doAssert(
                () -> Math.abs(expected - ((Number) object).longValue()) <= delta,
                "expected #{this} to be close to " + expected + " +/- " + delta,
                "expected #{this} not to be close to " + expected + " +/- " + delta
        );
    }
    public void closeTo(long expected, long delta) {
        closeTo(expected, delta, null);
    }


    @SuppressWarnings("unchecked")
    private boolean isSubsetOf(Object subsetParam, Object supersetParam) {
        final Collection subset;
        if (subsetParam instanceof Collection) {
            subset = (Collection)subsetParam;
        } else if (subsetParam.getClass().isArray()) {
            final int n = Array.getLength(subsetParam);
            subset = new ArrayList<>(n);
            for (int i=0; i < n; i++) {
                subset.add(Array.get(subsetParam, i));
            }
        } else {
            throw new AssertionError("expected " + formatValue(subsetParam) + " to be an Array or a Collection");
        }

        final Collection superset;
        if (supersetParam instanceof Collection) {
            superset = (Collection)supersetParam;
        } else if (supersetParam.getClass().isArray()) {
            final int n = Array.getLength(supersetParam);
            superset = new ArrayList<>(n);
            for (int i=0; i < n; i++) {
                superset.add(Array.get(supersetParam, i));
            }
        } else {
            throw new AssertionError("expected " + formatValue(supersetParam) + " to be an Array or a Collection");
        }

        return superset.containsAll(subset);
    }
    public void members(String prefix, Object... subset) {
        this.prefix = prefix;
        this.expected = subset;

        if (getContains()) {
            doAssert (
                    () -> isSubsetOf(subset, object),
                    "expected #{this} to be a superset of #{exp}",
                    "expected #{this} to not be a superset of #{exp}"
            );

        } else {
            doAssert(
                    () -> isSubsetOf(object, subset) && isSubsetOf(subset, object),
                    "expected #{this} to have the same members as #{exp}",
                    "expected #{this} to not have the same members as #{exp}"
            );
        }
    }
    public void members(Object... subset) {
        members(null, subset);
    }



    public Assertion change(Object bean, String property, String prefix) {
        this.prefix = prefix;
        Caj.expect(bean).to.have.property(property);

        final Object initial = nextProperty(bean, property);
        if (object instanceof Runnable) {
            ((Runnable)object).run();
        } else if (object instanceof Callable) {
            try {
                ((Callable) object).call();
            } catch (Exception e) {
                throw new AssertionError(getMessage("Calling #{this} threw an exception"), e);
            }
        } else {
            throw new AssertionError(getMessage("expected #{this} to be a Runnable or Callable"));
        }
        final Object changed = nextProperty(bean, property);

        doAssert(
                () -> initial == null ? changed != null : !initial.equals(changed),
                "expected ." + property + " to change",
                "expected ." + property + " to not change"
        );
        return Caj.expect(object);
    }
    public Assertion change(Object bean, String property) {
        return change(bean, property, null);
    }
    public Assertion changes(Object bean, String property, String prefix) {
        return change(bean, property, prefix);
    }
    public Assertion changes(Object bean, String property) {
        return change(bean, property, null);
    }



    private Assertion assertIncDec(Object bean, String property, String prefix, boolean inc) {
        this.prefix = prefix;
        Caj.expect(bean).to.have.property(property);

        final Object initial = nextProperty(bean, property);
        Caj.expect(initial).to.be.a(Number.class);

        if (object instanceof Runnable) {
            ((Runnable)object).run();
        } else if (object instanceof Callable) {
            try {
                ((Callable) object).call();
            } catch (Exception e) {
                throw new AssertionError(getMessage("Calling #{this} threw an exception"), e);
            }
        } else {
            throw new AssertionError(getMessage("expected #{this} to be a Runnable or Callable"));
        }
        final Object changed = nextProperty(bean, property);
        Caj.expect(changed).to.be.a(Number.class);

        final boolean ok;
        if (initial instanceof Long && changed instanceof Long) {
            final long initialValue = (Long)initial;
            final long changedValue = (Long)changed;
            ok = inc? changedValue > initialValue : changedValue < initialValue;
        } else {
            final double initialValue = ((Number)initial).doubleValue();
            final double changedValue = ((Number)changed).doubleValue();
            ok = inc? changedValue > initialValue : changedValue < initialValue;
        }

        final String verb = inc? "increase" : "decrease";

        doAssert(
                () -> ok,
                "expected ." + property + " to " + verb,
                "expected ." + property + " to not " + verb
        );
        return Caj.expect(object);
    }
    public Assertion increase(Object bean, String property, String prefix) {
        return assertIncDec(bean, property, prefix, true);
    }
    public Assertion increase(Object bean, String property) {
        return increase(bean, property, null);
    }
    public Assertion increases(Object bean, String property, String prefix) {
        return increase(bean, property, prefix);
    }
    public Assertion increases(Object bean, String property) {
        return increase(bean, property, null);
    }
    public Assertion decrease(Object bean, String property, String prefix) {
        return assertIncDec(bean, property, prefix, false);
    }
    public Assertion decrease(Object bean, String property) {
        return decrease(bean, property, null);
    }
    public Assertion decreases(Object bean, String property, String prefix) {
        return decrease(bean, property, prefix);
    }
    public Assertion decreases(Object bean, String property) {
        return decrease(bean, property, null);
    }



    private void assertEqual(Object expected, String prefix) {
        assertEqual(expected, prefix, null);
    }
    private void assertEqual(Object expected, String prefix, String op) {
        this.prefix = prefix;
        this.expected = expected;
        op = op != null? op : "equal";
        doAssert(
                () -> expected == null? object == null : expected.equals(object),
                "expected #{this} to " + op + " #{exp}",
                "expected #{this} to not \" + op + \" #{exp}"
        );
    }

    private int getLength() {
        final int size;
        if (object instanceof Collection) {
            size = ((Collection)object).size();
        } else if (object instanceof Map) {
            size = ((Map)object).size();
        } else if (object.getClass().isArray()) {
            size = Array.getLength(object);
        } else if (object instanceof String) {
            size = ((String)object).length();
        } else {
            throw new AssertionError(getMessage("expected #{this} to be an array, Collection, Map or String"));
        }
        return size;
    }

    private void doAssert(BooleanSupplier expr, String msg, String negateMsg) {
        final boolean negate = getNot();
        final boolean ok = negate? !expr.getAsBoolean() : expr.getAsBoolean();
        if (!ok) {
            final String message = getMessage(negate? negateMsg : msg);
            throw new AssertionError(message);
        }
    }

    private String getMessage(String message) {
        message = message == null? "" : message;
        message = message.replaceAll("#\\{this}",  formatValue(object).replaceAll("\\$", "\\\\\\$"));
        message = message.replaceAll("#\\{act}", formatValue(actual).replaceAll("\\$", "\\\\\\$"));
        message = message.replaceAll("#\\{exp}", formatValue(expected).replaceAll("\\$", "\\\\\\$"));
        if (prefix != null) {
            message = prefix + ": " + message;
        }
        return message;
    }

    private String formatValue(Object value) {
        if (value != null && value.getClass().isArray()) {
            final int n = Array.getLength(value);
            if (n == 0) {
                return "[]";
            }
            final StringBuilder builder = new StringBuilder("[").append(formatValue(Array.get(value, 0)));
            for (int i = 1; i < n; i++) {
                builder.append(", ").append(formatValue(Array.get(value, i)));
            }
            builder.append("]");
            return builder.toString();
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value instanceof Pattern) {
            final Pattern pattern = (Pattern)value;
            final StringBuilder builder = new StringBuilder("/").append(value).append('/');
            final int flags = pattern.flags();
            if ((flags & Pattern.UNIX_LINES) != 0) {
                builder.append('d');
            }
            if ((flags & Pattern.CASE_INSENSITIVE) != 0) {
                builder.append('i');
            }
            if ((flags & Pattern.COMMENTS) != 0) {
                builder.append('x');
            }
            if ((flags & Pattern.MULTILINE) != 0) {
                builder.append('m');
            }
            if ((flags & Pattern.DOTALL) != 0) {
                builder.append('s');
            }
            if ((flags & Pattern.UNICODE_CASE) != 0) {
                builder.append('u');
            }
            if ((flags & Pattern.UNICODE_CHARACTER_CLASS) != 0) {
                builder.append('U');
            }
            return builder.toString();
        }
        return String.valueOf(value);
    }

    private static class PathInfo {
        public static final PathInfo NOT_FOUND = new PathInfo(null) {
            public boolean exists() {
                return false;
            }
        };

        private final Object value;

        private PathInfo(Object value) {
            this.value = value;
        }

        public boolean exists() {
            return true;
        }

        public Object getValue() {
            return value;
        }
    }
}

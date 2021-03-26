package de.uni_passau.fim.se2.slicer.util.cfg;

import static com.google.common.truth.Truth.assert_;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalVariableTest {

  private LocalVariable foo;
  private LocalVariable fooClone;
  private LocalVariable fooModified;
  private LocalVariable bar;

  @BeforeEach
  void setUp() {
    foo = new LocalVariable("foo", "[D", null, 42);
    fooClone = new LocalVariable("foo", "[D", null, 42);
    fooModified = new LocalVariable("foo", "[F", null, 42);
    bar = new LocalVariable("bar", "I", null, 23);
  }

  @Test
  void test_foo_getIndex() {
    assert_().withMessage("Index does not match").that(foo.getIndex()).isEqualTo(42);
  }

  @Test
  void test_foo_getDescriptor() {
    assert_().withMessage("Descriptor does not match").that(foo.getDescriptor()).isEqualTo("[D");
  }

  @Test
  void test_foo_getName() {
    assert_().withMessage("Name does not match").that(foo.getName()).isEqualTo("foo");
  }

  @Test
  void test_foo_getSignature() {
    assert_().withMessage("Signature does not match").that(foo.getSignature()).isNull();
  }

  @Test
  void test_foo_toString() {
    assert_()
        .withMessage("String representation does not match")
        .that(foo.toString())
        .isEqualTo("LocalVariable{name='foo', descriptor='[D', signature='null', index=42}");
  }

  @Test
  void test_foo_equals_foo() {
    assert_().withMessage("Foo should be equals with itself").that(foo).isEqualTo(foo);
  }

  @Test
  void test_foo_notEquals_null() {
    assert_().withMessage("Foo should not be equals null").that(foo).isNotEqualTo(null);
  }

  @Test
  void test_foo_notEquals_String() {
    assert_()
        .withMessage("Foo should not be equals to String")
        .that(foo)
        .isNotEqualTo("Hello World");
  }

  @Test
  void test_foo_equals_fooClone() {
    assert_().withMessage("Variables should be equal").that(foo).isEqualTo(fooClone);
  }

  @Test
  void test_foo_sameAs_foo() {
    assertEquals(foo, foo);
  }

  @Test
  void test_foo_null() {
    assertNotEquals(null, foo);
  }

  @Test
  void test_foo_notEquals_fooModified() {
    assert_().withMessage("Variables should not be equal").that(foo).isNotEqualTo(fooModified);
  }

  @Test
  void test_foo_sameHashCode_fooClone() {
    assert_()
        .withMessage("HashCodes do not match")
        .that(foo.hashCode())
        .isEqualTo(fooClone.hashCode());
  }

  @Test
  void test_foo_differentHashCode_bar() {
    assert_()
        .withMessage("HashCodes should not match")
        .that(foo.hashCode())
        .isNotEqualTo(bar.hashCode());
  }
}

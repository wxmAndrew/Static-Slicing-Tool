package de.uni_passau.fim.se2.slicer.util.cfg;

import static com.google.common.truth.Truth.assert_;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;

class NodeTest {

  private static Node firstNode;
  private static Node secondNode;
  private static Node dummyNode;
  private static AbstractInsnNode firstInstruction;

  @BeforeAll
  static void setUp() {
    firstInstruction = mock(AbstractInsnNode.class);
    AbstractInsnNode secondInstruction = mock(AbstractInsnNode.class);

    firstNode = new Node(firstInstruction, 23);
    secondNode = new Node(secondInstruction, 42);
    dummyNode = new Node("DUMMY");
  }

  @Test
  void test_getInstruction() {
    assert_()
        .withMessage("Expected Instruction did not match")
        .that(firstNode.getInstruction())
        .isEqualTo(firstInstruction);
  }

  @Disabled
  @Test
  void test_toString() {}

  @Disabled
  @Test
  void test_prettyPrint() {}

  @Test
  void test_getLineNumber() {
    assert_()
        .withMessage("Expected line number did not match")
        .that(firstNode.getLineNumber())
        .isEqualTo(23);
  }

  @Test
  void test_getID() {
    assertAll(
        () -> assertEquals("1", firstNode.getID(), "ID did not match"),
        () -> assertEquals("\"DUMMY\"", dummyNode.getID(), "Dummy ID did not match"));
  }

  @Test
  void test_hashCode() {
    assertAll(
        () -> assertEquals(80, firstNode.hashCode()),
        () -> assertEquals(2138006363, dummyNode.hashCode()));
  }

  @Test
  void test_equals() {
    assertAll(
        () -> assertEquals(firstNode, firstNode),
        () -> assertNotEquals(firstNode, secondNode),
        () -> assertNotEquals(null, firstNode),
        () -> assertNotEquals("", firstNode));
  }
}

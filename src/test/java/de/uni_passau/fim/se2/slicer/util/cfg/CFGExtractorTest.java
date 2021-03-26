package de.uni_passau.fim.se2.slicer.util.cfg;

import static com.google.common.truth.Truth.assert_;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.MethodNode;

class CFGExtractorTest {

  @Test
  void test_computeInternalMethodName1() {
    final MethodNode node = mock(MethodNode.class);
    final List<String> exceptions = mockList();
    when(exceptions.size()).thenReturn(0);
    node.name = "foo";
    node.desc = "(I)D";
    node.signature = "double foo(int)";
    node.exceptions = exceptions;

    final String result = CFGExtractor.computeInternalMethodName(node);

    assert_()
        .withMessage("Internal method name not corret")
        .that(result)
        .isEqualTo("foo: (I)D; double foo(int)");
  }

  @Test
  void test_computeInternalMethodName2() {
    final MethodNode node = mock(MethodNode.class);
    final List<String> exceptions = mockList();
    when(exceptions.size()).thenReturn(1);
    when(exceptions.toArray(new String[0]))
        .thenReturn(new String[] {"ArrayIndexOutOfBoundsException"});
    node.name = "foo";
    node.desc = "(I)D";
    node.signature = "double foo(int)";
    node.exceptions = exceptions;

    final String result = CFGExtractor.computeInternalMethodName(node);

    assert_()
        .withMessage("Internal method name not correct")
        .that(result)
        .isEqualTo("foo: (I)D; double foo(int); [ArrayIndexOutOfBoundsException]");
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> mockList() {
    return mock(List.class);
  }
}

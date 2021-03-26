package de.uni_passau.fim.se2.slicer.util.cfg;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

class CFGLocalVariableTableVisitorTest {

  private CFGLocalVariableTableVisitor classVisitor;

  @BeforeEach
  void setUp() {
    classVisitor = new CFGLocalVariableTableVisitor();
  }

  @Test
  void test_visitLocalVariable() {
    MethodVisitor visitor =
        classVisitor.visitMethod(0, "foo", "(ILjava/lang/String;)[D", null, null);
    visitor.visitLocalVariable("a", "I", null, null, null, 23);
    visitor.visitEnd();
    final Map<String, LocalVariableTable> localVariables = classVisitor.getLocalVariables();

    assert_().withMessage("Expected one local variable").that(localVariables.size()).isEqualTo(1);
  }

  @Test
  void test_visitLocalVariableWithCV() throws Exception {
    final ClassVisitor cvMock = mock(ClassVisitor.class);
    final Field cvField = classVisitor.getClass().getSuperclass().getDeclaredField("cv");
    cvField.setAccessible(true);
    cvField.set(classVisitor, cvMock);

    final MethodVisitor visitor =
        classVisitor.visitMethod(0, "foo", "(ILjava/lang/String;)[D", null, null);
    visitor.visitLocalVariable("a", "I", null, null, null, 42);
    visitor.visitEnd();
    final Map<String, LocalVariableTable> localVariables = classVisitor.getLocalVariables();

    assert_().withMessage("Expected one local variable").that(localVariables.size()).isEqualTo(1);
    verify(cvMock, times(1)).visitMethod(0, "foo", "(ILjava/lang/String;)[D", null, null);
  }
}

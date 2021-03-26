package de.uni_passau.fim.se2.slicer.util.cfg;

import static org.objectweb.asm.Opcodes.ASM7;

import com.google.common.collect.Maps;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A class visitor that extracts the information of the local variable table for each method of a
 * class.
 *
 * @see LocalVariableTable
 * @see ClassVisitor
 */
public class CFGLocalVariableTableVisitor extends ClassVisitor {

  private final Map<String, LocalVariableTable> localVariables;

  public CFGLocalVariableTableVisitor() {
    super(ASM7);
    localVariables = Maps.newLinkedHashMap();
  }

  /** {@inheritDoc} */
  @Override
  public MethodVisitor visitMethod(
      final int pAccess,
      final String pName,
      final String pDescriptor,
      final String pSignature,
      final String[] pExceptions) {
    final MethodVisitor mv;
    if (cv != null) {
      mv = cv.visitMethod(pAccess, pName, pDescriptor, pSignature, pExceptions);
    } else {
      mv = null;
    }
    return new CFGLocalVariableTableMethodVisitor(
        this, mv, pName, pDescriptor, pSignature, pExceptions);
  }

  /**
   * Returns the information of the local variable tables for each method in a map of method name
   * and {@link LocalVariableTable}.
   *
   * @return A map of method name to {@link LocalVariableTable}
   */
  public Map<String, LocalVariableTable> getLocalVariables() {
    return localVariables;
  }

  private static class CFGLocalVariableTableMethodVisitor extends MethodVisitor {

    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;
    private final LocalVariableTable localVariableTable;
    private final CFGLocalVariableTableVisitor classVisitor;

    CFGLocalVariableTableMethodVisitor(
        final CFGLocalVariableTableVisitor pClassVisitor,
        final MethodVisitor pMethodVisitor,
        final String pName,
        final String pDescriptor,
        final String pSignature,
        final String[] pExceptions) {
      super(ASM7, pMethodVisitor);
      classVisitor = pClassVisitor;
      name = pName;
      descriptor = pDescriptor;
      signature = pSignature;
      exceptions = pExceptions;
      localVariableTable = new LocalVariableTable();
    }

    /** {@inheritDoc} */
    @Override
    public void visitLocalVariable(
        final String pName,
        final String pDescriptor,
        final String pSignature,
        final Label pStart,
        final Label pEnd,
        final int pIndex) {
      final LocalVariable variable = new LocalVariable(pName, pDescriptor, pSignature, pIndex);
      localVariableTable.addEntry(pIndex, variable);
      super.visitLocalVariable(pName, pDescriptor, pSignature, pStart, pEnd, pIndex);
    }

    /** {@inheritDoc} */
    @Override
    public void visitEnd() {
      final String methodName =
          CFGExtractor.computeInternalMethodName(name, descriptor, signature, exceptions);
      classVisitor.localVariables.put(methodName, localVariableTable);
    }
  }
}

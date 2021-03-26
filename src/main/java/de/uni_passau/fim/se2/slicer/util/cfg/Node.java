package de.uni_passau.fim.se2.slicer.util.cfg;

import com.google.errorprone.annotations.Var;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** Represents a node in the {@link ProgramGraph} */
public class Node {

  private AbstractInsnNode instruction = null;
  private final int lineNumber;

  private static Map<Object, String> sIds = null;
  static int sNextId = 1;

  private final String id;

  /**
   * Creates a new node object.
   *
   * @param pInstructionNode The instruction node this node is based on
   * @param pLineNumber The line number in the source file
   */
  Node(final AbstractInsnNode pInstructionNode, final int pLineNumber) {
    instruction = pInstructionNode;
    lineNumber = pLineNumber;
    id = getId(pInstructionNode);
  }

  public Node(final String pID) {
    id = "\"" + pID + "\"";
    lineNumber = -1;
  }

  /**
   * Provides the instruction.
   *
   * @return The instruction
   */
  public AbstractInsnNode getInstruction() {
    return instruction;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (instruction == null) {
      return id;
    }
    if (instruction instanceof LabelNode) {
      sb.append("LABEL");
    } else if (instruction instanceof LineNumberNode) {
      sb.append("LINENUMBER ").append(((LineNumberNode) instruction).line);
    } else if (instruction instanceof FrameNode) {
      sb.append("FRAME");
    } else {
      final int opcode = instruction.getOpcode();
      final String[] OPCODES = Printer.OPCODES;
      if (opcode > 0 && opcode <= OPCODES.length) {
        sb.append(OPCODES[opcode]);
        if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
          sb.append("(").append(((MethodInsnNode) instruction).name).append(")");
        }
      }
    }
    sb.append(getId(instruction));
    sb.append("  ").append("line number: ").append(lineNumber);

    return "\"" + sb.toString() + "\"";
  }

  /**
   * Provides a pretty printed version of this instruction.
   *
   * @return A string representation of this instruction
   */
  public String prettyPrint() {
    if (instruction == null) {
      return "";
    }
    final Textifier textifier = new Textifier();
    final MethodVisitor visitor = new TraceMethodVisitor(textifier);
    instruction.accept(visitor);
    final StringWriter writer = new StringWriter();
    try (PrintWriter pw = new PrintWriter(writer)) {
      textifier.print(pw);
    }
    return writer.toString();
  }

  private static String getId(final Object pObject) {
    if (sIds == null) {
      sIds = new HashMap<>();
    }

    @Var String id = sIds.get(pObject);
    if (id == null) {
      id = Integer.toString(sNextId++);
      sIds.put(pObject, id);
    }
    return id;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getID() {
    return id;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    @Var int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Node other = (Node) obj;
    if (id == null) {
      return other.id == null;
    } else return id.equals(other.id);
  }
}

package de.uni_passau.fim.se2.slicer.util.cfg;

import com.google.common.collect.Maps;
import com.google.errorprone.annotations.Var;
import java.util.Arrays;
import java.util.Map;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/** Extracts the control-flow graph for a method from ASM's {@link MethodNode}. */
public class CFGExtractor {

  /**
   * Builds the control flow graph for a {@link MethodNode}.
   *
   * @param pOwningClass The name of the owning class
   * @param pMethodNode The {@link MethodNode} to build a CFG for
   * @return A {@link ProgramGraph} representing the method
   * @throws AnalyzerException In case of errors during analysis
   */
  public static ProgramGraph getCFG(final String pOwningClass, final MethodNode pMethodNode)
      throws AnalyzerException {
    final ProgramGraph programGraph = buildGraph(pOwningClass, pMethodNode);
    final Node entry = new Node("Entry");
    final Node exit = new Node("Exit");
    programGraph.addNode(entry);
    programGraph.addNode(exit);
    for (Node node : programGraph.getNodes()) {
      if (node.toString().equals("\"Exit\"") || node.toString().equals("\"Entry\"")) {
        continue;
      }
      if (programGraph.getSuccessors(node).isEmpty()) {
        programGraph.addEdge(node, exit);
      }
      if (programGraph.getPredecessors(node).isEmpty()) {
        programGraph.addEdge(entry, node);
      }
    }
    return programGraph;
  }

  private static ProgramGraph buildGraph(final String pOwningClass, final MethodNode pMethodNode)
      throws AnalyzerException {
    final InsnList instructions = pMethodNode.instructions;
    final Map<AbstractInsnNode, Node> nodes = Maps.newHashMap();
    final ProgramGraph programGraph = new ProgramGraph();
    final Analyzer<BasicValue> analyzer =
        new Analyzer<BasicValue>(new BasicInterpreter()) {

          protected void newControlFlowEdge(final int pSourceIndex, final int pDestinationIndex) {
            final AbstractInsnNode from = instructions.get(pSourceIndex);
            final AbstractInsnNode to = instructions.get(pDestinationIndex);

            final int fromLineNumber = findLineNumber(instructions, pSourceIndex);
            final int toLineNumber = findLineNumber(instructions, pDestinationIndex);

            @Var Node srcNode = nodes.get(from);
            if (srcNode == null) {
              srcNode = new Node(from, fromLineNumber);
              nodes.put(from, srcNode);
              programGraph.addNode(srcNode);
            }

            @Var Node tgtNode = nodes.get(to);
            if (tgtNode == null) {
              tgtNode = new Node(to, toLineNumber);
              nodes.put(to, tgtNode);
              programGraph.addNode(tgtNode);
            }
            programGraph.addEdge(srcNode, tgtNode);
          }

          private int findLineNumber(final InsnList pInstructions, final int pStartPoint) {
            for (int i = pStartPoint; i >= 0; --i) {
              final AbstractInsnNode current = pInstructions.get(i);
              if (current instanceof LineNumberNode) {
                return ((LineNumberNode) current).line;
              }
            }
            return -1;
          }
        };

    analyzer.analyze(pOwningClass, pMethodNode);

    return programGraph;
  }

  /**
   * Computes the internal method name representation.
   *
   * @param pMethodNode The method node to get the information from
   * @return The internal method name representation
   */
  static String computeInternalMethodName(final MethodNode pMethodNode) {
    final String methodName = pMethodNode.name;
    final String descriptor = pMethodNode.desc;
    final String signature = pMethodNode.signature;
    final String[] exceptions;
    if (pMethodNode.exceptions.size() > 0) {
      exceptions = pMethodNode.exceptions.toArray(new String[0]);
    } else {
      exceptions = null;
    }
    return computeInternalMethodName(methodName, descriptor, signature, exceptions);
  }

  /**
   * Computes the internal method name representation.
   *
   * @param pMethodName The name of the method
   * @param pDescriptor The method's descriptor
   * @param pSignature The method's signature
   * @param pExceptions An array of exceptions thrown by the method
   * @return The internal method name representation
   */
  static String computeInternalMethodName(
      final String pMethodName,
      final String pDescriptor,
      final String pSignature,
      final String[] pExceptions) {
    final StringBuilder result = new StringBuilder();
    result.append(pMethodName);
    result.append(": ");
    result.append(pDescriptor);
    if (pSignature != null) {
      result.append("; ").append(pSignature);
    }
    if (pExceptions != null) {
      result.append("; ").append(Arrays.toString(pExceptions));
    }
    return result.toString();
  }
}

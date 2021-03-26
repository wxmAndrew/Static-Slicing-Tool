package de.uni_passau.fim.se2.slicer.analysis;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseFrame;
import br.usp.each.saeg.asm.defuse.Variable;
import java.util.Collection;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/** Provides a simple data-flow analysis. */
class DataFlowAnalysis {

  /** Prevent instantiation of utility class. */
  private DataFlowAnalysis() {}

  /**
   * Return the collection of variables that are used by the specified statement.
   *
   * @param pOwningClass The name of the owning class
   * @param pMethodNode The {@link MethodNode} of the method we analyse
   * @param pStatement The instruction node of the statement we analyse
   * @return A collection of {@link Variable}s that are used by the {@code statement}
   * @throws AnalyzerException In case an error occurs during analysis
   */
  static Collection<Variable> usedBy(
      final String pOwningClass, final MethodNode pMethodNode, final AbstractInsnNode pStatement)
      throws AnalyzerException {
    final DefUseAnalyzer analyzer = new DefUseAnalyzer();
    analyzer.analyze(pOwningClass, pMethodNode);

    final DefUseFrame[] frames = analyzer.getDefUseFrames();
    final int index = pMethodNode.instructions.indexOf(pStatement);

    return frames[index].getUses();
  }

  /**
   * Return the collection of variables that are defined by the specified statement.
   *
   * @param pOwingClass The name of the owning class
   * @param pMethodNode The {@link MethodNode} of the method we analyses
   * @param pStatement The instruction node of the statement we analyse
   * @return A collection of {@link Variable}s that are defined by the {@code statement}
   * @throws AnalyzerException In case an error occurs during analysis
   */
  static Collection<Variable> definedBy(
      final String pOwingClass, final MethodNode pMethodNode, final AbstractInsnNode pStatement)
      throws AnalyzerException {
    final DefUseAnalyzer analyzer = new DefUseAnalyzer();
    analyzer.analyze(pOwingClass, pMethodNode);

    final DefUseFrame[] frames = analyzer.getDefUseFrames();
    final int index = pMethodNode.instructions.indexOf(pStatement);

    return frames[index].getDefinitions();
  }
}

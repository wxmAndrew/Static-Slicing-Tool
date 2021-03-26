package de.uni_passau.fim.se2.slicer.analysis;

import com.google.errorprone.annotations.Var;
import de.uni_passau.fim.se2.slicer.util.cfg.CFGExtractor;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/** An abstract base class for analyses. */
public abstract class Analysis {

  final ProgramGraph controlFlowProgramGraph;

  final MethodNode methodNode;
  final ClassNode classNode;

  /**
   * Creates a new analysis instance.
   *
   * @param pClassNode The {@link ClassNode} to base the analysis on
   * @param pMethodNode The {@link MethodNode} of the method that should be analysed
   */
  Analysis(final ClassNode pClassNode, final MethodNode pMethodNode) {
    @Var ProgramGraph cfg = null;
    if (pClassNode == null) {
      // This should only happen under testing conditions.
      classNode = null;
      methodNode = null;
      controlFlowProgramGraph = null;
      return;
    }
    try {
      cfg = CFGExtractor.getCFG(pClassNode.name, pMethodNode);
    } catch (AnalyzerException e) {
      e.printStackTrace();
    }
    controlFlowProgramGraph = cfg;
    methodNode = pMethodNode;
    classNode = pClassNode;
  }

  Analysis(final ProgramGraph pCFG) {
    controlFlowProgramGraph = pCFG;
    methodNode = null;
    classNode = null;
  }

  /**
   * Mainly a testability method - returns the control flow graph.
   *
   * @return The control-flow graph
   */
  public ProgramGraph getControlFlowProgramGraph() {
    return controlFlowProgramGraph;
  }

  /**
   * Create a new graph object that returns a ProgramGraph representation of the results of the
   * analysis.
   *
   * @return A new graph object that is the result of the analysis
   */
  public abstract ProgramGraph computeResult();
}

package de.uni_passau.fim.se2.slicer.analysis;

import static com.google.common.truth.Truth.assert_;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.Var;

import de.uni_passau.fim.se2.slicer.util.cfg.CFGLocalVariableTableVisitor;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariable;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariableTable;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;


/**
 * Hard coded the calculator example to check backward slicing method 
 * */

 
class ProgramDependenceGraphTest {

	private String className = "de.uni_passau.fim.se2.examples.Calculator";
	private String methodName = "evaluate";
	private String methodDescriptor = "(Ljava/lang/String;)I" ;
	private int lineNumber = 8;
	private String variableName = "sum";
	private Map<String, LocalVariableTable> localVariableTables;
	private MethodNode methodNode;

	
	@Test
	void test_backwardSlice() throws Exception {
	    
		Set<Node> slicedNodes = executeSlicing();
		

		final Node n1 = new Node("LABEL1  line number: -1");
		final Node n2 = new Node("ISTORE4  line number: 6");
		final Node n3 = new Node("ISTORE15  line number: 7");
		final Node n4 = new Node("ISTORE13  line number: 7");
		final Node n5 = new Node("IINC39  line number: 7");
		final Node n6 = new Node("ASTORE10  line number: 7");
		final Node n7 = new Node("ASTORE29  line number: 7");
		final Node n8 = new Node("IF_ICMPGE20  line number: 7");
		final Node n9 = new Node("ISTORE36  line number: 8");
		final Node n10 = new Node("IINC39  line number: 7");
		
		Set<Node> ExpectedSlicedNodes = Sets.newHashSet();
		ExpectedSlicedNodes.add(n1);
		ExpectedSlicedNodes.add(n2);
		ExpectedSlicedNodes.add(n3);
		ExpectedSlicedNodes.add(n4);
		ExpectedSlicedNodes.add(n5);
		ExpectedSlicedNodes.add(n6);
		ExpectedSlicedNodes.add(n7);
		ExpectedSlicedNodes.add(n8);
		ExpectedSlicedNodes.add(n9);
		ExpectedSlicedNodes.add(n10);
		
		assert_().that(slicedNodes.toString().replaceAll("\\s+", "").equals((ExpectedSlicedNodes.toString().replaceAll("\\s+", ""))));

	}

	private Set<Node> executeSlicing() throws IOException {
		final ClassNode classNode = new ClassNode(Opcodes.ASM7);
		final ClassReader classReader = new ClassReader(className);
		classReader.accept(classNode, 0);

		final CFGLocalVariableTableVisitor localVariableTableVisitor = new CFGLocalVariableTableVisitor();
		classReader.accept(localVariableTableVisitor, 0);
		localVariableTables = localVariableTableVisitor.getLocalVariables();

		methodNode = classNode.methods.stream()
				.filter(method -> methodName.equals(method.name) && methodDescriptor.equals(method.desc)).findAny()
				.orElse(null);

		Preconditions.checkState(methodNode != null, "Could not find an appropriate method!");

		final ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
		final Node programLocation = getProgramLocation(pdg.getControlFlowProgramGraph(), methodNode,
				localVariableTables.get(methodName + ": " + methodDescriptor), lineNumber, variableName);

		return pdg.backwardSlice(programLocation);
	}

	private Node getProgramLocation(final ProgramGraph controlFlowProgramGraph, final MethodNode methodNode,
			final LocalVariableTable localVariableTable, final int lineNumber, final String variableName) {
		
	    @Var AbstractInsnNode searched = null;
	    for (final AbstractInsnNode insnNode : methodNode.instructions) {
	      if (insnNode instanceof LineNumberNode) {
	        if (((LineNumberNode) insnNode).line == lineNumber) {
	          searched = insnNode;
	          break;
	        }
	      }
	    }
	    @Var Node cfgNode = null;
	    if (searched != null) {
	      for (Node node : controlFlowProgramGraph.getNodes()) {
	        if (node.getInstruction().equals(searched)) {
	          cfgNode = node;
	          break;
	        }
	      }
	    }

	    for (Node successor : controlFlowProgramGraph.getSuccessorsUntilNextLineNumber(cfgNode)) {
	      if ((successor.getInstruction() instanceof VarInsnNode
	              || successor.getInstruction() instanceof IincInsnNode)
	          && isStoreOpCode(successor.getInstruction().getOpcode())) {
	        // For a local variable search in the local-variable table if we find a candidate
	        final int idx;
	        if (successor.getInstruction() instanceof VarInsnNode) {
	          idx = ((VarInsnNode) successor.getInstruction()).var;
	        } else {
	          idx = ((IincInsnNode) successor.getInstruction()).var;
	        }
	        final Optional<LocalVariable> entry = localVariableTable.getEntry(idx);
	        if (entry.isPresent() && entry.get().getName().equals(variableName)) {
	          return successor;
	        }
	      } else if (successor.getInstruction() instanceof FieldInsnNode
	          && (isFieldOpCode(successor.getInstruction().getOpcode()))) {
	        // For a field access compare the name directly
	        final String instructionVariableName = ((FieldInsnNode) successor.getInstruction()).name;
	        if (instructionVariableName != null && instructionVariableName.equals(variableName)) {
	          return successor;
	        }
	      }
	    }
	    throw new IllegalStateException(
	        "We were not able to determine a correct program location for the searched node");
	  }
	

	  private boolean isStoreOpCode(final int pOpCode) {
	    return pOpCode == Opcodes.AASTORE
	        || pOpCode == Opcodes.ASTORE
	        || pOpCode == Opcodes.SASTORE
	        || pOpCode == Opcodes.BASTORE
	        || pOpCode == Opcodes.CASTORE
	        || pOpCode == Opcodes.DASTORE
	        || pOpCode == Opcodes.DSTORE
	        || pOpCode == Opcodes.FASTORE
	        || pOpCode == Opcodes.FSTORE
	        || pOpCode == Opcodes.IASTORE
	        || pOpCode == Opcodes.ISTORE
	        || pOpCode == Opcodes.LASTORE
	        || pOpCode == Opcodes.LSTORE
	        || pOpCode == Opcodes.IINC;
	  }

	  private boolean isFieldOpCode(final int pOpCode) {
	    return pOpCode == Opcodes.PUTFIELD || pOpCode == Opcodes.PUTSTATIC;
	  }
}

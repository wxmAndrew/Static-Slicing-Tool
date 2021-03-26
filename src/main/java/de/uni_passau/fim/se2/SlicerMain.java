package de.uni_passau.fim.se2;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Var;
import de.uni_passau.fim.se2.slicer.analysis.ProgramDependenceGraph;
import de.uni_passau.fim.se2.slicer.util.cfg.CFGLocalVariableTableVisitor;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariable;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariableTable;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;
import de.uni_passau.fim.se2.slicer.util.output.ByteCodeExtractor;
import de.uni_passau.fim.se2.slicer.util.output.Extractor;
import de.uni_passau.fim.se2.slicer.util.output.SourceLineExtractor;
import de.uni_passau.fim.se2.slicer.util.output.XMLFileExtractor;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SlicerMain {
  private String className;
  private String methodName;
  private String methodDescriptor;
  private int lineNumber;
  private String variableName;
  private String sourceFilePath;
  private String targetFilePath;
  private boolean xmlExtraction = false;
  private Map<String, LocalVariableTable> localVariableTables;
  private MethodNode methodNode;

  public static void main(final String[] pArgs) throws IOException {
    SlicerMain slicerMain = new SlicerMain();
    slicerMain.parseArguments(pArgs);
    slicerMain.run();
  }

  /** Prevent initialisation of class */
  private SlicerMain() {}

  private void run() throws IOException {
    final Set<Node> backwardSlice = executeSlicing();

    final Extractor extractor;
    if (sourceFilePath != null) {
      extractor =
          new SourceLineExtractor(
              Paths.get(sourceFilePath), localVariableTables, className, methodNode, backwardSlice);
    } else if (xmlExtraction) {
      extractor = new XMLFileExtractor(backwardSlice);
    } else {
      extractor = new ByteCodeExtractor(backwardSlice);
    }

    if (targetFilePath == null) {
      System.out.println(extractor.extract());
    } else {
      extractor.extractToFile(Paths.get(targetFilePath));
    }
  }

  private Set<Node> executeSlicing() throws IOException {
    final ClassNode classNode = new ClassNode(Opcodes.ASM7);
    final ClassReader classReader = new ClassReader(className);
    classReader.accept(classNode, 0);

    final CFGLocalVariableTableVisitor localVariableTableVisitor =
        new CFGLocalVariableTableVisitor();
    classReader.accept(localVariableTableVisitor, 0);
    localVariableTables = localVariableTableVisitor.getLocalVariables();

    methodNode =
        classNode
            .methods
            .stream()
            .filter(
                method -> methodName.equals(method.name) && methodDescriptor.equals(method.desc))
            .findAny()
            .orElse(null);

    Preconditions.checkState(methodNode != null, "Could not find an appropriate method!");

    final ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
    final Node programLocation =
        getProgramLocation(
            pdg.getControlFlowProgramGraph(),
            methodNode,
            localVariableTables.get(methodName + ": " + methodDescriptor),
            lineNumber,
            variableName);

    return pdg.backwardSlice(programLocation);
  }

  private Node getProgramLocation(
      final ProgramGraph controlFlowProgramGraph,
      final MethodNode methodNode,
      final LocalVariableTable localVariableTable,
      final int lineNumber,
      final String variableName) {
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

  private void parseArguments(final String[] pArgs) {
    final Options options = new Options();

    final Option classNameOption = new Option("c", "class", true, "Path to the class file");
    classNameOption.setRequired(false);
    options.addOption(classNameOption);

    final Option variableNameOption = new Option("v", "variablename", true, "Name of variable");
    variableNameOption.setRequired(true);
    options.addOption(variableNameOption);

    final Option methodOption =
        new Option("m", "method", true, "Methodname and descriptor of the method");
    methodOption.setRequired(true);
    options.addOption(methodOption);

    final Option lineNumberOption =
        new Option("l", "linenumber", true, "Line number where to start the slice");
    methodOption.setRequired(true);
    options.addOption(lineNumberOption);

    final Option sourceFileOption =
        new Option("s", "sourcefile", true, "Path to the class file's source code");
    sourceFileOption.setRequired(false);
    options.addOption(sourceFileOption);

    final Option targetFileOption =
        new Option(
            "t", "targetfile", true, "Path to a target file where to write the slice code to");
    targetFileOption.setRequired(false);
    options.addOption(targetFileOption);

    final Option xmlExtractionOption =
        new Option("x", "xmlfile", false, "Extracts the result as an XML file for grading");
    xmlExtractionOption.setRequired(false);
    options.addOption(xmlExtractionOption);

    final CommandLineParser parser = new DefaultParser();
    final HelpFormatter formatter = new HelpFormatter();
    final CommandLine cmd;

    try {
      cmd = parser.parse(options, pArgs);
    } catch (final ParseException pE) {
      // TODO can we handle this more beautiful?
      System.err.println(pE.getMessage());
      formatter.printHelp("slicer", options);
      System.exit(1);
      return; // Should not be necessary but pacifies the IntelliJ inspections
    }

    variableName = cmd.getOptionValue("variablename");
    className = cmd.getOptionValue("class");
    final String[] methodInput = cmd.getOptionValue("method").split(":");
    methodName = methodInput[0];
    methodDescriptor = methodInput[1];
    lineNumber = Integer.parseInt(cmd.getOptionValue("linenumber"));
    if (cmd.hasOption("sourcefile")) {
      sourceFilePath = cmd.getOptionValue("sourcefile");
    }
    if (cmd.hasOption("targetfile")) {
      targetFilePath = cmd.getOptionValue("targetfile");
    }
    if (cmd.hasOption("xmlfile")) {
      xmlExtraction = true;
    }
  }
}

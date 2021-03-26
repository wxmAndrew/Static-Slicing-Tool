package de.uni_passau.fim.se2.slicer.util.output;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.Var;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariable;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariableTable;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class SourceLineExtractor implements Extractor {

  private final Path sourceFilePath;
  private final Map<String, LocalVariableTable> localVariableTables;
  private final String className;
  private final MethodNode methodNode;
  private final Set<Node> nodes;

  public SourceLineExtractor(
      final Path pSourceFilePath,
      final Map<String, LocalVariableTable> pLocalVariableTable,
      final String pClassName,
      final MethodNode pMethodNode,
      final Set<Node> pNodes) {
    sourceFilePath = pSourceFilePath;
    localVariableTables = pLocalVariableTable;
    className = pClassName;
    methodNode = pMethodNode;
    nodes = pNodes;
  }

  /** {@inheritDoc} */
  @Override
  public String extract() throws IOException {
    final List<String> lines = Lists.newArrayList();
    try (Stream<String> fileLines = Files.lines(sourceFilePath, Charset.defaultCharset())) {
      fileLines.forEach(lines::add);
    }

    final List<Node> sortedNodes = NodeSorter.sort(nodes);
    final StringBuilder builder = new StringBuilder();

    // if the first or second node is a LabelNode, it refers to a dependency on one of the parameter
    // variables
    if (sortedNodes.size() > 1
        && (sortedNodes.get(1).getInstruction() instanceof LabelNode
            || sortedNodes.get(0).getInstruction() instanceof LabelNode)) {
      builder.append(generateMethodDeclaration()).append("\n");
    }
    @Var int lastLineNumber = Integer.MIN_VALUE;
    for (Node node : sortedNodes) {
      final int lineNumber = node.getLineNumber();
      if (lineNumber > 1 && lineNumber != lastLineNumber) {
        final String line = lines.get(lineNumber - 1).trim();
        builder.append(line).append("\n");
      }
      lastLineNumber = lineNumber;
    }
    return builder.toString();
  }

  private String generateMethodDeclaration() {
    final LocalVariableTable localVariableTable =
        localVariableTables.get(methodNode.name + ": " + methodNode.desc);
    final Optional<LocalVariable> optionalFirstVariable = localVariableTable.getEntry(0);
    if (optionalFirstVariable.isEmpty()) {
      return "";
    }
    LocalVariable firstVariable = optionalFirstVariable.get();
    final boolean isVirtual =
        firstVariable.getName().equals("this")
            && firstVariable.getDescriptor().equals("L" + className.replace(".", "/") + ";");

    final String modifiers = getModifiers();

    final Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
    final String[] argumentTypeStrings = new String[argumentTypes.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      argumentTypeStrings[i] =
          replaceDescriptor(argumentTypes[i].toString())
              + " "
              + lookupLocalVariableName(argumentTypes[i], i, localVariableTable, isVirtual);
    }
    final Type returnType = Type.getReturnType(methodNode.desc);
    final String returnTypeString = replaceDescriptor(returnType.toString());

    final String arguments = String.join(", ", argumentTypeStrings);

    return String.format("%s%s %s(%s) {", modifiers, returnTypeString, methodNode.name, arguments);
  }

  private String lookupLocalVariableName(
      final Type pArgumentType,
      final int pIndex,
      final LocalVariableTable pLocalVariableTable,
      final boolean isVirtual) {
    final Optional<LocalVariable> variableOptional;
    if (isVirtual) {
      variableOptional = pLocalVariableTable.getEntry(pIndex + 1);
    } else {
      variableOptional = pLocalVariableTable.getEntry(pIndex);
    }
    Preconditions.checkState(variableOptional.isPresent());
    final LocalVariable variable = variableOptional.get();
    Preconditions.checkState(pArgumentType.toString().equals(variable.getDescriptor()));
    return variable.getName();
  }

  private String replaceDescriptor(final String pDescriptor) {
    final String descriptor = pDescriptor.replace("[", "");
    final int arrayLevel = pDescriptor.length() - descriptor.length();
    final StringBuilder result = new StringBuilder();

    switch (descriptor) {
      case "B":
        result.append("byte");
        break;
      case "C":
        result.append("char");
        break;
      case "D":
        result.append("double");
        break;
      case "F":
        result.append("float");
        break;
      case "I":
        result.append("int");
        break;
      case "J":
        result.append("long");
        break;
      case "S":
        result.append("short");
        break;
      case "Z":
        result.append("boolean");
        break;
      default:
        final String[] parts = descriptor.split("/");
        result.append(parts[parts.length - 1].replace(";", ""));
    }

    result.append("[]".repeat(Math.max(0, arrayLevel)));
    return result.toString();
  }

  private String getModifiers() {
    final int access = methodNode.access;
    final StringBuilder builder = new StringBuilder();
    if ((access & Opcodes.ACC_PUBLIC) != 0) {
      builder.append("public ");
    }
    if ((access & Opcodes.ACC_PRIVATE) != 0) {
      builder.append("private ");
    }
    if ((access & Opcodes.ACC_PROTECTED) != 0) {
      builder.append("protected ");
    }
    if ((access & Opcodes.ACC_FINAL) != 0) {
      builder.append("final ");
    }
    if ((access & Opcodes.ACC_STATIC) != 0) {
      builder.append("static ");
    }
    if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
      builder.append("synchronized ");
    }
    if ((access & Opcodes.ACC_VOLATILE) != 0) {
      builder.append("volatile ");
    }
    if ((access & Opcodes.ACC_TRANSIENT) != 0) {
      builder.append("transient ");
    }
    if ((access & Opcodes.ACC_ABSTRACT) != 0) {
      builder.append("abstract ");
    }
    if ((access & Opcodes.ACC_STRICT) != 0) {
      builder.append("strictfp ");
    }
    if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      builder.append("synthetic ");
    }
    if ((access & Opcodes.ACC_MANDATED) != 0) {
      builder.append("mandated ");
    }
    if ((access & Opcodes.ACC_ENUM) != 0) {
      builder.append("enum ");
    }

    return builder.toString();
  }
}

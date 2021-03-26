package de.uni_passau.fim.se2.slicer.util.cfg;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

/**
 * Implements a local variable table similar to the one of a Java class file.
 *
 * @see LocalVariable
 */
public class LocalVariableTable {

  private final Map<Integer, LocalVariable> localVariableTable;

  LocalVariableTable() {
    localVariableTable = Maps.newLinkedHashMap();
  }

  void addEntry(final int pIndex, final LocalVariable pLocalVariable) {
    if (!localVariableTable.containsKey(pIndex)) {
      localVariableTable.put(pIndex, pLocalVariable);
    }
  }

  public Optional<LocalVariable> getEntry(final int pIndex) {
    if (localVariableTable.containsKey(pIndex)) {
      return Optional.of(localVariableTable.get(pIndex));
    } else {
      return Optional.empty();
    }
  }
}

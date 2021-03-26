package de.uni_passau.fim.se2.slicer.util.cfg;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalVariableTableTest {

  private LocalVariable fooMock;
  private LocalVariable barMock;
  private LocalVariableTable table;

  @BeforeEach
  void setUp() {
    fooMock = mock(LocalVariable.class);
    barMock = mock(LocalVariable.class);
    table = new LocalVariableTable();
  }

  @Test
  void addEntryOnce() throws Exception {
    table.addEntry(42, fooMock);

    final Map<Integer, LocalVariable> variableTable = getLocalVariableMap();

    assert_()
        .withMessage("Expected one element in the table")
        .that(variableTable.size())
        .isEqualTo(1);
  }

  @Test
  void addEntryTwice() throws Exception {
    table.addEntry(42, fooMock);
    table.addEntry(42, fooMock);

    final Map<Integer, LocalVariable> variableTable = getLocalVariableMap();

    assert_()
        .withMessage("Expected one element in the table")
        .that(variableTable.size())
        .isEqualTo(1);
  }

  @Test
  void addTwoEntries() throws Exception {
    table.addEntry(42, fooMock);
    table.addEntry(23, barMock);

    final Map<Integer, LocalVariable> variableTable = getLocalVariableMap();

    assert_()
        .withMessage("Expected one element in the table")
        .that(variableTable.size())
        .isEqualTo(2);
  }

  @Test
  void getExistingEntry() {
    table.addEntry(42, fooMock);

    final Optional<LocalVariable> entry = table.getEntry(42);

    assert_().withMessage("No entry present").that(entry.isPresent()).isTrue();

    assert_().withMessage("Entry is not the expected one").that(entry.get()).isEqualTo(fooMock);
  }

  @Test
  void getNonExistingEntry() {
    table.addEntry(42, fooMock);

    final Optional<LocalVariable> entry = table.getEntry(23);

    assert_().withMessage("Unexpected entry present").that(entry.isPresent()).isFalse();

    assert_().withMessage("Entry is not the expected one").that(entry).isEqualTo(Optional.empty());
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, LocalVariable> getLocalVariableMap()
      throws NoSuchFieldException, IllegalAccessException {
    final Field tableField = table.getClass().getDeclaredField("localVariableTable");
    tableField.setAccessible(true);
    return (Map<Integer, LocalVariable>) tableField.get(table);
  }
}

package de.uni_passau.fim.se2.slicer.util.output;

import static com.google.common.truth.Truth.assert_;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.slicer.util.cfg.LocalVariableTable;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.MethodNode;

class SourceLineExtractorTest {

  private Set<Node> nodes;
  private Map<String, LocalVariableTable> localVariableTable;
  private String className;
  private MethodNode methodNode;

  @BeforeEach
  void setUp() {
    final Node firstNode = mock(Node.class);
    when(firstNode.getLineNumber()).thenReturn(16);
    final Node secondNode = mock(Node.class);
    when(secondNode.getLineNumber()).thenReturn(17);
    final Node dummyNode = mock(Node.class);
    when(dummyNode.getLineNumber()).thenReturn(-1);

    nodes = Sets.newHashSet();
    nodes.add(secondNode);
    nodes.add(firstNode);
    nodes.add(dummyNode);

    className = "TestDummy";
    localVariableTable = mockMap();
    methodNode = mock(MethodNode.class);
  }

  @Test
  void test_extract() throws IOException {
    final Extractor sourceLineExtractor =
        new SourceLineExtractor(
            Paths.get("src/test/java/de/uni_passau/fim/se2/slicer/fixtures/TestClassFixture.java"),
            localVariableTable,
            className,
            methodNode,
            nodes);
    final String expected = "arbitraryInt -= 15;\nnumFoos = numFoos + arbitraryInt;\n";

    assert_()
        .withMessage("Result does not match expected")
        .that(sourceLineExtractor.extract())
        .isEqualTo(expected);
  }

  @Test
  void test_extractToFile() throws IOException {
    final Extractor sourceLineExtractor =
        new SourceLineExtractor(
            Paths.get("src/test/java/de/uni_passau/fim/se2/slicer/fixtures/TestClassFixture.java"),
            localVariableTable,
            className,
            methodNode,
            nodes);
    final String expected = "arbitraryInt -= 15;\nnumFoos = numFoos + arbitraryInt;";

    final Path tempFile = Files.createTempFile(null, null);
    sourceLineExtractor.extractToFile(tempFile);
    final List<String> lines = Lists.newArrayList();
    try (Stream<String> lineStream = Files.lines(tempFile, Charset.defaultCharset())) {
      lineStream.forEach(lines::add);
    }

    assert_()
        .withMessage("Result does not match expected")
        .that(String.join("\n", lines))
        .isEqualTo(expected);
  }

  @SuppressWarnings("unchecked")
  private <Key, Value> Map<Key, Value> mockMap() {
    return mock(Map.class);
  }
}

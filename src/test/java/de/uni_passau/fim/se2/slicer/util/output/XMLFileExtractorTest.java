package de.uni_passau.fim.se2.slicer.util.output;

import static com.google.common.truth.Truth.assert_;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XMLFileExtractorTest {

  private Set<Node> nodes;

  @BeforeEach
  void setUp() {
    final Node firstNode = mock(Node.class);
    when(firstNode.getLineNumber()).thenReturn(16);
    when(firstNode.prettyPrint()).thenReturn("iload");
    when(firstNode.getID()).thenReturn("42");
    final Node secondNode = mock(Node.class);
    when(secondNode.getLineNumber()).thenReturn(17);
    when(secondNode.prettyPrint()).thenReturn("istore ");
    when(secondNode.getID()).thenReturn("23");
    final Node dummyNode = mock(Node.class);
    when(dummyNode.getLineNumber()).thenReturn(-1);
    when(dummyNode.prettyPrint()).thenReturn("DUMMY");
    when(dummyNode.getID()).thenReturn("");

    nodes = Sets.newHashSet();
    nodes.add(secondNode);
    nodes.add(firstNode);
    nodes.add(dummyNode);
  }

  @Test
  void test_extract() throws IOException {
    final Extractor xmlFileExtractor = new XMLFileExtractor(nodes);
    final String expected =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<report>\n"
            + "  <line nr=\"-1\" id=\"\" instruction=\"DUMMY\"/>\n"
            + "  <line nr=\"16\" id=\"42\" instruction=\"iload\"/>\n"
            + "  <line nr=\"17\" id=\"23\" instruction=\"istore\"/>\n"
            + "</report>\n";

    assert_()
        .withMessage("Result does not match expected")
        .that(xmlFileExtractor.extract())
        .isEqualTo(expected);
  }

  @Test
  void test_extractToFile() throws IOException {
    final Extractor xmlFileExtractor = new XMLFileExtractor(nodes);
    final String expected =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<report>\n"
            + "  <line nr=\"-1\" id=\"\" instruction=\"DUMMY\"/>\n"
            + "  <line nr=\"16\" id=\"42\" instruction=\"iload\"/>\n"
            + "  <line nr=\"17\" id=\"23\" instruction=\"istore\"/>\n"
            + "</report>";
    final Path tempFile = Files.createTempFile(null, null);
    xmlFileExtractor.extractToFile(tempFile);
    final List<String> lines = new ArrayList<>();
    try (Stream<String> lineStream = Files.lines(tempFile, Charset.defaultCharset())) {
      lineStream.forEach(lines::add);
    }

    assert_()
        .withMessage("Result does not match expected")
        .that(String.join("\n", lines))
        .isEqualTo(expected);
  }
}

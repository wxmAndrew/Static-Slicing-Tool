package de.uni_passau.fim.se2.slicer.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.Var;
import de.uni_passau.fim.se2.SlicerMain;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the test cases in the expected-results directory by parsing them and invoking the main
 * method.
 */
class ExpectedResultsTest {
  private static PrintStream sysOut;

  @BeforeAll
  static void beforeClass() {
    sysOut = System.out;
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Calculator.txt",
        "Complex.txt",
        "GCD.txt",
        "NestedLoop.txt",
        "Rational.txt",
        "SimpleInteger.txt",
        "TestClass.txt"
      })
  void test_expected_results(String filename) throws IOException {
    Path path = Paths.get("expected-results", filename);
    for (TestCase testCase : parseExpectedResult(path)) {
      sysOut.println();
      sysOut.println("Testing " + testCase.name);
      sysOut.println(String.join(" ", testCase.args));
      testMain(testCase);
    }
  }

  void testMain(TestCase testCase) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    SlicerMain.main(testCase.args);

    assertEquals(
        testCase.expected.replaceAll("\\s+", ""),
        out.toString(StandardCharsets.UTF_8).replaceAll("\\s+", ""));
  }

  Collection<TestCase> parseExpectedResult(Path path) throws IOException {
    final List<TestCase> testCases = Lists.newArrayList();
    final List<String> lines =
        Files.lines(path, Charset.defaultCharset()).collect(Collectors.toList());
    lines.add("");

    @Var boolean readingCommandLine = false;
    @Var boolean readingResult = false;

    @Var String[] args = null;
    @Var List<String> expected = Lists.newArrayList();
    @Var int num = 1;

    for (String line : lines) {
      if (readingCommandLine) {
        line = line.substring(line.lastIndexOf("jar") + 4);
        args = line.split("\\s+");
        readingCommandLine = false;
        continue;
      }

      if (readingResult) {
        if (line.isEmpty()) {
          testCases.add(new TestCase(path, num++, args, expected));
          args = null;
          expected = Lists.newArrayList();
          readingResult = false;
        } else {
          expected.add(line);
        }
        continue;
      }

      if (line.equalsIgnoreCase("Command line:")) {
        readingCommandLine = true;
      } else if (line.equalsIgnoreCase("Result:")) {
        readingResult = true;
      }
    }

    return testCases;
  }

  private static class TestCase {
    final String name;
    final String[] args;
    final String expected;

    TestCase(Path path, int num, String[] args, List<String> expected) {
      this.name = path + ":" + num;
      this.args = args;
      this.expected = String.join("\n", expected);
    }
  }
}

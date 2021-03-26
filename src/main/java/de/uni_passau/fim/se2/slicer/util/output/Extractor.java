package de.uni_passau.fim.se2.slicer.util.output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Defines an extractor that can be used to pretty print a set of {@link
 * de.uni_passau.fim.se2.slicer.util.cfg.Node}s.
 */
public interface Extractor {

  /**
   * Extracts the representation to a given file {@link Path}.
   *
   * @param pExtractionFile The {@link Path} to the result file
   * @throws IOException In case an IO error occurs
   */
  default void extractToFile(final Path pExtractionFile) throws IOException {
    Files.write(
        pExtractionFile, extract().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
  }

  /**
   * Extracts the representation to a {@link String} representation.
   *
   * @return A string representation
   * @throws IOException In case an IO error occurs
   */
  String extract() throws IOException;
}

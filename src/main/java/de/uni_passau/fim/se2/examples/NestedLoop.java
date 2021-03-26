package de.uni_passau.fim.se2.examples;

public class NestedLoop {

  public int nestedForLoops() {
    int result = 1;
    int rows = 5;
    for (int i = 0; i < rows; i++) {
      int rowResult = 1;
      for (int j = 0; j < i; j++) {
        rowResult += j;
      }
      result *= rowResult;
    }
    return result;
  }

  public int nestedWhileForLoops() {
    int result = 1;
    int rows = 5;
    int i = 0;
    while (i < rows) {
      int rowResult = 1;
      for (int j = 0; j < i; j++) {
        rowResult += j;
      }
      result *= rowResult;
      ++i;
    }
    return result;
  }

  public int loop() {
    int result = 0;
    for (int i = 0; i < 42; i++) {
      result += magic(i);
    }
    return result;
  }

  private int magic(final int pI) throws IllegalArgumentException {
    if (pI < 0) {
      throw new IllegalArgumentException("Thou shalt not put negative numbers!");
    }
    return pI % 3;
  }
}

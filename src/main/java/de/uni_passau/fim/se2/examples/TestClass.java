package de.uni_passau.fim.se2.examples;

public class TestClass {

  private String foo;

  public String removeSpaces(String input) {
    StringBuilder builder = new StringBuilder();

    for (char c : input.toCharArray()) {
      if (c != ' ') {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  public int countFoos(int input) {
    int numFoos = 0;
    int arbitraryInt = 5;
    arbitraryInt += 10;
    while (input > 0) {
      if (input % 3 == 0) {
        numFoos += 1;
      }
    }
    arbitraryInt -= 15;
    numFoos = numFoos + arbitraryInt;

    return numFoos;
  }

  public int countCharacters() {
    foo = "Foo";
    char[] chars = foo.toCharArray();
    foo = foo + "Bar";
    return chars.length;
  }
}

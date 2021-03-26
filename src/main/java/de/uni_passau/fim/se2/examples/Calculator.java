package de.uni_passau.fim.se2.examples;

public class Calculator {

  public int evaluate(final String pExpression) {
    int sum = 0;
    for (String summand : pExpression.split("\\+")) {
      sum += Integer.parseInt(summand);
    }
    return sum;
  }
}

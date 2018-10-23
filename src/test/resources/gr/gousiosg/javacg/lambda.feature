#Author: matthieu.vergne@gmail.com
Feature: Lambda
  I want to identify all lambdas within the analyzed code.

  Background: 
    # Introduce the lambda we will use
    Given I have the class "Runner" with code:
      """
      @FunctionalInterface
      public interface Runner {
       public void run();
      }
      """

  Scenario: Retrieve lambda in method
    Given I have the class "LambdaTest" with code:
      """
      public class LambdaTest {
       public void methodA() {
        Runner r = () -> methodB();
        r.run();
       }
       
       public void methodB() {}
      }
      """
    When I run the analyze
    # Creation of r in methodA
    Then the result should contain:
      """
      M:LambdaTest:methodA() (D)Runner:run(LambdaTest)
      """
    # Call of methodB in r
    And the result should contain:
      """
      M:LambdaTest:lambda$methodA$0() (M)LambdaTest:methodB()
      """

  Scenario: Retrieve nested lambdas
    Given I have the class "NestedLambdaTest" with code:
      """
      public class NestedLambdaTest {
       public void methodA() {
        Runner r = () -> {
         Runner r2 = () -> {
          Runner r3 = () -> methodB();
          r3.run();
         };
         r2.run();
        };
        r.run();
       }
       
       public void methodB() {}
      }
      """
    When I run the analyze
    # Creation of r in methodA
    Then the result should contain:
      """
      M:NestedLambdaTest:methodA() (D)Runner:run(NestedLambdaTest)
      """
    # Creation of r2 in r
    And the result should contain:
      """
      M:NestedLambdaTest:lambda$methodA$2() (D)Runner:run(NestedLambdaTest)
      """
    # Creation of r3 in r2
    And the result should contain:
      """
      M:NestedLambdaTest:lambda$lambda$methodA$2$1() (D)Runner:run(NestedLambdaTest)
      """
    # Call of methodB in r3
    And the result should contain:
      """
      M:NestedLambdaTest:lambda$lambda$lambda$methodA$2$1$0() (M)NestedLambdaTest:methodB()
      """

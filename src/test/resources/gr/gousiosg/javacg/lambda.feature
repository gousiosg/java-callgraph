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

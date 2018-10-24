#Author: matthieu.vergne@gmail.com
Feature: Interface
  I want to identify all interface methods within the analyzed code.

  Scenario: Retrieve interface method call
    Given I have the class "MyInterface" with code:
      """
      public interface MyInterface {
       public void doSomething();
      }
      """
    Given I have the class "InterfaceTest" with code:
      """
      public class InterfaceTest {
       public void execute(MyInterface x) {
        x.doSomething();
       }
      }
      """
    When I run the analyze
    Then the result should contain:
      """
      M:InterfaceTest:execute(MyInterface) (I)MyInterface:doSomething()
      """
#Author: matthieu.vergne@gmail.com
Feature: Abstract
  I want to identify all abstract methods within the analyzed code.

  Scenario: Retrieve abstract method call
    Given I have the class "MyAbstract" with code:
      """
      public abstract class MyAbstract {
       public abstract void doSomething();
      }
      """
    Given I have the class "AbstractTest" with code:
      """
      public class AbstractTest {
       public void execute(MyAbstract x) {
        x.doSomething();
       }
      }
      """
    When I run the analyze
    Then the result should contain:
      """
      M:AbstractTest:execute(MyAbstract) (M)MyAbstract:doSomething()
      """
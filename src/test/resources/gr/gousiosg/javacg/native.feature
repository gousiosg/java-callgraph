#Author: matthieu.vergne@gmail.com
Feature: Native
  I want to identify all native methods within the analyzed code.

  Scenario: Retrieve native method call
    Given I have the class "NativeTest" with code:
      """
      public class NativeTest {
       public void methodA() {
        methodB();
       }

       public native void methodB();
      }
      """
    When I run the analyze
    Then the result should contain:
      """
      M:NativeTest:methodA() (M)NativeTest:methodB()
      """
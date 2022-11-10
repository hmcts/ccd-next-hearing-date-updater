#====================================
@F-001
Feature: F-001: Update cases in CSV file
#====================================

  Background:
    Given an appropriate test context as detailed in the test data source
     And a successful call [to check the health of datastore] as in [Check_Datastore_Health]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.1 #HMAN-319 #AC01
  Scenario: CSV file contains only valid case references

    Given a successful call [to create test cases] as in [F-001-CreateTestCases]
      And the test csv contains case references from "F-001-CreateTestCases"

     When the next hearing date update job executes with maximum CSV limit "10000"

     Then a success exit value is received
      And a successful call [to verify next hearing date for Case1] as in [F-001_Verify_Case1]
      And a successful call [to verify next hearing date for Case2] as in [F-001_Verify_Case2]
      And a successful call [to verify next hearing date for Case3] as in [F-001_Verify_Case3]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.2 #HMAN-319 #AC02
  Scenario: CSV file contains only valid case references but too many for mamximum CSV limit

    Given a successful call [to create test cases] as in [F-001-CreateTestCases]
      And the test csv contains case references from "F-001-CreateTestCases"

     When the next hearing date update job executes with maximum CSV limit "2"

     Then a non-success exit value is received
      And the following response is logged as output: "001 More than 2 references in CSV"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.3 #HMAN-319 #AC03
  Scenario: CSV file contains a mix of valid and invalid case references
    Given a successful call [to create test cases] as in [F-001-CreateTestCases]
      And the test csv contains case references from "F-001-CreateTestCases" plus the following extra case references: "1111222233334449,3162255313,,not-a-number"

     When the next hearing date update job executes with maximum CSV limit "10000"

     Then a success exit value is received
      And the following response is logged as output: "002 Invalid Case Reference number '1111222233334449' in CSV"
      And the following response is logged as output: "002 Invalid Case Reference number '3162255313' in CSV"
      And the following response is logged as output: "002 Invalid Case Reference number '' in CSV"
      And the following response is logged as output: "002 Invalid Case Reference number 'not-a-number' in CSV"
      And a successful call [to verify next hearing date for Case1] as in [F-001_Verify_Case1]
      And a successful call [to verify next hearing date for Case2] as in [F-001_Verify_Case2]
      And a successful call [to verify next hearing date for Case3] as in [F-001_Verify_Case3]


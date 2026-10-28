#====================================
@F-001
Feature: F-001: Update cases in CSV file
#====================================

  Background:
    Given an appropriate test context as detailed in the test data source
     And a successful call [to register the existing ScenarioContext with NHDU BEAN_FACTORY] as in [Trigger_TestHookHandler]


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.00 #HMAN-319 #extra
  Scenario: CSV file is empty

    Given the test csv is empty

     When the next hearing date update job executes for CSV

     Then a success exit value is received
      And no WARN or ERROR logged in output
      And the following response is logged as output: "No Case References found to be processed"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.01 #HMAN-319 #AC01 #HMAN-322 #AC01
  Scenario: CSV file contains only valid case references

    Given a successful call [to create test cases] as in [F-001-CreateTestCases]
      And the test csv contains case references from "F-001-CreateTestCases"

     When the next hearing date update job executes for CSV with maximum CSV limit "10000"

     Then a success exit value is received
      And no WARN or ERROR logged in output
      And a successful call [to verify next hearing date for Case1] as in [F-001_Verify_Case1]
      And a successful call [to verify next hearing date for Case2] as in [F-001_Verify_Case2]
      And a successful call [to verify next hearing date for Case3] as in [F-001_Verify_Case3]


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.02 #HMAN-319 #AC02
  Scenario: CSV file contains only valid case references but too many for mamximum CSV limit

    Given the test csv contains case references: "4444333322221111,4444222233331111,4444111122223333"

     When the next hearing date update job executes for CSV with maximum CSV limit "2"

     Then a non-success exit value is received
      And the following response is logged as output: "001 More than 2 references in CSV"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.03 #HMAN-319 #AC03
  Scenario: CSV file contains a mix of valid and invalid case references
    Given a successful call [to create test cases] as in [F-001-CreateTestCases]
      And the test csv contains case references from "F-001-CreateTestCases" plus the following extra case references: "1111222233334449,3162255313,,not-a-number"

     When the next hearing date update job executes for CSV with maximum CSV limit "10000"

     Then a success exit value is received
      And the following response is logged as output: "002 Invalid Case Reference number '1111222233334449' in CSV"
      And the following response is logged as output: "002 Invalid Case Reference number '3162255313' in CSV"
      And the following response is logged as output: "002 Invalid Case Reference number '' in CSV"
      And the following response is logged as output: "002 Invalid Case Reference number 'not-a-number' in CSV"
      And a successful call [to verify next hearing date for Case1] as in [F-001_Verify_Case1]
      And a successful call [to verify next hearing date for Case2] as in [F-001_Verify_Case2]
      And a successful call [to verify next hearing date for Case3] as in [F-001_Verify_Case3]


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.12 #HMAN-322 #AC02
  Scenario: CSV file contains valid case reference but StartEvent has error

    Given a successful call [to create test cases] as in [F-001-CreateTestCases_Bad_StartEvent]
      And the test csv contains case references from "F-001-CreateTestCases_Bad_StartEvent"

     When the next hearing date update job executes for CSV

     Then the following response is logged as output: "Call to following downstream CCD endpoint failed: /cases/[0-9]*/event-triggers/UpdateNextHearingInfo"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.13 #HMAN-322 #AC03
  Scenario: CSV file contains valid case reference but start event will set NextHearingDate in the past

    Given a successful call [to create test cases] as in [F-001-CreateTestCases_Bad_DatePast]
      And the test csv contains case references from "F-001-CreateTestCases_Bad_DatePast"

     When the next hearing date update job executes for CSV

     Then the following response is logged as output: "003 hearingDateTime set is in the past '[0-9]*'"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.14 #HMAN-322 #AC04
  Scenario: CSV file contains valid case reference but start event will set NextHearingDate to null

    Given a successful call [to create test cases] as in [F-001-CreateTestCases_Bad_DateNull]
      And the test csv contains case references from "F-001-CreateTestCases_Bad_DateNull"

     When the next hearing date update job executes for CSV

     Then the following response is logged as output: "004 hearingDateTime set is null '[0-9]*'"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.15 #HMAN-322 #AC05
  Scenario: CSV file contains valid case reference but start event will set HearingID to null

    Given a successful call [to create test cases] as in [F-001-CreateTestCases_Bad_IDNull]
      And the test csv contains case references from "F-001-CreateTestCases_Bad_IDNull"

     When the next hearing date update job executes for CSV

     Then the following response is logged as output: "005 hearingID set is null '[0-9]*'"


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.16 #HMAN-322 #AC06
  Scenario: CSV file contains valid case reference and start event will set both NextHearingDate and HearingID to null

    Given a successful call [to create test cases] as in [F-001-CreateTestCases_Clear]
      And the test csv contains case references from "F-001-CreateTestCases_Clear"

     When the next hearing date update job executes for CSV

     Then a success exit value is received
      And no WARN or ERROR logged in output
      And a successful call [to verify next hearing date for Case1 has been cleared] as in [F-001_Verify_Case1_Cleared]


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.17 #HMAN-322 #AC07
  Scenario: CSV file contains valid case reference but SubmitEvent has error

    Given a successful call [to create test cases] as in [F-001-CreateTestCases_Bad_SubmitEvent]
      And the test csv contains case references from "F-001-CreateTestCases_Bad_SubmitEvent"

     When the next hearing date update job executes for CSV

     Then the following response is logged as output: "Call to following downstream CCD endpoint failed: /cases/[0-9]*/events"


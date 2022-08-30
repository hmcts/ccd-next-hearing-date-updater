#====================================
@F-001
Feature: F-001: Update cases in CSV file
#====================================

  Background:
    Given an appropriate test context as detailed in the test data source
    And a successful call [to check the health of datastore] as in [Check_Datastore_Health]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-001.1
  Scenario: CSV file contains only valid case references
    Given a successful call [to create test cases] as in [CreateTestCases]
    And the test csv contains case references from "CreateTestCases"
    When  the next hearing date update job executes
    Then  a successful call [to verify next hearing date for Case1] as in [F-001_Verify_Case1]
    Then  a successful call [to verify next hearing date for Case2] as in [F-001_Verify_Case2]
    Then  a successful call [to verify next hearing date for Case3] as in [F-001_Verify_Case3]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @Ignore
  @S-001.2
  Scenario: CSV file contains a mix of valid and invalid case references
    Given a successful call [to create test cases] as in [CreateTestCases]
#    And a wait time of [5] seconds [to allow for Logstash to index the case just created]
#    When  the next hearing date update job executes
#    Then  a successful call [to verify] as in [F-001_Verify]

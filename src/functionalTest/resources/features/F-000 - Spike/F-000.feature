#====================================
@F-000
Feature: F-000: Test Operation
#====================================

  Background:
    Given an appropriate test context as detailed in the test data source
    And a successful call [to check the health of datastore] as in [Check_Datastore_Health]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-000.1
  Scenario: A spike scenario-1
    Given a successful call [to create test cases] as in [CreateTestCases]
    And the test csv contains case references from "CreateTestCases"
    When  the next hearing date update job executes
    Then  a successful call [to verify] as in [F-000_Verify]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-000.2
  Scenario: A spike scenario-2
    Given a successful call [to create test cases] as in [CreateTestCases]
    And the cases created by "CreateTestCases" are indexed in elasticsearch
    When  the next hearing date update job executes
    Then  a successful call [to verify] as in [F-000_Verify]

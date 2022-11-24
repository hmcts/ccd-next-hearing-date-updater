#====================================
@F-002
Feature: F-002: Update cases in Elasticsearch
#====================================

  Background:
    Given an appropriate test context as detailed in the test data source
      And a successful call [to check the health of datastore] as in [Check_Datastore_Health]

#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-002.1
  Scenario: Update cases in Elasticsearch

    Given a successful call [to create test cases] as in [F-002-CreateTestCases]
      And the test csv is empty

     When the next hearing date update job executes for "FT_NextHearingDate"

     Then a success exit value is received
      And the following response is logged as output: "search for case type FT_NextHearingDate"
      And the following response is logged as output: "No CSV file specified"
      And a successful call [to verify next hearing date for Case1] as in [F-002_Verify_Case1]
      And a successful call [to verify next hearing date for Case2] as in [F-002_Verify_Case2]
      And a successful call [to verify next hearing date for Case3] as in [F-002_Verify_Case3]

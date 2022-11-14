#====================================
@F-002
Feature: F-002: Update cases in Elasticsearch
#====================================

  Background:
    Given an appropriate test context as detailed in the test data source
      And a successful call [to check the health of datastore] as in [Check_Datastore_Health]


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-002.01 #HMAN-320 #AC01
  Scenario: Update cases in Elasticsearch: using Paginated result of cases that need NextHearing date to be set

    Given a successful call [to create test cases] as in [F-002-Create5TestCases]
      And a wait time of [5] seconds [to allow for Logstash to re-index the case]

     When the next hearing date update job executes for "FT_NextHearingDate,FT_NextHearingDate_Clear" with pagination size "2"

     Then a success exit value is received
      And the following response is logged as output: "No CSV file specified"
      And the following response is logged as output: "search for case type FT_NextHearingDate"
      And the following response is logged as output: "The Next-Hearing-Date-Updater has processed caseDetails 3"
      And the following response is logged as output: "search for case type FT_NextHearingDate_Clear"
      And the following response is logged as output: "The Next-Hearing-Date-Updater has processed caseDetails 2"
      And a successful call [to verify next hearing date for Case1] as in [F-002_Verify_Case1Of5]
      And a successful call [to verify next hearing date for Case2] as in [F-002_Verify_Case2Of5]
      And a successful call [to verify next hearing date for Case3] as in [F-002_Verify_Case3Of5]
      And a successful call [to verify next hearing date for Case4] as in [F-002_Verify_Case4Of5]
      And a successful call [to verify next hearing date for Case5] as in [F-002_Verify_Case5Of5]


#-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @S-002.02 #HMAN-320 #AC02
  Scenario: Update cases in Elasticsearch: Elastic Search returns no cases

     When the next hearing date update job executes for "FT_NextHearingDate"

     Then a success exit value is received
      And the following response is logged as output: "No CSV file specified"
      And the following response is logged as output: "search for case type FT_NextHearingDate"
      And the following response is logged as output: "The Next-Hearing-Date-Updater has processed caseDetails 0"
      And the following response is logged as output: "No Case References found to be processed"


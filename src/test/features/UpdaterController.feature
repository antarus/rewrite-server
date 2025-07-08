Feature: Test datastore part

  Scenario: I update a repository
    When I update repository "904d1170-4bd7-3324-8139-95115b43bbe4" with recipe ""
    Then Http code must be 202

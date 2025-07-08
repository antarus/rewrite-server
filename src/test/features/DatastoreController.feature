Feature: Test datastore part

  Scenario: I create a datastore
    When I create a datastore for "https://github.com/jhipster/jhipster-lite"
    Then Http code must be 202
    Then I should have a bean
      | uuid | 5b4fc197-4dce-3b65-a8d1-7845f8c12a33 |

  Scenario: I create a datastore that already exists
    When I create a datastore for "https://github.com/jhipster/jhipster-lite"
    Then Http code must be 400

  Scenario: I am requesting information for an existing datastore
    When I am requesting information from an datastore "904d1170-4bd7-3324-8139-95115b43bbe4"
    Then Http code must be 200
    Then I should have a bean
      | datastoreId.uuid  | 904d1170-4bd7-3324-8139-95115b43bbe4     |
      | repositoryURL.url | https://github.com/antarus/jhipster-lite |

  Scenario: I am requesting information for non-existent datastore
    When I am requesting information from an datastore "904d1170-4bd7-3324-8139-95115b43baaa"
    Then Http code must be 400

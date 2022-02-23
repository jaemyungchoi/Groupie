Feature: Block list
  Scenario: Show blocked users
    Given I log in with username "__TEST__block_list_feature_3" and password "abc"
    When I go to the block list page
    Then I should see "__TEST__block_list_feature_1" in my block list
    And I should see "__TEST__block_list_feature_2" in my block list

  Scenario: Block valid user
    Given I log in with username "__TEST__block_list_feature_1" and password "test"
    When I go to the block list page
    And I input "__TEST__block_list_feature_2" in the field "John Doe"
    And I click button "Add"
    Then I should see "__TEST__block_list_feature_2" in my block list

  Scenario: Block invalid user
    Given I log in with username "__TEST__block_list_feature_1" and password "test"
    When I go to the block list page
    And I input "__TEST__block_list_feature_404" in the field "John Doe"
    And I click button "Add"
    Then I should see error message "Invalid username: __TEST__block_list_feature_404"

  Scenario: Unblock user
    Given I log in with username "__TEST__block_list_feature_2" and password "321"
    When I go to the block list page
    And I click delete button next to "__TEST__block_list_feature_1"
    Then I should not see "__TEST__block_list_feature_1" in my block list

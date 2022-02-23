Feature: User sign up
  Scenario: Valid username + password
    Given I am on the sign up page
    When I input "__TEST__user1" in the field "Name"
    And I input "123" in the field "Password"
    And I input "123" in the field "Confirm Password"
    And I click button "Create User"
    Then I should see text "My Proposals"
    
  Scenario: Empty username
    Given I am on the sign up page
    When I input "" in the field "Name"
    And I input "123" in the field "Password"
    And I input "123" in the field "Confirm Password"
    And I click button "Create User"
    Then I should see error message "Username cannot be empty."

  Scenario: Empty password
    Given I am on the sign up page
    When I input "__TEST__some_user" in the field "Name"
    And I input "" in the field "Password"
    And I input "" in the field "Confirm Password"
    And I click button "Create User"
    Then I should see error message "Password cannot be empty."

  Scenario: Password mismatch
    Given I am on the sign up page
    When I input "__TEST__some_user" in the field "Name"
    And I input "321" in the field "Password"
    And I input "123" in the field "Confirm Password"
    And I click button "Create User"
    Then I should see error message "Passwords must match."

  Scenario: Username taken
    Given I am on the sign up page
    When I input "__TEST__existing_user" in the field "Name"
    And I input "321" in the field "Password"
    And I input "321" in the field "Confirm Password"
    And I click button "Create User"
    Then I should see error message "Username already in use. Please choose another one."

  Scenario: Cancel
    Given I am on the sign up page
    And I click button "Cancel"
    Then I should see text "Log In"
Feature: LogIn
  Scenario: No inputted username or password
    Given I am on the index page
    When I click button "Log In"
    Then I should see text "Username cannot be empty"
    And I should see text "Password cannot be empty"

  Scenario: Only username inputted
    Given I am on the index page
    When I input "Test_Username" in the field "Enter Username"
    And I click button "Log In"
    Then I should see text "Password cannot be empty"

  Scenario: Only password inputted
    Given I am on the index page
    When I input "Test_Password" in the field "Enter Password"
    And I click button "Log In"
    Then I should see text "Username cannot be empty"

  Scenario: Invalid username and some password inputted
    Given I am on the index page
    When I input "Test_Username" in the field "Enter Username"
    And I input "Test_Password" in the field "Enter Password"
    And I click button "Log In"
    Then I should see text "Username does not exist"

  Scenario: Valid username and incorrect password inputted
    Given I am on the index page
    When I input "__TEST__existing_user" in the field "Enter Username"
    And I input "Test_Password" in the field "Enter Password"
    And I click button "Log In"
    Then I should see text "Incorrect Password"

  Scenario: Valid username and correct password inputted
    Given I am on the index page
    When I input "__TEST__existing_user" in the field "Enter Username"
    And I input "123" in the field "Enter Password"
    And I click button "Log In"
    Then I should see text "My Proposals"

  Scenario: Too many attempts
    Given I am on the index page
    When I log in with username "__TEST__lockout_user" and password "321"
    When I log in with username "__TEST__lockout_user" and password "abc"
    When I log in with username "__TEST__lockout_user" and password "???"
    # 3 bad attempts lock you out for 1 minute
    When I log in with username "__TEST__lockout_user" and password "123"
    # now you can't make more tries (even if the password is correct)
    Then I should see text "Too many attempts. You have been locked out."
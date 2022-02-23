Feature: Logout
  Scenario: Users can logout
    Given I log in with username "__TEST__existing_user" and password "123"
    When I click button "Log Out"
    # user gets logged out and redirected to the log in page
    Then I should see text "Log In"


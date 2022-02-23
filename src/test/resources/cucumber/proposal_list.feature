Feature: Proposal List page
  Scenario: Proposal status and user response
    Given I log in with username "__TEST__invite_bob" and password "bob"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see "__TEST__proposal_list" marked as "finalized"
#    And I wait
    And I should see "__TEST__invite_alice" in "Accepted Users" under "__TEST__proposal_list"
    And I should see "__TEST__invite_charlie" in "Declined Users" under "__TEST__proposal_list"
    And I should see "__TEST__invite_bob" in "Undecided Users" under "__TEST__proposal_list"

  Scenario: View my preferences for proposal
    Given I log in with username "__TEST__invite_bob" and password "bob"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I click the proposal title "__TEST__proposal_list"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see dropdown "Can Attend?" has value "Yes" under event "Phoenix Suns vs. Orlando Magic"
    And I should see dropdown "Excited?" has value "Meh" under event "Phoenix Suns vs. Orlando Magic"

  Scenario: Default proposal ordering
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see proposals ordered from newest to oldest

  Scenario: Alternative proposal ordering
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I click "oldest proposal first" toggle
    Then I should see proposals ordered from oldest to newest

  Scenario: Calendar no filtering - proposal in progress
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I click button "Day" on the calendar's toolbar
    # proposals that are not finalized appear on the day that they are created
    # the calendar defaults to "today"
    Then I should see proposal "__TEST__proposal_calendar_not_final" on the calendar

  Scenario: Calendar no filtering - finalized proposals
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I click button "Day" on the calendar's toolbar
    And I forward the calendar to "Saturday Feb 12"
    # finalized proposals appear on the same day as the best events
    Then I should see proposal "__TEST__proposal_calendar_final_no_response" on the calendar
    And I should see proposal "__TEST__proposal_calendar_final_accept" on the calendar

  Scenario: Calendar finalized only
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "Finalized only" in the "Filter by Proposal State" dropdown
    And I click button "Day" on the calendar's toolbar
    Then I should not see proposal "__TEST__proposal_calendar_not_final" on the calendar

  Scenario: Calendar in progress only
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "In progress only" in the "Filter by Proposal State" dropdown
    And I click button "Day" on the calendar's toolbar
    And I forward the calendar to "Saturday Feb 12"
    Then I should not see proposal "__TEST__proposal_calendar_final_no_response" on the calendar
    And I should not see proposal "__TEST__proposal_calendar_final_accept" on the calendar

  Scenario: Calendar responded only
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "Responded only" in the "Filter by My Response" dropdown
    And I click button "Day" on the calendar's toolbar
    And I forward the calendar to "Saturday Feb 12"
    Then I should not see proposal "__TEST__proposal_calendar_final_no_response" on the calendar

  Scenario: Calendar not responded only
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the proposal list page
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "Not responded only" in the "Filter by My Response" dropdown
    And I click button "Day" on the calendar's toolbar
    And I forward the calendar to "Saturday Feb 12"
    Then I should not see proposal "__TEST__proposal_calendar_final_accept" on the calendar



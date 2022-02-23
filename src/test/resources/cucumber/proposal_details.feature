Feature: Proposal details
  Scenario: Delete proposal
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__delete_proposal"
    And I click button "Delete Proposal"
    And I click button "OK"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "Proposal Deleted"

  Scenario: Delete event 1 event
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__delete_event_1_event"
    And I wait for things to load (time limit "10000" milliseconds)
    And I click the delete button next to event "Phoenix Suns vs. Orlando Magic"
    And I click button "OK"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "Proposal Deleted"

  Scenario: Delete event 2 event
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__delete_event_2_event"
    And I wait for things to load (time limit "10000" milliseconds)
    And I click the delete button next to event "Phoenix Suns vs. Orlando Magic"
    And I click button "OK"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should not see event "Phoenix Suns vs. Orlando Magic"

  Scenario: Delete user 1 user
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__delete_user_1_user"
    And I click the delete icon next to user "__TEST__invite_bob"
    And I click button "OK"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "Proposal Deleted"

  Scenario: Delete user 2 user
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__delete_user_2_user"
    And I click the delete icon next to user "__TEST__invite_bob"
    And I click button "OK"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should not see "__TEST__invite_bob" under "Invited Users"

  Scenario: Save preference draft
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__save_draft"
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "Yes" in dropdown "Can Attend?" under event "Phoenix Suns vs. Orlando Magic"
    And I click button "Save As Draft"
    And I wait for things to load (time limit "10000" milliseconds)
    # make sure the save is persistent
    And I refresh the page
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see dropdown "Can Attend?" has value "Yes" under event "Phoenix Suns vs. Orlando Magic"
    # draft doesn't count towards the result
    And I should see "Phoenix Suns vs. Orlando Magic" has yes count "0"
    And I should see "Phoenix Suns vs. Orlando Magic" has no count "0"
    And I should see "Phoenix Suns vs. Orlando Magic" has maybe count "0"
    And I should see "Phoenix Suns vs. Orlando Magic" has average excitement score "?"

  Scenario: Commit preference incomplete
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__commit_preference_incomplete"
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "Yes" in dropdown "Can Attend?" under event "Phoenix Suns vs. Orlando Magic"
    And I click button "Commit Preferences"
    Then I should see text "Please fill out preferences for all events."

  Scenario: Commit preference
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__commit_preference"
    And I wait for things to load (time limit "10000" milliseconds)
    And I select "Yes" in dropdown "Can Attend?" under event "Phoenix Suns vs. Orlando Magic"
    And I select "Meh" in dropdown "Excited?" under event "Phoenix Suns vs. Orlando Magic"
    And I click button "Commit Preferences"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "Your preferences have been recorded."
    And I should see "Phoenix Suns vs. Orlando Magic" has yes count "1"
    And I should see "Phoenix Suns vs. Orlando Magic" has no count "0"
    And I should see "Phoenix Suns vs. Orlando Magic" has maybe count "0"
    And I should see "Phoenix Suns vs. Orlando Magic" has average excitement score "3"

  Scenario: Reject proposal
    Given I log in with username "__TEST__invite_bob" and password "bob"
    When I go to details page for proposal "__TEST__reject_proposal"
    And I click button "Reject Proposal"
    And I click button "OK"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "Proposal Rejected"

  Scenario: Accept proposal
    Given I log in with username "__TEST__invite_bob" and password "bob"
    When I go to details page for proposal "__TEST__accept_proposal"
    And I click button "Accept"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see "__TEST__invite_bob" under "Accepted Users"

  Scenario: Decline proposal
    Given I log in with username "__TEST__invite_bob" and password "bob"
    When I go to details page for proposal "__TEST__decline_proposal"
    And I click button "Decline"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see "__TEST__invite_bob" under "Declined Users"

  Scenario: Accept then decline proposal
    Given I log in with username "__TEST__invite_bob" and password "bob"
    When I go to details page for proposal "__TEST__accept_decline_proposal"
    And I click button "Accept"
    And I wait for things to load (time limit "10000" milliseconds)
    And I click button "Update & Decline"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see "__TEST__invite_bob" under "Declined Users"
    And I should not see "__TEST__invite_bob" under "Accepted Users"

  Scenario: Finalize proposal
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to details page for proposal "__TEST__finalize_proposal"
    And I click button "Finalize"
    And I wait for things to load (time limit "10000" milliseconds)
    # Crash Test Dummies has more Yes votes
    Then I should see "Crash Test Dummies" selected as the best event
    And I should see "Crash Test Dummies" links to a Ticketmaster detail page
    # time & location
    And I should see "Crash Test Dummies" has meta "Jun 9th 2022, 11:00 am@Saint Luke's,Glasgow"
    And I should see "Crash Test Dummies" has average excitement score "2"
    # can no longer interact with the proposal
    And I should not see the "Commit Preferences" Button
    And I should not see the "Save As Draft" Button
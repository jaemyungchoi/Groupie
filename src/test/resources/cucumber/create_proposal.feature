Feature: Create proposal
  Scenario: Event search - USC sports game at LA next year
    Given I log in with username "__TEST__existing_user" and password "123"
    When I go to the create proposal page
    And I input "USC" in the field "Any Event"
    And I input "Los Angeles" in the field "Any City"
    And I input "01/01/2022" in the field "Any Start Date"
    And I input "12/31/2022" in the field "Any End Date"
    And I select "Sports" in the "Genre" dropdown
    And I click button "Search"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see at least one USC related event and its title links to a Ticketmaster event page

  Scenario: Add event
    Given I log in with username "__TEST__existing_user" and password "123"
    When I go to the create proposal page
    And I input "USC" in the field "Any Event"
    And I click button "Search"
    And I wait for things to load (time limit "10000" milliseconds)
    And I write down the title of the first event and click Add button next to the event
    Then I should see that event show up in Selected Events section

  Scenario: Remove event
    Given I log in with username "__TEST__existing_user" and password "123"
    When I go to the create proposal page
    And I input "USC" in the field "Any Event"
    And I click button "Search"
    And I wait for things to load (time limit "10000" milliseconds)
    And I write down the title of the first event and click Add button next to the event
    And I click Remove button next to that event in Selected Events section
    And I click button "OK"
    Then I should not see that event show up in Selected Events section

  Scenario: Invite user autocomplete
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__invite_b" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    Then I should see "__TEST__invite_bob" in the invited users list

  Scenario: Invite user nonexistent user
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__invite_nonexistent" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    Then I should not see "__TEST__invite_nonexistent" in the invited users list

  Scenario: Invite user blocked
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__invite_c" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    Then I should not see "__TEST__invite_charlie" in the invited users list
    And I should see "__TEST__invite_charlie" marked as unavailable in the candidate list

  Scenario: Remove invited user
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__invite_b" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    And I click the delete icon next to user "__TEST__invite_bob"
    And I click button "OK"
    Then I should not see "__TEST__invite_bob" in the invited users list

  Scenario: Remove invited user button turns red on hover
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__invite_b" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    And I mouseover the delete icon next to "__TEST__invite_bob"
    Then I should see the delete icon next to "__TEST__invite_bob" turn red

  Scenario: Create proposal form validation (no title, no invited user, no events)
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I click button "Create"
    Then I should see text "Please fill out the proposal title."
    And I should see text "Please invite at least one user."
    And I should see text "Please select at least one event."

  Scenario: Create proposal
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__proposal_title" in the field "Proposal Title"
    And I input "__TEST__invite_b" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    And I input "USC" in the field "Any Event"
    And I click button "Search"
    And I wait for things to load (time limit "10000" milliseconds)
    And I write down the title of the first event and click Add button next to the event
    And I click button "Create"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "__TEST__proposal_title"
    And I should see text "Created by __TEST__invite_alice"

  Scenario: Proposal draft
    Given I log in with username "__TEST__invite_alice" and password "alice"
    When I go to the create proposal page
    And I input "__TEST__proposal_draft" in the field "Proposal Title"
    And I input "__TEST__invite_b" in the invite user dropdown
    And I wait for things to load (time limit "10000" milliseconds)
    And I focus on the invite user dropdown and press enter
    And I input "USC" in the field "Any Event"
    And I click button "Search"
    And I wait for things to load (time limit "10000" milliseconds)
    And I write down the title of the first event and click Add button next to the event
    And I click button "Save As Draft"
    And I go to details page for draft "__TEST__proposal_draft"
    And I wait for things to load (time limit "10000" milliseconds)
    Then I should see text "__TEST__proposal_draft"
    And I should see "__TEST__invite_bob" in the invited users list
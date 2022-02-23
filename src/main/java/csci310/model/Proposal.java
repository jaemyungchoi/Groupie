package csci310.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import csci310.model.EventComparators.*;


public class Proposal {
    private final int id;
    private final User owner;
    private final String title;
    private final Event[] events;
    private final User[] invitedUsers;
    private final User[] acceptedUsers;
    private final User[] declinedUsers;
    private final User[] pendingUsers;
    private final Event finalizedEvent;
    private final User[] hiddenUsers;
    private final String createdAt;

    public Proposal(int id,
                    User owner,
                    String title,
                    Event[] events,
                    User[] invitedUsers,
                    Event finalizedEvent,
                    User[] acceptedUsers,
                    User[] declinedUsers,
                    User[] hiddenUsers,
                    String createdAt) {
        this.id = id;
        this.owner = owner;
        this.title = title;
        this.events = events;
        this.invitedUsers = invitedUsers;
        this.acceptedUsers = acceptedUsers;
        this.declinedUsers = declinedUsers;
        this.finalizedEvent = finalizedEvent;
        this.hiddenUsers = hiddenUsers;
        this.createdAt = createdAt;

        User[] pending = null;
        if (invitedUsers != null && acceptedUsers != null && declinedUsers != null && owner != null) {
            Set<Integer> responded = Stream.concat(Arrays.stream(acceptedUsers), Arrays.stream(declinedUsers))
                    .map(User::getId).collect(Collectors.toSet());
            pending = Stream.concat(Stream.of(owner), Arrays.stream(invitedUsers))
                    .filter(user -> !responded.contains(user.getId())).toArray(User[]::new);
        }
        pendingUsers = pending;
    }

    public int getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public Event[] getEvents() {
        return events;
    }

    public User[] getInvitedUsers() {
        return invitedUsers;
    }

    public User[] getAcceptedUsers() {
        return acceptedUsers;
    }

    public User[] getDeclinedUsers() {
        return declinedUsers;
    }

    // a proposal is finalized if finalizedEvent is not null
    public Event getFinalizedEvent() {
        return finalizedEvent;
    }

    // users who reject the proposal before it is finalized (which makes the proposal hidden on the list page)
    public User[] getHiddenUsers() {
        return hiddenUsers;
    }

    public User[] getPendingUsers() {
        return pendingUsers;
    }

    public boolean isFinalized() {
        return finalizedEvent != null;
    }

    // is user the owner or invited?
    public boolean canAccess(int userId) {
        return userId == owner.getId() || Arrays.stream(invitedUsers).anyMatch(user -> user.getId() == userId);
    }

    // is event a part of this proposal?
    public boolean hasEvent(int eventId) {
        return Arrays.stream(events).anyMatch(event -> event.getId() == eventId);
    }

    // Gets the best event
    public Event getBestEvent() {
        if (events.length == 0)
            return null;

        ArrayList<Event> eventsCopy = new ArrayList<>(Arrays.asList(events));
        // Filter by # yes
        Collections.sort(eventsCopy, new EventYesComparator().reversed());
        ArrayList<Event> yesResult = eventsCopy.stream()
                .filter(e -> e.getNumYes() == eventsCopy.get(0).getNumYes())
                .collect(Collectors.toCollection(ArrayList::new));
        if (yesResult.size() == 1)
            return yesResult.get(0);

        // Filter by excitement score
        Collections.sort(yesResult, new EventRatingComparator().reversed());
        ArrayList<Event> scoreResult = new ArrayList<>();
        for (Event e : eventsCopy) {
            if (e.getTotalEventScore() == yesResult.get(0).getTotalEventScore()) {
                scoreResult.add(e);
            }
        }

        if (scoreResult.size() == 1)
            return scoreResult.get(0);

        // Filter by num maybe
        Collections.sort(scoreResult, new EventMaybeComparator().reversed());
        ArrayList<Event> maybeResult = new ArrayList<>();
        for (Event e : eventsCopy)
        {
            if (e.getNumMaybe() == scoreResult.get(0).getNumMaybe())
                maybeResult.add(e);
        }
        if (maybeResult.size() == 1)
            return maybeResult.get(0);

        // Pick a random event
        return maybeResult.get((int) (Math.random() * maybeResult.size()));
    }

    public String getCreatedAt() { return createdAt; }
}

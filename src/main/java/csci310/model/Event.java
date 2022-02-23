package csci310.model;

import java.util.Arrays;

public class Event {
    private final int id;
    private final String tmEventKey;
    private final Vote[] votes;

    public Event(int id, String tmEventKey, Vote[] votes)
    {
        this.id = id;
        this.tmEventKey = tmEventKey;
        this.votes = votes;
    }

    public int getId() {
        return id;
    }

    public String getTmEventKey() {
        return tmEventKey;
    }

    public Vote[] getVotes() {
        return votes;
    }

    // Returns a numerical value for how "good" the event is in terms of availability/rating
    // See Proposal.getBestEvent for details on formula
    // DO NOT USE! This is outdated
    public int getEventRating()
    {
        double maybe_multiplier = 0.5;
        int totalRating = 0;
        for (Vote v : votes)
        {
            if (v.getCanAttend() == CanAttend.YES)
                totalRating += 1 * v.getRating();
            else if (v.getCanAttend() == CanAttend.MAYBE)
                totalRating += maybe_multiplier * v.getRating();
            else // No or none
                totalRating += 0 * v.getRating();
        }
        return totalRating;
    }

    public int getNumYes()
    {
        long numYes = Arrays.stream(votes)
                .filter(v -> !v.getIsDraft() && v.getCanAttend() == CanAttend.YES)
                .count();
        return (int)numYes;
    }

    // Return excitement rating among yes respondents
    public int getTotalEventScore()
    {
        long totalRating = Arrays.asList(votes)
                .stream()
                .filter(v -> !v.getIsDraft() && v.getCanAttend() == CanAttend.YES)
                .mapToInt(v -> v.getRating())
                .sum();
        return (int)totalRating;

    }

    public int getNumMaybe()
    {
        long numMaybe = Arrays.stream(votes)
                .filter(v -> !v.getIsDraft() && v.getCanAttend() == CanAttend.MAYBE)
                .count();
        return (int)numMaybe;
    }
}

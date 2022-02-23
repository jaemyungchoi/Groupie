package csci310.model;

public class Vote {
    private final int id;
    private final User user;
    private final CanAttend canAttend;
    private final Integer rating;
    private final boolean isDraft;

    public Vote(int id, User user, CanAttend canAttend, Integer rating, boolean isDraft)
    {
        this.id = id;
        this.user = user;
        this.canAttend = canAttend;
        this.rating = rating;
        this.isDraft = isDraft;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public CanAttend getCanAttend() {
        return canAttend;
    }

    public Integer getRating() { return rating; }

    public boolean getIsDraft() { return isDraft; }
}

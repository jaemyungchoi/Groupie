package csci310.model;

public class User {
    //no need for setter methods as user info is immutable
    private final int id;
    private final String username;

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }
}
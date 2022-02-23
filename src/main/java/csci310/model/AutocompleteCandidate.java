package csci310.model;

public class AutocompleteCandidate {

    private final String username;
    private final boolean onTheirBlockList;

    public AutocompleteCandidate(String username, boolean onTheirBlockList) {
        this.username = username;
        this.onTheirBlockList = onTheirBlockList;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean getOnTheirBlockList() {
        return this.onTheirBlockList;
    }
}
package csci310.model;

public class ProposalDraft {
    private final int id;
    private final User owner;
    private final String title;
    private String jsonData;

    public ProposalDraft(int id, User owner, String title, String jsonData)
    {
        this.id = id;
        this.owner = owner;
        this.title = title;
        this.jsonData = jsonData;
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

    public String getJsonData() { return jsonData; }
}

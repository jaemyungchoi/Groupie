package csci310.dao;

import csci310.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProposalDraftDAO extends DAOBase {

    public ProposalDraftDAO(String dbURL) {
        super(dbURL);
    }

    // Returns ProposalDraft object on success.
    // Only id and owner is guaranteed to be accurate and other attributes could be null.
    public ProposalDraft createProposalDraft(int userId, String title, String jsonData) throws SQLException {
        String insertStr = "INSERT INTO proposal_drafts (title, owner_uid, data) VALUES (?,?,?)";
        String selectStr = "SELECT last_insert_rowid() AS id";
        try (Connection connection = getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertStr);
             PreparedStatement selectIdStmt = connection.prepareStatement(selectStr)) {
            insertStmt.setString(1, title);
            insertStmt.setInt(2, userId);
            insertStmt.setString(3, jsonData);
            insertStmt.executeUpdate();
            try (ResultSet result = selectIdStmt.executeQuery()) {
                int draftId = result.getInt("id");
                return new ProposalDraft(
                        draftId,
                        new User(userId, null),
                        title,
                        jsonData
                );
            }
        }
    }

    // return true on success
    // Make sure the caller is the owner first!
    public boolean deleteProposalDraft(int id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM proposal_drafts WHERE id=?")) {
            deleteStmt.setInt(1, id);
            return deleteStmt.executeUpdate() > 0;
        }
    }

    // return a ProposalDraft object if id is valid, or null otherwise
    public ProposalDraft getProposalDraftById(int id) throws SQLException {
        String selectStr = "SELECT proposal_drafts.*, users.name AS username from proposal_drafts"
                + " LEFT JOIN users ON proposal_drafts.owner_uid=users.id WHERE proposal_drafts.id=?";
        ProposalDraft draft = null;
        try (Connection connection = getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(selectStr)) {
            selectStmt.setInt(1, id);
            try (ResultSet result = selectStmt.executeQuery()) {
                if (result.next()) {
                    String title = result.getString("title");
                    String jsonData = result.getString("data");
                    String username = result.getString("username");
                    int userId = result.getInt("owner_uid");
                    draft = new ProposalDraft(id, new User(userId, username), title, jsonData);
                }
            }
        }
        return draft;
    }

    public ProposalDraft[] getProposalDraftsByUser(int uid) throws SQLException {
        String queryText = "SELECT * FROM proposal_drafts WHERE proposal_drafts.owner_uid=? ORDER BY proposal_drafts.id";
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement selectSt = conn.prepareStatement(queryText)) {
            selectSt.setInt(1, uid);
            List<ProposalDraft> drafts = new ArrayList<>();
            try (ResultSet result = selectSt.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("id");
                    // FIXME: N+1 query
                    drafts.add(getProposalDraftById(id));
                }
            }
            return drafts.toArray(new ProposalDraft[0]);
        }
    }
}

package csci310.dao;

import csci310.model.AutocompleteCandidate;
import csci310.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutocompleteDAO extends DAOBase {

    public AutocompleteDAO(String dbURL) {
        super(dbURL);
    }

    // Returns a list of AutocompleteCandidate for user(id=uid) and a username prefix
    public AutocompleteCandidate[] getAutocompleteCandidates(int uid, String prefix, int maxCount) throws SQLException {
        String stmtText = "SELECT users.*, block_list.id as block_list_id"
                + " FROM users LEFT JOIN block_list"
                + " ON block_list.blocked_user_id=? AND block_list.user_id=users.id"
                + " WHERE users.name LIKE ? ESCAPE '!' AND users.id<>?"
                + " ORDER BY users.name LIMIT ?";
        try (Connection connection = getConnection();
             PreparedStatement queryStmt = connection.prepareStatement(stmtText)) {
            // https://stackoverflow.com/a/8248052
            prefix = prefix
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_")
                    .replace("[", "![");
            queryStmt.setInt(1, uid);
            queryStmt.setString(2, prefix + "%");
            queryStmt.setInt(3, uid);
            queryStmt.setInt(4, maxCount);
            try (ResultSet result = queryStmt.executeQuery()) {
                List<AutocompleteCandidate> candidates = new ArrayList<>();
                while (result.next()) {
                    String name = result.getString("name");
                    result.getInt("block_list_id");
                    boolean isBlocked = !result.wasNull();
                    candidates.add(new AutocompleteCandidate(name, isBlocked));
                }
                return candidates.toArray(new AutocompleteCandidate[0]);
            }
        }
    }
}

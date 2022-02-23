package csci310.dao;

import csci310.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlockListDAO extends DAOBase {

    public BlockListDAO(String dbURL) {
        super(dbURL);
    }

    // returns false if username is invalid
    public boolean blockUserByName(int uid, String blockedUsername) throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        User blockedUser = userDAO.getUserByName(blockedUsername);
        if (blockedUser == null) {
            return false;
        }
        try (Connection connection = getConnection();
             PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO block_list (user_id, blocked_user_id) VALUES (?,?)")) {
            insertStmt.setInt(1, uid);
            insertStmt.setInt(2, blockedUser.getId());
            insertStmt.executeUpdate();
        }
        return true;
    }

    // returns false if uid is invalid or not currently blocked
    public boolean unblockUserById(int uid, int blockedUserId) throws SQLException {
        String stmtText = "DELETE FROM block_list WHERE user_id=? AND blocked_user_id=?";
        try (Connection connection = getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement(stmtText)) {
            deleteStmt.setInt(1, uid);
            deleteStmt.setInt(2, blockedUserId);
            return deleteStmt.executeUpdate() > 0;
        }
    }

    // Returns a User[] if successful, null if user with id does not exist
    public User[] getBlockListForUser(int id) throws SQLException {
        String stmtText = "SELECT * FROM block_list LEFT JOIN users"
                + " ON block_list.blocked_user_id=users.id"
                + " WHERE block_list.user_id=? ORDER BY block_list.id";
        try (Connection connection = getConnection();
             PreparedStatement queryStmt = connection.prepareStatement(stmtText)) {
            queryStmt.setInt(1, id);
            try (ResultSet result = queryStmt.executeQuery()) {
                List<User> blockList = new ArrayList<>();
                while (result.next()) {
                    int blockedUserId = result.getInt("blocked_user_id");
                    String blockedUsername = result.getString("name");
                    blockList.add(new User(blockedUserId, blockedUsername));
                }
                return blockList.toArray(new User[0]);
            }
        }
    }

    // Returns a User[] if successful, null if user with id does not exist
    public User[] getUsersWhoBlockMe(int id) throws SQLException {
        String stmtText = "SELECT * FROM block_list LEFT JOIN users"
                + " ON block_list.user_id=users.id"
                + " WHERE block_list.blocked_user_id=? ORDER BY block_list.id";
        try (Connection connection = getConnection();
             PreparedStatement queryStmt = connection.prepareStatement(stmtText)) {
            queryStmt.setInt(1, id);
            try (ResultSet result = queryStmt.executeQuery()) {
                List<User> blockList = new ArrayList<>();
                while (result.next()) {
                    int blockedUserId = result.getInt("user_id");
                    String blockedUsername = result.getString("name");
                    blockList.add(new User(blockedUserId, blockedUsername));
                }
                return blockList.toArray(new User[0]);
            }
        }
    }
}

package csci310.dao;

import csci310.model.Proposal;
import csci310.model.User;
import csci310.dao.ProposalDAO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO extends DAOBase {

    public UserDAO(String dbURL) {
        super(dbURL);
    }

    // Returns a User with a positive id if successful
    public User createUser(String name, String password) throws SQLException {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        int id;
        try (Connection connection = getConnection();
             PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO users (name, password_hash) VALUES (?,?)");
             PreparedStatement selectIdStmt = connection.prepareStatement("SELECT last_insert_rowid() AS id"))
        {
            insertStmt.setString(1, name);
            insertStmt.setString(2, hash);
            insertStmt.executeUpdate();
            try (ResultSet result = selectIdStmt.executeQuery()) {
                id = result.getInt("id");
            }
        }
        return new User(id, name);
    }

    // Returns a User if successful, null if user with id does not exist
    public User getUserById(int id) throws SQLException {
        String username = null;
        try (Connection connection = getConnection();
             PreparedStatement selectUserStmt = connection.prepareStatement("SELECT * from users WHERE id=?"))
        {
            selectUserStmt.setInt(1, id);
            try (ResultSet result = selectUserStmt.executeQuery()) {
                if (result.next()) {
                    username = result.getString("name");
                }
            }
        }
        return username != null ? new User(id, username) : null;
    }

    // Returns a User if successful, null if user with id does not exist
    public User getUserByName(String username) throws SQLException {
        int id = -1;
        try (Connection connection = getConnection();
             PreparedStatement selectUserStmt = connection.prepareStatement("SELECT * from users WHERE name=?"))
        {
            selectUserStmt.setString(1, username);
            try (ResultSet result = selectUserStmt.executeQuery()) {
                if (result.next()) {
                    id = result.getInt("id");
                }
            }
        }
        return id > 0 ? new User(id, username) : null;
    }

    //assuming the user with the username exists, check if the password is correct
    public boolean passwordMatch(String username, String password) throws SQLException {
        try(Connection conn = DriverManager.getConnection(dbURL);
            PreparedStatement selectSt = conn.prepareStatement("SELECT password_hash FROM users WHERE name = (?)"))
        {
            selectSt.setString(1, username);
            try (ResultSet result = selectSt.executeQuery()) {
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                String pwHash = result.getString("password_hash");
                return encoder.matches(password, pwHash);
            }
        }
    }
}

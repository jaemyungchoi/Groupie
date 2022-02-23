package csci310.dao;

import csci310.model.Event;
import csci310.model.Proposal;
import csci310.model.User;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.*;

import static org.junit.Assert.*;

public class UserDAOTest {

    static private final String dbURL = System.getProperty("dbUrl");
    private static UserDAO userDAO = new UserDAO(dbURL);

    @BeforeClass
    public static void setUp() /* throws SQLException */ {
//        Add test users here
//        UserDAO userDAO = new UserDAO(dbURL);
//        userDAO.createUser("__TEST__dummy_user", "123");
    }

    @Test
    public void testCreateUser() throws SQLException {
        String username = "Tom";
        String password = "1234";
        User u = userDAO.createUser("__TEST__" + username, password);
        assertTrue("Expect user.id > 0, got " + u.getId(),u.getId() > 0);
        assertEquals("__TEST__" + username, u.getUsername());

        boolean checkCorrect = userDAO.passwordMatch("__TEST__" + username, password);
        assertTrue(checkCorrect);
        password += "5";
        checkCorrect = userDAO.passwordMatch("__TEST__" + username, password);
        assertFalse(checkCorrect);
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'"))
        {
            deleteStmt.executeUpdate();
        }
    }

    @Test
    public void testGetUserById() throws SQLException {
        assertNull(userDAO.getUserById(-1));
        User user = userDAO.createUser("__TEST__get_user_by_id", "123");
        User queryUser = userDAO.getUserById(user.getId());
        assertNotNull(queryUser);
        assertEquals("__TEST__get_user_by_id", queryUser.getUsername());
        assertEquals(user.getId(), queryUser.getId());
    }

    @Test
    public void testGetUserByName() throws SQLException {
        assertNull(userDAO.getUserByName("random non-existing user"));
        User user = userDAO.createUser("__TEST__get_user_by_name", "123");
        User queryUser = userDAO.getUserByName("__TEST__get_user_by_name");
        assertNotNull(queryUser);
        assertEquals("__TEST__get_user_by_name", queryUser.getUsername());
        assertEquals(user.getId(), queryUser.getId());
    }

    @Test
    public void testPasswordMatch() throws SQLException {
        String username = "David";
        String password = "1234";
        User u = userDAO.createUser("__TEST__" + username, password);
        //Verify user
        User queryUser = userDAO.getUserByName("__TEST__" + username);
        assertNotNull(queryUser);
        //Check password match - right password
        assertTrue(userDAO.passwordMatch("__TEST__" + username, password));
        //Check password match - wrong password
        assertFalse(userDAO.passwordMatch("__TEST__" + username, password + "5"));
    }

    @Test(expected = SQLException.class)
    public void testPasswordMatchInvalidUsername() throws SQLException {
        String username = "David";
        String password = "1234";
        userDAO.passwordMatch(username, password);
    }
}
package csci310.dao;

import csci310.model.AutocompleteCandidate;
import csci310.model.User;
import io.cucumber.java.bs.A;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.cglib.core.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class AutocompleteDAOTest {

    static private final String dbURL = System.getProperty("dbUrl");

    private static User testUser1;
    private static User testUser2;
    private static User testUser3;

    @BeforeClass
    public static void setUp() throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        testUser1 = userDAO.createUser("__TEST__ac_user_1", "123");
        testUser2 = userDAO.createUser("__TEST__ac_user_2", "123");
        testUser3 = userDAO.createUser("__TEST__ac_user_3", "123");
        BlockListDAO blockListDAO = new BlockListDAO(dbURL);
        blockListDAO.blockUserByName(testUser3.getId(), testUser1.getUsername());
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
    public void testGetAutocompleteCandidates() throws SQLException {
        AutocompleteDAO autocompleteDAO = new AutocompleteDAO(dbURL);
        AutocompleteCandidate[] candidates = autocompleteDAO.getAutocompleteCandidates(testUser1.getId(), "__TEST__ac_user", 100);
        assertEquals(2, candidates.length);
        assertEquals(testUser2.getUsername(), candidates[0].getUsername());
        assertFalse(candidates[0].getOnTheirBlockList());
        assertEquals(testUser3.getUsername(), candidates[1].getUsername());
        assertTrue(candidates[1].getOnTheirBlockList());
    }
}
package csci310.dao;

import csci310.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class BlockListDAOTest {

    private static final String dbURL = System.getProperty("dbUrl");
    private static User testUser1;
    private static User testUser2;
    private static User testUser3;
    private static User testUser4;
    private static User testUser5;
    private static User testUser6;
    private static BlockListDAO blockListDAO = new BlockListDAO(dbURL);

    @BeforeClass
    public static void setUp() throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        testUser1 = userDAO.createUser("__TEST__block_list_1", "123");
        testUser2 = userDAO.createUser("__TEST__block_list_2", "123");
        testUser3 = userDAO.createUser("__TEST__block_list_3", "123");
        testUser4 = userDAO.createUser("__TEST__block_list_4", "123");
        testUser5 = userDAO.createUser("__TEST__block_list_5", "123");
        testUser6 = userDAO.createUser("__TEST__block_list_6", "123");
        BlockListDAO blockListDAO = new BlockListDAO(dbURL);
        blockListDAO.blockUserByName(testUser2.getId(), testUser1.getUsername());
        blockListDAO.blockUserByName(testUser2.getId(), testUser3.getUsername());
        blockListDAO.blockUserByName(testUser3.getId(), testUser1.getUsername());
        blockListDAO.blockUserByName(testUser5.getId(), testUser4.getUsername());
        blockListDAO.blockUserByName(testUser6.getId(), testUser4.getUsername());
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
    public void testBlockUserByName() throws SQLException {
        assertTrue(blockListDAO.blockUserByName(testUser1.getId(), testUser2.getUsername()));
        User[] blockList = blockListDAO.getBlockListForUser(testUser1.getId());
        assertEquals(1, blockList.length);
        assertEquals(testUser2.getId(), blockList[0].getId());
    }

    @Test
    public void testBlockUserByNameInvalidUser() throws SQLException {
        assertFalse(blockListDAO.blockUserByName(testUser1.getId(), "random non-existing user"));
    }

    @Test
    public void testGetBlockListForUser() throws SQLException {
        User[] blockList = blockListDAO.getBlockListForUser(testUser2.getId());
        assertEquals(2, blockList.length);
        assertEquals(testUser1.getId(), blockList[0].getId());
        assertEquals(testUser3.getId(), blockList[1].getId());
    }

    @Test
    public void testUnblockUserById() throws SQLException {
        assertTrue(blockListDAO.unblockUserById(testUser3.getId(), testUser1.getId()));
        User[] blockList = blockListDAO.getBlockListForUser(testUser1.getId());
        assertEquals(0, blockList.length);
    }

    @Test
    public void testUnblockUserByIdInvalidId() throws SQLException {
        assertFalse(blockListDAO.unblockUserById(testUser3.getId(), testUser2.getId()));
    }

    @Test
    public void testGetUsersWhoBlockMe() throws SQLException {
        User[] blockBy = blockListDAO.getUsersWhoBlockMe(testUser4.getId());
        assertEquals(2, blockBy.length);
        assertEquals(testUser5.getId(), blockBy[0].getId());
        assertEquals(testUser6.getId(), blockBy[1].getId());
    }
}
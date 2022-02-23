package csci310.dao;

import csci310.model.Proposal;
import csci310.model.ProposalDraft;
import csci310.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class ProposalDraftDAOTest {

    private static final String dbURL = System.getProperty("dbUrl");
    private static User testUser1;
    private static User testUser2;
    private static int testDeleteProposalDraftId;
    private static int testGetProposalDraftId;
    private static ProposalDraftDAO proposalDraftDAO = new ProposalDraftDAO(dbURL);

    @BeforeClass
    public static void setUp() throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        testUser1 = userDAO.createUser("__TEST__test_create_proposal_1", "123");
        testUser2 = userDAO.createUser("__TEST__test_create_proposal_2", "123");
        ProposalDraftDAO proposalDraftDAO = new ProposalDraftDAO(dbURL);
        testDeleteProposalDraftId = proposalDraftDAO.createProposalDraft(testUser1.getId(), "__TEST__delete_draft", "{}").getId();
        testGetProposalDraftId = proposalDraftDAO.createProposalDraft(testUser2.getId(), "__TEST__get_draft", "{}").getId();
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'")) {
            deleteStmt.executeUpdate();
        }
    }

    @Test
    public void testCreateProposalDraft() throws SQLException {
        ProposalDraft draft = proposalDraftDAO.createProposalDraft(testUser1.getId(), "__TEST__create_draft", "{}");
        assertNotNull(draft);
        ProposalDraft getDraft = proposalDraftDAO.getProposalDraftById(draft.getId());
        assertEquals("__TEST__create_draft", getDraft.getTitle());
        assertEquals("{}", getDraft.getJsonData());
        assertEquals(testUser1.getId(), getDraft.getOwner().getId());
    }

    @Test
    public void testDeleteProposalDraft() throws SQLException {
        assertTrue(proposalDraftDAO.deleteProposalDraft(testDeleteProposalDraftId));
        assertNull(proposalDraftDAO.getProposalDraftById(testDeleteProposalDraftId));
    }

    @Test
    public void testDeleteProposalDraftInvalidId() throws SQLException {
        assertFalse(proposalDraftDAO.deleteProposalDraft(-1));
    }

    @Test
    public void testGetProposalDraftById() throws SQLException {
        ProposalDraft draft = proposalDraftDAO.getProposalDraftById(testGetProposalDraftId);
        assertEquals(testGetProposalDraftId, draft.getId());
        assertEquals("__TEST__get_draft", draft.getTitle());
        assertEquals("{}", draft.getJsonData());
        assertEquals(testUser2.getId(), draft.getOwner().getId());
    }

    @Test
    public void testGetProposalDraftByIdInvalidId() throws SQLException {
        assertNull(proposalDraftDAO.getProposalDraftById(-1));
    }

    @Test
    public void testGetProposalDraftsByUser() throws SQLException {
        ProposalDraft[] drafts = proposalDraftDAO.getProposalDraftsByUser(testUser2.getId());
        assertEquals(1, drafts.length);
        assertEquals(testGetProposalDraftId, drafts[0].getId());
    }
}
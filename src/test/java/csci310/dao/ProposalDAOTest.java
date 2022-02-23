package csci310.dao;

import csci310.model.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ProposalDAOTest {

    private static final String dbURL = System.getProperty("dbUrl");
    private static User testUser1;
    private static User testUser2;
    private static User testUserIvan;
    private static User testUserPyotr;
    private static User testUserCatherine;
    private static Proposal proposalCreatedByIvan;
    private static int testProposalId;
    private static Proposal testDeleteEventProposal;
    private static int testProposalDeleteUserId;
    private static int testProposalRejectId;
    private static ProposalDAO dao = new ProposalDAO(dbURL);

    @BeforeClass
    public static void setUp() throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        testUser1 = userDAO.createUser("__TEST__test_create_proposal_1", "123");
        testUser2 = userDAO.createUser("__TEST__test_create_proposal_2", "123");
        ProposalDAO proposalDAO = new ProposalDAO(dbURL);
        String[] testEvents = new String[]{"Z7r9jZ1AdFUqk", "vvG1HZpRndBscg"};
        int[] invitedUserIds = new int[]{testUser2.getId()};
        testProposalId = proposalDAO.createProposal(testUser1.getId(), "__TEST__existing_proposal", testEvents, invitedUserIds).getId();

        // get proposal by user
        testUserIvan = userDAO.createUser("__TEST__Ivan", "1547");
        testUserPyotr = userDAO.createUser("__TEST__Pyotr", "1682");
        testUserCatherine = userDAO.createUser("__TEST__Catherine", "1762");

        proposalCreatedByIvan = proposalDAO.createProposal(
                testUserIvan.getId(),
                "Declaration of the Tsardom of Russia",
                new String[0],
                new int[]{testUserPyotr.getId()}
        );

        testDeleteEventProposal = proposalDAO.createProposal(testUser1.getId(), "__TEST__existing_proposal", testEvents, invitedUserIds);
        testDeleteEventProposal = proposalDAO.getProposalById(testDeleteEventProposal.getId());

        testProposalDeleteUserId = proposalDAO.createProposal(testUser1.getId(), "__TEST__existing_proposal", testEvents, invitedUserIds).getId();
        testProposalRejectId = proposalDAO.createProposal(testUser1.getId(), "__TEST__existing_proposal", testEvents, invitedUserIds).getId();
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'")) {
            deleteStmt.executeUpdate();
        }
    }

    @Test
    public void testCreateProposal() throws SQLException {
        String title = "__TEST__PROPOSAL_create";

        String[] tmEvents = new String[]{"123", "456"};
        int[] invitedUserIds = new int[]{testUser2.getId()};

        Proposal proposal = dao.createProposal(testUser1.getId(), title, tmEvents, invitedUserIds);
        assertNotNull(proposal);
        assertEquals(testUser1.getId(), proposal.getOwner().getId());
        assertEquals(title, proposal.getTitle());

        Proposal insertedProposal = dao.getProposalById(proposal.getId());
        Assert.assertEquals(title, insertedProposal.getTitle());
        Assert.assertEquals(testUser1.getId(), insertedProposal.getOwner().getId());
        Event[] events = insertedProposal.getEvents();
        String[] eventKeys = Arrays.stream(events)
                .map(Event::getTmEventKey)
                .toArray(String[]::new);
        assertArrayEquals(tmEvents, eventKeys);
        assertEquals(1, insertedProposal.getInvitedUsers().length);
        assertEquals(testUser2.getId(), insertedProposal.getInvitedUsers()[0].getId());
        assertArrayEquals(new User[0], insertedProposal.getHiddenUsers());
        assertNotNull(insertedProposal.getCreatedAt());
    }

    @Test
    public void testDeleteProposal() throws SQLException {
        assertFalse(dao.deleteProposal(-1));
    }

    @Test
    public void testDeleteProposalInvalidProposal() throws SQLException {
        String title = "__TEST__PROPOSAL_delete";
        String[] tmEvents = new String[]{"aaa", "bbb"};
        int[] invitedUserIds = new int[]{testUser2.getId()};

        Proposal proposal = dao.createProposal(testUser1.getId(), title, tmEvents, invitedUserIds);
        int id = proposal.getId();
        // delete that proposal
        dao.deleteProposal(id);

        assertNull(dao.getProposalById(id));
    }

    @Test
    public void testGetProposalByIdInvalidId() throws SQLException {
        // valid id is tested in testCreateProposal
        assertNull(dao.getProposalById(-1));
    }

    @Test
    public void createVote() throws SQLException {
        Proposal proposal = dao.getProposalById(testProposalId);
        assertEquals(2, proposal.getEvents().length);
        assertEquals(0, proposal.getEvents()[0].getVotes().length);
        assertEquals(0, proposal.getEvents()[1].getVotes().length);
        Vote vote = dao.createVote(testUser1.getId(), proposal.getEvents()[0].getId(), CanAttend.YES, 3, true);
        assertTrue(vote.getId() > 0);
        proposal = dao.getProposalById(testProposalId);
        assertEquals(1, proposal.getEvents()[0].getVotes().length);
        assertTrue(proposal.getEvents()[0].getVotes()[0].getIsDraft());
        assertEquals(0, proposal.getEvents()[1].getVotes().length);
        assertEquals(testUser1.getId(), proposal.getEvents()[0].getVotes()[0].getUser().getId());
        assertEquals(CanAttend.YES, proposal.getEvents()[0].getVotes()[0].getCanAttend());
        assertEquals(3, (int)proposal.getEvents()[0].getVotes()[0].getRating());
        Vote vote2 = dao.createVote(testUser2.getId(), proposal.getEvents()[0].getId(), CanAttend.NO, 4, false);
        proposal = dao.getProposalById(testProposalId);
        assertEquals(2, proposal.getEvents()[0].getVotes().length);
        assertEquals(0, proposal.getEvents()[1].getVotes().length);
    }

    @Test
    public void testGetProposalsByUserCreatedProposal() throws SQLException {
        Proposal[] proposals = dao.getProposalsByUser(testUserIvan.getId());
        assertEquals(proposals.length, 1);
        assertEquals(proposals[0].getInvitedUsers().length, 1);
        assertEquals(proposals[0].getOwner().getUsername(), testUserIvan.getUsername());
        assertEquals(proposals[0].getId(), proposalCreatedByIvan.getId());
        assertEquals(proposals[0].getTitle(), proposalCreatedByIvan.getTitle());
    }

    @Test
    public void testGetProposalsByUserInvitedToProposal() throws SQLException {
        Proposal[] proposals = dao.getProposalsByUser(testUserPyotr.getId());
        assertEquals(proposals.length, 1);
        assertEquals(proposals[0].getTitle(), proposalCreatedByIvan.getTitle());
        assertEquals(proposals[0].getInvitedUsers().length, 1);
        assertEquals(proposals[0].getInvitedUsers()[0].getId(), testUserPyotr.getId());
        assertEquals(proposals[0].getInvitedUsers()[0].getUsername(), testUserPyotr.getUsername());
        assertEquals(proposals[0].getOwner().getUsername(), testUserIvan.getUsername());
        assertEquals(proposals[0].getId(), proposalCreatedByIvan.getId());
        assertEquals(proposals[0].getTitle(), proposalCreatedByIvan.getTitle());
    }

    @Test
    public void testGetProposalsByUserNullUser() throws SQLException {
        Proposal[] proposals = dao.getProposalsByUser(-1);
        assertEquals(proposals.length, 0);
    }

    @Test
    public void testGetProposalsByUserDifferentProposal() throws SQLException {
        Proposal[] proposals = dao.getProposalsByUser(testUserCatherine.getId());
        // Catherine wasn't invited to Ivan's party
        assertEquals(proposals.length, 0);
    }

    @Test
    public void testAcceptProposal() throws SQLException {
        Proposal proposal = dao.getProposalById(testProposalId);
        assertEquals(0, proposal.getAcceptedUsers().length);
        assertTrue(dao.acceptProposal(testUser2.getId(), testProposalId, true));
        proposal = dao.getProposalById(testProposalId);
        assertEquals(1, proposal.getAcceptedUsers().length);
        assertEquals(testUser2.getId(), proposal.getAcceptedUsers()[0].getId());
    }

    @Test
    public void testAcceptProposalDecline() throws SQLException {
        Proposal proposal = dao.getProposalById(testProposalId);
        assertEquals(0, proposal.getDeclinedUsers().length);
        assertTrue(dao.acceptProposal(testUser1.getId(), testProposalId, false));
        proposal = dao.getProposalById(testProposalId);
        assertEquals(1, proposal.getDeclinedUsers().length);
        assertEquals(testUser1.getId(), proposal.getDeclinedUsers()[0].getId());
    }

    @Test
    public void testAcceptProposalInvalidId() throws SQLException {
        Proposal proposal = dao.getProposalById(testProposalId);
        assertFalse(dao.acceptProposal(-1, -1, true));
    }

    @Test
    public void testFinalizeProposalValid() throws SQLException
    {
        Proposal proposal = dao.getProposalById(testProposalId);
        // Ensure proposal is not finalized
        assertFalse(proposal.isFinalized());

        // Ensure can successfully finalize the valid proposal
        proposal = dao.finalizeProposal(proposal.getId());
        Event finalized_event = proposal.getFinalizedEvent();

        assertNotNull(finalized_event);

        // Ensure finalized event is one of the events in the proposal
        boolean in_list = false;
        for (Event e : proposal.getEvents())
            if (e.getId() == finalized_event.getId()) {
                in_list = true;
                break;
            }
        assertTrue(in_list);

        // Ensure proposal is finalized
        assertTrue(proposal.isFinalized());
    }

    @Test
    public void testFinalizeProposalNoEvents() throws SQLException
    {

        // Make a proposal with no events
        String[] testEventsNone = new String[]{};
        int[] invitedUserIds = new int[]{testUser2.getId()};
        Proposal no_event_proposal = dao.createProposal(testUser1.getId(), "__TEST__no_event_proposal", testEventsNone, invitedUserIds);

        // Shouldn't be able to finalize a proposal with no events. Ensure an exception is thrown
        assertThrows(IllegalStateException.class, () -> dao.finalizeProposal(no_event_proposal.getId()));

        // Ensure proposal is NOT finalized in the database
        assertNull(dao.getProposalById(no_event_proposal.getId()).getFinalizedEvent());
        assertFalse(dao.getProposalById(no_event_proposal.getId()).isFinalized());

        // clean up
        dao.deleteProposal(no_event_proposal.getId());
    }

    @Test
    public void testDeleteEvent() throws SQLException {
        assertEquals(2, testDeleteEventProposal.getEvents().length);
        assertTrue(dao.deleteEvent(testDeleteEventProposal.getEvents()[0].getId()));
        Proposal updatedProposal = dao.getProposalById(testDeleteEventProposal.getId());
        assertEquals(1, updatedProposal.getEvents().length);
        assertEquals(testDeleteEventProposal.getEvents()[1].getId(), updatedProposal.getEvents()[0].getId());
    }

    @Test
    public void testDeleteEventInvalidEvent() throws SQLException {
        assertFalse(dao.deleteEvent(-1));
    }

    @Test
    public void testDeleteInvitedUser() throws SQLException {
        assertTrue(dao.deleteInvitedUser(testProposalDeleteUserId, testUser2.getId()));
        Proposal proposal = dao.getProposalById(testProposalDeleteUserId);
        assertEquals(0, proposal.getInvitedUsers().length);
    }

    @Test
    public void testDeleteInvitedUserInvalidUser() throws SQLException {
        assertFalse(dao.deleteInvitedUser(testProposalDeleteUserId, -1));
    }

    @Test
    public void testRejectProposal() throws SQLException {
        Proposal proposal = dao.getProposalById(testProposalRejectId);
        assertEquals(0, proposal.getHiddenUsers().length);
        assertTrue(dao.rejectProposal(testProposalRejectId, testUser2.getId(), true));
        proposal = dao.getProposalById(testProposalRejectId);
        assertEquals(1, proposal.getHiddenUsers().length);
        assertEquals(testUser2.getId(), proposal.getHiddenUsers()[0].getId());
    }

    @Test
    public void testRejectProposalInvalidId() throws SQLException {
        assertFalse(dao.rejectProposal(testProposalRejectId, -1, true));
    }
}

package cucumber;

import csci310.dao.BlockListDAO;
import csci310.dao.DAOBase;
import csci310.dao.ProposalDAO;
import csci310.dao.UserDAO;
import csci310.model.CanAttend;
import csci310.model.Proposal;
import csci310.model.User;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.security.core.parameters.P;

import java.sql.*;

/**
 * Run all the cucumber tests in the current package.
 */
@RunWith(Cucumber.class)
@CucumberOptions(strict = true)
//@CucumberOptions(strict = true, features = {"src/test/resources/cucumber/x.feature", "src/test/resources/cucumber/y.feature"}) will run only feature files x.feature and y.feature.
public class RunCucumberTests {

    static private final String dbURL = System.getProperty("dbUrl");

    @BeforeClass
    public static void setup() throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        userDAO.createUser("__TEST__existing_user", "123");
        userDAO.createUser("__TEST__lockout_user", "123");

        // Block list
        User blockListTestUser1 = userDAO.createUser("__TEST__block_list_feature_1", "test");
        User blockListTestUser2 = userDAO.createUser("__TEST__block_list_feature_2", "321");
        User blockListTestUser3 = userDAO.createUser("__TEST__block_list_feature_3", "abc");
        BlockListDAO blockListDAO = new BlockListDAO(dbURL);
        blockListDAO.blockUserByName(blockListTestUser2.getId(), blockListTestUser1.getUsername());
        blockListDAO.blockUserByName(blockListTestUser3.getId(), blockListTestUser1.getUsername());
        blockListDAO.blockUserByName(blockListTestUser3.getId(), blockListTestUser2.getUsername());

        User inviteUserTestUser1 = userDAO.createUser("__TEST__invite_alice", "alice");
        User inviteUserTestUser2 = userDAO.createUser("__TEST__invite_bob", "bob");
        User inviteUserTestUser3 = userDAO.createUser("__TEST__invite_charlie", "charlie");
        blockListDAO.blockUserByName(inviteUserTestUser3.getId(), inviteUserTestUser1.getUsername());

        ProposalDAO proposalDAO = new ProposalDAO(dbURL);

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__delete_proposal",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__delete_event_1_event",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__delete_event_2_event",
                new String[]{"G5v0Zpsu6tKyQ", "1AwZAOgGkdnF3BM"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__delete_user_1_user",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__delete_user_2_user",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__save_draft",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__commit_preference_incomplete",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__commit_preference",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__reject_proposal",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        Proposal acceptProposal = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__accept_proposal",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        proposalDAO.finalizeProposal(acceptProposal.getId());

        Proposal declineProposal = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__decline_proposal",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        proposalDAO.finalizeProposal(declineProposal.getId());

        Proposal acceptDeclineProposal = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__accept_decline_proposal",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        proposalDAO.finalizeProposal(acceptDeclineProposal.getId());

        Proposal finalizeProposal = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__finalize_proposal",
                new String[]{"G5v0Zpsu6tKyQ", "1AwZAOgGkdnF3BM"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        finalizeProposal = proposalDAO.getProposalById(finalizeProposal.getId());
        proposalDAO.createVote(
                inviteUserTestUser2.getId(),
                finalizeProposal.getEvents()[0].getId(),
                CanAttend.MAYBE,
                4,
                false
        );
        proposalDAO.createVote(
                inviteUserTestUser2.getId(),
                finalizeProposal.getEvents()[1].getId(),
                CanAttend.YES,
                2,
                false
        );

        Proposal proposalListProposal = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__proposal_list",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        proposalListProposal = proposalDAO.getProposalById(proposalListProposal.getId());
        proposalDAO.createVote(inviteUserTestUser2.getId(), proposalListProposal.getEvents()[0].getId(), CanAttend.YES, 3, false);
        proposalDAO.finalizeProposal(proposalListProposal.getId());
        proposalDAO.acceptProposal(inviteUserTestUser1.getId(), proposalListProposal.getId(), true);
        proposalDAO.acceptProposal(inviteUserTestUser3.getId(), proposalListProposal.getId(), false);

        proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__proposal_calendar_not_final",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );

        Proposal proposalCalendarFinalNoResponse = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__proposal_calendar_final_no_response",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        proposalDAO.finalizeProposal(proposalCalendarFinalNoResponse.getId());

        Proposal proposalCalendarFinalAccept = proposalDAO.createProposal(
                inviteUserTestUser1.getId(),
                "__TEST__proposal_calendar_final_accept",
                new String[]{"G5v0Zpsu6tKyQ"},
                new int[]{inviteUserTestUser2.getId(), inviteUserTestUser3.getId()}
        );
        proposalDAO.finalizeProposal(proposalCalendarFinalAccept.getId());
        proposalDAO.acceptProposal(inviteUserTestUser1.getId(), proposalCalendarFinalAccept.getId(), true);

        WebDriverManager.chromedriver().setup();
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'")) {
            deleteStmt.executeUpdate();
        }
    }

}

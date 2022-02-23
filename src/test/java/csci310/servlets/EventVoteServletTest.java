package csci310.servlets;

import com.google.gson.*;
import csci310.dao.BlockListDAO;
import csci310.dao.DAOBase;
import csci310.dao.ProposalDAO;
import csci310.dao.UserDAO;
import csci310.model.CanAttend;
import csci310.model.Proposal;
import csci310.model.User;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sqlite.SQLiteConfig;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class EventVoteServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDAO realProposalDAO;
    static private UserDAO realUserDAO;
    static private User testUser;
    static private User testUser2;
    static private Proposal testProposal;
    static private Proposal testProposalFinalized;
    static private Proposal testProposalVoteDraft;


    @BeforeClass
    public static void setUp() throws SQLException {
        realProposalDAO = new ProposalDAO(dbURL);
        realUserDAO = new UserDAO(dbURL);
        testUser = realUserDAO.createUser("__TEST__create_proposal_test_user", "123");
        testUser2 = realUserDAO.createUser("__TEST__create_proposal_user_1", "123");
        testProposal = realProposalDAO.createProposal(
                testUser.getId(),
                "__TEST__proposal",
                new String[]{"Z7r9jZ1AdFUqk", "vvG1HZpRndBscg", "G5v0Zpsu6_Nz-"},
                new int[]{testUser2.getId()}
        );
        testProposal = realProposalDAO.getProposalById(testProposal.getId()); // get all fields

        testProposalFinalized = realProposalDAO.createProposal(
                testUser.getId(),
                "__TEST__proposal_finalized",
                new String[]{"Z7r9jZ1AdFUqk", "vvG1HZpRndBscg", "G5v0Zpsu6_Nz-"},
                new int[]{testUser2.getId()}
        );
        realProposalDAO.finalizeProposal(testProposalFinalized.getId());

        testProposalVoteDraft =  realProposalDAO.createProposal(
                testUser.getId(),
                "__TEST__proposal_finalized",
                new String[]{"Z7r9jZ1AdFUqk", "vvG1HZpRndBscg", "G5v0Zpsu6_Nz-"},
                new int[]{testUser2.getId()}
        );
        testProposalVoteDraft = realProposalDAO.getProposalById(testProposalVoteDraft.getId()); // get all fields
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'"))
        {
            deleteStmt.executeUpdate();
        }
    }

    @Test
    public void testInit() {
        EventVoteServlet servlet = mock(EventVoteServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.proposalDAO);
    }

    @Test
    public void testDoPost() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("isDraft", new JsonPrimitive(false));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(testProposal.getEvents()[0].getId()));
        vote1.add("canAttend", new JsonPrimitive(CanAttend.YES.toString()));
        vote1.add("rating", new JsonPrimitive(1));
        votes.add(vote1);
        JsonObject vote2 = new JsonObject();
        vote2.add("eventId", new JsonPrimitive(testProposal.getEvents()[1].getId()));
        vote2.add("canAttend", new JsonPrimitive(CanAttend.NO.toString()));
        vote2.add("rating", new JsonPrimitive(2));
        votes.add(vote2);
        JsonObject vote3 = new JsonObject();
        vote3.add("eventId", new JsonPrimitive(testProposal.getEvents()[2].getId()));
        vote3.add("canAttend", new JsonPrimitive(CanAttend.MAYBE.toString()));
        vote3.add("rating", new JsonPrimitive(3));
        votes.add(vote3);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).getWriter();
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verifyNoMoreInteractions(response);

        Proposal deserializedProposal = gson.fromJson(stringWriter.toString(), Proposal.class);
        assertEquals(1, deserializedProposal.getEvents()[0].getVotes().length);
        assertEquals(testUser2.getId(), deserializedProposal.getEvents()[0].getVotes()[0].getUser().getId());
        assertEquals(CanAttend.YES, deserializedProposal.getEvents()[0].getVotes()[0].getCanAttend());
        assertEquals(1, (int)deserializedProposal.getEvents()[0].getVotes()[0].getRating());
    }

    @Test
    public void testDoPostDraft() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposalVoteDraft.getId()));
        requestData.add("isDraft", new JsonPrimitive(true));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(testProposalVoteDraft.getEvents()[0].getId()));
        vote1.add("canAttend", JsonNull.INSTANCE);
         vote1.add("rating", JsonNull.INSTANCE);
        votes.add(vote1);
        JsonObject vote2 = new JsonObject();
        vote2.add("eventId", new JsonPrimitive(testProposalVoteDraft.getEvents()[1].getId()));
        votes.add(vote2);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).getWriter();
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verifyNoMoreInteractions(response);

        Proposal deserializedProposal = gson.fromJson(stringWriter.toString(), Proposal.class);
        assertEquals(1, deserializedProposal.getEvents()[0].getVotes().length);
        assertEquals(testUser2.getId(), deserializedProposal.getEvents()[0].getVotes()[0].getUser().getId());
        assertNull(deserializedProposal.getEvents()[0].getVotes()[0].getCanAttend());
        assertNull(deserializedProposal.getEvents()[0].getVotes()[0].getRating());
    }

    @Test
    public void testDoPostNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.doPost(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostInvalidProposalId() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(-1));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUserNotOwnerOrInvited() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(-1);
        when(request.getReader()).thenReturn(reader);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostFinalized() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposalFinalized.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostInvalidEventId() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("isDraft", new JsonPrimitive(false));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(-1));
        vote1.add("canAttend", new JsonPrimitive(CanAttend.YES.toString()));
        vote1.add("rating", new JsonPrimitive(1));
        votes.add(vote1);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotDraftIncomplete() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("isDraft", new JsonPrimitive(false));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(testProposal.getEvents()[0].getId()));
        vote1.add("canAttend", new JsonPrimitive(CanAttend.YES.toString()));
        vote1.add("rating", new JsonPrimitive(1));
        votes.add(vote1);
        JsonObject vote2 = new JsonObject();
        vote2.add("eventId", new JsonPrimitive(testProposal.getEvents()[1].getId()));
        vote2.add("canAttend", new JsonPrimitive(CanAttend.NO.toString()));
        vote2.add("rating", new JsonPrimitive(2));
        votes.add(vote2);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotDraftMissingCanAttend() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("isDraft", new JsonPrimitive(false));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(testProposal.getEvents()[0].getId()));
        vote1.add("rating", new JsonPrimitive(1));
        votes.add(vote1);
        JsonObject vote2 = new JsonObject();
        vote2.add("eventId", new JsonPrimitive(testProposal.getEvents()[1].getId()));
        vote2.add("canAttend", new JsonPrimitive(CanAttend.NO.toString()));
        vote2.add("rating", new JsonPrimitive(2));
        votes.add(vote2);
        JsonObject vote3 = new JsonObject();
        vote3.add("eventId", new JsonPrimitive(testProposal.getEvents()[2].getId()));
        vote3.add("canAttend", new JsonPrimitive(CanAttend.MAYBE.toString()));
        vote3.add("rating", new JsonPrimitive(3));
        votes.add(vote3);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotDraftMissingRating() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("isDraft", new JsonPrimitive(false));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(testProposal.getEvents()[0].getId()));
        vote1.add("canAttend", new JsonPrimitive(CanAttend.YES.toString()));
        votes.add(vote1);
        JsonObject vote2 = new JsonObject();
        vote2.add("eventId", new JsonPrimitive(testProposal.getEvents()[1].getId()));
        vote2.add("canAttend", new JsonPrimitive(CanAttend.NO.toString()));
        vote2.add("rating", new JsonPrimitive(2));
        votes.add(vote2);
        JsonObject vote3 = new JsonObject();
        vote3.add("eventId", new JsonPrimitive(testProposal.getEvents()[2].getId()));
        vote3.add("canAttend", new JsonPrimitive(CanAttend.MAYBE.toString()));
        vote3.add("rating", new JsonPrimitive(3));
        votes.add(vote3);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostMalformedJson() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        BufferedReader reader = new BufferedReader(new StringReader("{"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        JsonArray votes = new JsonArray();
        JsonObject vote1 = new JsonObject();
        vote1.add("eventId", new JsonPrimitive(testProposal.getEvents()[0].getId()));
        vote1.add("canAttend", new JsonPrimitive(CanAttend.YES.toString()));
        vote1.add("rating", new JsonPrimitive(1));
        votes.add(vote1);
        requestData.add("votes", votes);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDAO proposalDAO = mock(ProposalDAO.class);
        when(proposalDAO.getProposalById(anyInt())).thenThrow(SQLException.class);

        EventVoteServlet servlet = new EventVoteServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verifyNoMoreInteractions(response);
    }
}
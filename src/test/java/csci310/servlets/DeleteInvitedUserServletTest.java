package csci310.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import csci310.dao.DAOBase;
import csci310.dao.ProposalDAO;
import csci310.dao.UserDAO;
import csci310.model.Proposal;
import csci310.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.sqlite.SQLiteConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DeleteInvitedUserServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDAO realProposalDAO;
    static private UserDAO realUserDAO;
    static private User testUser;
    static private User testUser2;
    static private Proposal testProposal;
    static private Proposal testProposalFinalized;

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
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'")) {
            deleteStmt.executeUpdate();
        }
    }

    @Test
    public void testInit() {
        DeleteInvitedUserServlet servlet = mock(DeleteInvitedUserServlet.class);
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
        requestData.add("invitedUserId", new JsonPrimitive(testUser2.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
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
        assertEquals(0, deserializedProposal.getInvitedUsers().length);
    }

    @Test
    public void testDoPostNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
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
        requestData.add("invitedUserId", new JsonPrimitive(testUser2.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUserNotOwner() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("invitedUserId", new JsonPrimitive(testUser2.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUserFinalized() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposalFinalized.getId()));
        requestData.add("invitedUserId", new JsonPrimitive(testUser2.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostInvalidInvitedUserId() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("proposalId", new JsonPrimitive(testProposal.getId()));
        requestData.add("invitedUserId", new JsonPrimitive(-1));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
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
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
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
        requestData.add("invitedUserId", new JsonPrimitive(testUser2.getId()));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDAO proposalDAO = mock(ProposalDAO.class);
        doCallRealMethod().when(proposalDAO).getProposalById(anyInt());
        doCallRealMethod().when(proposalDAO).getConnection();
        when(proposalDAO.deleteInvitedUser(anyInt(), anyInt())).thenThrow(SQLException.class);

        DeleteInvitedUserServlet servlet = new DeleteInvitedUserServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verifyNoMoreInteractions(response);
    }
}
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

public class DeleteProposalServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDAO realProposalDAO;
    static private UserDAO realUserDAO;
    static private User testUser;
    static private User testUser2;
    static private int testProposalId;
    static private int testProposalId2;

    @BeforeClass
    public static void setUp() throws SQLException {
        realProposalDAO = new ProposalDAO(dbURL);
        realUserDAO = new UserDAO(dbURL);
        testUser = realUserDAO.createUser("__TEST__create_proposal_test_user", "123");
        testUser2 = realUserDAO.createUser("__TEST__create_proposal_user_1", "123");
        testProposalId = realProposalDAO.createProposal(
                testUser.getId(),
                "__TEST__proposal",
                new String[]{"Z7r9jZ1AdFUqk", "vvG1HZpRndBscg", "G5v0Zpsu6_Nz-"},
                new int[]{testUser2.getId()}
        ).getId();
        testProposalId2 = realProposalDAO.createProposal(
                testUser.getId(),
                "__TEST__proposal",
                new String[]{"Z7r9jZ1AdFUqk", "vvG1HZpRndBscg", "G5v0Zpsu6_Nz-"},
                new int[]{testUser2.getId()}
        ).getId();
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
        DeleteProposalServlet servlet = mock(DeleteProposalServlet.class);
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
        requestData.add("proposalId", new JsonPrimitive(testProposalId));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        DeleteProposalServlet servlet = new DeleteProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        DeleteProposalServlet servlet = new DeleteProposalServlet();
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
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        DeleteProposalServlet servlet = new DeleteProposalServlet();
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
        requestData.add("proposalId", new JsonPrimitive(testProposalId2));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getReader()).thenReturn(reader);

        DeleteProposalServlet servlet = new DeleteProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
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

        DeleteProposalServlet servlet = new DeleteProposalServlet();
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
        requestData.add("proposalId", new JsonPrimitive(testProposalId));

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
        when(proposalDAO.deleteEvent(anyInt())).thenThrow(SQLException.class);

        DeleteProposalServlet servlet = new DeleteProposalServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verifyNoMoreInteractions(response);
    }
}
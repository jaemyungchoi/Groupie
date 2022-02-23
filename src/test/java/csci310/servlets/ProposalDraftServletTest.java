package csci310.servlets;

import com.google.gson.*;
import csci310.dao.DAOBase;
import csci310.dao.ProposalDAO;
import csci310.dao.ProposalDraftDAO;
import csci310.dao.UserDAO;
import csci310.model.Proposal;
import csci310.model.ProposalDraft;
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

public class ProposalDraftServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDraftDAO realProposalDraftDAO;
    static private UserDAO realUserDAO;
    static private User testUser1;
    static private User testUser2;
    static private int oldDraftId;
    static private int oldDraft2Id;

    @BeforeClass
    public static void setUp() throws SQLException {
        realProposalDraftDAO = new ProposalDraftDAO(dbURL);
        realUserDAO = new UserDAO(dbURL);
        testUser1 = realUserDAO.createUser("__TEST__create_proposal_test_user1", "123");
        testUser2 = realUserDAO.createUser("__TEST__create_proposal_test_user2", "123");
        oldDraftId = realProposalDraftDAO.createProposalDraft(testUser1.getId(), "__TEST__old", "{}").getId();
        oldDraft2Id = realProposalDraftDAO.createProposalDraft(testUser2.getId(), "__TEST__old", "{}").getId();
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
        ProposalDraftServlet servlet = mock(ProposalDraftServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.proposalDraftDAO);
    }

    @Test
    public void testDoPost() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__title"));
        requestData.add("someOtherData", new JsonPrimitive("123"));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).getWriter();
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostExplicitNullOldDraftId() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__title"));
        requestData.add("oldDraftId", JsonNull.INSTANCE);
        requestData.add("someOtherData", new JsonPrimitive("123"));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).getWriter();
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.doPost(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUpdate() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__title"));
        requestData.add("oldDraftId", new JsonPrimitive(oldDraftId));
        requestData.add("someOtherData", new JsonPrimitive("123"));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).getWriter();
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verifyNoMoreInteractions(response);

        ProposalDraft deserializedDraft = gson.fromJson(stringWriter.toString(), ProposalDraft.class);
        assertTrue(deserializedDraft.getId() == oldDraftId
                || realProposalDraftDAO.getProposalDraftById(oldDraftId) == null);
    }

    @Test
    public void testDoPostUpdateInvalidOldProposal() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__title"));
        requestData.add("oldDraftId", new JsonPrimitive(-1));
        requestData.add("someOtherData", new JsonPrimitive("123"));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUpdateNotOldProposalOwner() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__title"));
        requestData.add("oldDraftId", new JsonPrimitive(oldDraft2Id));
        requestData.add("someOtherData", new JsonPrimitive("123"));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
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
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
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
        requestData.add("title", new JsonPrimitive("__TEST__title"));
        requestData.add("someOtherData", new JsonPrimitive("123"));

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        ProposalDraftDAO proposalDraftDAO = mock(ProposalDraftDAO.class);
        when(proposalDraftDAO.createProposalDraft(anyInt(), anyString(), anyString())).thenThrow(SQLException.class);

        ProposalDraftServlet servlet = new ProposalDraftServlet();
        servlet.proposalDraftDAO = proposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verifyNoMoreInteractions(response);
    }
}
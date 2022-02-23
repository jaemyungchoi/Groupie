package csci310.servlets;

import com.google.gson.*;
import csci310.dao.*;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PostProposalServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDAO realProposalDAO;
    static private BlockListDAO realBlockListDAO;
    static private ProposalDraftDAO realProposalDraftDAO;
    static private UserDAO realUserDAO;
    static private User testUser;
    static private User testUser2;
    static private User blockedUser;
    static private int oldDraftId;
    static private int oldDraft2Id;

    @Captor
    private ArgumentCaptor<ArrayList<String>> errorMessageCaptor;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUp() throws SQLException {
        realProposalDAO = new ProposalDAO(dbURL);
        realUserDAO = new UserDAO(dbURL);
        realBlockListDAO = new BlockListDAO(dbURL);
        realProposalDraftDAO= new ProposalDraftDAO(dbURL);
        testUser = realUserDAO.createUser("__TEST__create_proposal_test_user", "123");
        testUser2 = realUserDAO.createUser("__TEST__create_proposal_user_1","123");
        blockedUser = realUserDAO.createUser("__TEST__create_proposal_user_2","123");
        realBlockListDAO.blockUserByName(testUser.getId(), blockedUser.getUsername());
        oldDraftId = realProposalDraftDAO.createProposalDraft(testUser.getId(), "__TEST__draft", "{}").getId();
        oldDraft2Id = realProposalDraftDAO.createProposalDraft(testUser2.getId(), "__TEST__draft", "{}").getId();
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
        PostProposalServlet servlet = mock(PostProposalServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.proposalDAO);
        assertNotNull(servlet.blockListDAO);
        assertNotNull(servlet.userDAO);
    }

    @Test
    public void testDoPost() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
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
    public void testDoPostNullDraft() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("oldDraftId", JsonNull.INSTANCE);
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
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
    public void testDoPostDraft() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        requestData.add("oldDraftId", new JsonPrimitive(oldDraftId));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).getWriter();
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verifyNoMoreInteractions(response);
        assertNull(realProposalDraftDAO.getProposalDraftById(oldDraftId));
    }

    @Test
    public void testDoPostInvalidDraft() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        requestData.add("oldDraftId", new JsonPrimitive(-1));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostDraftNotOwner() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        requestData.add("oldDraftId", new JsonPrimitive(oldDraft2Id));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.doPost(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostEmptyTitle() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive(""));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNoInvites() throws IOException, ServletException{
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNoEvents() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostMalformedJson() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);
        ProposalDAO proposalDAO = mock(ProposalDAO.class);

        BufferedReader reader = new BufferedReader(new StringReader("{"));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(proposalDAO.createProposal(anyInt(), anyString(), any(String[].class), any(int[].class))).thenThrow(SQLException.class);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUnexpectedSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);
        ProposalDAO proposalDAO = mock(ProposalDAO.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_user_1"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(proposalDAO.createProposal(anyInt(), anyString(), any(String[].class), any(int[].class))).thenThrow(SQLException.class);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUserIsBlocked() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__create_proposal_test_user"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(blockedUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostInvalidInvitedUser() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        Gson gson = new Gson();
        JsonObject requestData = new JsonObject();
        requestData.add("title", new JsonPrimitive("__TEST__proposal_title"));
        JsonArray users = new JsonArray();
        users.add(new JsonPrimitive("__TEST__nonexistent_user"));
        requestData.add("users", users);
        JsonArray events = new JsonArray();
        events.add(new JsonPrimitive("Z7r9jZ1AdFUqk"));
        events.add(new JsonPrimitive("vvG1HZpRndBscg"));
        events.add(new JsonPrimitive("G5v0Zpsu6_Nz"));
        requestData.add("events", events);

        String jsonData = gson.toJson(requestData);
        BufferedReader reader = new BufferedReader(new StringReader(jsonData));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getReader()).thenReturn(reader);
        when(response.getWriter()).thenReturn(writer);

        PostProposalServlet servlet = new PostProposalServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.blockListDAO = realBlockListDAO;
        servlet.userDAO = realUserDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getReader();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    //if no users at all, if the users are blocked
}
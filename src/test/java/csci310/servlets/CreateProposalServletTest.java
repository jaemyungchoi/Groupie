package csci310.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import csci310.dao.*;
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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CreateProposalServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDraftDAO realProposalDraftDAO;
    static private User testUser;
    static private User testUser2;
    static private User blockedUser;
    static int draftId;

    @Captor
    private ArgumentCaptor<ArrayList<String>> errorMessageCaptor;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setUp() throws SQLException {
        UserDAO userDAO = new UserDAO(dbURL);
        testUser = userDAO.createUser("__TEST__create_proposal_test_user", "123");
        testUser2 = userDAO.createUser("__TEST__create_proposal_test_user_2", "123");
        realProposalDraftDAO = new ProposalDraftDAO(dbURL);
        draftId = realProposalDraftDAO.createProposalDraft(testUser.getId(), "__TEST__draft", "{}").getId();
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
        CreateProposalServlet servlet = mock(CreateProposalServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.proposalDraftDAO);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getParameter("draft-id")).thenReturn(null);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        CreateProposalServlet servlet = new CreateProposalServlet();
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("draft-id");
        verify(request).getRequestDispatcher("/create-proposal.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoGetDraft() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getParameter("draft-id")).thenReturn(Integer.toString(draftId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        CreateProposalServlet servlet = new CreateProposalServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("draft-id");
        verify(request).setAttribute(eq("draft"), anyString());
        verify(request).setAttribute(eq("draftId"), anyInt());
        verify(request).getRequestDispatcher("/create-proposal.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoGetInvalidDraft() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getParameter("draft-id")).thenReturn("-1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        CreateProposalServlet servlet = new CreateProposalServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("draft-id");
        verify(request).setAttribute("error", "Proposal draft does not exist");
        verify(request).getRequestDispatcher("/create-proposal.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoGetNotDraftOwner() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getParameter("draft-id")).thenReturn(Integer.toString(draftId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        CreateProposalServlet servlet = new CreateProposalServlet();
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("draft-id");
        verify(request).setAttribute("error", "You don't have permission to access this draft");
        verify(request).getRequestDispatcher("/create-proposal.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoGetSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);
        ProposalDraftDAO proposalDraftDAO = mock(ProposalDraftDAO.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getParameter("draft-id")).thenReturn(Integer.toString(draftId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(proposalDraftDAO.getProposalDraftById(anyInt())).thenThrow(SQLException.class);

        CreateProposalServlet servlet = new CreateProposalServlet();
        servlet.proposalDraftDAO = proposalDraftDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("draft-id");
        verify(request).setAttribute("error", "Unexpected SQLException retrieving draft");
        verify(request).getRequestDispatcher("/create-proposal.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoGetNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        CreateProposalServlet servlet = new CreateProposalServlet();
        servlet.doGet(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).sendRedirect("/login");
        verifyNoMoreInteractions(response);

    }
}
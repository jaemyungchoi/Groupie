package csci310.servlets;

import csci310.dao.DAOBase;
import csci310.dao.ProposalDAO;
import csci310.dao.UserDAO;
import csci310.model.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
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

import static org.junit.Assert.*;

public class ProposalDetailsServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDAO realProposalDAO;
    static private UserDAO realUserDAO;
    static private User testUser;
    static private User testUser2;
    static private User testUser3;
    static private int testProposalId;

    @BeforeClass
    public static void setUp() throws SQLException {
        realProposalDAO = new ProposalDAO(dbURL);
        realUserDAO = new UserDAO(dbURL);
        testUser = realUserDAO.createUser("__TEST__create_proposal_test_user", "123");
        testUser2 = realUserDAO.createUser("__TEST__create_proposal_user_1", "123");
        testUser3 = realUserDAO.createUser("__TEST__create_proposal_user_3", "123");
        testProposalId = realProposalDAO.createProposal(
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
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'")) {
            deleteStmt.executeUpdate();
        }
    }

    @Test
    public void testInit() {
        ProposalDetailsServlet servlet = mock(ProposalDetailsServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.proposalDAO);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getParameter("id")).thenReturn(Integer.toString(testProposalId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalDetailsServlet servlet = new ProposalDetailsServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("id");
        verify(request).setAttribute("uid", testUser2.getId());
        verify(request).setAttribute(eq("proposal"), anyString());
        verify(request).getRequestDispatcher("/proposal-details.jsp");
        verify(dispatcher).forward(request, response);
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);
        when(request.getParameter("id")).thenReturn(Integer.toString(testProposalId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalDetailsServlet servlet = new ProposalDetailsServlet();
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("id");
        verify(request).setAttribute("error", "You need to log in to view this proposal.");
        verify(request).getRequestDispatcher("/proposal-details.jsp");
        verify(dispatcher).forward(request, response);
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetNoProposalId() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getParameter("id")).thenReturn(null);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalDetailsServlet servlet = new ProposalDetailsServlet();
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("id");
        verify(request).setAttribute("error", "Proposal not found.");
        verify(request).getRequestDispatcher("/proposal-details.jsp");
        verify(dispatcher).forward(request, response);
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetInvalidProposalId() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getParameter("id")).thenReturn("-1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalDetailsServlet servlet = new ProposalDetailsServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("id");
        verify(request).setAttribute("error", "Proposal not found.");
        verify(request).getRequestDispatcher("/proposal-details.jsp");
        verify(dispatcher).forward(request, response);
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetNoPermission() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser3.getId());
        when(request.getParameter("id")).thenReturn(Integer.toString(testProposalId));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalDetailsServlet servlet = new ProposalDetailsServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("id");
        verify(request).setAttribute("error", "You don't have permission to view this proposal.");
        verify(request).getRequestDispatcher("/proposal-details.jsp");
        verify(dispatcher).forward(request, response);
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        ProposalDAO proposalDAO = mock(ProposalDAO.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser2.getId());
        when(request.getParameter("id")).thenReturn(Integer.toString(testProposalId));
        when(proposalDAO.getProposalById(anyInt())).thenThrow(SQLException.class);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalDetailsServlet servlet = new ProposalDetailsServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getParameter("id");
        verify(request).setAttribute("error", "Unexpected SQLException.");
        verify(request).getRequestDispatcher("/proposal-details.jsp");
        verify(dispatcher).forward(request, response);
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }
}
package csci310.servlets;

import csci310.dao.*;
import csci310.model.Proposal;
import csci310.model.ProposalDraft;
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ProposalListServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private ProposalDAO realProposalDAO;
    static private ProposalDraftDAO realProposalDraftDAO;
    static private BlockListDAO realBlockListDAO;
    static private UserDAO realUserDAO;
    static private User testUser;
    static private User testUser2;
    static private User blockedUser;

    @BeforeClass
    public static void setUp() throws SQLException {
        realProposalDAO = new ProposalDAO(dbURL);
        realUserDAO = new UserDAO(dbURL);
        testUser = realUserDAO.createUser("__TEST__create_proposal_test_user", "123");
        realProposalDraftDAO = new ProposalDraftDAO(dbURL);
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
        ProposalListServlet servlet = mock(ProposalListServlet.class);
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
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        ProposalListServlet servlet = new ProposalListServlet();
        servlet.proposalDAO = realProposalDAO;
        servlet.proposalDraftDAO = realProposalDraftDAO;
        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/proposal-list.jsp");
        verify(request).getSession();
        verify(request).setAttribute(eq("proposalList"), anyString());
        verify(request).setAttribute(eq("proposalDrafts"), anyString());
        verify(request).setAttribute(eq("uid"), anyString());
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

        ProposalListServlet servlet = new ProposalListServlet();
        servlet.doGet(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).sendRedirect("/login");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ProposalDAO proposalDAO = mock(ProposalDAO.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(session.getAttribute("uid")).thenReturn(testUser.getId());
        when(proposalDAO.getProposalsByUser(anyInt())).thenThrow(SQLException.class);

        ProposalListServlet servlet = new ProposalListServlet();
        servlet.proposalDAO = proposalDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).setAttribute("error", "Unexpected SQLException");
        verify(request).getRequestDispatcher("/proposal-list.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }
}
package csci310.servlets;

import csci310.dao.BlockListDAO;
import csci310.dao.DAOBase;
import csci310.dao.ProposalDAO;
import csci310.dao.UserDAO;
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

public class BlockListServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private BlockListDAO realBlockListDAO;
    static private User testUser1;
    static private User testUser2;
    static private User testUser3;

//    @Captor
//    private ArgumentCaptor<ArrayList<String>> errorMessageCaptor;
//
//    @Before
//    public void init(){
//        MockitoAnnotations.initMocks(this);
//    }

    @BeforeClass
    public static void setUp() throws SQLException {
        realBlockListDAO = new BlockListDAO(dbURL);
        UserDAO userDAO = new UserDAO(dbURL);
        testUser1 = userDAO.createUser("__TEST__block_list_user_1", "123");
        testUser2 = userDAO.createUser("__TEST__block_list_user_2", "123");
        testUser3 = userDAO.createUser("__TEST__block_list_user_3", "123");
        realBlockListDAO.blockUserByName(testUser1.getId(), testUser3.getUsername());
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
        BlockListServlet servlet = mock(BlockListServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.blockListDAO);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(1);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        BlockListServlet servlet = new BlockListServlet();
        servlet.blockListDAO = realBlockListDAO;
        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/block-list.jsp");
        verify(request).getSession();
        verify(request).setAttribute(eq("block_list"), any(User[].class));
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoGetSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        HttpSession session = mock(HttpSession.class);
        BlockListDAO blockListDAO = mock(BlockListDAO.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(1);
        when(blockListDAO.getBlockListForUser(anyInt())).thenThrow(SQLException.class);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        BlockListServlet servlet = new BlockListServlet();

        servlet.blockListDAO = blockListDAO;
        servlet.doGet(request, response);

        verify(request).getSession();
        verify(request).getRequestDispatcher("/block-list.jsp");
        verify(request).setAttribute("error_message", "Unexpected SQL error.");
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

        BlockListServlet servlet = new BlockListServlet();
        servlet.doGet(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).sendRedirect("/login");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostBlock() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("action")).thenReturn("block");
        when(request.getParameter("blocked-user")).thenReturn(testUser2.getUsername());

        BlockListServlet servlet = new BlockListServlet();
        servlet.blockListDAO = realBlockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verifyNoMoreInteractions(request);
        verify(response).sendRedirect("/block-list");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUnblock() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("action")).thenReturn("unblock");
        when(request.getParameter("blocked-user")).thenReturn(Integer.toString(testUser2.getId()));

        BlockListServlet servlet = new BlockListServlet();
        servlet.blockListDAO = realBlockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("action");
        verify(request).getParameter("blocked-user");
        verifyNoMoreInteractions(request);
        verify(response).sendRedirect("/block-list");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        BlockListServlet servlet = new BlockListServlet();
        servlet.doPost(request, response);

        verify(request).getSession();
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostNoBlockedUser() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn(null);

        BlockListServlet servlet = new BlockListServlet();
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostInvalidAction() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn("123");
        when(request.getParameter("action")).thenReturn("123");

        BlockListServlet servlet = new BlockListServlet();
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verifyNoMoreInteractions(request);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostBlockInvalidBlockedUser() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        BlockListServlet servlet = mock(BlockListServlet.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn("random username that should not exist");
        when(request.getParameter("action")).thenReturn("block");
        doCallRealMethod().when(servlet).doPost(any(), any());

        servlet.blockListDAO = realBlockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verify(request).setAttribute("error_message", "Invalid username: random username that should not exist");
        verify(servlet).doGet(any(), any());
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostBlockAlreadyBlocked() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        BlockListServlet servlet = mock(BlockListServlet.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn(testUser3.getUsername());
        when(request.getParameter("action")).thenReturn("block");
        doCallRealMethod().when(servlet).doPost(any(), any());

        servlet.blockListDAO = realBlockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verify(request).setAttribute("error_message", testUser3.getUsername() + " is already in your block list.");
        verify(servlet).doGet(any(), any());
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostBlockSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        BlockListServlet servlet = mock(BlockListServlet.class);
        BlockListDAO blockListDAO = mock(BlockListDAO.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn("random username that should not exist");
        when(request.getParameter("action")).thenReturn("block");
        doCallRealMethod().when(servlet).doPost(any(), any());
        when(blockListDAO.blockUserByName(anyInt(), anyString())).thenThrow(SQLException.class);

        servlet.blockListDAO = blockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verify(request).setAttribute("error_message", "Unexpected SQL error.");
        verify(servlet).doGet(any(), any());
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUnblockInvalidBlockedUser() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        BlockListServlet servlet = mock(BlockListServlet.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn("-1");
        when(request.getParameter("action")).thenReturn("unblock");
        doCallRealMethod().when(servlet).doPost(any(), any());

        servlet.blockListDAO = realBlockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verify(request).setAttribute("error_message", "Invalid uid: -1");
        verify(servlet).doGet(any(), any());
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoPostUnblockSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        BlockListServlet servlet = mock(BlockListServlet.class);
        BlockListDAO blockListDAO = mock(BlockListDAO.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("blocked-user")).thenReturn("1");
        when(request.getParameter("action")).thenReturn("unblock");
        doCallRealMethod().when(servlet).doPost(any(), any());
        when(blockListDAO.unblockUserById(anyInt(), anyInt())).thenThrow(SQLException.class);

        servlet.blockListDAO = blockListDAO;
        servlet.doPost(request, response);

        verify(request).getSession();
        verify(request).getParameter("blocked-user");
        verify(request).getParameter("action");
        verify(request).setAttribute("error_message", "Unexpected SQL error.");
        verify(servlet).doGet(any(), any());
        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(response);
    }
}
package csci310.servlets;

import csci310.dao.DAOBase;
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
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class LogInServletTest extends Mockito{

    static private final String dbURL = System.getProperty("dbUrl");

    @Captor
    private ArgumentCaptor<ArrayList<String>> errorMessageCaptor;

    static private UserDAO realUserDAO;

    @BeforeClass
    public static void setUp() throws SQLException {
        realUserDAO = new UserDAO(dbURL);
        realUserDAO.createUser("__TEST__existing_user", "123");
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'"))
        {
            deleteStmt.executeUpdate();
        }
    }

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        LogInServlet servlet = new LogInServlet();
        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/login.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPost() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        LogInServlet servlet = new LogInServlet();
        servlet.dao = new UserDAO(dbURL);

        String username = "__TEST__Bobby";
        String password = "1234";
        User u = servlet.dao.createUser(username, password);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("username", username);
        verify(session).setAttribute("uid", u.getId());
        verify(response).sendRedirect("/");
    }

    @Test
    public void testDoPostNonexistentUser() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        LogInServlet servlet = new LogInServlet();
        servlet.dao = new UserDAO(dbURL);

        String username = "blahblahblah";
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn("1234");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(request).setAttribute("username_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Username does not exist", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostIncorrectPassword() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        LogInServlet servlet = new LogInServlet();
        servlet.dao = new UserDAO(dbURL);

        String username = "__TEST__David";
        String password = "1234";
        User u = servlet.dao.createUser(username, password);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn("1235");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(request).setAttribute("password_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Incorrect Password", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostIncorrectPasswordTooManyTries() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        LogInServlet servlet = new LogInServlet();
        servlet.dao = new UserDAO(dbURL);

        String username = "__TEST__David2";
        String password = "1234";
        User u = servlet.dao.createUser(username, password);

        Queue<Date> attemptsDates = new ArrayDeque<>();
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date aWhileAgo = calendar.getTime();
        attemptsDates.add(aWhileAgo);
        attemptsDates.add(now);
        attemptsDates.add(now);
        attemptsDates.add(now);
        Map<Integer, Queue<Date>> Attempts = new HashMap<>();
        Attempts.put(u.getId(), attemptsDates);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn("1235");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("AttemptMap")).thenReturn(Attempts);

        servlet.doPost(request, response);

        verify(request).setAttribute("password_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Too many attempts. You have been locked out.", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostEmptyUsername() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("password")).thenReturn("test_password");

        LogInServlet servlet = new LogInServlet();

        servlet.doPost(request, response);

        verify(request).setAttribute("username_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Username cannot be empty", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostNullUsername() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("test_password");

        LogInServlet servlet = new LogInServlet();

        servlet.doPost(request, response);

        verify(request).setAttribute("username_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Username cannot be empty", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostEmptyPassword() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("test_username");
        when(request.getParameter("password")).thenReturn("");

        LogInServlet servlet = new LogInServlet();

        servlet.doPost(request, response);

        verify(request).setAttribute("password_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Password cannot be empty", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostNullPassword() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("test_username");
        when(request.getParameter("password")).thenReturn(null);

        LogInServlet servlet = new LogInServlet();

        servlet.doPost(request, response);

        verify(request).setAttribute("password_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Password cannot be empty", errorMessages.get(0));

        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

//    @Test
//    public void testDoGet() throws IOException, ServletException {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        HttpSession session = mock(HttpSession.class);
//        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
//
//        when(request.getParameter("username")).thenReturn("test_username");
//        when(request.getParameter("password")).thenReturn("test_password");
//        when(request .getSession()).thenReturn(session);
//
//        LogInServlet servlet = new LogInServlet();
//
//        servlet.doGet(request, response);
//
//        verify(session).setAttribute("username", "test_username");
//        verify(response).sendRedirect("/");
//    }
//
//    @Test
//    public void testDoGetEmptyUsername() throws IOException, ServletException {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        HttpSession session = mock(HttpSession.class);
//        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
//
//        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
//        when(request.getParameter("username")).thenReturn("");
//        when(request.getParameter("password")).thenReturn("test_password");
//
//        LogInServlet servlet = new LogInServlet();
//
//        servlet.doGet(request, response);
//
//        verify(request).setAttribute("error", "Invalid username/password");
//        verify(request).getRequestDispatcher("/login.jsp");
//        verify(dispatcher).forward(any(), any());
//    }
//
//    @Test
//    public void testDoGetEmptyPassword() throws IOException, ServletException {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        HttpSession session = mock(HttpSession.class);
//        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
//
//        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
//        when(request.getParameter("username")).thenReturn("test_username");
//        when(request.getParameter("password")).thenReturn("");
//
//        LogInServlet servlet = new LogInServlet();
//
//        servlet.doGet(request, response);
//
//        verify(request).setAttribute("error", "Invalid username/password");
//        verify(request).getRequestDispatcher("/login.jsp");
//        verify(dispatcher).forward(any(), any());
//    }
//
//    @Test
//    public void testDoGetNullUsername() throws IOException, ServletException {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        HttpSession session = mock(HttpSession.class);
//        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
//
//        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
//        when(request.getParameter("username")).thenReturn(null);
//        when(request.getParameter("password")).thenReturn("test_password");
//
//        LogInServlet servlet = new LogInServlet();
//
//        servlet.doGet(request, response);
//
//        verify(request).setAttribute("error", "Invalid username/password");
//        verify(request).getRequestDispatcher("/login.jsp");
//        verify(dispatcher).forward(any(), any());
//    }
//
//    @Test
//    public void testDoGetNullPassword() throws IOException, ServletException {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        HttpSession session = mock(HttpSession.class);
//        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
//
//        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
//        when(request.getParameter("username")).thenReturn("test_username");
//        when(request.getParameter("password")).thenReturn(null);
//
//        LogInServlet servlet = new LogInServlet();
//
//        servlet.doGet(request, response);
//
//        verify(request).setAttribute("error", "Invalid username/password");
//        verify(request).getRequestDispatcher("/login.jsp");
//        verify(dispatcher).forward(any(), any());
//    }


// Previously commented

    @Test
    public void testDoPostSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        UserDAO userDAO = mock(UserDAO.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("test_username");
        when(request.getParameter("password")).thenReturn("test_password");
        when(userDAO.getUserByName(anyString())).thenThrow(SQLException.class);

        LogInServlet servlet = new LogInServlet();
        servlet.dao = userDAO;

        servlet.doPost(request, response);

        verify(request).setAttribute("username_error", true);
        verify(request).getRequestDispatcher("/login.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testInit() {
        LogInServlet servlet = mock(LogInServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.dao);
    }

//    @Test
//    public void testDoPostUnexpectedError() throws IOException, ServletException, SQLException {
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        HttpSession session = mock(HttpSession.class);
//        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
//
//        when(request.getParameter("username")).thenReturn("");
//        when(request.getParameter("password")).thenReturn("test_password");
//        when(request .getSession()).thenReturn(session);
//        when(request.getRequestDispatcher(anyString())).thenThrow(IOException.class);
//
//        LogInServlet servlet = new LogInServlet();
//
//        servlet.doGet(request, response);
//
//        verify(request).setAttribute("ExceptionError", "Exception error caught");
//    }
}
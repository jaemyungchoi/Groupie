package csci310.servlets;

import csci310.dao.DAOBase;
import csci310.dao.UserDAO;
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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SignUpServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private UserDAO realUserDAO;

    @Captor
    private ArgumentCaptor<ArrayList<String>> errorMessageCaptor;

    @BeforeClass
    public static void setUp() throws SQLException {
        realUserDAO = new UserDAO(dbURL);
        realUserDAO.createUser("__TEST__existing_user", "123");
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

        SignUpServlet servlet = new SignUpServlet();
        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/signup.jsp");
        verifyNoMoreInteractions(request);
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPost() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getParameter("username")).thenReturn("__TEST__Jerry");
        when(request.getParameter("password")).thenReturn("test123");
        when(request.getParameter("password2")).thenReturn("test123");
        when(request .getSession()).thenReturn(session);

        SignUpServlet servlet = new SignUpServlet();
        servlet.userDAO = realUserDAO;

        servlet.doPost(request, response);

        verify(session).setAttribute("username", "__TEST__Jerry");
        verify(session).setAttribute(eq("uid"), anyInt());
        verify(response).sendRedirect("/");

    }

    @Test
    public void testDoPostEmptyUsername() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        UserDAO userDAO = mock(UserDAO.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("password")).thenReturn("test123");
        when(request.getParameter("password2")).thenReturn("test123");

        SignUpServlet servlet = new SignUpServlet();
        servlet.userDAO = userDAO;

        servlet.doPost(request, response);

        verify(request).setAttribute("form_error", true);
        verify(request).setAttribute("username_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());
        verify(request).setAttribute("username", "");
        verify(userDAO, never()).createUser(anyString(), anyString());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Username cannot be empty.", errorMessages.get(0));

        verify(request).getRequestDispatcher("/signup.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostMismatchPassword() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        UserDAO userDAO = mock(UserDAO.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("__TEST__James");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getParameter("password2")).thenReturn("321");

        SignUpServlet servlet = new SignUpServlet();
        servlet.userDAO = userDAO;

        servlet.doPost(request, response);

        verify(request).setAttribute("form_error", true);
        verify(request).setAttribute("password_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());
        verify(request).setAttribute("username", "__TEST__James");
        verify(userDAO, never()).createUser(anyString(), anyString());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Passwords must match.", errorMessages.get(0));

        verify(request).getRequestDispatcher("/signup.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostEmptyPassword() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        UserDAO userDAO = mock(UserDAO.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("__TEST__James");
        when(request.getParameter("password")).thenReturn("");
        when(request.getParameter("password2")).thenReturn("");

        SignUpServlet servlet = new SignUpServlet();
        servlet.userDAO = userDAO;

        servlet.doPost(request, response);

        verify(request).setAttribute("form_error", true);
        verify(request).setAttribute("password_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());
        verify(request).setAttribute("username", "__TEST__James");
        verify(userDAO, never()).createUser(anyString(), anyString());

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Password cannot be empty.", errorMessages.get(0));

        verify(request).getRequestDispatcher("/signup.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostExistingUser() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("__TEST__existing_user");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("password2")).thenReturn("password");

        SignUpServlet servlet = new SignUpServlet();
        servlet.userDAO = realUserDAO;

        servlet.doPost(request, response);

        verify(request).setAttribute("form_error", true);
        verify(request).setAttribute("username_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());
        verify(request).setAttribute("username", "__TEST__existing_user");

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Username already in use. Please choose another one.", errorMessages.get(0));

        verify(request).getRequestDispatcher("/signup.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostUnexpectedSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        UserDAO userDAO = mock(UserDAO.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getParameter("username")).thenReturn("__TEST__Valid User");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getParameter("password2")).thenReturn("123");
        when(userDAO.createUser(anyString(), anyString())).thenThrow(SQLException.class);

        SignUpServlet servlet = new SignUpServlet();
        servlet.userDAO = userDAO;

        servlet.doPost(request, response);

        verify(request).setAttribute("form_error", true);
        verify(request).setAttribute(eq("error_messages"), errorMessageCaptor.capture());
        verify(request).setAttribute("username", "__TEST__Valid User");

        ArrayList<String> errorMessages = errorMessageCaptor.getValue();

        assertEquals(1, errorMessages.size());
        assertEquals("Unexpected SQL error.", errorMessages.get(0));

        verify(request).getRequestDispatcher("/signup.jsp");
        verify(dispatcher).forward(any(), any());
    }

    @Test
    public void testDoPostBadRequestUsernameMissing() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);
        when(request.getParameter("password2")).thenReturn(null);

        SignUpServlet servlet = new SignUpServlet();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
        verify(request).getParameter("username");
        verify(request).getParameter("password");
        verify(request).getParameter("password2");
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testDoPostBadRequestPasswordMissing() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("username")).thenReturn("Name");
        when(request.getParameter("password")).thenReturn(null);
        when(request.getParameter("password2")).thenReturn(null);

        SignUpServlet servlet = new SignUpServlet();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
        verify(request).getParameter("username");
        verify(request).getParameter("password");
        verify(request).getParameter("password2");
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testDoPostBadRequestPassword2Missing() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("username")).thenReturn("Name");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("password2")).thenReturn(null);

        SignUpServlet servlet = new SignUpServlet();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
        verify(request).getParameter("username");
        verify(request).getParameter("password");
        verify(request).getParameter("password2");
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testInit() {
        SignUpServlet servlet = mock(SignUpServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        servlet.init();
        assertNotNull(servlet.userDAO);
    }

    @AfterClass
    public static void removeTestUsers() throws SQLException {
        try (Connection connection = new DAOBase(dbURL).getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM users WHERE name LIKE '__TEST__%'"))
        {
            deleteStmt.executeUpdate();
        }
    }
}
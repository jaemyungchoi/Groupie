package csci310.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import csci310.dao.AutocompleteDAO;
import csci310.dao.UserDAO;
import csci310.model.AutocompleteCandidate;
import csci310.model.User;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AutocompleteServletTest extends Mockito {

    static private final String dbURL = System.getProperty("dbUrl");
    static private AutocompleteDAO realAutocompleteDAO;
    static private User testUser1;
    static private User testUser2;
    static private User testUser3;
    static private User testUser4;
    static private User testUser5;
    static private int num_test_users = 5;

    @BeforeClass
    public static void setUp() throws SQLException {
        realAutocompleteDAO = new AutocompleteDAO(dbURL);
        UserDAO userDAO = new UserDAO(dbURL);
        testUser1 = userDAO.createUser("__TEST__ac_servlet_user_1", "123");
        testUser2 = userDAO.createUser("__TEST__ac_servlet_user_2", "123");
        testUser3 = userDAO.createUser("__TEST__ac_servlet_user_3", "123");
        testUser4 = userDAO.createUser("__TEST__ac_servlet_user_4", "123");
        testUser5 = userDAO.createUser("__TEST__ac_servlet_user_5", "123");
    }

    @Test
    public void testInit() {
        AutocompleteServlet servlet = mock(AutocompleteServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("dbURL")).thenReturn("dbURL");
        when(context.getInitParameter("autocompleteCandidateLimit")).thenReturn("20");
        servlet.init();
        assertNotNull(servlet.autocompleteDAO);
        assertEquals(20, servlet.maxCandidateCount);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("prefix")).thenReturn("__TEST__ac_servlet_user");
        when(response.getWriter()).thenReturn(writer);

        AutocompleteServlet servlet = new AutocompleteServlet();
        servlet.autocompleteDAO = realAutocompleteDAO;
        servlet.maxCandidateCount = 20;

        ArgumentCaptor<String> result = ArgumentCaptor.forClass(String.class);

        servlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(anyInt());
        verify(response).getWriter();
        verify(writer).write(result.capture());
        verify(writer).flush();
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(writer);

        Gson gson = new Gson();
        JsonObject jsonResult = gson.fromJson(result.getValue(), JsonObject.class);
        assertFalse(jsonResult.get("partial").getAsBoolean());
        String candidatesAsString = jsonResult.getAsJsonArray("candidates").toString();
        AutocompleteCandidate[] parsedCandidates = gson.fromJson(candidatesAsString, AutocompleteCandidate[].class);
        assertEquals(num_test_users - 1, parsedCandidates.length);
        assertEquals("__TEST__ac_servlet_user_2", parsedCandidates[0].getUsername());
        assertEquals("__TEST__ac_servlet_user_3", parsedCandidates[1].getUsername());
        assertEquals("__TEST__ac_servlet_user_4", parsedCandidates[2].getUsername());
        assertEquals("__TEST__ac_servlet_user_5", parsedCandidates[3].getUsername());
    }

    @Test
    public void testDoGetNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        AutocompleteServlet servlet = new AutocompleteServlet();
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetMaxOneCandidate() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("prefix")).thenReturn("__TEST__ac_servlet_user");
        when(response.getWriter()).thenReturn(writer);

        AutocompleteServlet servlet = new AutocompleteServlet();
        servlet.autocompleteDAO = realAutocompleteDAO;
        servlet.maxCandidateCount = 1;

        ArgumentCaptor<String> result = ArgumentCaptor.forClass(String.class);

        servlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(anyInt());
        verify(response).getWriter();
        verify(writer).write(result.capture());
        verify(writer).flush();
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(writer);

        Gson gson = new Gson();
        JsonObject jsonResult = gson.fromJson(result.getValue(), JsonObject.class);
        assertTrue(jsonResult.get("partial").getAsBoolean());
        assertEquals(1, jsonResult.getAsJsonArray("candidates").size());
    }

    @Test
    public void testDoGetMaxThreeCandidates() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("prefix")).thenReturn("__TEST__ac_servlet_user");
        when(response.getWriter()).thenReturn(writer);

        AutocompleteServlet servlet = new AutocompleteServlet();
        servlet.autocompleteDAO = realAutocompleteDAO;
        servlet.maxCandidateCount = 3;

        ArgumentCaptor<String> result = ArgumentCaptor.forClass(String.class);

        servlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(anyInt());
        verify(response).getWriter();
        verify(writer).write(result.capture());
        verify(writer).flush();
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(writer);

        Gson gson = new Gson();
        JsonObject jsonResult = gson.fromJson(result.getValue(), JsonObject.class);
        assertTrue(jsonResult.get("partial").getAsBoolean());
        assertEquals(3, jsonResult.getAsJsonArray("candidates").size());
    }

    @Test
    public void testDoGetSQLException() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        AutocompleteDAO autocompleteDAO = mock(AutocompleteDAO.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("prefix")).thenReturn("__TEST__ac_servlet_user");
        when(autocompleteDAO.getAutocompleteCandidates(anyInt(), anyString(), anyInt())).thenThrow(SQLException.class);

        AutocompleteServlet servlet = new AutocompleteServlet();
        servlet.autocompleteDAO = autocompleteDAO;
        servlet.maxCandidateCount = 20;

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verifyNoMoreInteractions(response);
    }

    @Test
    public void testDoGetNullPrefix() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(testUser1.getId());
        when(request.getParameter("prefix")).thenReturn(null);

        AutocompleteServlet servlet = new AutocompleteServlet();

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoMoreInteractions(response);
    }
}
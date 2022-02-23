package csci310.servlets;

import csci310.dao.UserDAO;
import csci310.model.User;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static org.junit.Assert.*;

public class LogOutServletTest extends Mockito {

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);

        LogOutServlet servlet = new LogOutServlet();
        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/login");
    }
}
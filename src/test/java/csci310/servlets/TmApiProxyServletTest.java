package csci310.servlets;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TmApiProxyServletTest extends Mockito {

    @Test
    public void testInit() {
        TmApiProxyServlet servlet = mock(TmApiProxyServlet.class);
        ServletContext context = mock(ServletContext.class);
        when(servlet.getServletContext()).thenReturn(context);
        doCallRealMethod().when(servlet).init();
        when(context.getInitParameter("ticketmasterApiURL")).thenReturn("test123");
        when(context.getInitParameter("ticketmasterApiToken")).thenReturn("test123");
        servlet.init();
        assertNotNull(servlet.apiURL);
        assertNotNull(servlet.apiToken);
    }

    @Test
    public void testDoGet() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        HttpSession session = mock(HttpSession.class);

        Map<String, String[]> params = new HashMap<>();
        params.put("keyword", new String[]{"abc"});
        params.put("locale", new String[]{"*"});

        when(request.getSession()).thenReturn(session);
        when(request.getParameterMap()).thenReturn(params);
        when(response.getWriter()).thenReturn(writer);
        when(session.getAttribute("uid")).thenReturn(0);

        TmApiProxyServlet servlet = new TmApiProxyServlet();
        // since init() is not called, we need to init the member variables here
        servlet.apiURL = "https://app.ticketmaster.com/discovery/v2/events";
        servlet.apiToken = "qq7RqATbnDHFtSJHo3QUJ9f6uMd5cGNn";
        servlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).getWriter();
        verify(response).setStatus(anyInt());
        verify(writer).write(anyString());
        verify(writer).flush();
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testDoGetNotLoggedIn() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        TmApiProxyServlet servlet = new TmApiProxyServlet();
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoMoreInteractions(response);
    }
}
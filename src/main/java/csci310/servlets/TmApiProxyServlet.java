package csci310.servlets;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class TmApiProxyServlet extends HttpServlet {

    String apiURL;
    String apiToken;

    @Override
    public void init() {
        apiURL = getServletContext().getInitParameter("ticketmasterApiURL");
        apiToken = getServletContext().getInitParameter("ticketmasterApiToken");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Object uid = request.getSession().getAttribute("uid");
        if (uid == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(apiURL).newBuilder();
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String,String[]> entry : params.entrySet()) {
            for (String val : entry.getValue()) {
                urlBuilder.addQueryParameter(entry.getKey(), val);
            }
        }
        urlBuilder.addQueryParameter("apikey", apiToken);

        OkHttpClient client = new OkHttpClient();
        Request apiRequest = new Request.Builder()
                .url(urlBuilder.build())
                .build();
        Response apiResponse = client.newCall(apiRequest).execute();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(apiResponse.code());
        PrintWriter writer = response.getWriter();
        writer.write(apiResponse.body().string());
        writer.flush();
    }
}

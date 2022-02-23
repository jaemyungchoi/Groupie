package csci310.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import csci310.dao.AutocompleteDAO;
import csci310.dao.ProposalDAO;
import csci310.model.AutocompleteCandidate;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class AutocompleteServlet extends HttpServlet {

    AutocompleteDAO autocompleteDAO;
    int maxCandidateCount = -1;

    @Override
    public void init() {
        autocompleteDAO = new AutocompleteDAO(getServletContext().getInitParameter("dbURL"));
        maxCandidateCount = Integer.parseInt(getServletContext().getInitParameter("autocompleteCandidateLimit"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // make sure the user has logged in
        Object uid = request.getSession().getAttribute("uid");
        if (uid == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // if missing any field, return 400
        String prefix = request.getParameter("prefix");
        if (prefix == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        AutocompleteCandidate[] candidates;
        try {
            candidates = autocompleteDAO.getAutocompleteCandidates((Integer)uid, prefix, maxCandidateCount + 1);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Gson gson = new Gson();
        JsonArray candidatesArray = gson.toJsonTree(candidates).getAsJsonArray();
        boolean partialResults = false;
        // if candidates has one more than maxCandidateCount then it means the results isn't complete
        if (candidates.length > maxCandidateCount) {
            partialResults = true;
            // make candidatesArray exactly maxCandidateCount in size
            candidatesArray.remove(candidatesArray.size() - 1);
        }

        JsonObject result = new JsonObject();
        result.add("candidates", candidatesArray);
        result.addProperty("partial", partialResults);

        String serializedResult = gson.toJson(result);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = response.getWriter();
        writer.write(serializedResult);
        writer.flush();
    }
}

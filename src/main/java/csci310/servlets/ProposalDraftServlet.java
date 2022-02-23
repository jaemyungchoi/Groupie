package csci310.servlets;

import com.google.gson.*;
import csci310.dao.ProposalDAO;
import csci310.dao.ProposalDraftDAO;
import csci310.model.CanAttend;
import csci310.model.Proposal;
import csci310.model.ProposalDraft;
import csci310.model.Vote;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class ProposalDraftServlet extends HttpServlet {

    ProposalDraftDAO proposalDraftDAO;

    @Override
    public void init() {
        proposalDraftDAO = new ProposalDraftDAO(getServletContext().getInitParameter("dbURL"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession();
        Object uidObj = session.getAttribute("uid");
        // user must be logged in
        if (uidObj == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int uid = (Integer) uidObj;

        Reader reader = request.getReader();
        Gson gson = new Gson();
        try {
            JsonObject requestData = gson.fromJson(reader, JsonObject.class);
            String title = requestData.get("title").getAsString();

            if (requestData.has("oldDraftId") && !requestData.get("oldDraftId").isJsonNull()) {
                int oldDraftId = requestData.get("oldDraftId").getAsInt();
                ProposalDraft oldDraft = proposalDraftDAO.getProposalDraftById(oldDraftId);
                if (oldDraft == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                } else if (oldDraft.getOwner().getId() != uid) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                proposalDraftDAO.deleteProposalDraft(oldDraftId);
                requestData.remove("oldDraftId");
            }

            ProposalDraft draft = proposalDraftDAO.createProposalDraft(uid, title, gson.toJson(requestData));

            String draftJson = gson.toJson(draft);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter writer = response.getWriter();
            writer.write(draftJson);
            writer.flush();
        } catch (JsonSyntaxException | JsonIOException // error deserializing json
                | ClassCastException | IllegalStateException | NullPointerException  // error parsing or accessing elements
                | IllegalArgumentException  // invalid title value
                ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

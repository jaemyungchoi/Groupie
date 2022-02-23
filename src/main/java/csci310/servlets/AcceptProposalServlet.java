package csci310.servlets;

import com.google.gson.*;
import csci310.dao.ProposalDAO;
import csci310.model.CanAttend;
import csci310.model.Proposal;
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

public class AcceptProposalServlet extends HttpServlet {

    ProposalDAO proposalDAO;

    @Override
    public void init() {
        proposalDAO = new ProposalDAO(getServletContext().getInitParameter("dbURL"));
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
            JsonElement requestData = gson.fromJson(reader, JsonElement.class);
            int proposalId = requestData.getAsJsonObject().get("proposalId").getAsInt();
            Proposal proposal = proposalDAO.getProposalById(proposalId);

            // make sure proposal is valid
            if (proposal == null) {
                // nonexistent proposal
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            } else if (!proposal.canAccess(uid)) {
                // make sure the user can access on that proposal
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            } else if (!proposal.isFinalized()) {
                // can't accept/decline if proposal is not finalized
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean accept = requestData.getAsJsonObject().get("accept").getAsBoolean();

            proposalDAO.acceptProposal(uid, proposalId, accept);

            // return updated proposal
            proposal = proposalDAO.getProposalById(proposalId);
            String proposalJson = gson.toJson(proposal);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter writer = response.getWriter();
            writer.write(proposalJson);
            writer.flush();
        } catch (JsonSyntaxException | JsonIOException // error deserializing json
                | ClassCastException | IllegalStateException | NullPointerException  // error parsing or accessing elements
                | IllegalArgumentException  // invalid accept value
                ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

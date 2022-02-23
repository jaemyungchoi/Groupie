package csci310.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import csci310.dao.ProposalDAO;
import csci310.model.Proposal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;

public class FinalizeProposalServlet extends HttpServlet {

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
            } else if (proposal.getOwner().getId() != uid) {
                // make sure the user is the owner
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            } else if (proposal.isFinalized()) {
                // can't finalize is already finalized
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // do the actual finalization and return updated proposal
            proposal = proposalDAO.finalizeProposal(proposalId);
            String proposalJson = gson.toJson(proposal);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter writer = response.getWriter();
            writer.write(proposalJson);
            writer.flush();
        } catch (JsonSyntaxException | JsonIOException // error deserializing json
                | ClassCastException | IllegalStateException | NullPointerException  // error parsing or accessing elements
                | IllegalArgumentException  // invalid canAttend value
                ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

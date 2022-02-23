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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class EventVoteServlet extends HttpServlet {

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
            } else if (!proposal.canAccess(uid)) { // make sure the user can vote on that proposal
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            } else if (proposal.isFinalized()) {
                // can't vote if proposal is already finalized
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean isDraft = requestData.getAsJsonObject().get("isDraft").getAsBoolean();
            // parse votes and make sure all events are child of the proposal
            JsonArray requestVotesArray = requestData.getAsJsonObject().get("votes").getAsJsonArray();
            Map<Integer, Vote> votes = new TreeMap<>();
            for (JsonElement voteElement : requestVotesArray) {
                JsonObject voteObj = voteElement.getAsJsonObject();
                int eventId = voteObj.get("eventId").getAsInt();
                CanAttend canAttend = null;
                if (voteObj.has("canAttend")) {
                    if (!voteObj.get("canAttend").isJsonNull()) {
                        canAttend = CanAttend.valueOf(voteObj.get("canAttend").getAsString());
                    }
                }
                Integer rating = null;
                if (voteObj.has("rating")) {
                    if (!voteObj.get("rating").isJsonNull()) {
                        rating = voteObj.get("rating").getAsInt();
                    }
                }

                if (!proposal.hasEvent(eventId)) {
                    // bad event id
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                votes.put(eventId, new Vote(0, null, canAttend, rating, false));
            }

            if (!isDraft) {
                if (votes.size() != proposal.getEvents().length) {
                    // if votes are final, all events must have matching votes
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                for (Vote vote : votes.values()) {
                    if (vote.getCanAttend() == null || vote.getRating() == null) {
                        // if votes are final, all votes can't have null canAttend / rating
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                }
            }

            // do the actual voting
            for (Map.Entry<Integer, Vote> entry : votes.entrySet()) {
                proposalDAO.createVote(uid, entry.getKey(), entry.getValue().getCanAttend(), entry.getValue().getRating(), isDraft);
            }

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
                | IllegalArgumentException  // invalid canAttend value
                ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

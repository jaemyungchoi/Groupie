package csci310.servlets;

import com.google.gson.*;
import csci310.dao.BlockListDAO;
import csci310.dao.ProposalDAO;
import csci310.dao.ProposalDraftDAO;
import csci310.dao.UserDAO;
import csci310.model.Proposal;
import csci310.model.ProposalDraft;
import csci310.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostProposalServlet extends HttpServlet {

    ProposalDAO proposalDAO;
    UserDAO userDAO;
    BlockListDAO blockListDAO;
    ProposalDraftDAO proposalDraftDAO;

    @Override
    public void init() {
        proposalDAO = new ProposalDAO(getServletContext().getInitParameter("dbURL"));
        userDAO = new UserDAO(getServletContext().getInitParameter("dbURL"));
        blockListDAO = new BlockListDAO(getServletContext().getInitParameter("dbURL"));
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
            List<String> events = new ArrayList<>();
            for (JsonElement eventElem : requestData.getAsJsonArray("events")) {
                events.add(eventElem.getAsString());
            }

            JsonArray invited = requestData.getAsJsonArray("users");
            List<String> users = new ArrayList<>();
            List<Integer> invitedUserIds = new ArrayList<>();
            // Get Block List for User
            User[] blockedBy = blockListDAO.getUsersWhoBlockMe(uid);
            for (JsonElement userElem : invited) {
                String user = userElem.getAsString();
                //if the username is an empty string, "" it will be handled by the fact that it's not a real username
                User invitedUser = userDAO.getUserByName(user); // checkValid.getList() <- this
                if (invitedUser == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                //if the user is valid, check to see if it is on the blocked list for that user
                boolean isBlocked = Arrays.stream(blockedBy)
                        .anyMatch(blockedByUser -> blockedByUser.getUsername().equals(invitedUser.getUsername()));
                if (isBlocked) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                invitedUserIds.add(invitedUser.getId());
            }

            // client side checks should handle this, we can just return BAD_REQUEST
            if (title.isEmpty() || events.isEmpty() || invitedUserIds.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // delete old draft
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
            }

            int[] invitedUserIdInts = invitedUserIds.stream().mapToInt(i -> i).toArray(); //convert arraylist to int[]
            Proposal proposal = proposalDAO.createProposal(uid, title, events.toArray(new String[0]), invitedUserIdInts);

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


// create proposal servlet is missing the part which accepts the users
// ex: on the frontend sends json response to backend containing
// servlet knows how to handle invite user part, passing array of users <- no tests before
// servlet checks blocked list, need to do tests
// create proposal servlet test, need to add new users. You want test data to be valid

// just make the feature branch off of develop for search
// skip the part where the date is within range.
// bare minimum: interact with city, date. <- can copy paste features
// make tests for city, date, and category. Month/day/year.
// create proposal. test feature in resources? change city, etc.


// username: meggie4 pw:awsd, blocks meggie5
// username: meggie5 pw:awsd, tries to invite meggie4

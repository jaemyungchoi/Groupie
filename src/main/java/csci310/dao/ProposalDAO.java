package csci310.dao;

import csci310.model.*;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProposalDAO extends DAOBase {

    public ProposalDAO(String dbURL) {
        super(dbURL);
    }

    // Returns Proposal object on success.
    // Only proposal.owner is guaranteed to be accurate and other attributes could be null.
    public Proposal createProposal(int userId, String title, String[] tmEvents, int[] invitedUserIds) throws SQLException {
        String insertStr = "INSERT INTO proposals (title, owner_uid) VALUES (?,?)";
        String selectStr = "SELECT last_insert_rowid() AS id";
        String insertEventStr = "INSERT INTO proposal_events (proposal_id, tm_event_key) VALUES (?,?)";
        String insertUserStr = "INSERT INTO proposal_users (proposal_id, user_id) VALUES (?,?)";
        try (Connection connection = getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertStr);
             PreparedStatement selectIdStmt = connection.prepareStatement(selectStr);
             PreparedStatement insertEventStmt = connection.prepareStatement(insertEventStr);
             PreparedStatement insertUserStmt = connection.prepareStatement(insertUserStr)) {
            insertStmt.setString(1, title);
            insertStmt.setInt(2, userId);
            insertStmt.executeUpdate();
            // don't need to set id (it's INTEGER PRIMARY KEY and will autoincrement)
            try (ResultSet result = selectIdStmt.executeQuery()) {
                int proposalId = result.getInt("id");
                for (String event : tmEvents) {
                    insertEventStmt.setInt(1, proposalId);
                    insertEventStmt.setString(2, event);
                    insertEventStmt.addBatch();
                }
                insertEventStmt.executeBatch();
                // also add owner to invited user list
                insertUserStmt.setInt(1, proposalId);
                insertUserStmt.setInt(2, userId);
                insertUserStmt.addBatch();
                for (int invitedUserId : invitedUserIds) {
                    insertUserStmt.setInt(1, proposalId);
                    insertUserStmt.setInt(2, invitedUserId);
                    insertUserStmt.addBatch();
                }
                insertUserStmt.executeBatch();
                return new Proposal(
                        proposalId,
                        new User(userId, null),
                        title,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
    }

    public boolean deleteProposal(int id) throws SQLException {
        /* NOTE: This function will probably not be used in practice, since
         *  proposals will instead be declared "expired," for the purposes of
         *  a "proposal history." Still, it is useful for unit testing.      */
        // For now, just insert title and desc into DB
        try (Connection connection = getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM proposals WHERE id=(?)")) {
            deleteStmt.setInt(1, id);
            return deleteStmt.executeUpdate() > 0;
        }
    }

    public Proposal getProposalById(int id) throws SQLException {
        String queryProposal = "SELECT proposals.*, users.name"
                + " FROM proposals LEFT JOIN users"
                + " ON proposals.owner_uid=users.id"
                + " WHERE proposals.id=?";
        String queryInvitedUsers = "SELECT proposal_users.*, users.name"
                + " FROM proposal_users"
                + " LEFT JOIN users ON proposal_users.user_id=users.id"
                + " WHERE proposal_users.proposal_id=?"
                + " ORDER BY proposal_users.id";
        String queryEvents = "SELECT proposal_events.id AS event_id, event_votes.id AS vote_id, *"
                + " FROM proposal_events"
                + " LEFT JOIN event_votes ON event_votes.event_id=proposal_events.id"
                + " LEFT JOIN users ON event_votes.user_id=users.id"
                + " LEFT JOIN proposal_users ON event_votes.user_id=proposal_users.id"
                + " WHERE proposal_events.proposal_id=?"
                + " ORDER BY proposal_events.id, event_votes.id";

        Proposal proposal = null;

        try (Connection connection = getConnection();
             PreparedStatement queryProposalStmt = connection.prepareStatement(queryProposal);
             PreparedStatement queryInvitedUserStmt = connection.prepareStatement(queryInvitedUsers);
             PreparedStatement queryEventsStmt = connection.prepareStatement(queryEvents)) {
            // query proposal
            queryProposalStmt.setInt(1, id);
            try (ResultSet proposalResult = queryProposalStmt.executeQuery()) {
                //  make  sure we have a matching proposal
                if (proposalResult.next()) {
                    int proposalId = proposalResult.getInt("id");
                    int ownerId = proposalResult.getInt("owner_uid");
                    String ownerUsername = proposalResult.getString("name");
                    String title = proposalResult.getString("title");
                    String createdAt = proposalResult.getString("created_at");
                    int finalizedEventId = proposalResult.getInt("finalized_event_id");
                    boolean finalizedEventIsNull = proposalResult.wasNull();

                    // get list of invited users
                    User[] invitedUsers;
                    User[] acceptedUsers;
                    User[] declinedUsers;
                    User[] hiddenUsers;
                    queryInvitedUserStmt.setInt(1, proposalId);
                    try (ResultSet invitedUserResult = queryInvitedUserStmt.executeQuery()) {
                        List<User> users = new ArrayList<>();
                        List<User> accepted = new ArrayList<>();
                        List<User> declined = new ArrayList<>();
                        List<User> hidden = new ArrayList<>();
                        while (invitedUserResult.next()) {
                            int userId = invitedUserResult.getInt("user_id");
                            boolean accept = invitedUserResult.getBoolean("accept_proposal");
                            boolean acceptWasNull = invitedUserResult.wasNull();
                            String username = invitedUserResult.getString("name");
                            boolean isHidden = invitedUserResult.getBoolean("hide_proposal");
                            User user = new User(userId, username);
                            if (userId != ownerId) {
                                users.add(user);
                            }
                            if (!acceptWasNull) {
                                if (accept) {
                                    accepted.add(user);
                                } else {
                                    declined.add(user);
                                }
                            }
                            if (isHidden) {
                                hidden.add(user);
                            }
                        }
                        invitedUsers = users.toArray(new User[0]);
                        acceptedUsers = accepted.toArray(new User[0]);
                        declinedUsers = declined.toArray(new User[0]);
                        hiddenUsers = hidden.toArray(new User[0]);
                    }

                    // get the list of events and corresponding votes
                    Event[] events;
                    Event finalizedEvent = null;
                    queryEventsStmt.setInt(1, proposalId);
                    try (ResultSet eventsResult = queryEventsStmt.executeQuery()) {
                        List<Integer> eventIds = new ArrayList<>();
                        List<String> tmEventKeys = new ArrayList<>();
                        List<List<Vote>> voteLists = new ArrayList<>();
                        while (eventsResult.next()) {
                            int eventId = eventsResult.getInt("event_id");
                            String tmEventKey = eventsResult.getString("tm_event_key");

                            if (eventIds.isEmpty() || eventId != eventIds.get(eventIds.size() - 1)) {
                                eventIds.add(eventId);
                                tmEventKeys.add(tmEventKey);
                                voteLists.add(new ArrayList<>());
                            }

                            String username = eventsResult.getString("name");
                            // it is possible that we don't have any votes
                            if (username != null) {
                                int voteId = eventsResult.getInt("vote_id");
                                int userId = eventsResult.getInt("user_id");

                                int canAttend = eventsResult.getInt("can_attend");
                                boolean canAttendIsNull = eventsResult.wasNull();
                                int rating = eventsResult.getInt("rating");
                                boolean ratingIsNull = eventsResult.wasNull();
                                boolean isDraft = eventsResult.getBoolean("is_draft");

                                User user = new User(userId, username);
                                Vote vote = new Vote(
                                        voteId,
                                        user,
                                        canAttendIsNull ? null : CanAttend.fromInt(canAttend),
                                        ratingIsNull ? null : rating,
                                        isDraft
                                );

                                voteLists.get(voteLists.size() - 1).add(vote);
                            }
                        }

                        List<Event> eventList = new ArrayList<>();
                        for (int i = 0; i < eventIds.size(); i++) {
                            Vote[] votes = voteLists.get(i).toArray(new Vote[0]);
                            Event event = new Event(eventIds.get(i), tmEventKeys.get(i), votes);
                            if (!finalizedEventIsNull && event.getId() == finalizedEventId) {
                                finalizedEvent = event;
                            }
                            eventList.add(event);
                        }
                        events = eventList.toArray(new Event[0]);
                        User owner = new User(ownerId, ownerUsername);
                        proposal = new Proposal(
                                proposalId,
                                owner,
                                title,
                                events,
                                invitedUsers,
                                finalizedEvent,
                                acceptedUsers,
                                declinedUsers,
                                hiddenUsers,
                                createdAt
                        );
                    }
                }
            }
        }
        return proposal;
    }

    public Proposal[] getProposalsByUser(int uid) throws SQLException {
        String queryTest = "SELECT proposals.* FROM proposals LEFT JOIN proposal_users"
                + " ON proposals.id=proposal_users.proposal_id AND proposal_users.user_id=?"
                + " WHERE (proposals.owner_uid=? OR proposal_users.id IS NOT NULL) AND proposal_users.hide_proposal=0"
                + " ORDER BY proposals.id";
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement selectSt = conn.prepareStatement(queryTest)) {
            selectSt.setInt(1, uid);
            selectSt.setInt(2, uid);
            List<Proposal> proposals = new ArrayList<>();
            try (ResultSet result = selectSt.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("id");
                    // FIXME: still has the N+1 query problem, will fix later
                    proposals.add(getProposalById(id));
                }
            }
            return proposals.toArray(new Proposal[0]);
        }
    }

    // Returns Event object on success.
    // Only vote.id is guaranteed to be accurate and other attributes could be null.
    // Use Proposal.canAccess(userId) and Proposal.hasEvent(eventId) first to ensure the operation is valid!
    public Vote createVote(int userId, int eventId, CanAttend canAttend, Integer rating, boolean isDraft) throws SQLException {
        String insertVote = "INSERT INTO event_votes (event_id, user_id, can_attend, rating, is_draft) VALUES (?,?, ?,?,?)";
        String getId = "SELECT last_insert_rowid() AS id";
        try (Connection connection = getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertVote);
             PreparedStatement selectIdStmt = connection.prepareStatement(getId)) {
            insertStmt.setInt(1, eventId);
            insertStmt.setInt(2, userId);
            if (canAttend == null) {
                insertStmt.setNull(3, Types.INTEGER);
            } else {
                insertStmt.setInt(3, canAttend.getCode());
            }
            if (rating == null) {
                insertStmt.setNull(4, Types.INTEGER);
            } else {
                insertStmt.setInt(4, rating);
            }
            insertStmt.setBoolean(5, isDraft);
            insertStmt.executeUpdate();
            try (ResultSet result = selectIdStmt.executeQuery()) {
                int voteId = result.getInt("id");
                return new Vote(voteId, new User(userId, null), canAttend, rating, isDraft);
            }
        }
    }

    // Returns true if update is successful
    // Use Proposal.canAccess(userId) first to ensure the operation is valid!
    public boolean acceptProposal(int userId, int proposalId, boolean accept) throws SQLException {
        String setAccept = "UPDATE proposal_users SET accept_proposal=? WHERE proposal_id=? AND user_id=?";
        try (Connection connection = getConnection();
             PreparedStatement updateStmt = connection.prepareStatement(setAccept)) {
            updateStmt.setBoolean(1, accept);
            updateStmt.setInt(2, proposalId);
            updateStmt.setInt(3, userId);
            return updateStmt.executeUpdate() > 0;
        }
    }

    // Finalizes a proposal, returns the finalized proposal
    public Proposal finalizeProposal(int proposalId) throws SQLException, IllegalStateException {
        Proposal p = getProposalById(proposalId);

        // If there are no events, throw an exception
        if (p.getEvents().length == 0)
            throw new IllegalStateException("Cannot finalize a proposal without events");

        // Choose a random event
        Event finalized_event = p.getBestEvent();

        // Update database
        String setFinalizedEvent = "UPDATE proposals SET finalized_event_id=? WHERE id=?";
        try (Connection connection = getConnection();
             PreparedStatement updateStmt = connection.prepareStatement(setFinalizedEvent)) {
            updateStmt.setInt(1, finalized_event.getId());
            updateStmt.setInt(2, proposalId);
            updateStmt.executeUpdate();
            return getProposalById(proposalId);
        }
    }

    // Returns true if update is successful
    // Use Proposal.canAccess(userId) and Proposal.hasEvent(eventId) first to ensure the operation is valid!
    public boolean deleteEvent(int id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM proposal_events WHERE id=?")) {
            deleteStmt.setInt(1, id);
            return deleteStmt.executeUpdate() > 0;
        }
    }

    // Returns true if update is successful
    // Make sure the caller is the owner of the proposal!
    public boolean deleteInvitedUser(int proposalId, int userId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM proposal_users WHERE proposal_id=? AND user_id=?")) {
            deleteStmt.setInt(1, proposalId);
            deleteStmt.setInt(2, userId);
            return deleteStmt.executeUpdate() > 0;
        }
    }

    // Returns true if update is successful
    // Make sure the caller is an invited user first!
    public boolean rejectProposal(int proposalId, int userId, boolean reject) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement updateStmt = connection.prepareStatement("UPDATE proposal_users set hide_proposal=? WHERE proposal_id=? AND user_id=?")) {
            updateStmt.setInt(2, proposalId);
            updateStmt.setInt(3, userId);
            updateStmt.setBoolean(1, reject);
            return updateStmt.executeUpdate() > 0;
        }
    }
}

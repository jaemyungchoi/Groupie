package csci310.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import csci310.dao.BlockListDAO;
import csci310.dao.ProposalDAO;
import csci310.dao.UserDAO;
import csci310.model.Proposal;
import csci310.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProposalDetailsServlet extends HttpServlet {

    ProposalDAO proposalDAO;

    private final String detailURL = "/proposal-details.jsp";

    @Override
    public void init() {
        proposalDAO = new ProposalDAO(getServletContext().getInitParameter("dbURL"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object uidObj = session.getAttribute("uid");
        String proposalId = request.getParameter("id");

        if (uidObj == null) {
            request.setAttribute("error", "You need to log in to view this proposal.");
            request.getRequestDispatcher(detailURL).forward(request, response);
            return;
        }

        int uid = (Integer)uidObj;

        try {
            Proposal proposal = proposalId == null ? null : proposalDAO.getProposalById(Integer.parseInt(proposalId));

            if (proposal == null) {
                request.setAttribute("error", "Proposal not found.");
                request.getRequestDispatcher(detailURL).forward(request, response);
                return;
            }

            if (!proposal.canAccess(uid)) {
                request.setAttribute("error", "You don't have permission to view this proposal.");
                request.getRequestDispatcher(detailURL).forward(request, response);
                return;
            }

            Gson gson = new Gson();
            String jsonData = gson.toJson(proposal);

            request.setAttribute("uid", uid);
            request.setAttribute("proposal", jsonData);
        } catch (SQLException ex) {
            request.setAttribute("error", "Unexpected SQLException.");
        }

        request.getRequestDispatcher(detailURL).forward(request, response);
    }
}

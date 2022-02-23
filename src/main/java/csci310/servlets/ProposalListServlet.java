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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProposalListServlet extends HttpServlet {

    ProposalDAO proposalDAO;
    ProposalDraftDAO proposalDraftDAO;

    @Override
    public void init() {
        proposalDAO = new ProposalDAO(getServletContext().getInitParameter("dbURL"));
        proposalDraftDAO = new ProposalDraftDAO(getServletContext().getInitParameter("dbURL"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object uid = session.getAttribute("uid");
        if (uid == null) {
            response.sendRedirect("/login");
            return;
        }
        try {
            Proposal[] proposalList = proposalDAO.getProposalsByUser((int)uid);
            ProposalDraft[] proposalDrafts = proposalDraftDAO.getProposalDraftsByUser((int)uid);
            Gson gson = new GsonBuilder().serializeNulls().create();
            request.setAttribute("proposalList", gson.toJson(proposalList));
            request.setAttribute("proposalDrafts", gson.toJson(proposalDrafts));
            request.setAttribute("uid", Integer.toString((int)uid));
        } catch (SQLException ex) {
            request.setAttribute("error", "Unexpected SQLException");
        }
        request.getRequestDispatcher("/proposal-list.jsp").forward(request, response);
    }
}

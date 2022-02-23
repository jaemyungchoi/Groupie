package csci310.servlets;

import com.google.gson.*;
import csci310.dao.BlockListDAO;
import csci310.dao.ProposalDAO;
import csci310.dao.ProposalDraftDAO;
import csci310.dao.UserDAO;
import csci310.model.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.*;

public class CreateProposalServlet extends HttpServlet {

    ProposalDraftDAO proposalDraftDAO;

    @Override
    public void init() {
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
        String draftId = request.getParameter("draft-id");
        if (draftId != null) {
            try {
                ProposalDraft draft = proposalDraftDAO.getProposalDraftById(Integer.parseInt(draftId));
                if (draft == null) {
                    request.setAttribute("error","Proposal draft does not exist");
                } else if (draft.getOwner().getId() != (Integer)uid) {
                    request.setAttribute("error","You don't have permission to access this draft");
                } else {
                    request.setAttribute("draft",draft.getJsonData());
                    request.setAttribute("draftId",draft.getId());
                }
            } catch (SQLException ex) {
                request.setAttribute("error","Unexpected SQLException retrieving draft");
            }
        }
        request.getRequestDispatcher("/create-proposal.jsp").forward(request, response);
    }
}
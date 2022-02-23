package csci310.servlets;

import csci310.dao.BlockListDAO;
import csci310.model.User;
import org.sqlite.SQLiteErrorCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

public class BlockListServlet extends HttpServlet {

    BlockListDAO blockListDAO;

    @Override
    public void init() {
        blockListDAO = new BlockListDAO(getServletContext().getInitParameter("dbURL"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object uid = session.getAttribute("uid");
        if (uid == null) {
            response.sendRedirect("/login");
            return;
        }
        try{
            User[] blockList = blockListDAO.getBlockListForUser((Integer)uid);
            request.setAttribute("block_list", blockList);
        } catch (SQLException ex) {
            ex.printStackTrace(System.out);
            request.setAttribute("error_message", "Unexpected SQL error.");
        }
        request.getRequestDispatcher("/block-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object uid = session.getAttribute("uid");
        if (uid == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String error = null;

        // get blocked user
        String blockedUser = request.getParameter("blocked-user");
        if (blockedUser == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // get action (block or unblock)
        String action = request.getParameter("action");
        if ("block".equals(action)) {
            try{
                if (!blockListDAO.blockUserByName((Integer)uid, blockedUser)) {
                    error = "Invalid username: " + blockedUser;
                }
            } catch (SQLException ex) {
                if (ex.getErrorCode() == SQLiteErrorCode.SQLITE_CONSTRAINT.code) {
                    error = blockedUser + " is already in your block list.";
                } else {
                    ex.printStackTrace(System.out);
                    error = "Unexpected SQL error.";
                }
            }
        } else if ("unblock".equals(action)) {
            int blockedUserId = Integer.parseInt(blockedUser);
            try{
                if (!blockListDAO.unblockUserById((Integer)uid, blockedUserId)) {
                    error = "Invalid uid: " + blockedUserId;
                }
            } catch (SQLException ex) {
                ex.printStackTrace(System.out);
                error = "Unexpected SQL error.";
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (error != null) {
            request.setAttribute("error_message", error);
            doGet(request, response); // HACK: get it to render the block list
        } else {
            response.sendRedirect("/block-list");
        }
    }
}

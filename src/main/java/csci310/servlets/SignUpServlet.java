package csci310.servlets;

import csci310.dao.UserDAO;
import csci310.model.User;
import org.sqlite.SQLiteErrorCode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class SignUpServlet extends HttpServlet {

    UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO(getServletContext().getInitParameter("dbURL"));
    }

    // when user go to the signup page
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("/signup.jsp").forward(request, response);
    }

    // handle form posts
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // get form fields
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        ArrayList<String> errors = new ArrayList<>();

        // if missing any field, return 400
        if (username == null || password==null || password2 == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // sanity check username
        if (username.isEmpty()) {
            request.setAttribute("username_error", true);
            errors.add("Username cannot be empty.");
        }

        // also check passwords
        if (!password.equals(password2)) {
            request.setAttribute("password_error", true);
            errors.add("Passwords must match.");
        } else if (password.isEmpty()) {
            request.setAttribute("password_error", true);
            errors.add("Password cannot be empty.");
        }

        // if no errors so far, try register the user
        if (errors.isEmpty()) {
            try{
                User user = userDAO.createUser(username, password);
                request.getSession().setAttribute("username", user.getUsername());
                request.getSession().setAttribute("uid", user.getId());
            } catch (SQLException ex) {
                if (ex.getErrorCode() == SQLiteErrorCode.SQLITE_CONSTRAINT.code) {
                    request.setAttribute("username_error", true);
                    errors.add("Username already in use. Please choose another one.");
                } else {
                    ex.printStackTrace(System.out);
                    errors.add("Unexpected SQL error.");
                }
            }
        }

        // if we have any errors, send user back to this page
        if (!errors.isEmpty())
        {
            request.setAttribute("form_error", true);
            request.setAttribute("error_messages", errors);
            request.setAttribute("username", username);
            request.getRequestDispatcher("/signup.jsp").forward(request, response);
            return;
        }

        // else redirect user to the loggedin page
        response.sendRedirect("/");
    }
}

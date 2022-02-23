package csci310.servlets;

import csci310.dao.UserDAO;
import csci310.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogInServlet extends HttpServlet {

    private final String usernameAttr = "username";
    private final String passwordAttr = "password";
    private final String usernameErr = "username_error";
    private final String passwordErr = "password_error";

    UserDAO dao;

    @Override
    public void init() {
        dao = new UserDAO(getServletContext().getInitParameter("dbURL"));
    }

    // when user goes to the login page
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    // handle form posts
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter(usernameAttr);
        String password = request.getParameter(passwordAttr);
        ArrayList<String> errors = new ArrayList<>();

        // Check if username/password is null or empty
        if(username == null || username.isEmpty()) {
            request.setAttribute(usernameErr, true);
            errors.add("Username cannot be empty");
        }
        if(password == null || password.isEmpty()) {
            request.setAttribute(passwordErr, true);
            errors.add("Password cannot be empty");
        }

        User user = null;
        // If a valid(not empty) username and password has been inputted
        if(errors.isEmpty()) {
            try {
                // Check if username exists on database
                user = dao.getUserByName(username);
                if (user == null) {
                    request.setAttribute(usernameErr, true);
                    errors.add("Username does not exist");
                } else {
                    @SuppressWarnings("unchecked") Map<Integer, Queue<Date>> Attempts = (Map<Integer, Queue<Date>>)request.getSession().getAttribute("AttemptMap");
                    if (Attempts == null) {
                        Attempts = new HashMap<>();
                    }
                    Queue<Date> wrongAttemptDates = Attempts.get(user.getId());
                    if (wrongAttemptDates == null) {
                        wrongAttemptDates = new ArrayDeque<>();
                    }
                    Date now = new Date();
                    while (!wrongAttemptDates.isEmpty() &&  now.getTime() - wrongAttemptDates.element().getTime() > 60000) {
                        wrongAttemptDates.remove();
                    }
                    // too many attempts
                    if (wrongAttemptDates.size() >= 3) {
                        request.setAttribute(passwordErr, true);
                        errors.add("Too many attempts. You have been locked out.");
                    }
                    // User exists, check if password matches
                    else if (!dao.passwordMatch(username, password)) {
                        request.setAttribute(passwordErr, true);
                        errors.add("Incorrect Password");
                        wrongAttemptDates.add(now);
                        Attempts.put(user.getId(), wrongAttemptDates);
                    }
                    request.getSession().setAttribute("AttemptMap", Attempts);
                }
            } catch (SQLException e) {
                request.setAttribute(usernameErr, true);
                errors.add("Unexpected SQLException");
            }
        }

        // No errors found, proceed to loggedin
        if (errors.isEmpty()) {
            request.getSession().setAttribute("username", username);
            request.getSession().setAttribute("uid", user.getId());
            response.sendRedirect("/");
        }
        // Errors found, return to login page
        else {
            request.setAttribute("error_messages", errors);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}

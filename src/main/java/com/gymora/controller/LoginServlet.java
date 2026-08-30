
package com.gymora.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ==============================
    // DATABASE CONNECTION
    // ==============================

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/gymora";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD = "";


    // ==============================
    // LOGIN
    // ==============================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Get username/email and password from login.html
        String username = request.getParameter("username");
        String password = request.getParameter("password");


        // Check empty fields
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.sendRedirect("login.html");
            return;
        }


        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        try {

            // ==============================
            // LOAD MYSQL DRIVER
            // ==============================

            Class.forName("com.mysql.cj.jdbc.Driver");


            // ==============================
            // CONNECT DATABASE
            // ==============================

            con = DriverManager.getConnection(
                    DB_URL,
                    DB_USER,
                    DB_PASSWORD
            );


            // ==============================
            // LOGIN QUERY
            // Username OR Email
            // ==============================

            String sql =
                    "SELECT id, username, password, role, "
                    + "full_name, email, phone "
                    + "FROM login "
                    + "WHERE (username = ? OR email = ?) "
                    + "AND password = ?";


            ps = con.prepareStatement(sql);

            ps.setString(1, username.trim());
            ps.setString(2, username.trim());
            ps.setString(3, password);


            rs = ps.executeQuery();


            // ==============================
            // LOGIN SUCCESS
            // ==============================

            if (rs.next()) {

                int userId = rs.getInt("id");

                String dbUsername =
                        rs.getString("username");

                String fullName =
                        rs.getString("full_name");

                String email =
                        rs.getString("email");

                String phone =
                        rs.getString("phone");

                String role =
                        rs.getString("role");


                // ==============================
                // CREATE SESSION
                // ==============================

                HttpSession session =
                        request.getSession();

                session.setAttribute(
                        "userId",
                        userId
                );

                session.setAttribute(
                        "username",
                        dbUsername
                );

                session.setAttribute(
                        "fullName",
                        fullName
                );

                session.setAttribute(
                        "email",
                        email
                );

                session.setAttribute(
                        "phone",
                        phone
                );

                session.setAttribute(
                        "role",
                        role
                );


                // ==============================
                // REDIRECT ACCORDING TO ROLE
                // ==============================

                if (role.equalsIgnoreCase("admin")) {

                    response.sendRedirect(
                            "admin-dashboard.html"
                    );

                }

                else if (role.equalsIgnoreCase("coach")) {

                    response.sendRedirect(
                            "coach-dashboard.html"
                    );

                }

                else if (role.equalsIgnoreCase("member")) {

                    response.sendRedirect(
                            "member-dashboard.html"
                    );

                }

                else {

                    response.setContentType(
                            "text/html;charset=UTF-8"
                    );

                    response.getWriter().println(
                            "<h2>Invalid user role.</h2>"
                    );
                }

            }


            // ==============================
            // LOGIN FAILED
            // ==============================

            else {

                response.setContentType(
                        "text/html;charset=UTF-8"
                );

                response.getWriter().println(
                        "<html>"
                        + "<head>"
                        + "<title>GYMORA Login</title>"
                        + "</head>"
                        + "<body "
                        + "style='font-family:Arial;"
                        + "text-align:center;"
                        + "padding-top:100px;'>"

                        + "<h2 style='color:#ff603d;'>"
                        + "Login Failed"
                        + "</h2>"

                        + "<p>"
                        + "Invalid username/email or password."
                        + "</p>"

                        + "<br>"

                        + "<a href='login.html' "
                        + "style='color:#ff603d;"
                        + "font-weight:bold;'>"
                        + "Back to Login"
                        + "</a>"

                        + "</body>"
                        + "</html>"
                );
            }


        }

        // ==============================
        // DATABASE ERROR
        // ==============================

        catch (Exception e) {

            e.printStackTrace();

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            response.getWriter().println(

                    "<html>"
                    + "<head>"
                    + "<title>GYMORA Error</title>"
                    + "</head>"

                    + "<body "
                    + "style='font-family:Arial;"
                    + "text-align:center;"
                    + "padding-top:80px;'>"

                    + "<h2 style='color:red;'>"
                    + "Database Connection Error"
                    + "</h2>"

                    + "<p style='color:#555;'>"
                    + e.getMessage()
                    + "</p>"

                    + "<br>"

                    + "<a href='login.html' "
                    + "style='color:#ff603d;"
                    + "font-weight:bold;'>"
                    + "Back to Login"
                    + "</a>"

                    + "</body>"
                    + "</html>"
            );
        }


        // ==============================
        // CLOSE DATABASE RESOURCES
        // ==============================

        finally {

            try {

                if (rs != null) {
                    rs.close();
                }

                if (ps != null) {
                    ps.close();
                }

                if (con != null) {
                    con.close();
                }

            }

            catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}


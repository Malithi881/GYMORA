
package com.gymora.controller;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/ForgotPasswordServlet")

public class ForgotPasswordServlet extends HttpServlet {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/gymora";

    private static final String DB_USER =
            "root";

    private static final String DB_PASSWORD =
            "";

    private static final String EMAIL =
            "gymora.system@gmail.com";

    private static final String APP_PASSWORD =
            "hwlwkhmrdfkjaord";


    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("FORGOT PASSWORD STARTED");

        String identifier =
                request.getParameter("identifier");

        System.out.println("INPUT : " + identifier);


        try {

            Class.forName(
                    "com.mysql.cj.jdbc.Driver"
            );


            Connection con =
                    DriverManager.getConnection(
                            DB_URL,
                            DB_USER,
                            DB_PASSWORD
                    );


            // LOGIN TABLE
            // Get username and email
            String sql =
                    "SELECT username, email FROM login WHERE email=? OR username=?";


            PreparedStatement ps =
                    con.prepareStatement(sql);


            ps.setString(1, identifier);
            ps.setString(2, identifier);


            ResultSet rs =
                    ps.executeQuery();


            if (rs.next()) {

                System.out.println("USER FOUND");


                // Get username
                String username =
                        rs.getString("username");


                // Get member email
                String email =
                        rs.getString("email");


                /*
                 * Create customer name from username.
                 *
                 * Example:
                 *
                 * membermalithi
                 *       ↓
                 * malithi
                 *       ↓
                 * Malithi
                 */

                String memberName =
                        getMemberName(username);


                // Generate simple 6-character password
                // containing letters and numbers
                String newPassword =
                        generatePassword();


                String update =
                        "UPDATE login SET password=? WHERE email=?";


                PreparedStatement ups =
                        con.prepareStatement(update);


                ups.setString(1, newPassword);
                ups.setString(2, email);


                ups.executeUpdate();


                System.out.println("PASSWORD UPDATED");


                // Send email
                sendEmail(
                        email,
                        memberName,
                        newPassword
                );


                System.out.println("MAIL SENT SUCCESS");


                response.sendRedirect(
                        "password-success.html"
                );

            }
            else {

                System.out.println("USER NOT FOUND");


                // No registered email/username
                // Password will NOT be changed
                // Email will NOT be sent

                response.sendRedirect(
                        "forgot-password.html?error=notfound"
                );
            }


            con.close();

        }
        catch (Exception e) {

            System.out.println("ERROR");

            e.printStackTrace();


            response.sendRedirect(
                    "forgot-password.html?error=system"
            );

        }

    }


    /*
     * Get customer name from username.
     *
     * Example:
     *
     * membermalithi → Malithi
     * membersahan → Sahan
     * memberkasun → Kasun
     */

    private String getMemberName(String username) {

        if (username == null || username.trim().isEmpty()) {

            return "Member";
        }


        String name =
                username.trim();


        // Remove "member" prefix
        // if username starts with member
        if (name.toLowerCase().startsWith("member")) {

            name =
                    name.substring(6);
        }


        // Make first letter uppercase
        // and remaining letters lowercase

        if (!name.isEmpty()) {

            name =
                    name.substring(0, 1).toUpperCase()
                    + name.substring(1).toLowerCase();

        }


        return name;
    }


    /*
     * Generate a simple 6-character password.
     *
     * Password contains:
     * - Letters
     * - Numbers
     */

    private String generatePassword() {

        String letters =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


        String numbers =
                "0123456789";


        String allCharacters =
                letters + numbers;


        Random random =
                new Random();


        StringBuilder sb =
                new StringBuilder();


        // Make sure there is at least
        // one letter
        sb.append(
                letters.charAt(
                        random.nextInt(
                                letters.length()
                        )
                )
        );


        // Make sure there is at least
        // one number
        sb.append(
                numbers.charAt(
                        random.nextInt(
                                numbers.length()
                        )
                )
        );


        // Generate remaining 4 characters
        for (int i = 0; i < 4; i++) {

            sb.append(
                    allCharacters.charAt(
                            random.nextInt(
                                    allCharacters.length()
                            )
                    )
            );

        }


        // Shuffle the generated password
        for (int i = sb.length() - 1; i > 0; i--) {

            int j =
                    random.nextInt(i + 1);


            char temp =
                    sb.charAt(i);


            sb.setCharAt(
                    i,
                    sb.charAt(j)
            );


            sb.setCharAt(
                    j,
                    temp
            );

        }


        return sb.toString();

    }


    private void sendEmail(
            String receiver,
            String memberName,
            String password)
            throws Exception {


        Properties props =
                new Properties();


        props.put(
                "mail.smtp.host",
                "smtp.gmail.com"
        );


        props.put(
                "mail.smtp.port",
                "587"
        );


        props.put(
                "mail.smtp.auth",
                "true"
        );


        props.put(
                "mail.smtp.starttls.enable",
                "true"
        );


        Session session =
                Session.getInstance(
                        props,
                        new Authenticator() {

                            protected PasswordAuthentication
                            getPasswordAuthentication() {

                                return new PasswordAuthentication(
                                        EMAIL,
                                        APP_PASSWORD
                                );

                            }

                        }
                );


        Message message =
                new MimeMessage(session);


        message.setFrom(
                new InternetAddress(EMAIL)
        );


        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(receiver)
        );


        message.setSubject(
                "GYMORA Password Reset"
        );


        message.setText(

                "Hello "
                + memberName
                + ",\n\n"

                + "Your new password is : "
                + password

                + "\n\nPlease login and change your password."

                + "\n\nThank you."
                + "\nGYMORA Fitness Club"

        );


        Transport.send(message);

    }

}



package com.gymora.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ==========================================
    // DATABASE
    // ==========================================

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/gymora";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD = "";


    // ==========================================
    // GYMORA EMAIL
    // ==========================================

    private static final String EMAIL_USERNAME =
            "gymora.system@gmail.com";

    // KEEP YOUR WORKING GMAIL APP PASSWORD HERE
    private static final String EMAIL_PASSWORD =
            "pcpp htmu ozrk pgly";


    // ==========================================
    // POST
    // ==========================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        // ==========================================
        // GET FORM VALUES
        // ==========================================

        String fullName =
                request.getParameter("fullName");

        String email =
                request.getParameter("email");

        String phone =
                request.getParameter("phone");

        String dob =
                request.getParameter("dob");

        String gender =
                request.getParameter("gender");


        // ==========================================
        // BASIC VALIDATION
        // ==========================================

        if (isEmpty(fullName) ||
            isEmpty(email) ||
            isEmpty(phone) ||
            isEmpty(dob) ||
            isEmpty(gender)) {

            showValidationError(
                    response,
                    "Please fill in all required fields."
            );

            return;
        }


        fullName = fullName.trim();
        email = email.trim();
        phone = phone.trim();
        dob = dob.trim();
        gender = gender.trim();


        // ==========================================
        // FULL NAME VALIDATION
        // ==========================================

        if (!fullName.matches(
                "^[A-Za-z ]{2,100}$")) {

            showValidationError(
                    response,
                    "Full name can contain letters and spaces only."
            );

            return;
        }


        // ==========================================
        // EMAIL VALIDATION
        // ==========================================

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showValidationError(
                    response,
                    "Please enter a valid email address."
            );

            return;
        }


        // ==========================================
        // PHONE VALIDATION
        // ==========================================

        // Must contain numbers only
        if (!phone.matches("^[0-9]+$")) {

            showValidationError(
                    response,
                    "Phone number must contain numbers only."
            );

            return;
        }


        // Must contain exactly 10 digits
        if (phone.length() != 10) {

            showValidationError(
                    response,
                    "Phone number must contain exactly 10 digits."
            );

            return;
        }


        // Sri Lankan mobile number should start with 07
        if (!phone.startsWith("07")) {

            showValidationError(
                    response,
                    "Phone number must start with 07."
            );

            return;
        }


        // ==========================================
        // DATE VALIDATION
        // ==========================================

        try {

            java.time.LocalDate birthDate =
                    java.time.LocalDate.parse(dob);

            java.time.LocalDate today =
                    java.time.LocalDate.now();


            // Future DOB is not allowed
            if (birthDate.isAfter(today)) {

                showValidationError(
                        response,
                        "Date of birth cannot be a future date."
                );

                return;
            }


            // Today is not accepted as DOB
            if (birthDate.isEqual(today)) {

                showValidationError(
                        response,
                        "Please select a valid date of birth."
                );

                return;
            }

        } catch (Exception e) {

            showValidationError(
                    response,
                    "Please select a valid date of birth."
            );

            return;
        }


        // ==========================================
        // GENDER VALIDATION
        // ==========================================

        if (!gender.equalsIgnoreCase("Male") &&
            !gender.equalsIgnoreCase("Female")) {

            showValidationError(
                    response,
                    "Please select a valid gender."
            );

            return;
        }


        Connection con = null;
        PreparedStatement checkPs = null;
        PreparedStatement ps = null;
        ResultSet checkRs = null;


        try {

            // ==========================================
            // MYSQL CONNECTION
            // ==========================================

            Class.forName(
                    "com.mysql.cj.jdbc.Driver"
            );

            con = DriverManager.getConnection(
                    DB_URL,
                    DB_USER,
                    DB_PASSWORD
            );


            // ==========================================
            // CHECK EMAIL
            // ==========================================

            String checkSql =
                    "SELECT id FROM login WHERE email = ?";

            checkPs =
                    con.prepareStatement(checkSql);

            checkPs.setString(
                    1,
                    email
            );

            checkRs =
                    checkPs.executeQuery();


            if (checkRs.next()) {

                showAlreadyRegisteredPage(
                        response,
                        email
                );

                return;
            }


            // ==========================================
            // GENERATE USERNAME
            // ==========================================

            String username =
                    generateUsername(
                            fullName,
                            con
                    );


            // ==========================================
            // GENERATE PASSWORD
            // ==========================================

            String password =
                    generatePassword();


            // ==========================================
            // INSERT MEMBER
            // ==========================================

            String sql =
                    "INSERT INTO login " +
                    "(username, password, role, full_name, email, phone) " +
                    "VALUES (?, ?, 'member', ?, ?, ?)";


            ps =
                    con.prepareStatement(sql);

            ps.setString(
                    1,
                    username
            );

            ps.setString(
                    2,
                    password
            );

            ps.setString(
                    3,
                    fullName
            );

            ps.setString(
                    4,
                    email
            );

            ps.setString(
                    5,
                    phone
            );


            int result =
                    ps.executeUpdate();


            // ==========================================
            // SUCCESS
            // ==========================================

            if (result > 0) {

                // Send email
                sendEmail(
                        email,
                        fullName,
                        username,
                        password
                );


                // Show success page
                showSuccessPage(
                        response,
                        fullName,
                        email,
                        username,
                        password
                );

            } else {

                showValidationError(
                        response,
                        "Registration failed. Please try again."
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            showErrorPage(
                    response,
                    e.getMessage()
            );


        } finally {

            try {
                if (checkRs != null)
                    checkRs.close();
            } catch (Exception ignored) {}

            try {
                if (checkPs != null)
                    checkPs.close();
            } catch (Exception ignored) {}

            try {
                if (ps != null)
                    ps.close();
            } catch (Exception ignored) {}

            try {
                if (con != null)
                    con.close();
            } catch (Exception ignored) {}
        }
    }


    // ==========================================
    // EMPTY CHECK
    // ==========================================

    private boolean isEmpty(String value) {

        return value == null ||
               value.trim().isEmpty();
    }


    // ==========================================
    // VALIDATION ERROR PAGE
    // ==========================================

    private void showValidationError(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.getWriter().println(

                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta charset='UTF-8'>" +

                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>" +

                "<title>GYMORA - Validation</title>" +

                "<style>" +

                "body{" +
                "margin:0;" +
                "font-family:Arial,sans-serif;" +
                "min-height:100vh;" +
                "display:flex;" +
                "justify-content:center;" +
                "align-items:center;" +
                "background:linear-gradient(135deg,#ff512f,#dd2476);" +
                "padding:25px;" +
                "}" +

                ".box{" +
                "background:white;" +
                "max-width:480px;" +
                "width:100%;" +
                "padding:40px;" +
                "border-radius:25px;" +
                "text-align:center;" +
                "box-shadow:0 20px 50px rgba(0,0,0,.25);" +
                "}" +

                ".icon{" +
                "width:75px;" +
                "height:75px;" +
                "margin:auto;" +
                "border-radius:50%;" +
                "background:#fff3cd;" +
                "color:#d97706;" +
                "font-size:42px;" +
                "line-height:75px;" +
                "font-weight:bold;" +
                "}" +

                "h2{" +
                "color:#333;" +
                "margin-top:20px;" +
                "}" +

                "p{" +
                "color:#666;" +
                "line-height:1.6;" +
                "}" +

                ".btn{" +
                "display:inline-block;" +
                "margin-top:20px;" +
                "padding:13px 30px;" +
                "background:linear-gradient(135deg,#ff512f,#dd2476);" +
                "color:white;" +
                "text-decoration:none;" +
                "border-radius:10px;" +
                "font-weight:bold;" +
                "}" +

                ".brand{" +
                "margin-top:25px;" +
                "font-size:12px;" +
                "color:#aaa;" +
                "font-weight:bold;" +
                "letter-spacing:2px;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='box'>" +

                "<div class='icon'>!</div>" +

                "<h2>Please Check Your Details</h2>" +

                "<p>" +
                message +
                "</p>" +

                "<a class='btn' href='register.html'>" +
                "BACK TO REGISTRATION" +
                "</a>" +

                "<div class='brand'>" +
                "GYMORA FITNESS CLUB" +
                "</div>" +

                "</div>" +

                "</body>" +

                "</html>"
        );
    }


    // ==========================================
    // SUCCESS PAGE
    // ==========================================

    private void showSuccessPage(
            HttpServletResponse response,
            String fullName,
            String email,
            String username,
            String password)
            throws IOException {


        response.getWriter().println(

                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta charset='UTF-8'>" +

                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>" +

                "<title>GYMORA - Registration Successful</title>" +

                "<style>" +

                "*{box-sizing:border-box;}" +

                "body{" +
                "margin:0;" +
                "font-family:Arial,sans-serif;" +
                "min-height:100vh;" +
                "display:flex;" +
                "justify-content:center;" +
                "align-items:center;" +
                "padding:30px;" +
                "background:linear-gradient(135deg,#ff512f,#dd2476);" +
                "}" +

                ".container{" +
                "width:100%;" +
                "max-width:520px;" +
                "background:white;" +
                "border-radius:25px;" +
                "padding:45px 40px;" +
                "text-align:center;" +
                "box-shadow:0 25px 60px rgba(0,0,0,.25);" +
                "}" +

                ".success-icon{" +
                "width:85px;" +
                "height:85px;" +
                "margin:0 auto 20px;" +
                "border-radius:50%;" +
                "background:linear-gradient(135deg,#22c55e,#16a34a);" +
                "color:white;" +
                "font-size:50px;" +
                "line-height:85px;" +
                "font-weight:bold;" +
                "}" +

                "h1{" +
                "color:#222;" +
                "font-size:30px;" +
                "margin-bottom:10px;" +
                "}" +

                ".welcome{" +
                "color:#666;" +
                "font-size:16px;" +
                "margin-bottom:25px;" +
                "}" +

                ".details-box{" +
                "background:#fff5f2;" +
                "border:1px solid #ffd7ce;" +
                "border-radius:18px;" +
                "padding:25px;" +
                "text-align:left;" +
                "margin-bottom:22px;" +
                "}" +

                ".detail{" +
                "margin-bottom:18px;" +
                "}" +

                ".detail:last-child{" +
                "margin-bottom:0;" +
                "}" +

                ".label{" +
                "display:block;" +
                "font-size:13px;" +
                "color:#888;" +
                "font-weight:bold;" +
                "margin-bottom:7px;" +
                "text-transform:uppercase;" +
                "letter-spacing:1px;" +
                "}" +

                ".value{" +
                "display:block;" +
                "background:white;" +
                "padding:13px 15px;" +
                "border-radius:10px;" +
                "font-size:18px;" +
                "font-weight:bold;" +
                "color:#333;" +
                "border:1px solid #eee;" +
                "word-break:break-word;" +
                "}" +

                ".email-note{" +
                "background:#f0fdf4;" +
                "color:#166534;" +
                "padding:14px;" +
                "border-radius:12px;" +
                "font-size:14px;" +
                "margin-bottom:25px;" +
                "}" +

                ".warning{" +
                "color:#777;" +
                "font-size:13px;" +
                "margin-bottom:25px;" +
                "}" +

                ".login-btn{" +
                "display:inline-block;" +
                "width:100%;" +
                "padding:15px;" +
                "border-radius:12px;" +
                "background:linear-gradient(135deg,#ff512f,#dd2476);" +
                "color:white;" +
                "text-decoration:none;" +
                "font-size:16px;" +
                "font-weight:bold;" +
                "}" +

                ".brand{" +
                "margin-top:22px;" +
                "font-size:13px;" +
                "color:#aaa;" +
                "font-weight:bold;" +
                "letter-spacing:2px;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='container'>" +

                "<div class='success-icon'>✓</div>" +

                "<h1>Registration Successful!</h1>" +

                "<p class='welcome'>" +
                "Welcome to GYMORA, " +
                fullName +
                "!" +
                "</p>" +

                "<div class='details-box'>" +

                "<div class='detail'>" +

                "<span class='label'>Username</span>" +

                "<span class='value'>" +
                username +
                "</span>" +

                "</div>" +

                "<div class='detail'>" +

                "<span class='label'>Password</span>" +

                "<span class='value'>" +
                password +
                "</span>" +

                "</div>" +

                "</div>" +

                "<div class='email-note'>" +

                "✓ Your login details have also been sent to<br>" +
                "<b>" +
                email +
                "</b>" +

                "</div>" +

                "<p class='warning'>" +

                "Please keep your username and password safe. " +
                "Do not share them with anyone." +

                "</p>" +

                "<a class='login-btn' href='login.html'>" +
                "GO TO LOGIN →" +
                "</a>" +

                "<div class='brand'>" +
                "GYMORA FITNESS CLUB" +
                "</div>" +

                "</div>" +

                "</body>" +

                "</html>"
        );
    }


    // ==========================================
    // EMAIL ALREADY REGISTERED
    // ==========================================

    private void showAlreadyRegisteredPage(
            HttpServletResponse response,
            String email)
            throws IOException {


        response.getWriter().println(

                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta charset='UTF-8'>" +

                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>" +

                "<title>GYMORA - Email Already Registered</title>" +

                "<style>" +

                "*{box-sizing:border-box;}" +

                "body{" +
                "margin:0;" +
                "font-family:Arial,sans-serif;" +
                "min-height:100vh;" +
                "display:flex;" +
                "justify-content:center;" +
                "align-items:center;" +
                "background:linear-gradient(135deg,#667eea,#764ba2);" +
                "padding:30px;" +
                "}" +

                ".box{" +
                "background:white;" +
                "width:100%;" +
                "max-width:480px;" +
                "padding:45px 35px;" +
                "border-radius:25px;" +
                "text-align:center;" +
                "box-shadow:0 25px 60px rgba(0,0,0,.25);" +
                "}" +

                ".icon{" +
                "width:80px;" +
                "height:80px;" +
                "margin:0 auto 20px;" +
                "border-radius:50%;" +
                "background:#fff3cd;" +
                "color:#d97706;" +
                "font-size:42px;" +
                "line-height:80px;" +
                "}" +

                "h1{" +
                "color:#222;" +
                "font-size:27px;" +
                "margin-bottom:12px;" +
                "}" +

                "p{" +
                "color:#666;" +
                "line-height:1.6;" +
                "}" +

                ".email{" +
                "font-weight:bold;" +
                "color:#444;" +
                "word-break:break-word;" +
                "}" +

                ".buttons{" +
                "margin-top:25px;" +
                "display:flex;" +
                "gap:12px;" +
                "flex-direction:column;" +
                "}" +

                ".btn{" +
                "padding:14px;" +
                "border-radius:11px;" +
                "text-decoration:none;" +
                "font-weight:bold;" +
                "}" +

                ".login{" +
                "background:linear-gradient(135deg,#ff512f,#dd2476);" +
                "color:white;" +
                "}" +

                ".back{" +
                "background:#f1f1f1;" +
                "color:#555;" +
                "}" +

                ".brand{" +
                "margin-top:25px;" +
                "font-size:12px;" +
                "color:#aaa;" +
                "letter-spacing:2px;" +
                "font-weight:bold;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='box'>" +

                "<div class='icon'>!</div>" +

                "<h1>Email Already Registered</h1>" +

                "<p>" +
                "An account is already registered with:" +
                "</p>" +

                "<p class='email'>" +
                email +
                "</p>" +

                "<p>" +
                "Please login using your existing account or use another email address." +
                "</p>" +

                "<div class='buttons'>" +

                "<a class='btn login' href='login.html'>" +
                "GO TO LOGIN" +
                "</a>" +

                "<a class='btn back' href='register.html'>" +
                "USE ANOTHER EMAIL" +
                "</a>" +

                "</div>" +

                "<div class='brand'>" +
                "GYMORA FITNESS CLUB" +
                "</div>" +

                "</div>" +

                "</body>" +

                "</html>"
        );
    }


    // ==========================================
    // ERROR PAGE
    // ==========================================

    private void showErrorPage(
            HttpServletResponse response,
            String error)
            throws IOException {


        response.getWriter().println(

                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta charset='UTF-8'>" +

                "<title>GYMORA - Error</title>" +

                "<style>" +

                "body{" +
                "font-family:Arial;" +
                "background:#f5f5f5;" +
                "display:flex;" +
                "justify-content:center;" +
                "align-items:center;" +
                "height:100vh;" +
                "}" +

                ".box{" +
                "background:white;" +
                "padding:40px;" +
                "border-radius:20px;" +
                "text-align:center;" +
                "box-shadow:0 15px 40px rgba(0,0,0,.15);" +
                "max-width:550px;" +
                "}" +

                "h2{color:#dc2626;}" +

                ".error{" +
                "color:#555;" +
                "word-break:break-word;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='box'>" +

                "<h2>Registration Error</h2>" +

                "<p class='error'>" +
                error +
                "</p>" +

                "</div>" +

                "</body>" +

                "</html>"
        );
    }


    // ==========================================
    // SEND EMAIL
    // ==========================================

    private void sendEmail(
            String recipientEmail,
            String fullName,
            String username,
            String password)
            throws Exception {


        Properties properties =
                new Properties();


        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        properties.put(
                "mail.smtp.host",
                "smtp.gmail.com"
        );

        properties.put(
                "mail.smtp.port",
                "587"
        );


        Session session =
                Session.getInstance(
                        properties,
                        new Authenticator() {

                            @Override
                            protected PasswordAuthentication
                            getPasswordAuthentication() {

                                return new PasswordAuthentication(
                                        EMAIL_USERNAME,
                                        EMAIL_PASSWORD
                                );
                            }
                        }
                );


        Message message =
                new MimeMessage(session);


        message.setFrom(
                new InternetAddress(
                        EMAIL_USERNAME,
                        "GYMORA"
                )
        );


        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(
                        recipientEmail
                )
        );


        message.setSubject(
                "Welcome to GYMORA - Your Login Details"
        );


        String emailBody =

                "Dear " + fullName + ",\n\n" +

                "Welcome to GYMORA Fitness Club!\n\n" +

                "Your member account has been created successfully.\n\n" +

                "YOUR LOGIN DETAILS\n" +
                "===================\n\n" +

                "Username: " + username + "\n" +

                "Password: " + password + "\n\n" +

                "Please keep these details safe and do not share them with anyone.\n\n" +

                "You can now login to the GYMORA system.\n\n" +

                "Thank you,\n" +

                "GYMORA Fitness Club";


        message.setText(emailBody);


        Transport.send(message);
    }


    // ==========================================
    // GENERATE USERNAME
    // ==========================================

    private String generateUsername(
            String fullName,
            Connection con)
            throws Exception {


        String name =
                fullName
                .toLowerCase()
                .replaceAll(
                        "[^a-zA-Z0-9]",
                        ""
                );


        String base =
                "member" + name;


        String username =
                base;


        int number = 1;


        while (
                usernameExists(
                        username,
                        con
                )
        ) {

            username =
                    base + number;

            number++;
        }


        return username;
    }


    // ==========================================
    // CHECK USERNAME
    // ==========================================

    private boolean usernameExists(
            String username,
            Connection con)
            throws Exception {


        String sql =
                "SELECT id FROM login WHERE username = ?";


        PreparedStatement ps =
                con.prepareStatement(sql);


        ps.setString(
                1,
                username
        );


        ResultSet rs =
                ps.executeQuery();


        boolean exists =
                rs.next();


        rs.close();

        ps.close();


        return exists;
    }


    // ==========================================
    // GENERATE PASSWORD
    // ==========================================

    private String generatePassword() {

        int number =
                1000 +
                (int)(Math.random() * 9000);

        return "Gym@" + number;
    }
}


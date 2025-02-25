package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Member;
import service.DatabaseService;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemberServlet extends HttpServlet {
    private final DatabaseService databaseService;

    public MemberServlet(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Member> members = databaseService.getAllMembers();
            String template = loadTemplate(request);

            // Replace placeholders
            StringBuilder membersHtml = new StringBuilder();
            for (Member member : members) {
                membersHtml.append("<tr>")
                        .append("<td>").append(member.getId()).append("</td>")
                        .append("<td>").append(member.getName()).append("</td>")
                        .append("<td>").append(member.getEmail()).append("</td>")
                        .append("<td>").append(member.getJoinDate()).append("</td>")
                        .append("</tr>");
            }

            // Handle empty context path
            String html = template.replace("${members}", membersHtml.toString());

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println(html);
            System.out.println("Final HTML: " + html);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    private String loadTemplate(HttpServletRequest request) throws IOException {
        StringBuilder template = new StringBuilder();
        System.out.println("Loading template: " + "members.html"); // Debugging

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getServletContext().getResourceAsStream("/templates/" + "members.html")))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                template.append(line).append("\n");
                System.out.println("Read line: " + line); // Debugging
            }
        } catch (NullPointerException e) {
            System.err.println("Template file not found: " + "members.html"); // Debugging
            throw new IOException("Template file not found: " + "members.html", e);
        }

        System.out.println("Template content: " + template); // Debugging
        return template.toString();
    }

//    @Override
//    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String name = request.getParameter("name");
//        String email = request.getParameter("email");
//
//        try {
//            databaseService.addMember(name, email);
//            response.sendRedirect("/members");
//        } catch (SQLException e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
//        }
//    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        // Validate input
        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "All fields are required");
            return;
        }

        // Enhanced email validation
        if (!isValidEmail(email)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid email format");
            return;
        }

        try {
            databaseService.addMember(name, email);
            response.sendRedirect(request.getContextPath() + "/members");
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique violation
                response.sendError(HttpServletResponse.SC_CONFLICT, "Email already registered");
            } else {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            }
        }
    }

    private boolean isValidEmail(String email) {
        // Simple email regex validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }


    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo(); // Get the path info (e.g., "/1")
        if (path == null || path.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing member ID");
            return;
        }

        String idStr = path.substring(1); // Extract the ID from the path (e.g., "1")
        if (!idStr.matches("\\d+")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid member ID format");
            return;
        }

        try {
            // Manually parse the request body
            String body = request.getReader().lines().collect(Collectors.joining());
            Map<String, String> params = parseFormData(body);
            String name = params.get("name");
            String email = params.get("email");

            if (name == null || email == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters: name or email");
                return;
            }

            int id = Integer.parseInt(idStr);
            databaseService.updateMember(id, name, email);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid member ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    // Helper method to parse form data
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo(); // Get the path info (e.g., "/1")
        if (path == null || path.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing member ID");
            return;
        }

        String idStr = path.substring(1); // Extract the ID from the path (e.g., "1")
        if (!idStr.matches("\\d+")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid member ID format");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            databaseService.deleteMember(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid member ID format");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("No member found with ID")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Member not found");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            }
        }
    }
}
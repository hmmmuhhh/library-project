package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Book;
import service.DatabaseService;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class BookServlet extends HttpServlet {
    private final DatabaseService databaseService;

    public BookServlet(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Book> books = databaseService.getAllBooks();
            String template = loadTemplate(request);

            StringBuilder booksHtml = new StringBuilder();
            for (Book book : books) {
                booksHtml.append("<tr>")
                        .append("<td>").append(book.code()).append("</td>")
                        .append("<td>").append(book.title()).append("</td>")
                        .append("<td>").append(book.author()).append("</td>")
                        .append("</tr>");
            }

            String errorMessage = request.getParameter("error");
            String successMessage = request.getParameter("success");
            StringBuilder messageHtml = new StringBuilder();
            if (errorMessage != null) {
                messageHtml.append("<div class=\"error\">").append(errorMessage).append("</div>");
            }
            if (successMessage != null) {
                messageHtml.append("<div class=\"success\">").append(successMessage).append("</div>");
            }

            String html = template.replace("${books}", booksHtml.toString())
                    .replace("${messages}", messageHtml.toString());

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println(html);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    private String loadTemplate(HttpServletRequest request) throws IOException {
        StringBuilder template = new StringBuilder();
        System.out.println("Loading template: " + "books.html");

        InputStream inputStream = request.getServletContext().getResourceAsStream("/templates/" + "books.html");
        if (inputStream == null) {
            throw new IOException("Template file not found: " + "books.html");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                template.append(line).append("\n");
                System.out.println("Read line: " + line);
            }
        }
        System.out.println("Template content: " + template);
        return template.toString();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String title = request.getParameter("title");
        String author = request.getParameter("author");

        if (code == null || code.isEmpty() || title == null || title.isEmpty() || author == null || author.isEmpty()) {
            response.sendRedirect("/books?error=All fields are required");
            return;
        }

        try {
            Integer.parseInt(code);
        } catch (NumberFormatException e) {
            response.sendRedirect("/books?error=Book code must be a number");
            return;
        }

        try {
            databaseService.addBook(code, title, author);
            response.sendRedirect("/books?success=Book added successfully");
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique violation
                response.sendRedirect("/books?error=Book code already exists");
            } else {
                e.printStackTrace();
                response.sendRedirect("/books?error=Database error");
            }
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            response.sendRedirect("/books?error=Missing book code");
            return;
        }

        String code = path.substring(1);

        try {

            String body = request.getReader().lines().collect(Collectors.joining());
            Map<String, String> params = parseFormData(body);
            String title = params.get("title");
            String author = params.get("author");

            if (title == null || author == null) {
                response.sendRedirect("/books?error=Missing parameters: title or author");
                return;
            }

            databaseService.updateBook(code, title, author);
            response.sendRedirect("/books?success=Book updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("/books?error=Database error");
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            response.sendRedirect("/books?error=Missing book code");
            return;
        }

        String code = path.substring(1);
        try {
            databaseService.deleteBook(code);
            response.sendRedirect("/books?success=Book deleted successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("No book found with code")) {
                response.sendRedirect("/books?error=Book not found");
            } else {
                response.sendRedirect("/books?error=Database error");
            }
        }
    }

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

}
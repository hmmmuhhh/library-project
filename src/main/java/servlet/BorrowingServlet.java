package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Borrowing;
import service.DatabaseService;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class BorrowingServlet extends HttpServlet {
    private final DatabaseService databaseService;

    public BorrowingServlet(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Borrowing> borrowings = databaseService.getAllBorrowings();
            String template = loadTemplate(request);

            StringBuilder borrowingsHtml = new StringBuilder();
            for (Borrowing borrowing : borrowings) {
                borrowingsHtml.append("<tr>")
                        .append("<td>").append(borrowing.bookCode()).append("</td>")
                        .append("<td>").append(borrowing.memberId()).append("</td>")
                        .append("<td>").append(borrowing.borrowDate()).append("</td>")
                        .append("<td>").append(borrowing.returnDate() != null ? borrowing.returnDate() : "Not Returned").append("</td>")
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

            String html = template.replace("${borrowings}", borrowingsHtml.toString())
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
        System.out.println("Loading template: " + "borrowings.html");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getServletContext().getResourceAsStream("/templates/" + "borrowings.html")))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                template.append(line).append("\n");
                System.out.println("Read line: " + line);
            }
        } catch (NullPointerException e) {
            throw new IOException("Template file not found: " + "borrowings.html", e);
        }

        System.out.println("Template content: " + template);
        return template.toString();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo() != null ? request.getPathInfo() : "";

        if (path.equals("/return")) {
            handleReturn(request, response);
        } else {
            handleBorrow(request, response);
        }
    }

    private void handleReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String bookCode = request.getParameter("bookCode");

        if (bookCode == null || bookCode.isEmpty()) {
            response.sendRedirect("/borrow?error=Missing book code");
            return;
        }

        try {
            if (databaseService.bookExists(bookCode)) {
                response.sendRedirect("/borrow?error=Book not found");
                return;
            }

            if (!databaseService.isBookAlreadyBorrowed(bookCode)) {
                response.sendRedirect("/borrow?error=No active borrowing found for this book");
                return;
            }

            int rowsUpdated = databaseService.returnBook(bookCode);
            if (rowsUpdated == 0) {
                response.sendRedirect("/borrow?error=Failed to return the book");
            } else {
                response.sendRedirect("/borrow?success=Book returned successfully");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("/borrow?error=Database error");
        }
    }

    private void handleBorrow(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String bookCode = request.getParameter("bookCode");
        String memberIdStr = request.getParameter("memberId");

        if (bookCode == null || bookCode.isEmpty() || memberIdStr == null || memberIdStr.isEmpty()) {
            response.sendRedirect("/borrow?error=All fields are required");
            return;
        }

        try {
            int memberId = Integer.parseInt(memberIdStr);

            if (!databaseService.memberExists(memberId)) {
                response.sendRedirect("/borrow?error=Member not found");
                return;
            }

            if (databaseService.bookExists(bookCode)) {
                response.sendRedirect("/borrow?error=Book not found");
                return;
            }

            if (databaseService.isBookAlreadyBorrowed(bookCode)) {
                response.sendRedirect("/borrow?error=Book is already borrowed");
                return;
            }

            databaseService.borrowBook(bookCode, memberId);
            response.sendRedirect("/borrow?success=Book borrowed successfully");

        } catch (NumberFormatException e) {
            response.sendRedirect("/borrow?error=Member ID must be a number");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("/borrow?error=Database error");
        }
    }
}
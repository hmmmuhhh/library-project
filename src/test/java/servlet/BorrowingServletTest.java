package servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Borrowing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DatabaseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowingServletTest {

    private BorrowingServlet borrowingServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() throws Exception {
        databaseService = mock(DatabaseService.class);
        borrowingServlet = new BorrowingServlet(databaseService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet() throws IOException, SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        borrowings.add(new Borrowing("B001", 1, LocalDate.now(), null));
        borrowings.add(new Borrowing("B002", 2, LocalDate.now(), LocalDate.now().plusDays(14)));
        when(databaseService.getAllBorrowings()).thenReturn(borrowings);

        borrowingServlet.doGet(request, response);

        writer.flush();
        String result = stringWriter.toString();
        assertTrue(result.contains("B001"));
        assertTrue(result.contains("B002"));
    }

    @Test
    void testDoPost() throws IOException, SQLException {
        when(request.getParameter("bookCode")).thenReturn("B001");
        when(request.getParameter("memberId")).thenReturn("1");

        when(databaseService.bookExists("B001")).thenReturn(true);
        when(databaseService.memberExists(1)).thenReturn(true);
        when(databaseService.isBookAlreadyBorrowed("B001")).thenReturn(false);

        borrowingServlet.doPost(request, response);

        verify(databaseService).borrowBook("B001", 1);
        verify(response).sendRedirect("/borrowings?success=Book borrowed successfully");
    }

    @Test
    void testDoPostWithMissingParameters() throws IOException {
        when(request.getParameter("bookCode")).thenReturn(null);
        when(request.getParameter("memberId")).thenReturn("1");

        borrowingServlet.doPost(request, response);

        verify(response).sendRedirect("/borrowings?error=Missing parameters");
    }

    @Test
    void testDoPostWithInvalidBook() throws IOException, SQLException {
        when(request.getParameter("bookCode")).thenReturn("B001");
        when(request.getParameter("memberId")).thenReturn("1");

        when(databaseService.bookExists("B001")).thenReturn(false);

        borrowingServlet.doPost(request, response);

        verify(response).sendRedirect("/borrowings?error=Book does not exist");
    }

    @Test
    void testDoPostWithInvalidMember() throws IOException, SQLException {
        when(request.getParameter("bookCode")).thenReturn("B001");
        when(request.getParameter("memberId")).thenReturn("1");

        when(databaseService.bookExists("B001")).thenReturn(true);
        when(databaseService.memberExists(1)).thenReturn(false);

        borrowingServlet.doPost(request, response);

        verify(response).sendRedirect("/borrowings?error=Member does not exist");
    }

    @Test
    void testDoPostWithAlreadyBorrowedBook() throws IOException, SQLException {
        when(request.getParameter("bookCode")).thenReturn("B001");
        when(request.getParameter("memberId")).thenReturn("1");

        when(databaseService.bookExists("B001")).thenReturn(true);
        when(databaseService.memberExists(1)).thenReturn(true);
        when(databaseService.isBookAlreadyBorrowed("B001")).thenReturn(true);

        borrowingServlet.doPost(request, response);

        verify(response).sendRedirect("/borrowings?error=Book is already borrowed");
    }

    @Test
    void testDoDelete() throws Exception {
        when(request.getPathInfo()).thenReturn("/B001");

        Method doDeleteMethod = BorrowingServlet.class.getDeclaredMethod(
                "doDelete", HttpServletRequest.class, HttpServletResponse.class
        );
        doDeleteMethod.setAccessible(true);
        doDeleteMethod.invoke(borrowingServlet, request, response);

        verify(databaseService).returnBook("B001");
        verify(response).sendRedirect("/borrowings?success=Book returned successfully");
    }

    @Test
    void testDoDeleteWithMissingPath() throws Exception {
        when(request.getPathInfo()).thenReturn(null);

        Method doDeleteMethod = BorrowingServlet.class.getDeclaredMethod(
                "doDelete", HttpServletRequest.class, HttpServletResponse.class
        );
        doDeleteMethod.setAccessible(true);
        doDeleteMethod.invoke(borrowingServlet, request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing book code");
    }
}
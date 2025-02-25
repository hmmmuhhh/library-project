package servlet;

import servlet.BorrowingServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Borrowing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import service.DatabaseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BorrowingServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DatabaseService databaseService;

    private BorrowingServlet borrowingServlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        borrowingServlet = new BorrowingServlet(databaseService);
    }

    @Test
    void testDoGet() throws IOException, SQLException {
        // Mock database response
        when(databaseService.getAllBorrowings()).thenReturn(Collections.singletonList(
                new Borrowing("ABC123", 1, java.time.LocalDate.now(), null)
        ));

        // Mock response writer
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Call doGet
        borrowingServlet.doGet(request, response);

        // Verify response
        writer.flush();
        assertTrue(stringWriter.toString().contains("ABC123"));
    }

    @Test
    void testDoPostBorrow() throws IOException, SQLException {
        // Mock request parameters
        when(request.getParameter("bookCode")).thenReturn("ABC123");
        when(request.getParameter("memberId")).thenReturn("1");

        // Call doPost
        borrowingServlet.doPost(request, response);

        // Verify databaseService.borrowBook was called
        verify(databaseService).borrowBook("ABC123", 1);
    }

    @Test
    void testDoPostReturn() throws IOException, SQLException {
        // Mock path info
        when(request.getPathInfo()).thenReturn("/return");

        // Mock request parameters
        when(request.getParameter("bookCode")).thenReturn("ABC123");

        // Call doPost
        borrowingServlet.doPost(request, response);

        // Verify databaseService.returnBook was called
        verify(databaseService).returnBook("ABC123");
    }
}
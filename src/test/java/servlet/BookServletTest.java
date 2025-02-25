package servlet;

import servlet.BookServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Book;
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

class BookServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DatabaseService databaseService;

    private BookServlet bookServlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookServlet = new BookServlet(databaseService);
    }

    @Test
    void testDoGet() throws IOException, SQLException {
        // Mock database response
        when(databaseService.getAllBooks()).thenReturn(Collections.singletonList(
                new Book("ABC123", "Test Book", "Test Author")
        ));

        // Mock response writer
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Call doGet
        bookServlet.doGet(request, response);

        // Verify response
        writer.flush();
        assertTrue(stringWriter.toString().contains("Test Book"));
    }

    @Test
    void testDoPost() throws IOException, SQLException {
        // Mock request parameters
        when(request.getParameter("code")).thenReturn("ABC123");
        when(request.getParameter("title")).thenReturn("Test Book");
        when(request.getParameter("author")).thenReturn("Test Author");

        // Call doPost
        bookServlet.doPost(request, response);

        // Verify databaseService.addBook was called
        verify(databaseService).addBook("ABC123", "Test Book", "Test Author");
    }

    @Test
    void testDoPut() throws IOException, SQLException {
        // Mock path info
        when(request.getPathInfo()).thenReturn("/ABC123");

        // Mock request body
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader("title=Updated Title&author=Updated Author")));

        // Call doPut
        bookServlet.doPut(request, response);

        // Verify databaseService.updateBook was called
        verify(databaseService).updateBook("ABC123", "Updated Title", "Updated Author");
    }

    @Test
    void testDoDelete() throws IOException, SQLException {
        // Mock path info
        when(request.getPathInfo()).thenReturn("/ABC123");

        // Call doDelete
        bookServlet.doDelete(request, response);

        // Verify databaseService.deleteBook was called
        verify(databaseService).deleteBook("ABC123");
    }
}
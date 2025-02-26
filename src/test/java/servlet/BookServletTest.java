package servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DatabaseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServletTest {

    private BookServlet bookServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() throws Exception {
        databaseService = mock(DatabaseService.class);
        bookServlet = new BookServlet(databaseService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet() throws IOException, SQLException {
        List<Book> books = new ArrayList<>();
        books.add(new Book("B001", "The Great Gatsby", "F. Scott Fitzgerald"));
        books.add(new Book("B002", "1984", "George Orwell"));
        when(databaseService.getAllBooks()).thenReturn(books);

        bookServlet.doGet(request, response);

        writer.flush();
        String result = stringWriter.toString();
        assertTrue(result.contains("The Great Gatsby"));
        assertTrue(result.contains("1984"));
        assertTrue(result.contains("F. Scott Fitzgerald"));
        assertTrue(result.contains("George Orwell"));
    }

    @Test
    void testDoPost() throws IOException, SQLException {
        when(request.getParameter("code")).thenReturn("B003");
        when(request.getParameter("title")).thenReturn("New Book");
        when(request.getParameter("author")).thenReturn("New Author");

        bookServlet.doPost(request, response);

        verify(databaseService).addBook("B003", "New Book", "New Author");
        verify(response).sendRedirect("/books?success=Book added successfully");
    }

    @Test
    void testDoPostWithMissingParameters() throws IOException {
        when(request.getParameter("code")).thenReturn(null);
        when(request.getParameter("title")).thenReturn("New Book");
        when(request.getParameter("author")).thenReturn("New Author");

        bookServlet.doPost(request, response);

        verify(response).sendRedirect("/books?error=Missing parameters");
    }

    @Test
    void testDoPut() throws IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/B001");
        when(request.getParameter("title")).thenReturn("Updated Title");
        when(request.getParameter("author")).thenReturn("Updated Author");

        bookServlet.doPut(request, response);

        verify(databaseService).updateBook("B001", "Updated Title", "Updated Author");
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void testDoPutWithMissingPath() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        bookServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing book code");
    }

    @Test
    void testDoDelete() throws IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/B001");

        bookServlet.doDelete(request, response);

        verify(databaseService).deleteBook("B001");
        verify(response).sendRedirect("/books?success=Book deleted successfully");
    }

    @Test
    void testDoDeleteWithMissingPath() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        bookServlet.doDelete(request, response);

        verify(response).sendRedirect("/books?error=Missing book code");
    }
}
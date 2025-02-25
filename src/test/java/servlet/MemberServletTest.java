package servlet;

import servlet.MemberServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Member;
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

class MemberServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private DatabaseService databaseService;

    private MemberServlet memberServlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        memberServlet = new MemberServlet(databaseService);
    }

    @Test
    void testDoGet() throws IOException, SQLException {
        // Mock database response
        when(databaseService.getAllMembers()).thenReturn(Collections.singletonList(
                new Member(1, "John Doe", "john@example.com", java.time.LocalDate.now())
        ));

        // Mock response writer
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Call doGet
        memberServlet.doGet(request, response);

        // Verify response
        writer.flush();
        assertTrue(stringWriter.toString().contains("John Doe"));
    }

    @Test
    void testDoPost() throws IOException, SQLException {
        // Mock request parameters
        when(request.getParameter("name")).thenReturn("John Doe");
        when(request.getParameter("email")).thenReturn("john@example.com");

        // Call doPost
        memberServlet.doPost(request, response);

        // Verify databaseService.addMember was called
        verify(databaseService).addMember("John Doe", "john@example.com");
    }

    @Test
    void testDoPut() throws IOException, SQLException {
        // Mock path info
        when(request.getPathInfo()).thenReturn("/1");

        // Mock request body
        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader("name=Updated Name&email=updated@example.com")));

        // Call doPut
        memberServlet.doPut(request, response);

        // Verify databaseService.updateMember was called
        verify(databaseService).updateMember(1, "Updated Name", "updated@example.com");
    }

    @Test
    void testDoDelete() throws IOException, SQLException {
        // Mock path info
        when(request.getPathInfo()).thenReturn("/1");

        // Call doDelete
        memberServlet.doDelete(request, response);

        // Verify databaseService.deleteMember was called
        verify(databaseService).deleteMember(1);
    }
}
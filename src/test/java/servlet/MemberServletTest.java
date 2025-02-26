package servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DatabaseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServletTest {

    private MemberServlet memberServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() throws Exception {
        databaseService = mock(DatabaseService.class);
        memberServlet = new MemberServlet(databaseService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet() throws IOException, SQLException {
        List<Member> members = new ArrayList<>();
        members.add(new Member(1, "John Doe", "john.doe@example.com", LocalDate.now()));
        members.add(new Member(2, "Jane Doe", "jane.doe@example.com", LocalDate.now()));
        when(databaseService.getAllMembers()).thenReturn(members);

        memberServlet.doGet(request, response);

        writer.flush();
        String result = stringWriter.toString();
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("Jane Doe"));
        assertTrue(result.contains("john.doe@example.com"));
        assertTrue(result.contains("jane.doe@example.com"));
    }

    @Test
    void testDoPost() throws IOException, SQLException {
        // Mock request parameters
        when(request.getParameter("name")).thenReturn("New Member");
        when(request.getParameter("email")).thenReturn("new.member@example.com");

        memberServlet.doPost(request, response);

        verify(databaseService).addMember("New Member", "new.member@example.com");
        verify(response).sendRedirect("/members?success=Member added successfully");
    }

    @Test
    void testDoPostWithMissingParameters() throws IOException {
        when(request.getParameter("name")).thenReturn(null);
        when(request.getParameter("email")).thenReturn("new.member@example.com");

        memberServlet.doPost(request, response);

        verify(response).sendRedirect("/members?error=Missing parameters");
    }

    @Test
    void testDoDelete() throws IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");

        memberServlet.doDelete(request, response);

        verify(databaseService).deleteMember(1);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void testDoDeleteWithMissingPath() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        memberServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing member ID");
    }
}
package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String homePage = new String(Files.readAllBytes(Paths.get("C:/Users/Admin/Downloads/Servlets/Mziuri/src/main/webapp/templates/home.html")));
        response.getWriter().write(homePage);
    }
}


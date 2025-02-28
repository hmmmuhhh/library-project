import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import service.DatabaseService;

import servlet.*;

public class Main {

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("tomcatSvr");
        tomcat.setPort(8080);

        String contextPath = "";
        String docBase = "C:\\Users\\Admin\\Downloads\\Servlets\\Mziuri\\src\\main\\webapp";

        Context context = tomcat.addContext(contextPath, docBase);

        DatabaseService dbService = DatabaseService.getInstance();

        Tomcat.addServlet(context, "homeServlet", new HomeServlet());
        context.addServletMappingDecoded("/home", "homeServlet");

        Tomcat.addServlet(context, "borrowingServlet", new BorrowingServlet(dbService));
        context.addServletMappingDecoded("/borrow/*", "borrowingServlet");

        Tomcat.addServlet(context, "bookServlet", new BookServlet(dbService));
        context.addServletMappingDecoded("/books", "bookServlet");
        context.addServletMappingDecoded("/books/*", "bookServlet");

        Tomcat.addServlet(context, "memberServlet", new MemberServlet(dbService));
        context.addServletMappingDecoded("/members/*", "memberServlet");

        tomcat.start();
        tomcat.getConnector();
        tomcat.getServer().await();
    }
}
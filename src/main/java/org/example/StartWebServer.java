package org.example;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.riversun.jetty.basicauth.BasicAuth;
import org.riversun.jetty.basicauth.BasicAuthResourceHandler;

/**
 * 
 * Start web application server.
 * 
 * Web server can serve Servlets/static contents with ResourceHandler
 * 
 *
 */
public class StartWebServer {

    public void start() {

        final int PORT = 8080;

        BasicAuth basicAuth = new BasicAuth.Builder().setRealm("private site")
                .addUserPath("user1", "pass1", "/*")
                .addUserPath("user2", "pass2", "/index.html,/api")
                .addUserPath("user3", "pass3", "/api")// allows only "/api"
                .addUserPath("user4", "pass4", "/private1/index.html")// allows only "/private1/index.html"
                .addUserPath("user5", "pass5", "/private1/*")// allows "/private1/" directory
                .build();

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

        servletContextHandler.addServlet(ExampleServlet.class, "/api");

        final HandlerList handlerList = new HandlerList();

        // logging for basic auth
        // org.riversun.jetty.basicauth.LogSetup.enableLogging();

        // If you want to use basic authentication,you can use
        // BasicAuthResourceHandler instead of ResourceHandler.
        final BasicAuthResourceHandler resourceHandler = new BasicAuthResourceHandler();

        resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");
        resourceHandler.setDirectoriesListed(false);

        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setCacheControl("no-store,no-cache,must-revalidate");

        resourceHandler.setBasicAuth(basicAuth);
        resourceHandler.setRetryBasicAuth(true);

        resourceHandler.addRetryBasicAuthExcludedPath("/favicon.ico");

        handlerList.addHandler(resourceHandler);
        handlerList.addHandler(servletContextHandler);

        final Server jettyServer = new Server();
        jettyServer.setHandler(handlerList);

        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);

        final HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpConfig);
        final ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnFactory);
        httpConnector.setPort(PORT);
        jettyServer.setConnectors(new Connector[] { httpConnector });

        try {
            jettyServer.start();
            System.out.println("Server started.");
            jettyServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("serial")
    public static class ExampleServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            final String CONTENT_TYPE = "text/plain; charset=UTF-8";
            resp.setContentType(CONTENT_TYPE);

            final PrintWriter out = resp.getWriter();
            out.println("OK");
            out.close();
        }

    }

    public static void main(String[] args) {
        new StartWebServer().start();
    }

}
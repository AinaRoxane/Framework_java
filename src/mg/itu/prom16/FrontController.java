package mg.itu.prom16;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import annotation.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    String classList = "<br> <h3 style='margin-left:10%;'>";

    public void init() throws ServletException {
        String packagename = this.getInitParameter("package");
        packagename  = packagename .replace(".", "/");
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            java.net.URL resource = classLoader.getResource(packagename );
            String filepath = resource.getFile().replace("%20"," ");
            File directory = new File(filepath);
            if(directory.isDirectory()){
                packagename  = packagename .replace("/", ".");
                for (String filename: directory.list()){
                        filename = filename.substring(0, filename.length()- 6);
                        String className = packagename +"."+ filename;
                            Class<?> clazz = Class.forName(className);
                        if(clazz.isAnnotationPresent(Controller.class)){
                            classList += className +"<br>";
                        }
                }
            }
            classList += "</h3>";
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());                
        }
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<h2> CONTROLLER LIST: </h2>"+ classList);
        }
}

@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
processRequest(request, response);
}

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
processRequest(request, response);
}
}


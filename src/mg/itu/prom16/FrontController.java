package mg.itu.prom16;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

import annotation.Controller;
import annotation.Get;
import utils.Mapping;
import utils.PackageScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
        PackageScanner scanner;
        HashMap<String , Mapping > ListService;

    public void init() {
        scanner = new PackageScanner();
        // get parameter written in web.xml:
        String packagename = this.getInitParameter("package");
        // get all controller within packagename:
        ListService = scanner.getMapping(packagename, Controller.class);
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
            String url = scanner.conform_url(request.getRequestURL().toString());
            try {
                Mapping mapping = ListService.get(url);
                Class<?> clazz =  java.lang.Class.forName(mapping.getClassName());
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Method method = clazz.getDeclaredMethod(mapping.getMethodName());
                String result = (String) method.invoke(instance);
                out.println(result);
            } catch (Exception e) {
                out.println("Aucun controller ne prend en compte cet url!");
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


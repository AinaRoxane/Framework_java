package mg.itu.prom16;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import annotation.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.PackageScanner;

public class FrontController extends HttpServlet {
        PackageScanner scanner;
        boolean isChecked;
        String ListService;

    public void init() {
        scanner = new PackageScanner();
        // get parameter written in web.xml:
        String packagename = this.getInitParameter("package");
        // get all controller within packagename:
        ListService = scanner.getAnnotatedClassWithin(packagename, Controller.class);
        // mark the package as checked which means we have the list of controllers within the package:
        isChecked = true;
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        if(!isChecked){
            init();
        } else{
            try (PrintWriter out = response.getWriter()) {
                out.println(ListService);
            }
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


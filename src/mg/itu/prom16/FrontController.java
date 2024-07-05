package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Controller;
import exception.terminal.DuplicateGetMappingException;
import exception.web.ReturnTypeException;
import utils.MapHandler;
import utils.Mapping;
import utils.ModelView;
import utils.MySession;
import utils.PackageScanner;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class FrontController extends HttpServlet {
    private PackageScanner scanner;
    private HashMap<String, Mapping> ListService;

    @Override
    public void init() {
        try {
            scanner = new PackageScanner();
            String packagename = this.getInitParameter("controller-package");
            ListService = scanner.getMapping(packagename, Controller.class);
        } catch (DuplicateGetMappingException e) {
            log("DuplicateGetMappingException occurred: " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String url = scanner.conform_url(request.getRequestURL().toString());
            Mapping mapping = ListService.get(url);
            if (mapping == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL not mapped.");
                return;
            }

            MapHandler mapHandler = new MapHandler();
            String modelPackage = this.getInitParameter("model-package");

            try {
                Class<?> clazz = Class.forName(mapping.getClassName());
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Method method = mapHandler.getMethod(clazz, mapping.getMethodName());

                Enumeration<String> parameterNames = request.getParameterNames();
                Map<String, Object> objets = mapHandler.getAllObject(parameterNames, modelPackage, request);
                
                HttpSession session = request.getSession();
                if (mapHandler.hasMySessionAttribute(clazz.getDeclaredFields()) != -1) {
                    out.println("miditra");
                    Method sessionGetter = clazz.getDeclaredMethod("getMySession");
                    MySession mysession=  (MySession) sessionGetter.invoke(instance);
                    mysession.setSession(session);
                }
                
                List<Object> methodArgs = mapHandler.getMethodArguments(method, objets, session);

                Object result = method.invoke(instance, methodArgs.toArray());

                if (result instanceof String) {
                    out.println(result);
                } else if (result instanceof ModelView) {
                    ModelView mv = (ModelView) result;
                    if (mv.getData() != null) {
                        mv.getData().forEach(request::setAttribute);
                    }
                    RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
                    dispatcher.forward(request, response);
                } else {
                    throw new ReturnTypeException("Return type not handled for: " + url);
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                log("Error occurred while invoking method: " + cause);
                cause.printStackTrace(out);
            } catch (Exception e) {
                out.println("<h3>Oops!</h3>");
                out.println("<p>An error occurred while processing the request.</p>");
                e.printStackTrace(out);
            }
        }finally {
            if (out != null) {
                out.close(); 
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

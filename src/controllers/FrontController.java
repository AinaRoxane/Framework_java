package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.google.gson.Gson;

import models.ModelView;
import models.annotations.classes.Controller;
import models.annotations.methods.Auth;
import models.annotations.methods.RestAPI;
import models.exceptions.WebException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.maps.MapHandler;
import models.maps.Mapping;
import models.scanners.PackageScanner;
import models.utils.RequestHandler;
import models.utils.RequestVerb;
import models.utils.VerbMethod;
import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class FrontController extends HttpServlet {
    private PackageScanner scanner;
    private HashMap<String, Mapping> ListService;
    private RequestHandler requestHandler;

    @Override
    public void init() {
        try {
            scanner = new PackageScanner();
            requestHandler = new RequestHandler();
            String packagename = this.getInitParameter("controller-package");
            ListService = scanner.getMapping(packagename, Controller.class);
        } catch (Exception e) {
            log("DuplicateGetMappingException occurred: " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String url = scanner.conform_url(request.getRequestURL().toString());
            Mapping mapping = ListService.get(url);
            if (mapping == null && !url.contains("assets")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL not mapped.");
                return;
            } 

            MapHandler mapHandler = new MapHandler();
            try {
                Class<?> clazz = Class.forName(mapping.getClassName());

                // Vérification de l'annotation @Auth au niveau de la classe
                if (clazz.isAnnotationPresent(Auth.class)) {
                    Auth auth = clazz.getAnnotation(Auth.class);
                    HttpSession session = request.getSession();
                    String userRole = (String) session.getAttribute("user-role");
                    if (userRole != null && !userRole.equals(auth.role())) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "User has not enough privileges to access this page!");
                        return;
                    }
                }

                Object instance = clazz.getDeclaredConstructor().newInstance();
                Method method = mapHandler.getMethod(clazz, mapping.getMethodName());

                // Vérification de l'annotation @Auth
                if (method.isAnnotationPresent(Auth.class)) {
                    Auth auth = method.getAnnotation(Auth.class);
                    String requiredRole = auth.role();
                    HttpSession session = request.getSession(false);
                    String userRole = (session != null) ? (String) session.getAttribute("user-role") : null;
                    
                    if (userRole == null || !userRole.equals(requiredRole)) {
                        throw new SecurityException("User has not enough privileges to access this page!");
                    }
                }

                // Check if method is annotated with @RestAPI
                if (method.isAnnotationPresent(RestAPI.class)) {
                    Object result = method.invoke(instance);
                    Gson gson = new Gson();
                    String jsonResponse;

                    if (result instanceof ModelView) {
                        ModelView mv = (ModelView) result;
                        jsonResponse = gson.toJson(mv.getData());
                    } else {
                        jsonResponse = gson.toJson(result);
                    }

                    response.setContentType("application/json");
                    out.print(jsonResponse);
                    return;
                }


                String methodVerb = RequestVerb.getMethodVerb(method);
                VerbMethod verbMethod = new VerbMethod(method, methodVerb);

                if (!verbMethod.isRequestValid(request)) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP verb not allowed for this URL.");
                    return;
                }
                if (RequestVerb.GET.equalsIgnoreCase(methodVerb)) {
                    requestHandler.handleGetRequest(request, response, clazz, instance, method, this.getInitParameter("model-package"));
                } else if (RequestVerb.POST.equalsIgnoreCase(methodVerb)) {
                    requestHandler.handlePostRequest(request, response, clazz, instance, method,
                            this.getInitParameter("model-package"));
                }
            } catch (Exception e) {
                WebException.showMessage(out, e);
            }
        } finally {
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

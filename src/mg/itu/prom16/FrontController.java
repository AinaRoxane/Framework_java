package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import annotation.Controller;
import annotation.Get;
import annotation.Param;
import exception.terminal.DuplicateGetMappingException;
import exception.web.ReturnTypeException;
import utils.Convertor;
import utils.Mapping;
import utils.ModelView;
import utils.PackageScanner;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private PackageScanner scanner;
    private HashMap<String, Mapping> ListService;

    @Override
    public void init() {
        try {
            scanner = new PackageScanner();
            String packagename = this.getInitParameter("package");
            ListService = scanner.getMapping(packagename, Controller.class);
        } catch (DuplicateGetMappingException e) {
            log("DuplicateGetMappingException occurred: " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String url = scanner.conform_url(request.getRequestURL().toString());
        Mapping mapping = ListService.get(url);
        if (mapping == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL not mapped.");
            return;
        }

        try {
            Class<?> clazz = Class.forName(mapping.getClassName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getDeclaredMethod(mapping.getMethodName());

            // Prepare method arguments
            List<Object> methodArgs = new ArrayList<>();
            String paramName = "";
            for(Parameter parameter : method.getParameters()){
                if(parameter.isAnnotationPresent(Param.class)){
                    paramName = parameter.getAnnotation(Param.class).name();
                } else{
                    paramName = parameter.getName();
                }
                String paramValue = request.getParameter(paramName);
                methodArgs.add(Convertor.convertParameter(parameter.getType(), paramValue));
                
            }

            Object result = method.invoke(instance, methodArgs.toArray());

            if (result instanceof String) {
                out.println(result);
            }
            else if (result instanceof ModelView) {
                ModelView mv = (ModelView) result;
                request.setAttribute(mv.getVariableName(), mv.getData().get(mv.getVariableName()));
                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
                dispatcher.forward(request, response);
            } else{
                throw new ReturnTypeException("Return type no handled for :"+url);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | ReturnTypeException e) {
            out.println("<h3>Oops!</h3>");
            out.println("<p>An error occurred while processing the request.</p>");
            out.println(e);
        } catch (Exception e) {
            out.println("<h3>Oops!</h3>");
            out.println("<p>An unexpected error occurred.</p>");
            out.println(e);;
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

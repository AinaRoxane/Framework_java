package models.utils;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import models.ModelView;
import models.maps.MapHandler;
import models.sessions.MySession;
import models.uploads.File;
import models.uploads.FileUtils;

public class RequestHandler {

    public void handleGetRequest(HttpServletRequest request, HttpServletResponse response, Class<?> clazz, Object instance, Method method, 
             String modelPackage) throws Exception {
        MapHandler mapHandler = new MapHandler();
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, Object> objets = mapHandler.getAllObject(parameterNames, modelPackage, request, response);
        FormHandler.validateParameters(request, response, method, objets);
        proceed(request, response, mapHandler, clazz, instance, method, objets);
    }
    
    public void handlePostRequest(HttpServletRequest request, HttpServletResponse response, Class<?> clazz, Object instance, Method method, 
            String modelPackage) throws Exception {
        // Check if the request is multipart (file upload)
        if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
            // Handle file upload
            handleFileUpload(request, response, clazz, instance, method);
        } else {
            // Handle regular POST requests
            handleGetRequest(request, response, clazz, instance, method , modelPackage);
        }
    }

    public void handleFileUpload(HttpServletRequest request, HttpServletResponse response, Class<?> clazz, Object instance, Method method) throws Exception {
        MapHandler mapHandler = new MapHandler();
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, Object> objects = new HashMap<>();

        for (Part part : request.getParts()) {
            String paramName = part.getName();
            File uploadedFile = FileUtils.createRequestFile(paramName, request);
            objects.put(paramName, uploadedFile);
        }

        // Add other parameters to the objects map
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (!paramName.startsWith("file")) {
                objects.put(paramName, request.getParameter(paramName));
            }
        }

        proceed(request, response, mapHandler, clazz, instance, method, objects);
    }

    public void proceed (HttpServletRequest request, HttpServletResponse response, MapHandler mapHandler, Class<?> clazz, Object instance, Method method, Map<String, Object> objects) throws Exception{
        // Handle session attributes (if any)
        HttpSession session = request.getSession();
        if (mapHandler.hasMySessionAttribute(clazz.getDeclaredFields()) != -1) {
            Method sessionGetter = clazz.getDeclaredMethod("getMySession");
            MySession mysession = (MySession) sessionGetter.invoke(instance);
            mysession.setSession(session);
        }

        // Prepare method arguments and invoke the method
        List<Object> methodArgs = mapHandler.getMethodArguments(method, objects, session);
        Object result = method.invoke(instance, methodArgs.toArray()); // line 79

        if (result instanceof String) {
            request.setAttribute("result", result);
        } else if (result instanceof ModelView) {
            ModelView mv = (ModelView) result;
            if (mv.getData() != null) {
                mv.getData().forEach(request::setAttribute);
            }
            
            if(mv.getNextPage() != null){
                response.sendRedirect("./"+mv.getNextPage());
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
                dispatcher.forward(request, response);
            }
        } else {
            throw new Exception("Return type not handled for: " + clazz.getName());
        }
    }

}

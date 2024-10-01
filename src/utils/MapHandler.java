package utils;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Param;
import exception.web.ReturnTypeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpSession;

public class MapHandler {
    public int hasMySessionAttribute(Field[] fields){
        int index = -1;
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            Class<?> fieldClazz = fields[i].getType();
            if(fieldClazz.equals(MySession.class)){
                index = i;
            }
        }
        return index;
    }

    public Method getMethod(Class<?> clazz, String methodName){
        Method method = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        return method;
    }

    public Map<String, Object> getAllObject( Enumeration<String> parameterNames, String modelPackage, HttpServletRequest request) throws Exception{
        Map<String, Object> objets = new HashMap<>();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] parts = paramName.split("\\.");
            if (parts.length > 1) {
                String objectName = parts[0];
                Class<?> objetclazz = Class.forName(modelPackage + "." + objectName);
                Object mydataholder;
                if (!objets.containsKey(objectName)) {
                    mydataholder = objetclazz.getDeclaredConstructor().newInstance();
                    objets.put(objectName, mydataholder);
                } else {
                    mydataholder = objets.get(objectName);
                }
                Method datasetter = null;
                for (Method m : objetclazz.getDeclaredMethods()) {
                    if (m.getName().equals("set" + parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1))) {
                        datasetter = m;
                        break;
                    }
                }
                String paramValue = request.getParameter(paramName);
                datasetter.invoke(mydataholder, Convertor.convertParameter(datasetter.getParameterTypes()[0], paramValue));
            } else {
                String paramValue = request.getParameter(paramName);
                objets.put(paramName, paramValue);
            }
        }
        return objets;
    }
    
    public List<Object> getMethodArguments (Method method, Map<String, Object> objets, HttpSession session) throws Exception{
        List<Object> methodArgs = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            String paramName = "";
            if(parameter.isAnnotationPresent(Param.class)){
                paramName = parameter.getAnnotation(Param.class).name();
                methodArgs.add(objets.get(paramName));
            }
            else if(parameter.getType().equals(MySession.class)){
                methodArgs.add(session);
            }
            else{
                throw new ReturnTypeException("<br><h4> Annotation (@Param) missing in "+method.getName()+"</h4>");
            }
        }
        return methodArgs;
    }
}
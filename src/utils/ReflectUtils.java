package utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import annotation.RequestParameter;
import utils.manager.data.Session;
import exception.AnnotationNotPresentException;
import utils.manager.url.Mapping;

public class ReflectUtils {
    static List<Object> methodParameters;

    public static List<Object> getMethodParameters() {
        return methodParameters;
    }

    public static void setMethodParameters(List<Object> methodParameters) {
        ReflectUtils.methodParameters = methodParameters;
    }


    private ReflectUtils() {
    }

    public static boolean hasAttributeOfType(Class<?> clazz, Class<?> type) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static String getMethodName(String initial, String attributeName) {
        return initial + Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
    }

    public static String getSetterMethod(String attributeName) {
        return getMethodName("set", attributeName);
    }

    public static void setSessionAttribute(Object object, HttpServletRequest request) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String methodName = null; 
        for(Field field : object.getClass().getDeclaredFields()) {
            if (field.getType().equals(Session.class)) {
                methodName = getSetterMethod(field.getName());
                Session session = new Session(request.getSession());
                executeMethod(object, methodName, session);
            }
        }
    }

    public static Object executeRequestMethod( Mapping mapping, HttpServletRequest request, HttpServletResponse response, String verb) throws Exception {
        List<Object> objects = new ArrayList<>();
        Class<?> objClass = mapping.getClazz();
        Object requestObject = objClass.getConstructor().newInstance();
        Method method = mapping.getSpecificVerbMethod(verb).getMethod();

        setSessionAttribute(requestObject, request);

        // Collect parameters
        for (Parameter parameter : method.getParameters()) {
            Class<?> clazz = parameter.getType();
            Object paramInstance = ObjectUtils.getDefaultValue(clazz);
            
            if (!parameter.isAnnotationPresent(RequestParameter.class) && !clazz.equals(Session.class)) {
                throw new AnnotationNotPresentException(
                    "One of your parameter requires the `@RequestParameter` annotation");
            }

            paramInstance = ObjectUtils.getParameterInstance(request, parameter, clazz, paramInstance);

            // Validate the parameter object if necessary
            Map<String, String> validationErrors = ValidationUtils.validateObject(paramInstance);
            if (!validationErrors.isEmpty()) {
                // Store errors in request scope for rendering on the previous page
                request.setAttribute("validationErrors", validationErrors);

                // Redirect to the previous page with the errors
                String referer = request.getHeader("Referer"+"?");
                if (referer != null) {
                        public static Object executeRequestMethod( Mapping mapping, HttpServletRequest request, HttpServletResponse response, String verb) throws Exception {
        List<Object> objects = new ArrayList<>();
        Class<?> objClass = mapping.getClazz();
        Object requestObject = objClass.getConstructor().newInstance();
        Method method = mapping.getSpecificVerbMethod(verb).getMethod();

        setSessionAttribute(requestObject, request);

        // Collect parameters
        for (Parameter parameter : method.getParameters()) {
            Class<?> clazz = parameter.getType();
            Object paramInstance = ObjectUtils.getDefaultValue(clazz);
            
            if (!parameter.isAnnotationPresent(RequestParameter.class) && !clazz.equals(Session.class)) {
                throw new AnnotationNotPresentException(
                    "One of your parameter requires the `@RequestParameter` annotation");
            }

            paramInstance = ObjectUtils.getParameterInstance(request, parameter, clazz, paramInstance);

            // Validate the parameter object if necessary
            Map<String, String> validationErrors = ValidationUtils.validateObject(paramInstance);
            if (!validationErrors.isEmpty()) {
                // Store errors in request scope for rendering on the previous page
                request.setAttribute("validationErrors", validationErrors);

                // Redirect to the previous page with the errors
                String referer = request.getHeader("Referer");
                if (referer != null) {
                    try {
                        URL url = new URL(referer);
                        String refererUri = url.getPath();
                        response.sendRedirect(refererUri);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
            objects.add(paramInstance);
        }
        return executeMethod(requestObject, method.getName(), objects.toArray());
    }
                    String errorParam = "errors=" + URLEncoder.encode(validationErrors.toString(), "UTF-8");
                    response.sendRedirect(referer + "?" + errorParam);
                    return null; // Stop further processing
                }
            }
            objects.add(paramInstance);
        }
        return executeMethod(requestObject, method.getName(), objects.toArray());
    }


    public static Class<?>[] getArgsClasses(Object... args) {
        Class<?>[] classes = new Class[args.length];
        int i = 0;

        for (Object object : args) {
            classes[i] = object.getClass();
            i++;
        }

        return classes;
    }

    public static Object executeMethod(Object object, String methodName, Object... args) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = object.getClass().getMethod(methodName, getArgsClasses(args));
        return method.invoke(object, args);
    }

    public static Object executeClassMethod(Class<?> clazz, String methodName, Object... args)
            throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            InstantiationException {
        Object object = clazz.getConstructor().newInstance();
        return executeMethod(object, methodName, args);
    }
}

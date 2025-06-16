package models.maps;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.annotations.args.Param;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.convertors.Convertor;
import models.sessions.MySession;
import models.utils.FormHandler;
import models.exceptions.WebException;

public class MapHandler {
    public int hasMySessionAttribute(Field[] fields) {
        int index = -1;
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            Class<?> fieldClazz = fields[i].getType();
            if (fieldClazz.equals(MySession.class)) {
                index = i;
            }
        }
        return index;
    }

    public Method getMethod(Class<?> clazz, String methodName) {
        Method method = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        return method;
    }

    public List<Object> getMethodArguments(Method method, Map<String, Object> objets, HttpSession session)
        throws Exception {
        List<Object> methodArgs = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Param.class)) {
                String paramName = parameter.getAnnotation(Param.class).name();
                Object value = objets.get(paramName);
                
                if (value == null) {
                    methodArgs.add(null);
                    continue;
                }
                
                // Handle conversion for all types consistently
                if (!parameter.getType().isInstance(value)) {
                    String stringValue = value.toString();
                    Object convertedValue = Convertor.convertParameter(parameter.getType(), stringValue);
                    methodArgs.add(convertedValue);
                } else {
                    methodArgs.add(value);
                }
            } else if (parameter.getType().equals(MySession.class)) {
                methodArgs.add(session);
            } else {
                throw new Exception(WebException.reformulate("Annotation (@Param) missing in " + method.getName()));
            }
        }
        return methodArgs;
    }

    public Map<String, Object> getAllObject(Enumeration<String> parameterNames, String baseModelPackage,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> objects = new HashMap<>();
        PrintWriter out = response.getWriter();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (paramName.contains(".")) {
                if (paramName.contains("!")) {
                    Map<String, Object> attributes = new HashMap<>();
                    String[] pieces = paramName.split("\\!");

                    getObject(request, paramName, pieces[1], baseModelPackage, attributes); // the matter lies in here attribute.field to initialize isn't initialized!
                    
                    String[] ps = pieces[0].split("\\.");
                    String fieldName = ps[ps.length - 1];
                    String objectName = ps[ps.length - 2];
                    String packagePath = String.join(".", Arrays.copyOf(ps, ps.length - 2));
                    String fullClassName = baseModelPackage + "." + packagePath + "." + objectName;
                    try {
                        Class<?> objectClass = Class.forName(fullClassName);
                        Object dataHolder = getOrCreateInstance(objectClass, objectName, objects);
                        Method setterMethod = findSetterMethod(objectClass, fieldName);
                        setterMethod.setAccessible(true);
                        Object value = null;
                        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                            value = entry.getValue();
                        }
                        setterMethod.invoke(dataHolder, value);
                        objects.put(objectName, dataHolder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    getObject(request, paramName, paramName,baseModelPackage, objects);
                }
            } else {
                String paramValue = request.getParameter(paramName);
                objects.put(paramName, paramValue);
            }
        }
        
        // Debugging: Print all objects in the Map
        /*out.println("<h3>Debugging: Objects Map Contents</h3>");
        out.println("<pre>");
        for (Map.Entry<String, Object> entry : objects.entrySet()) {
            out.println("Key: " + entry.getKey());
            Object value = entry.getValue();

            if (value == null) {
                out.println("  Value: null");
            } else {
                out.println("  Value Type: " + value.getClass().getName());

                // Try to print object fields if it's not a simple type
                if (!(value instanceof Number) &&
                        !(value instanceof String) &&
                        !(value instanceof Boolean)) {
                    try {
                        out.println("  Fields:");
                        for (Field field : value.getClass().getDeclaredFields()) {
                            field.setAccessible(true);
                            out.println("    " + field.getName() + ": " + field.get(value));
                        }
                    } catch (Exception e) {
                        out.println("  Could not inspect object fields: " + e.getMessage());
                    }
                } else {
                    out.println("  Value: " + value);
                }
            }
            out.println("----------------------");
        }
        out.println("</pre>"); */
        return objects;
    }
    
    public void getObject(HttpServletRequest request, String toGet, String paramName, String baseModelPackage,
            Map<String, Object> objects) {
        
        String[] parts = paramName.split("\\.");
        String fieldName = parts[parts.length - 1];
        String objectName = parts[parts.length - 2];
        String packagePath = String.join(".", Arrays.copyOf(parts, parts.length - 2));

        String fullClassName = baseModelPackage + "." + packagePath + "." + objectName;

        try {
            Class<?> objectClass = Class.forName(fullClassName);
            Object dataHolder = getOrCreateInstance(objectClass, objectName, objects);
            Method setterMethod = findSetterMethod(objectClass, fieldName);
            setValue(request, toGet, setterMethod, dataHolder, objects, objectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getOrCreateInstance(Class<?> objectClass, String objectName, Map<String, Object> objects) 
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (objects.containsKey(objectName)) {
            return objects.get(objectName);
        }
        return objectClass.getDeclaredConstructor().newInstance();
    }

    private Method findSetterMethod(Class<?> objectClass, String fieldName) throws NoSuchMethodException {
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        
        for (Method method : objectClass.getDeclaredMethods()) {
            if (method.getName().equals(setterName)) {
                return method;
            }
        }
        
        throw new NoSuchMethodException("No setter found for field: " + fieldName);
    }

    private void setValue(HttpServletRequest request, String paramName, Method setterMethod, 
            Object dataHolder, Map<String, Object> objects, String objectName) throws Exception {
        Class<?> paramType = setterMethod.getParameterTypes()[0];
        String paramValue = request.getParameter(paramName);
        Object convertedValue = Convertor.convertParameter(paramType, paramValue);

        setterMethod.setAccessible(true);
        setterMethod.invoke(dataHolder, convertedValue);
        objects.put(objectName, dataHolder);
    }
}

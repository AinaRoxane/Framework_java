package models.scanners;

import models.annotations.methods.Get;
import models.annotations.methods.Post;
import models.annotations.methods.RestAPI;
import models.annotations.methods.Url;
import models.exceptions.WebException;
import models.maps.Mapping;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;

public class PackageScanner {
    
    public String getAnnotatedClassWithin(String packagename, Class<? extends Annotation> annotationClass) {
        String ListService = "";
        packagename = packagename.replace(".", "/");

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagename);

            if (resource == null) {
                System.out.println("Package not found: " + packagename);
                return "";
            }

            String filepath = resource.getFile().replace("%20", " ");
            File directory = new File(filepath);

            if (directory.isDirectory()) {
                packagename = packagename.replace("/", ".");

                for (String filename : directory.list()) {
                    if (filename.endsWith(".class")) {
                        filename = filename.substring(0, filename.length() - 6);
                        String className = packagename + "." + filename;
                        System.out.println("Found class: " + className);
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            System.out.println("Annotated class found: " + className);
                            ListService += className + ",";
                        }
                    }
                }
            } else {
                System.out.println("Not a directory: " + filepath);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }
        return ListService;
    }

    public HashMap<String, Mapping> getMapping(String packagename, Class<? extends Annotation> annotationClass) throws Exception {
        String[] ListController = getAnnotatedClassWithin(packagename, annotationClass).split(",");
        HashMap<String, Mapping> ListClasses = new HashMap<>();
        for (String className : ListController) {
            if (className.isEmpty()) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    // Handle @Get annotation
                    if (method.isAnnotationPresent(RestAPI.class) || method.isAnnotationPresent(Get.class) || method.isAnnotationPresent(Post.class)) {
                        if (method.isAnnotationPresent(Url.class)) {
                            String url = method.getAnnotation(Url.class).value();
                            Mapping value = new Mapping(className, method.getName());
                            if (!wasUsed(url, ListClasses)) {
                                ListClasses.put(url, value);
                            } else {
                                throw new Exception(WebException.reformulate("The URL: " + url + " from " + className
                                        + " method " + method.getName() + " is already used by another class!"));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error processing class: " + className + " - " + e.getMessage());
            }
        }
        return ListClasses;
    }

    public boolean wasUsed(String url, HashMap<String, Mapping> ListClasses) {
        return ListClasses.containsKey(url);
    }

    public String conform_url(String url) {
        String newURL = "/";
        String[] path1 = url.split("//");
        String[] path = path1[1].split("/");
        for (int i = 2; i < path.length; i++) {
            newURL += path[i] + "/";
        }
        return newURL.substring(0, newURL.length() - 1);
    }
}

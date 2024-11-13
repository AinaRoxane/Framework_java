package utils;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Controller;
import annotation.request.URL;
import utils.manager.data.RequestVerb;
import utils.manager.data.VerbMethod;
import exception.DuplicateUrlException;
import exception.InvalidControllerPackageException;
import utils.manager.url.Mapping;

public class PackageScanner {
    private PackageScanner() {
    }

    public static Map<String, Mapping> scanPackage(String packageName)
            throws ClassNotFoundException, IOException, DuplicateUrlException, InvalidControllerPackageException {
        if (packageName == null || packageName.isBlank()) {
            throw new InvalidControllerPackageException("Controller package provider cannot be null");
        }

        Map<String, Mapping> result = new HashMap<>();

        ArrayList<Class<?>> classes = (ArrayList<Class<?>>) PackageUtils.getClassesWithAnnotation(packageName,
                Controller.class);
        for (Class<?> clazz : classes) {
            List<Method> classMethods = PackageUtils.getClassMethodsWithAnnotation(clazz, URL.class);

            for (Method method : classMethods) {
                URL methodAnnotation = method.getAnnotation(URL.class);
                String url = methodAnnotation.value();
                
                VerbMethod verbMethod = new VerbMethod(method, RequestVerb.getMethodVerb(method));

                Mapping mapping = result.get(url);
                
                if (mapping != null) {
                    mapping.addVerbMethod(verbMethod);
                } else {
                    mapping = new Mapping(clazz);

                    mapping.addVerbMethod(verbMethod);
                    result.put(url, mapping);
                }
            }
        }

        return result;
    }
}
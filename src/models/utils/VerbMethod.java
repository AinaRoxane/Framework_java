package models.utils;

import java.lang.reflect.Method;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import models.annotations.methods.RestAPI;

public class VerbMethod {
    Method method;
    String verb;

    // Method
    public boolean isRequestValid(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        return this.verb.equalsIgnoreCase(httpMethod) || 
               ("GET".equalsIgnoreCase(httpMethod) && "POST".equalsIgnoreCase(this.verb)) ||
               ("POST".equalsIgnoreCase(httpMethod) && "GET".equalsIgnoreCase(this.verb));
    }

    public boolean isRestAPI() {
        return method.isAnnotationPresent(RestAPI.class);
    }

    // Constructor
    public VerbMethod(Method method, String verb) {
        setMethod(method);
        setVerb(verb);
    }

    // Getters and setters
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    // Override methods
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VerbMethod)) {
            return false;
        }
        VerbMethod other = (VerbMethod) obj;

        return other.getVerb().equalsIgnoreCase(this.getVerb());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getVerb());
    }
}
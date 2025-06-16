package models.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.annotations.args.Param;
import models.annotations.validations.Required;

public class FormHandler {
    public static void validateParameters(HttpServletRequest request,
            HttpServletResponse response,
            Method method,
            Map<String, Object> parametersMap) throws IOException {

        Parameter[] parameters = method.getParameters();
        Map<String, String> validationErrors = new HashMap<>();

        for (Parameter param : parameters) {
            String paramName = getParameterName(param);
            Object paramValue = parametersMap.get(paramName);

            validateRequiredField(param, paramName, paramValue, validationErrors);
            // Add other validations here if needed (@Size, @Pattern, etc.)
        }

        if (!validationErrors.isEmpty()) {
            handleValidationErrors(request, response, validationErrors);
        }
    }

    private static String getParameterName(Parameter param) {
        return param.isAnnotationPresent(Param.class)
                ? param.getAnnotation(Param.class).name()
                : param.getName();
    }

    private static void validateRequiredField(Parameter param,
            String paramName,
            Object paramValue,
            Map<String, String> validationErrors) {
        if (param.isAnnotationPresent(Required.class)) {
            if (paramValue == null) {
                validationErrors.put(paramName, paramName + " is required");
            } else if (paramValue instanceof String && ((String) paramValue).trim().isEmpty()) {
                validationErrors.put(paramName, paramName + " cannot be empty");
            }
            // Add more type-specific checks if needed
        }
    }

    private static void handleValidationErrors(HttpServletRequest request,
            HttpServletResponse response,
            Map<String, String> validationErrors) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("validationErrors", validationErrors);

        String referer = request.getHeader("referer");
        if (referer == null) {
            referer = request.getContextPath();
        }
        response.sendRedirect(referer);
    }
}
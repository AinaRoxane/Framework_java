package utils.manager.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletResponse;
import exception.UrlNotFoundException;

public class ExceptionHandler {
    private static final Logger logger = Logger.getLogger(ExceptionHandler.class.getName());
    
    private ExceptionHandler() {
    }

    public static void processError(HttpServletResponse response, int statusCode, Exception exception) throws IOException {
        String errorName = getErrorName(statusCode);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang=\"en\">");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">");
            writer.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            writer.println("<title>ERROR</title>");
            writer.println("<style>");
            writer.println(
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #e6f2ff; color: #333; line-height: 1.6; padding: 20px; }");
            writer.println("h1 { color: #0066cc; text-align: center; }");
            writer.println("h2 { color: #004080; }");
            writer.println(
                    ".container { max-width: 800px; margin: 0 auto; background-color: #f0f8ff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
            writer.println(".error-code { font-size: 1.2em; color: #0066cc; }");
            writer.println(".error-message { font-weight: bold; color: #003366; }");
            writer.println(
                    ".stack-trace { background-color: #e1ecf4; border: 1px solid #cce0ff; padding: 10px; overflow-x: auto; color: #002b4f; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<div class=\"container\">");
            writer.println("<h1>" + statusCode + " " + errorName + "</h1>");
            writer.println("<p class=\"error-message\">" + exception.getMessage() + "</p>");
            writer.println("<pre class=\"stack-trace\">");
            exception.printStackTrace(writer);
            writer.println("</pre>");
            writer.println("</div>");
            writer.println("</body>");
            writer.println("</html>");
        }
    }

    private static String getErrorName(Integer statusCode) {
        if (statusCode == null)
            return "Unknown Error";
        switch (statusCode) {
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "HTTP Error " + statusCode;
        }
    }

    public static void handleException(Exception e, HttpServletResponse response) {
        try {
            if (!response.isCommitted()) {
                int errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                if (e instanceof UrlNotFoundException) {
                    errorCode = HttpServletResponse.SC_NOT_FOUND;
                }
                processError(response, errorCode, e);
            }
        } catch (IOException exc) {
            logger.log(Level.SEVERE, exc.getMessage());
        }
    }

    public static void handleExceptions(List<Exception> exceptions, HttpServletResponse response) {
        for (Exception e : exceptions) {
            handleException(e, response);
        }
    }
}

package models.exceptions;

import java.io.PrintWriter;

public class WebException {
    public static String reformulate(String message) {
        String msg = " <script>\n" +
                "        // Affiche une alerte au chargement de la page\n" +
                "        alert(\"" + message + "\");\n" +
                "    </script>";
        return msg;
    }

    public static void showMessage(PrintWriter out, Exception e) {
        out.println("<div style='"
                + "font-family: Arial, sans-serif;"
                + "max-width: 900px;"
                + "margin: 20px auto;"
                + "padding: 20px;"
                + "border-radius: 5px;"
                + "background-color: #f8d7da;"
                + "color: #721c24;"
                + "border: 1px solid #f5c6cb;"
                + "'>");
        out.println("<h3 style='"
                + "margin-top: 0;"
                + "color: #721c24;"
                + "'>⚠️ Oops!</h3>");
        out.println("<p style='"
                + "margin-bottom: 15px;"
                + "'>An error occurred while processing the request.</p>");
        out.println("<div style='"
                + "padding: 5%;"
                + "background-color: white;"
                + "border-radius: 3px;"
                + "border: 1px solid #ddd;"
                + "font-family: monospace;"
                + "font-size: 14px;"
                + "color: #dc3545;"
                + "'>");
        e.printStackTrace(out);
        out.println("</div>");
        out.println("<p style='"
                + "margin-top: 15px;"
                + "font-size: 14px;"
                + "'>Please try again or contact support if the problem persists.</p>");
        out.println("</div>");
    }
}
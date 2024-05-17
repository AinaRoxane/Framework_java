package utils;
import java.io.File;
import java.lang.annotation.Annotation;

public class PackageScanner {
        public String getAnnotatedClassWithin (String packagename,   Class<? extends Annotation> annotationClass){
            String ListService = "<strong> List of the corresponding service within "+packagename+" : </strong> <br> <p style='margin:-5% 10%;'>";
            packagename  = packagename .replace(".", "/");
            
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                java.net.URL resource = classLoader.getResource(packagename );

                String filepath = resource.getFile().replace("%20"," ");
                File directory = new File(filepath);

                if(directory.isDirectory()){
                    packagename  = packagename .replace("/", ".");

                    for (String filename: directory.list()){
                            filename = filename.substring(0, filename.length()- 6);
                            String className = packagename +"."+ filename;
                                Class<?> clazz = Class.forName(className);
                            if(clazz.isAnnotationPresent(annotationClass)){
                                ListService += className +"<br>";
                            }
                    }
                }
                ListService += "</p>";
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());                
            }
            return ListService;
        }
}

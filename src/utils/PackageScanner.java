package utils;
import java.io.File;
import annotation.Get;
import exception.terminal.DuplicateGetMappingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

public class PackageScanner {
        public String getAnnotatedClassWithin (String packagename,   Class<? extends Annotation> annotationClass){
            String ListService = "";
            
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
                                ListService += className +",";
                            }
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());                
            }
            return ListService;
        }

        public HashMap<String , Mapping>  getMapping(String packagename, Class<? extends Annotation> annotationClass) throws DuplicateGetMappingException {
            String[] ListController = getAnnotatedClassWithin(packagename, annotationClass).split(",");
            HashMap<String , Mapping> ListClasses = new HashMap<String , Mapping>();
            for (int i = 0; i < ListController.length ; i++) {
                try {
                    String className = ListController[i];
                    Class<?> clazz = Class.forName(className);
                        Method[] methods = clazz.getDeclaredMethods();
                        for (int j = 0; j < methods.length; j++) {
                            if(methods[j].isAnnotationPresent(Get.class)){
                                Mapping value = new Mapping(className, methods[j].getName());
                                String key = methods[j].getAnnotation(Get.class).url();
                                if(!wasUsed(key, ListClasses)){
                                    ListClasses.put(key,value);
                                } else{
                                    throw new DuplicateGetMappingException("The url : "+key+" from "+className+" method "+methods[j].getName()+" is already used by another class!");
                                }
                            }
                        }
                } catch (Exception e) {
                    
                }      
            }
            return ListClasses; 
        }

        public boolean wasUsed(String url, HashMap<String , Mapping> ListClasses){
            Mapping map = ListClasses.get(url);
            if(map.getClassName() != null){
                return true;
            }
            return false;
        }

        public String conform_url (String url){
            String newURL ="/";
            String[] path1 = url.split("//");
            String[] path = path1[1].split("/");
            for (int i = 2; i < path.length; i++) {
                newURL += path[i]+"/";
            }
            url = newURL.substring(0, newURL.length()-1);
            return url;
        }
}
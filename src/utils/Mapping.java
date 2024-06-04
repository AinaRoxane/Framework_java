package utils;


public class Mapping {
        String className;
        String methodName;

        public Mapping(String my_class_name, String my_method_name){
            setClassName(my_class_name);
            setMethodName(my_method_name);
        }
        public String getClassName(){
            return className;
        }
        public void setClassName(String my_class_name){
            this.className = my_class_name;
        }
        public String getMethodName() {
            return methodName;
        }
        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
}

package utils;

import java.util.HashMap;
import java.util.Set;

public class  ModelView {
        String url;
        HashMap<String, Object> data; 
        
        public void addObject(String variableName, Object variablevalue){
            data = new HashMap<String , Object>();
            data.put(variableName, variablevalue);
        }
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public HashMap<String, Object> getData() {
            return data;
        }

        public void setData(HashMap<String, Object> data) {
            this.data = data;
        }

        public String getVariableName(){
            Set<String> keys = data.keySet();
            String key = keys.iterator().next();
            return key;
        }
}
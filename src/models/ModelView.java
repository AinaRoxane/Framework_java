package models;

import java.util.HashMap;
import java.util.Set;

public class  ModelView {
        String url;
        HashMap<String, Object> data; 
        String nextPage;

        public ModelView (){
            data = new HashMap<String , Object>();
        }
        
        public void addObject(String variableName, Object variablevalue){
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

        public String getNextPage() {
            return nextPage;
        }

        public void setNextPage(String nextPage) {
            this.nextPage = nextPage;
        }

        public String getVariableName(){
            Set<String> keys = data.keySet();
            String key = keys.iterator().next();
            return key;
        }
}
package models.sessions;
import jakarta.servlet.http.HttpSession;

public class MySession {
    HttpSession session;
    
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }


    public void add(String key, Object value){
        session.setAttribute(key, value);
    }

    public Object get(String key){
        return this.session.getAttribute(key);
    }

    public void remove(String key){
        session.removeAttribute(key);
    }

   
}

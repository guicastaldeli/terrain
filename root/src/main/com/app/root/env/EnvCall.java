package main.com.app.root.env;
import java.lang.reflect.Method;

public class EnvCall {
    public static void call(Object instance, String ...method) {
        Object curr = instance;
        for(String name : method) {
            try {
                Method m = curr.getClass().getMethod(name);
                curr = m.invoke(curr);
            } catch(Exception err) {
                throw new RuntimeException(
                    "Failed in method chain at: " + method, err
                );
            }
        }
    }
}

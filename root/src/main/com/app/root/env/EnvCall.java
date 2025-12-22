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
    public static Object callReturn(
        Object instance,
        Object param, 
        String ...method
    ) {
        if(method.length == 0) {
            throw new IllegalArgumentException("Method chain cannot be empty");
        }
        
        Object curr = instance;
        try {
            for(int i = 0; i < method.length - 1; i++) {
                Method m = curr.getClass().getMethod(method[i]);
                curr = m.invoke(curr);
            }

            String lastMethodName = method[method.length - 1];
            Method lasMethod = curr.getClass().getMethod(lastMethodName, param.getClass());
            return lasMethod.invoke(curr, param);
        } catch(Exception err) {
            throw new RuntimeException(
                "Failed in method chain ending with: " + method[method.length - 1], err
            );
        }
    }
}

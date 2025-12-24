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
    public static Object callReturn(Object instance, String ...method) {
        if(method.length == 0) {
            throw new IllegalArgumentException("Method chain cannot be empty");
        }
        
        Object curr = instance;
        try {
            for(String methodName : method) {
                Method m = curr.getClass().getMethod(methodName);
                curr = m.invoke(curr);
            }
            return curr;
        } catch(Exception err) {
            throw new RuntimeException(
                "Failed in method chain: " + String.join(".", method), err
            );
        }
    }
    public static Object callReturnWithParams(
        Object instance, 
        Object[] params, 
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
            Class<?>[] paramTypes = new Class<?>[params.length];
            for(int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
                if(params[i] instanceof Float) {
                    paramTypes[i] = float.class;
                } else if(params[i] instanceof Integer) {
                    paramTypes[i] = int.class;
                }
            }
            
            Method lasMethod = curr.getClass().getMethod(lastMethodName, paramTypes);
            return lasMethod.invoke(curr, params);
        } catch(Exception err) {
            throw new RuntimeException(
                "Failed in method chain ending with: " + method[method.length - 1], err
            );
        }
    }
    public static void callWithParams(Object instance, Object[] params, String methodName) {
        try {
            Class<?>[] paramTypes = new Class<?>[params.length];
            for(int i = 0; i < params.length; i++) {
                if(params[i] instanceof Float) {
                    paramTypes[i] = float.class;
                } else if(params[i] instanceof Integer) {
                    paramTypes[i] = int.class;
                } else if(params[i] instanceof Double) {
                    paramTypes[i] = double.class;
                } else if(params[i] instanceof Boolean) {
                    paramTypes[i] = boolean.class;
                } else {
                    paramTypes[i] = params[i].getClass();
                }
            }
            
            Method method = instance.getClass().getMethod(methodName, paramTypes);
            method.invoke(instance, params);
        } catch(Exception err) {
            throw new RuntimeException(
                "Failed to call method: " + methodName, err
            );
        }
    }
}

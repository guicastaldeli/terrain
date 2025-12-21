//
// **This Dependency Container is for any environment
//  that have classes with many deps :)...
//

package main.com.app.root;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyContainer {
    private final Map<Class<?>, Object> deps = new HashMap<>();
    private final Map<String, Object> namedDeps = new HashMap<>();
    private final Map<Class<?>, List<Object>> typedCollections = new HashMap<>();
    
    /**
     * Register
     */
    public <T> DependencyContainer register(Class<T> type, T instance) {
        deps.put(type, instance);
        return this;
    }
    
    public <T> DependencyContainer registerAll(Object ...instances) {
        for(Object instance : instances) {
            if(instance != null) {
                deps.put(instance.getClass(), instance);

                for(Class<?> iface : instance.getClass().getInterfaces()) {
                    deps.put(iface, instance);
                }

                Class<?> sClass = instance.getClass().getSuperclass();
                while(sClass != null && sClass != Object.class) {
                    deps.put(sClass, instance);
                    sClass = sClass.getSuperclass();
                }
            }
        }
        return this;
    }

    /**
     * Get Dependency
     */
    public <T> T get(Class<T> type) {
        T instance = (T) deps.get(type);
        if(instance != null) return instance;
        
        for(Map.Entry<Class<?>, Object> entry : deps.entrySet()) {
            if(type.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }

        List<Object> collection = typedCollections.get(type);
        if(collection != null && !collection.isEmpty()) {
            return (T) collection.get(0);
        }

        throw new DependencyNotFoundException("Dependency not found: " + type.getName()); 
    }

    public <T> List<T> getAll(Class<T> type) {
        List<T> res = new ArrayList<>();

        T direct = (T) deps.get(type);
        if(direct != null) res.add(direct);

        deps.entrySet().stream()
            .filter(entry -> type.isAssignableFrom(entry.getKey()))
            .map(entry -> (T) entry.getValue())
            .forEach(res::add);

        List<Object> collection = typedCollections.get(type);
        if(collection != null) {
            collection
                .stream()
                .filter(type::isInstance)
                .map(obj -> (T) obj)
                .forEach(res::add);
        }

        return res;
    }

    /**
     * Resolve Params
     */
    private Object resolveParameter(Class<?> paramType, Annotation[] annotations) {
        if(List.class.isAssignableFrom(paramType) && paramType.getTypeParameters().length > 0) {
            Type type = ((ParameterizedType) paramType.getGenericSuperclass()).getActualTypeArguments()[0];
            if(type instanceof Class) {
                return getAll((Class<?>) type);
            }
        }
        return get(paramType);
    }

    /**
     * Create Instance
     */
    public <T> T createInstance(Class<T> injection) {
        try {
            Constructor<?> constructor = findConstructor(injection);
            if(constructor.getParameterCount() == 0) {
                T instance = (T) constructor.newInstance();
                injectDependencies(instance);
                return instance;
            } else {
                throw new RuntimeException("Constructor with parameters not supported: " + injection.getName());
            }
        } catch(Exception err) {
            throw new RuntimeException("Failed to create instance of " + injection.getName(), err);
        }
    }

    private Constructor<?> findConstructor(Class<?> injection) {
        Constructor<?>[] constructors = injection.getConstructors();
        for(Constructor<?> constructor : constructors) {
            if(constructor.getParameterCount() == 0) {
                return constructor;
            }
        }

        throw new RuntimeException("No parameterless constructor found for " + injection.getName());
    }

    /**
     * Inject Dependencies
     */
    public void injectDependencies(Object instance) {
        Class<?> instanceClass = instance.getClass();
        for(Field field : instanceClass.getDeclaredFields()) {
            if(field.isAnnotationPresent(DependencyValue.class)) {
                DependencyValue dep = field.getAnnotation(DependencyValue.class);
                try {
                    field.setAccessible(true);

                    Object value;
                    if (!dep.value().isEmpty()) {
                        value = namedDeps.get(dep.value());
                        if (value == null) {
                            throw new DependencyNotFoundException(
                                "Named dependency not found: " + dep.value()
                            );
                        }
                    } else {
                        value = get(field.getType());
                    }

                    field.set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency into field: " + field.getName(), e);
                }
            }
        }
    }

    private Object findMatchInstance(Class<?> depClass, Map<Class<?>, Object> instanceMap) {
        Object instance = instanceMap.get(depClass);
        if(instance != null) return instance;

        for(Map.Entry<Class<?>, Object> entry : instanceMap.entrySet()) {
            if(depClass.isAssignableFrom(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static class DependencyNotFoundException extends RuntimeException {
        public DependencyNotFoundException(String message) {
            super(message);
        }
        public DependencyNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
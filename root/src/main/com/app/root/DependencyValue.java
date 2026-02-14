package main.com.app.root;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DependencyValue {
    String value() default "";
    boolean required() default true;
}
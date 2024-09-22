package table;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableAttribute
{
    int     column();
    String  header();
    int     width();
    int     decimals() default 0;
    boolean editable() default false;
}

package kronops.apigenerator.annotation;


import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCParam{
    String value();

    /**
     * List item type
     * @return
     */
    Class<?> listOf() default SimpleItem.class;
}

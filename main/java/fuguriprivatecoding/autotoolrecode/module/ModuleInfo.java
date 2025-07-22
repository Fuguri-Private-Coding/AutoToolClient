package fuguriprivatecoding.autotoolrecode.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
	String name();
	Category category();
	int key() default 0;
	boolean toggled() default false;
	boolean hide() default false;
	boolean loadFromConfig() default true;
	String description() default "";
}

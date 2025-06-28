package fuguriprivatecoding.autotoolrecode.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
	String name();
	Category category();
	int key() default 0;
	boolean toggled() default false;
	boolean hide() default false;
}

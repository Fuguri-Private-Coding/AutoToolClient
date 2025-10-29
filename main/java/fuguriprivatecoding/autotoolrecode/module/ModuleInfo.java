package fuguriprivatecoding.autotoolrecode.module;

import org.atteo.classindex.IndexAnnotated;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
	String name();
	Category category();
	int key() default 0;
	boolean toggled() default false;
	boolean hide() default false;
	String description() default "";
}

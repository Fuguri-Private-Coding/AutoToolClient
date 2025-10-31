package fuguriprivatecoding.autotoolrecode.setting;

import java.util.function.BooleanSupplier;

public interface ISetting {
	String getName();
	void setVisible(boolean visible);
	void setVisible(BooleanSupplier visible);
	boolean isVisible();
}

package er.extensions;

public class ERXPatternLayout extends er.extensions.logging.ERXPatternLayout {
	public ERXPatternLayout() {
		System.err.println("You are referencing er.extensions.ERXPatternLayout but should use er.extensions.logging.ERXPatternLayout instead");
	}
}

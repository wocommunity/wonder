package er.iui.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

/**
 *
 * @binding initialScale
 * @binding maximumScale
 * @binding userScalable
 * @binding width
 */
public class ERIUIViewport extends ERXComponent {
  public ERIUIViewport(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
  
  public String content() {
    String width = stringValueForBinding("width", "device-width");
    float initialScale = floatValueForBinding("initialScale", 1.0f);
    float maximumScale = floatValueForBinding("maximumScale", 1.0f);
    boolean userScalable = booleanValueForBinding("userScalable", false);
    int userScalableInt = (userScalable) ? 1 : 0;
    String content = "width=" + width + "; initial-scale=" + initialScale + "; maximum-scale=" + maximumScale + "; user-scalable=" + userScalableInt + ";";
    return content;
  }
}

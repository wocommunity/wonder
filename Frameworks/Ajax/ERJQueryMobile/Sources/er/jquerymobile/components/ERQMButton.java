package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;

/**
 * data-icon :
 * 
 * Left arrow - data-icon="arrow-l"
 * Right arrow - data-icon="arrow-r"
 * Up arrow - data-icon="arrow-u"
 * Down arrow - data-icon="arrow-d"
 * Delete - data-icon="delete"
 * Plus - data-icon="plus"
 * Minus - data-icon="minus"
 * Check - data-icon="check"
 * Gear - data-icon="gear"
 * Refresh - data-icon="refresh"
 * Forward - data-icon="forward"
 * Back - data-icon="back"
 * Grid - data-icon="grid"
 * Star - data-icon="star"
 * Alert - data-icon="alert"
 * Info - data-icon="info"
 * Home - data-icon="home"
 * Search - data-icon="search"
 * 
 * data-iconpos : 
 * 
 * ["left"], "right", "top", "bottom", "notext"
 * 
 * data-inline :
 * 
 * ["false"], "true"
 * 
 * 
 * Custom Icons
 * To use custom icons, specify a data-icon value that has a unique name like myapp-email and 
 * the button plugin will generate a class by prefixing ui-icon- to the data-icon value and apply 
 * it to the button: ui-icon-myapp-email. You can then write a CSS rule in your stylesheet that 
 * targets the ui-icon-myapp-email class to specify the icon background source. To maintain 
 * visual consistency with the rest of the icons, create a white icon 18x18 pixels saved as a 
 * PNG-8 with alpha transparency.
 * 
 * .ui-icon-myapp-email {
 *    background-image: url("app-icon-email.png");
 * }
 * 
 * This will create the standard resolution icon, but many devices now have very high resolution displays, 
 * like the retina display on the iPhone 4. To add a HD icon, create an icon that is 36x36 pixels (exactly double the 18 pixel size), 
 * and add second rule that uses the -webkit-min-device-pixel-ratio: 2 media query to target a rule only to high resolution displays. 
 * Specify the background image for the HD icon file and set the background size to 18x18 pixels which will fit the 36 pixel 
 * icon into the same 18 pixel space. The media query block can wrap multiple icon rules:
 * 
 * {@literal @}media only screen and (-webkit-min-device-pixel-ratio: 2) {
 *  .ui-icon-myapp-email {
 *    background-image: url("app-icon-email-highres.png");
 *    background-size: 18px 18px;
 *  }
 *  ...more HD icon rules go here...
 * }
 * 
 * @author ishimoto
 */
@SuppressWarnings("serial")
public class ERQMButton extends ERXNonSynchronizingComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMButton(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String cssClass() {
    String result = stringValueForBinding("class", "");

    if(booleanValueForBinding("disabled", false)) {
      result = ERXStringUtilities.stringByAppendingCSSClass(result, "ui-disabled");
    }

    return result;
  }

  public boolean hasAction() {
    return hasBinding("action");
  }

  public String otherTagString() {
    StringBuilder sb = new StringBuilder();

    if(booleanValueForBinding("externalLink", false)) {
      sb.append("data-rel=\"external\"");
    }

    if(booleanValueForBinding("mini", false)) {
      sb.append(" data-mini=\"true\"");
    }
    return sb.toString();
  }
}

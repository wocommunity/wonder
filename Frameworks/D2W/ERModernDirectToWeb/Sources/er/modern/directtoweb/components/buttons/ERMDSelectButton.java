package er.modern.directtoweb.components.buttons;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.SelectPageInterface;

/**
 * Select button for repetitions
 * 
 * @binding object
 *
 * @d2wKey selectButtonLabel
 * @d2wKey classForSelectObjButton
 * @d2wKey idForParentMainContainer
 * 
 * @author davidleber
 *
 */
public class ERMDSelectButton extends ERMDActionButton {
	
	public interface Keys extends ERMDActionButton.Keys {
		public static final String selectButtonLabel = "selectButtonLabel";
		public static final String classForSelectObjButton = "classForSelectObjButton";
		public static final String idForParentMainContainer = "idForParentMainContainer";
	}
	
    public ERMDSelectButton(WOContext context) {
        super(context);
    }
    
    /**
     * Label for select button
     * <p>
     * Defaults to "Select"
     */
	public String buttonLabel() {
		if (_buttonLabel == null) {
			_buttonLabel = stringValueForBinding(Keys.selectButtonLabel, "Select");
		}
		return _buttonLabel;
	}
    
	/**
	 * CSS class for the select button
	 * <p>
	 * Defaults to "Button ObjButton SelectObjButton"
	 */
	public String buttonClass() {
		if (_buttonClass == null) {
			_buttonClass = stringValueForBinding(Keys.classForSelectObjButton, "Button ObjButton SelectObjButton");
		}
		return _buttonClass;
	}
    
	/**
	 * Action performed by the select button
	 */
    public WOComponent selectObjectAction() {
        SelectPageInterface parent = parentSelectPage();
        if(parent != null) {
            parent.setSelectedObject(object());
            return nextPageInPage((D2WPage)parent);
        }
        throw new IllegalStateException("This page is not an instance of SelectPageInterface. I can't select here.");
    }
    
    @Override
    public String updateContainer() {
    	if (_updateContainer == null) {
			_updateContainer = stringValueForBinding(Keys.idForParentMainContainer);
		}
		return _updateContainer;
    }
}

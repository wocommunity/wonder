//
//  ERXNavigationItem.java
//  ERExtensions
//
//  Created by Max Muller on Wed Oct 30 2002.
//
package er.extensions;

import com.webobjects.foundation.*;

public class ERXNavigationItem {

    private static int counter = 0;

    /** logging supprt */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXNavigationItem.class);
    
    public String _uniqueID;

    protected String _action;
    protected NSArray _conditions;
    protected String _directActionName;
    protected String _name;
    protected String _pageName;
    protected String _displayName;
    protected String _hasActivity;
    protected NSArray _children, _childrenConditions;
    protected String _childrenBinding;
    protected NSDictionary _childrenChoices;

    protected int _height;
    protected int _width;

    public ERXNavigationItem(NSDictionary values) {
        //set uniqueID
        _uniqueID=new String("id" + counter);
        counter++;
        if (values!=null) {
            if (log.isDebugEnabled())
                log.debug("ERXNavigationItem " + uniqueID() + "assigned these values at creation:\n" + values);
            _action=(String)values.valueForKey("action");
            if (values.valueForKey("conditions") == null || ((String)values.valueForKey("conditions")).equals("")) {
                _conditions = NSArray.EmptyArray;
            } else {
                _conditions=NSArray.componentsSeparatedByString((String)values.valueForKey("conditions"),",");
            }
            _directActionName=(String)values.valueForKey("directActionName");
            if (values.valueForKey("height")!=null)
                _height=Integer.valueOf((String)values.valueForKey("height")).intValue();
            if (values.valueForKey("width")!=null)
                _width=Integer.valueOf((String)values.valueForKey("width")).intValue();
            _name=(String)values.valueForKey("name");
            _displayName=(String)values.valueForKey("displayName");
            if (_displayName==null || _displayName.length()==0) _displayName=_name;
            _pageName=(String)values.valueForKey("pageName");
            _hasActivity=(String)values.valueForKey("hasActivity");
            if (values.valueForKey("children") != null && values.valueForKey("children") instanceof NSArray) {
                _children = (NSArray)values.valueForKey("children");
            } else if (values.valueForKey("children") != null && values.valueForKey("children") instanceof String) {
                _childrenBinding = (String)values.valueForKey("children");
            } else {
                _children = NSArray.EmptyArray;
            }
            if (values.valueForKey("childrenChoices") != null) {
                _childrenChoices = (NSDictionary)values.valueForKey("childrenChoices");
            } else {
                _childrenChoices = ERXConstant.EmptyDictionary;
            }
            if (values.valueForKey("childrenConditions") == null || ((String)values.valueForKey("childrenConditions")).equals("")) {
                _childrenConditions = NSArray.EmptyArray;
            } else {
                _childrenConditions=NSArray.componentsSeparatedByString((String)values.valueForKey("childrenConditions"),",");
            }
        } else {
            log.warn("Constructing a ERXNavigationItem with a null dictionary!");
        }
    }

    public NSArray children() { return _children; }
    public String childrenBinding() { return _childrenBinding; }
    public NSArray childrenConditions() { return _childrenConditions; }
    public NSDictionary childrenChoices() { return _childrenChoices; }
    public String action() { return _action; }
    public NSArray conditions() { return _conditions; }
    public String directActionName() { return _directActionName; }
    public int height() { return _height; }
    public int width() { return _width; }
    public String name() { return _name; }
    public String pageName() { return _pageName; }
    public String uniqueID() { return _uniqueID; }
    public String displayName() { return _displayName; }
    public String hasActivity() { return _hasActivity; }

    public String toString() { return "< " + name() + " >"; }    
}

package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.misc.ERDHasChangesMarker;

/**
 * Customizable hasChangesMarker component
 * 
 * @d2wKey changesMarkerImageFrameworkName
 * @d2wKey changesMarkerImageName
 * 
 * @author davidleber
 *
 */
public class ERMDHasChangesMarker extends ERDHasChangesMarker {
	
	public static interface Keys {
		 public static final String changesMarkerImageFrameworkName = "changesMarkerImageFrameworkName";
		 public static final String changesMarkerImageName = "changesMarkerImageName";
	}
	
	private String _indicatorFileName;
	private String _indicatorFrameworkName;
	
    public ERMDHasChangesMarker(WOContext context) {
        super(context);
    }

    /**
     * Convenience getter for the indicator image framework name
     * <p>
     * Defaults to: "ERModernDirectToWeb"
     */
	public String indicatorFrameworkName() {
		if (_indicatorFrameworkName == null) {
			_indicatorFrameworkName = stringValueForBinding(Keys.changesMarkerImageFrameworkName, "ERModernDirectToWeb");
		}
		return _indicatorFrameworkName;
	}

	public void setIndicatorFrameworkName(String name) {
		_indicatorFrameworkName = name;
	}

    /**
     * Convenience getter for the indicator image name
     * <p>
     * Defaults to: "reddot.png"
     */
	public String indicatorFileName() {
		if (_indicatorFileName == null) {
			_indicatorFileName = stringValueForBinding(Keys.changesMarkerImageName, "reddot.png");
		}
		return _indicatorFileName;
	}
 
	public void setIndicatorFileName(String name) {
		_indicatorFileName = name;
	}
    
	@Override
	public void reset() {
		_indicatorFileName = null;
		_indicatorFrameworkName = null;
		super.reset();
	}

}

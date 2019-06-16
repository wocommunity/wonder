import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class MultiUpdateExample extends WOComponent {

	private NSMutableArray<String> _updateContainerIDs = new NSMutableArray<>("container1");
	private long now;
	
	public MultiUpdateExample(WOContext context) {
		super(context);
	}

	@Override
	public void awake() {
		now = System.currentTimeMillis();

		super.awake();
	}
	
	public long now() {
	    return now;
	}

	public NSArray containerIdsToUpdate() {
		return _updateContainerIDs;
	}

	public void setUpdateContainer2(boolean updateContainer2) {
		setUpdateContainer("container2", updateContainer2);
	}

	public boolean updateContainer2() {
		return _updateContainerIDs.containsObject("container2");
	}

	public void setUpdateContainer3(boolean updateContainer3) {
		setUpdateContainer("container3", updateContainer3);
	}

	public boolean updateContainer3() {
		return _updateContainerIDs.containsObject("container3");
	}

	protected void setUpdateContainer(String id, boolean updateContainer3) {
		if (updateContainer3) {
			if (!_updateContainerIDs.containsObject(id)) {
				_updateContainerIDs.addObject(id);
			}
		}
		else {
			_updateContainerIDs.removeObject(id);
		}
	}
}

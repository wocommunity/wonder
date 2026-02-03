package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EODataSource;

import er.directtoweb.components.ERD2WExtraDisplayPropertyKeysComponent;

public class R2D2WExtraDisplayPropertyKeys extends ERD2WExtraDisplayPropertyKeysComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private EODataSource dataSource;
	
	public R2D2WExtraDisplayPropertyKeys(WOContext context) {
        super(context);
    }

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(EODataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the dataSource
	 */
	public EODataSource dataSource() {
		return dataSource;
	}
}
package er.jasperreports;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXAssert;
import er.extensions.foundation.ERXProperties;


/**
 *	Takes an an NSArray of {@link NSKeyValueCodingAdditions} (think keypaths) objects
 * 
 **/
public class ERJRFoundationDataSource implements JRDataSource {
	
	public final static String REPORT_KEYPATH_SEPARATOR = ERXProperties.stringForKeyWithDefault("er.jasperreports.keyPathSeparator", "_");
	private final static String WEBOBJECTS_KEYPATH_SEPARATOR = ".";
	
	private static final Logger log = LoggerFactory.getLogger(ERJRFoundationDataSource.class);
	
	/** 
	 * Sometimes we want might want a reference to the current row, perhaps to pass to a custom function in a scriptlet.
	 * This functionality allows us to specify a field name that returns the current row object itself
	 * rather than an attribute of the current row.
	 * 
	 * The default fieldname is "_currentRow", however this can be overridden in system properties using: 
	 * 	er.jasperreports.currentRow.fieldName=myCustomCurrentRowFieldname
	 **/
	private static final String FIELD_NAME_FOR_CURRENT_ROW = ERXProperties.stringForKeyWithDefault("er.jasperreports.currentRow.fieldName", "_currentRow");
	
	/**
	 * Private NSArray that contains the database values we wish to use in the report.
	 *
	 */
	protected NSKeyValueCodingAdditions currRow;
	protected Enumeration<? extends NSKeyValueCodingAdditions> e;
	protected boolean filterNulls = true;
	protected NSMutableDictionary<String, Object> debugRow;
	
	private int processedCount = 0;
	private int totalCount = 100;  //prevent divide by zero
	
	
	public ERJRFoundationDataSource(NSArray<? extends NSKeyValueCodingAdditions> arr) {
		e = arr.objectEnumerator();
		totalCount = arr.count();
	}
	
	public ERJRFoundationDataSource(Enumeration<? extends NSKeyValueCodingAdditions> enumeration, int itemCount) {
		ERXAssert.PRE.notNull(enumeration);
		e = enumeration;
		totalCount = itemCount;
	}

	/* (non-Javadoc)
	 * @see net.sf.jasperreports.engine.JRDataSource#next()
	 */
	public boolean next() throws JRException {

		if (e.hasMoreElements()) {
			currRow = e.nextElement();
			processedCount++;
			if (log.isInfoEnabled()) {
				if (debugRow != null) {
					log.info("DetailRow: {}", debugRow);
				} //~ if (debugRow != null)
				debugRow = new NSMutableDictionary<String, Object>();
				if (currRow instanceof EOEnterpriseObject) {
					EOEnterpriseObject eo = (EOEnterpriseObject) currRow;
					debugRow.takeValueForKey(eo.editingContext().globalIDForObject(eo), "_globalID");
				} //~ if (currRow instanceof EOEnterpriseObject)
			} //~ if (log.isDebugEnabled())
			return true;
		}
		return false;
	}
	
	public boolean getFilterNulls() {
		return filterNulls;
	}
	
	public void setFilterNulls(boolean filters) {
		filterNulls = filters;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
	 */
	public Object getFieldValue(JRField jrField) throws JRException {
		// Check for special field names
		if (jrField.getName().equals(FIELD_NAME_FOR_CURRENT_ROW)) {
			return currRow;
		}
		
		
		// Fields in JasperReports become Java variables in .java files when
		// the report is compiled.  Because we are using key-path coding, 
		// we would normally have periods in the field name.  However, this makes
		// Jasper Reports very unhappy, and it will not compile the report.
		// Therefore, the field names as entered in Jasper Reports use
		// underscores instead of periods.  This also means that to get the value
		// we need to replace the underscores in the field name to periods so that
		// key-path coding will work and so that the report will compile.
		// This has the obvious caveat that the key-path cannot contain an underscore
		// or this conversion will mess things up
		//
		//
		boolean isStringValueClass = jrField.getValueClass().equals(String.class);
		
		Object fieldValue = null;
		try {
			fieldValue = (currRow).valueForKeyPath(jrField.getName().replaceAll(REPORT_KEYPATH_SEPARATOR, WEBOBJECTS_KEYPATH_SEPARATOR));
			if (log.isDebugEnabled())
				log.debug("value = {}; jrField = {}", fieldValue, (jrField == null ? "null" : ERJRUtilities.toString(jrField)));
			// Allow for implied toString methods
			if (isStringValueClass) {
				// At least let's do toString by default when JasperReports expects a String return
				
				fieldValue = (fieldValue == null ? null : fieldValue.toString());
			}
		}
		catch (Exception ex) {
			NSLog.err.appendln("Error while retrieving value" + jrField.getName().replaceAll("_", "."));
			NSLog.err.appendln(ex);
		}
		if (filterNulls && fieldValue == null && isStringValueClass) {
			fieldValue = "";
		}
		
		if (log.isInfoEnabled()) {
			debugRow.takeValueForKey(fieldValue == null ? "null" : fieldValue, jrField.getName());
		} //~ if (log.isDebugEnabled())
		
		return fieldValue;
	}

	public double percentProcessed() {
		return (double)processedCount / (double)totalCount;
	}
}

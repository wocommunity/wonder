package er.jasperreports;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;

import er.jasperreports.ERJRFoundationDataSource;
import er.jasperreports.ERJRReportTaskFromEO;
import er.jasperreports.ERJRUtilities;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.concurrency.ERXTaskPercentComplete;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXAssert;

public class ERJRReportTaskFromEO implements Callable<File>, ERXTaskPercentComplete {

	private static final Logger log = Logger.getLogger(ERJRReportTaskFromEO.class);

	private File reportFile;
	private final String frameworkName;
	private final String jasperCompiledReportFileName;
	private Map<String, Object> parameters;

	private EOGlobalID theObjectGID;    // Use threadsafe global IDs for background task ivars
	
	private EOEnterpriseObject myObject;
//	private EOEnterpriseObject theObject;

	// iVar so we can get percentage complete
	private ERJRFoundationDataSource jrDataSource;

	public ERJRReportTaskFromEO(EOEnterpriseObject theObject, String jasperCompiledReportFileName) {
		this(theObject, jasperCompiledReportFileName, null, null);
	}

	public ERJRReportTaskFromEO(EOEnterpriseObject theObject, String jasperCompiledReportFileName, HashMap<String, Object> parameters) {
		this(theObject, jasperCompiledReportFileName, null, parameters);
	}

	public ERJRReportTaskFromEO(EOEnterpriseObject theObject, String jasperCompiledReportFileName, String frameworkName, HashMap<String, Object> parameters) {

		ERXAssert.PRE.notNull(theObject);
		ERXAssert.PRE.notNull(jasperCompiledReportFileName);

		this.myObject = theObject;
		
		this.jasperCompiledReportFileName = jasperCompiledReportFileName;
		this.frameworkName = frameworkName;
		this.parameters = parameters;

		// grab the global ID in the constructor. The constructor executes in the thread that creates the task instance
		this.theObjectGID = theObject.editingContext().globalIDForObject( theObject ); 
		ERXAssert.PRE.notNull(theObjectGID);  // If the object is new, this will be null. test and throw.

		if (this.parameters == null) {
			this.parameters = new HashMap<String, Object>();
		}
	}


	/**
	 * Callable interface implementation
	 * 
	 * @throws Exception
	 */
	public File call() throws Exception {

		ERXApplication._startRequest();
		try {
			return _call();
		} catch (Exception e) {
			log.error("Error in JR task", e);
			throw e;
		} finally {
			// Unlocks any locked editing contexts
			ERXApplication._endRequest();
		}

	}

	private File _call() {
		// If development
		if (ERXApplication.isDevelopmentModeSafe()) {
			parameters.put("_isDevelopmentMode", Boolean.TRUE);
		} else {
			parameters.put("_isDevelopmentMode", Boolean.FALSE );
		}

		reportFile = null;

		if (log.isDebugEnabled())
			log.debug("Starting JasperReportTask: " + this.toString());

		/**
		 * create a new editing context and work on the object there
		 * to prevent you app from trying to touch the same object.
		 */
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {

//			EOEnterpriseObject theObject = ec.faultForGlobalID( theObjectGID, ec);
			
			EOEnterpriseObject theObjectToPDF = EOUtilities.localInstanceOfObject(ec, myObject);
			jrDataSource = new ERJRFoundationDataSource(new NSArray<EOEnterpriseObject>(theObjectToPDF));

			if (jasperCompiledReportFileName != null) {
				reportFile = ERJRUtilities.runCompiledReportToPDFFile(jasperCompiledReportFileName, frameworkName, parameters, jrDataSource);
			}

		} catch (Exception e) {
			throw new NestableRuntimeException(e);
		} finally {
			ec.unlock();
		}

		return reportFile;
	}

	public File file() {
		return reportFile;
	}

	public Double percentComplete() {
		// TODO Auto-generated method stub
		return null;
	}
}

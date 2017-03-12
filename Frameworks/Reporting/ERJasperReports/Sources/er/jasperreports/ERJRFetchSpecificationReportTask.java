package er.jasperreports;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.foundation.ERXAssert;

/**
 * A background task class that creates a JasperReports report in the context
 * of a WebObjects application. Sensible defaults are used.
 * 
 * A convenient Builder pattern inner class is provided too.
 * 
 * @author kieran
 */
public class ERJRFetchSpecificationReportTask implements Callable<File>, IERXPercentComplete {
	private static final Logger log = LoggerFactory.getLogger(ERJRFetchSpecificationReportTask.class);
	
	private File reportFile;
	private final String frameworkName;
	private final EOFetchSpecification fetchSpecification;
	private final String jasperCompiledReportFileName;
	private Map<String, Object> parameters;
	
	// iVar so we can get percentage complete
	private ERJRFoundationDataSource jrDataSource;
	
	public ERJRFetchSpecificationReportTask(EOFetchSpecification fetchSpecification, String jasperCompiledReportFileName) {
		this(fetchSpecification, jasperCompiledReportFileName, null, null);
	}
	
	public ERJRFetchSpecificationReportTask(EOFetchSpecification fetchSpecification, String jasperCompiledReportFileName, HashMap<String, Object> parameters) {
		this(fetchSpecification, jasperCompiledReportFileName, null, parameters);
	}
	
	
	public ERJRFetchSpecificationReportTask(EOFetchSpecification fetchSpecification, String jasperCompiledReportFileName, String frameworkName, HashMap<String, Object> parameters) {
		ERXAssert.PRE.notNull(fetchSpecification);
		ERXAssert.PRE.notNull(jasperCompiledReportFileName);
		
		// Since it is likely the fetch spec will be used in a different
		// background thread during report generation, we need to ensure we have a schema-based
		// qualifier instead of a memory-based qualifier that might have references to EOs
		// in an RR-locked editing context
		EOFetchSpecification fs = null;
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			fs = schemaBasedFetchSpecification(fetchSpecification); 
		} catch (Exception e) {
			throw new RuntimeException("Failed to convert fetchSpecification to schema-based", e);
		} finally {
			ec.unlock();
		}
		this.fetchSpecification = fs;
		
		
		this.jasperCompiledReportFileName = jasperCompiledReportFileName;
		this.frameworkName = frameworkName;
		this.parameters = parameters;
		
		if (this.parameters == null) {
			this.parameters = new HashMap<>();
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
		
		log.debug("Starting JasperReportTask: {}", this);
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			
			@SuppressWarnings("unchecked")
			NSArray<EOEnterpriseObject> objects = ec.objectsWithFetchSpecification(fetchSpecification);
			
			jrDataSource = new ERJRFoundationDataSource(objects);
			
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

	/* (non-Javadoc)
	 * @see er.extensions.concurrency.IERXPercentComplete#percentComplete()
	 * 
	 * Some whacky logic just so the user can be comfortable that we are making some progress.
	 */
	public Double percentComplete() {
		if (jrDataSource == null) {
			return Double.valueOf(0.1);
		} else {
			double percent = 0.1 + jrDataSource.percentProcessed() * 0.8;
			return Double.valueOf(percent);
		}
	}
	
	/**
	 * Ensures we don't have references to EOs before using this in the background thread.
	 * 
	 * @param fs
	 * @return a clone of the fetchSpecification with the EOQualifier converted to a schema-based qualifier or the same {@link EOFetchSpecification}
	 * if there is no qualifier.
	 */
	private EOFetchSpecification schemaBasedFetchSpecification(EOFetchSpecification fetchSpecification) {
		
		EOQualifier q = fetchSpecification.qualifier();
		if (q != null) {
			
			// Clone the fetchSpec
			fetchSpecification = (EOFetchSpecification) fetchSpecification.clone();

			EOEditingContext ec = ERXEC.newEditingContext();
			ec.lock();
			try {
				EOEntity entity = ERXEOAccessUtilities.entityMatchingString(ec, fetchSpecification.entityName());
				// Convert the qualifier to a schema-based qualifier
				q = entity.schemaBasedQualifier(q);
				fetchSpecification.setQualifier(q);
			} finally {
				ec.unlock();
			}

		} //~ if (q != null)
		return fetchSpecification;
		
	}
	
}

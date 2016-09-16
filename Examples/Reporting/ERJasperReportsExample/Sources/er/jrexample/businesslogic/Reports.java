package er.jrexample.businesslogic;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import webobjectsexamples.businesslogic.movies.common.Movie;
import webobjectsexamples.businesslogic.movies.server._Studio;

import com.webobjects.eocontrol.EOQualifier;

import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXSortOrdering.ERXSortOrderings;
import er.jasperreports.ERJRFetchSpecificationReportTask;

public class Reports {
	
	/**
	 * @return a {@link Callable} task that creates and returns a StudioRevenueReport PDF file
	 */
	public static Callable<File> createStudioRevenueReportTask() {
		// SortOrderings
		// Sort by studio name alphabetical sort
		ERXSortOrderings sortOrderings = Movie.STUDIO.dot(_Studio.NAME).ascs();
		
		// Then sort by Movie Title
		sortOrderings = sortOrderings.then(Movie.TITLE.asc());
		
		// EOQualifier, null for this demo
		EOQualifier qualifier = null;
		
		ERXFetchSpecification<Movie> fs = new ERXFetchSpecification<>(Movie.ENTITY_NAME, qualifier, sortOrderings);
		
		String reportDescription = "A report that subtotals revenue by Studio and lists the Movie revenue detail for each Studio";
		
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("reportDescription", reportDescription);
		parameters.put("userName", "WOWODC Demo");
		
		// Builder pattern for constructor since.
		ERJRFetchSpecificationReportTask reportTask = new ERJRFetchSpecificationReportTask(fs, "StudioRevenueReport.jasper", parameters);
		
		return reportTask;
	}
	
}

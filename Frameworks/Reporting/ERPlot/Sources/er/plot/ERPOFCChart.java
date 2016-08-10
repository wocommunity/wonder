package er.plot;

import jofc2.model.Chart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;

public class ERPOFCChart extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERPOFCChart.class);

	private Chart _chart;

	private String _safeElementID;

	private Integer _height;

	private Integer _width;

	private String _id;

	public ERPOFCChart(WOContext context) {
		super(context);
	}

	@Override
	public void appendToResponse(WOResponse r, WOContext c) {
		ERXResponseRewriter.addScriptResourceInHead(r, c, "ERPlot", "js/swfobject.js");
		ERXResponseRewriter.addScriptResourceInHead(r, c, "ERPlot", "js-ofc-library/open_flash_chart.js");
		r.appendContentString("<script type=\"text/javascript\">");
		r.appendContentString("swfobject.embedSWF(\"");
		r.appendContentString(openFlashChartSwf());
		r.appendContentString("\", \"");
		r.appendContentString(id());
		r.appendContentString("\", \"");
		r.appendContentString(width().toString());
		r.appendContentString("\", \"");
		r.appendContentString(height().toString());
		r.appendContentString("\", \"9.0.0\", \"expressInstall.swf\");");
		r.appendContentString("</script>");
		r.appendContentString("<script type=\"text/javascript\">");
		r.appendContentString("function open_flash_chart_data() { return JSON.stringify(data);}");
		r.appendContentString("var data =");
		r.appendContentString(json());
		r.appendContentString("</script>");
		r.appendContentString("<div id=\"");
		r.appendContentString(id());
		r.appendContentString("\"></div>");
	}

	public Chart chart() {
		if (_chart == null) {
			_chart = (Chart) valueForBinding("chart");
		}
		return _chart;
	}

	public Integer height() {
		if (_height == null) {
			_height = intValueForBinding("height", Integer.valueOf(300));
			log.debug("height = {}", _height);
		}
		return _height;
	}

	public String id() {
		if (_id == null) {
			_id = stringValueForBinding("id", safeElementID());
			log.debug("id = {}", _id);
		}
		return _id;
	}

	public String json() {
		return chart().toString();
	}

	/**
	 * @return the swfLocation
	 */
	public String openFlashChartSwf() {
		return application().resourceManager().urlForResourceNamed("open-flash-chart-full-embedded-font.swf",
			"ERPlot", context()._languages(), context().request());
	}

	@Override
	public void reset() {
		super.reset();
		_chart = null;
		_safeElementID = null;
		_height = null;
		_width = null;
		_id = null;
	}

	/** @return a safe element name element. */
	public String safeElementID() {
		if (_safeElementID == null) {
			_safeElementID = ERXStringUtilities.safeIdentifierName(context().elementID());
			log.debug("safeElementID = {}", _safeElementID);
		}
		return _safeElementID;
	}

	public Integer width() {
		if (_width == null) {
			_width = intValueForBinding("width", Integer.valueOf(500));
			log.debug("width = {}", _width);
		}
		return _width;
	}

}

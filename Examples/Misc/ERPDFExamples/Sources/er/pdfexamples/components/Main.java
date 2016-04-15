package er.pdfexamples.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {
	public Main(WOContext context) {
		super(context);
	}

	public WOActionResults combinePdfStreams() {

		CombineTheStreams combined = pageWithName(CombineTheStreams.class);
		WOContext ctx = context();

		SimplePDFGeneration1 spg = (SimplePDFGeneration1) application().pageWithName(SimplePDFGeneration1.class.getName(), ctx);
		combined.pdfsToCombine.add(spg.generateResponse().content().stream());
		spg = null;
		
		ctx = context();
		SimpleXML2FOP2PDF1 sxp = (SimpleXML2FOP2PDF1) application().pageWithName(SimpleXML2FOP2PDF1.class.getName(), ctx);
		combined.pdfsToCombine.add(sxp.generateResponse().content().stream());
		sxp = null;

		
		
		return combined;
	}
}

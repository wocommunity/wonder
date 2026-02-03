package er.r2d2w.components.charting;

import java.text.DecimalFormat;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXDictionaryUtilities;

public class R2DPieChart extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final int rx = 160;
	private static final int ry = 80;
	private static final int cx = 180;
	private static final int cy = 100;
	private static final int animateOffset = 20;
	private static final double initialTheta = 0; // Starting angle in degrees: [0-360)
	private static final int labelSize = 14;
	
	private static final String arc = "A";
	private static final String close = "Z";
	private static final String line = "L";
	private static final String move = "M";
	
	private static final String closeParenthesis = ")";
	private static final String colon = ":";
	private static final String hashMark = "#";
	private static final String openParenthesis = "(";
	private static final String percentMark = "%";
	private static final String space = " ";
	private static final String semicolon = ";";
	private static final String zeroOffset = "0 0";
	
	private NSDictionary<String, Number> chartDictionary;
	private NSArray<String> sortedChartKeys;
	private String sliceKey;
	private Number total;
	private double startTheta = initialTheta;
	private double stopTheta = initialTheta;
	private int largeArc = 0;

	public R2DPieChart(WOContext context) {
        super(context);
    }
	
	public void reset() {
		super.reset();
		chartDictionary = null;
		largeArc = 0;
		sliceKey = null;
		sortedChartKeys = null;
		startTheta = initialTheta;
		stopTheta = initialTheta;
		total = null;
	}
    
	public NSDictionary<String, Number> chartDictionary() {
		if(chartDictionary == null) {
			chartDictionary = (NSDictionary<String, Number>)valueForBinding("chartDictionary");
		}
		return chartDictionary;
	}
	
	public NSArray<String> sortedChartKeys() {
		if(sortedChartKeys == null) {
			NSArray<String> tmpKeys = ERXDictionaryUtilities.keysSortedByValueAscending(chartDictionary());
			NSMutableArray<String> reverseKeys = new NSMutableArray<String>();
			for(int i = tmpKeys.count() -1; i >= 0; i--) {
				reverseKeys.add(tmpKeys.objectAtIndex(i));
			}
			sortedChartKeys = reverseKeys.immutableClone();
		}
		return sortedChartKeys;
	}
	
	public Number total() {
		if(total == null) {
			total = totalForArray(chartDictionary().allValues());
		}
		return total;
	}
	
	public Number totalForArray(NSArray<Number> array) {
		return (Number)NSArray.operatorForKey(NSArray.SumOperatorName).compute(array, "doubleValue");
	}

	/**
	 * @return the sliceKey
	 */
	public String sliceKey() {
		return sliceKey;
	}

	/**
	 * @param sliceKey the sliceKey to set
	 */
	public void setSliceKey(String key) {
		if(key == null) { return; }
		sliceKey = key;
		
		startTheta = stopTheta;
		stopTheta = chartDictionary().objectForKey(key).doubleValue()*360/total().doubleValue() + startTheta;
		largeArc = (stopTheta - startTheta) > 180?1:0;
	}
	
	protected double xForTheta(double angle) {
		double d = Math.cos(Math.toRadians(angle));
		return cx + rx*d;
	}
	
	protected double yForTheta(double angle) {
		double d = Math.sin(Math.toRadians(angle));
		return cy + ry*d;
	}
	
	protected double centerTheta() {
		return (stopTheta-startTheta)/2 + startTheta;
	}
	
	public String animateValues() {
		double offX = animateOffset*Math.cos(Math.toRadians(centerTheta()));
		double offY = animateOffset*Math.sin(Math.toRadians(centerTheta()));
		StringBuilder path = new StringBuilder(zeroOffset).append(semicolon);
		path.append(offX).append(space).append(offY).append(semicolon);
		path.append(offX).append(space).append(offY).append(semicolon);
		path.append(offX).append(space).append(offY).append(semicolon);
		path.append(zeroOffset);
		return path.toString();
	}
	
	public String pathData() {
		//Origin
		StringBuilder path = new StringBuilder(move).append(space);
		path.append(cx).append(space);
		path.append(cy).append(space);
		
		//Line to arc start
		path.append(line).append(space);
		path.append(xForTheta((largeArc==0)?stopTheta:startTheta)).append(space);
		path.append(yForTheta((largeArc==0)?stopTheta:startTheta)).append(space);
		
		//Begin arc
		path.append(arc).append(space).append(rx).append(space).append(ry).append(space);
		
		//Set arc flags
		path.append(0).append(space).append(largeArc).append(space).append(largeArc).append(space);
		
		//Set arc point
		path.append(xForTheta((largeArc==0)?startTheta:stopTheta)).append(space);
		path.append(yForTheta((largeArc==0)?startTheta:stopTheta)).append(space).append(close);
		
		return path.toString();
	}
	
	public String fillColor() {
		StringBuilder fillColor = new StringBuilder(hashMark);
		int i = sortedChartKeys().indexOf(sliceKey());
		int j = sortedChartKeys().count();
		int min = 33 + (222 * i)/j;
		int mid = 66 + (189 * i)/j;
		int max = 99 + (156 * i)/j;
		fillColor.append(Integer.toHexString(min));
		fillColor.append(Integer.toHexString(mid));
		fillColor.append(Integer.toHexString(max));
		return fillColor.toString();
	}

	public int labelBoxY() {
		int i = sortedChartKeys().indexOf(sliceKey());
		int x = labelSize();
		return 2*x + 2*x*i;
	}
	
	public int labelTextY() {
		int i = sortedChartKeys().indexOf(sliceKey());
		int x = labelSize();
		return 3*x + 2*x*i;
	}
	
	public boolean showPercentages() {
		return booleanValueForBinding("showPercentages", true);
	}

	public String labelString() {
		StringBuilder label = new StringBuilder(sliceKey());
		double d = chartDictionary().objectForKey(sliceKey()).doubleValue()*100/total().doubleValue();
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(2);
		String percentValue = format.format(d);
		label.append(colon).append(space).append(chartDictionary().objectForKey(sliceKey()));
		if(showPercentages()) {
			if(chartDictionary().count() == 1) {percentValue = "100";}
			label.append(space).append(openParenthesis).append(percentValue).append(percentMark).append(closeParenthesis);
		}
		return label.toString();
	}
	
	public int labelSize() {
		return labelSize;
	}
	
	public boolean singleKey() {
		return (chartDictionary().count() == 1)?true:false;
	}
	
	// To appease the WOD parser... doesn't like statics apparently
	public String cx() {return Integer.toString(cx);}
	public String cy() {return Integer.toString(cy);}
	public String rx() {return Integer.toString(rx);}
	public String ry() {return Integer.toString(ry);}
	
	//TODO add chart caption, title, and description.
	
}
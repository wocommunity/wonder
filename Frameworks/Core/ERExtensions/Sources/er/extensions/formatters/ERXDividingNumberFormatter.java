package er.extensions.formatters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.FieldPosition;

import com.webobjects.foundation.NSNumberFormatter;

/**
 * @author david
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ERXDividingNumberFormatter extends NSNumberFormatter {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private float factor;
    /**
     * 
     */
    public ERXDividingNumberFormatter() {
        super();
    }

    /**
     * @param arg0
     */
    public ERXDividingNumberFormatter(String arg0) {
        super(arg0);
    }

    
    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSNumberFormatter#pattern()
     */
    @Override
    public String pattern() {
        String pattern = super.pattern();
        return pattern;
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSNumberFormatter#setPattern(java.lang.String)
     */
    @Override
    public void setPattern(String pattern) {
        
        try {
            if (pattern.indexOf("=)") == -1) {
                super.setPattern(pattern);
                return;
            }
            String realPattern = pattern.substring(pattern.indexOf("=)")+2);
            String f = pattern.substring(1, pattern.indexOf("=)"));
            try {
                factor = Float.parseFloat(f);
            } catch (NumberFormatException e1) {
                throw new IllegalArgumentException("ERXDividingNumberFormatter must have a pattern like '(1024=)0.00',"+
                " where 1024 is the factor.");
            }
            super.setPattern(realPattern);
            
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("ERXDividingNumberFormatter must have a pattern like '(1024=)0.00',"+
            " where 1024 is the factor.");
        }
    }

    /* (non-Javadoc)
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(Object arg0, StringBuffer arg1,
            FieldPosition arg2) {
        if (!(arg0 instanceof Number)) {
            return super.format(arg0, arg1, arg2);
        }
        Number n = (Number)arg0;
        if (arg0 instanceof BigDecimal) {
            BigDecimal b = (BigDecimal)arg0;
            b = b.divide(new BigDecimal(factor), BigDecimal.ROUND_HALF_UP);
            return super.format(b, arg1, arg2);
        } else if (arg0 instanceof BigInteger) {
            BigInteger b = (BigInteger)arg0;
            b = b.divide(new BigInteger(""+factor));
            return super.format(b, arg1, arg2);
        } else {
            double d = n.doubleValue();
            d /= factor;
            return super.format(Double.valueOf(d), arg1, arg2);
        }
    }
}

package er.extensions.formatters;

import junit.framework.Assert;
import er.erxtest.ERXTestCase;


/**
 * Tests for ERXOrdinalFormatter. 
 *
 * @author chill
 */
public class ERXOrdinalFormatterTests extends ERXTestCase {

    
    public void testEnglishFormat() {
        ERXOrdinalFormatter formatter = new ERXOrdinalFormatter();
        
       Assert.assertEquals("", formatter.format(null));
       Assert.assertEquals("1st", formatter.format(Integer.valueOf(1)));
       Assert.assertEquals("2nd", formatter.format(Integer.valueOf(2)));
       Assert.assertEquals("3rd", formatter.format(Integer.valueOf(3)));
       Assert.assertEquals("4th", formatter.format(Integer.valueOf(4)));
       Assert.assertEquals("10th", formatter.format(Integer.valueOf(10)));
       Assert.assertEquals("11th", formatter.format(Integer.valueOf(11)));
       Assert.assertEquals("20th", formatter.format(Integer.valueOf(20)));
       Assert.assertEquals("21st", formatter.format(Integer.valueOf(21)));
    }
    
    
}

// YearlyData.java
// Created on Mon Nov 05 11:45:01  2001 by Apple EOModeler Version 410

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;

public class YearlyData extends _YearlyData {

    public YearlyData() {
        super();
        setYear(currentYear());
        setVacationCarryOver(new Integer(0));
        setVacationEntitled(new Integer(0));
    }

    public YearlyData (Integer newYear) {
        super();
        setYear(newYear);
        setVacationCarryOver(new Integer(0));
        setVacationEntitled(new Integer(0));
    }
    
    public Integer currentYear() {
        return new Integer((new GregorianCalendar()).get(GregorianCalendar.YEAR));
    }

}

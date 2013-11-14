package er.rest.model;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;

public class Company extends _Company {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(Company.class);

    public String nonModelAttribute() {
        return "NonModelAttribute";
    }

    public NSArray<Manufacturer> manufacturers() {
        return Manufacturer.manufacturers();
    }
}

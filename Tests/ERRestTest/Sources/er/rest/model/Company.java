package er.rest.model;

import com.webobjects.foundation.NSArray;

public class Company extends _Company {
    public String nonModelAttribute() {
        return "NonModelAttribute";
    }

    public NSArray<Manufacturer> manufacturers() {
        return Manufacturer.manufacturers();
    }
}

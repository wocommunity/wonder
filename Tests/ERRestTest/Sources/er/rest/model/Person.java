package er.rest.model;

import org.apache.log4j.Logger;

public class Person extends _Person {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(Person.class);

    public Car car() {
        return Car.cars().lastObject();
    }
}

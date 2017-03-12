package er.rest.model;

public class Person extends _Person {
    public Car car() {
        return Car.cars().lastObject();
    }
}

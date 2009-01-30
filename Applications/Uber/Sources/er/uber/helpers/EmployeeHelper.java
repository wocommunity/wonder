package er.uber.helpers;

import er.uber.model.Employee;

public class EmployeeHelper {
  public String displayName(Employee employee) {
    return employee.firstName() + " " + employee.lastName();
  }
}

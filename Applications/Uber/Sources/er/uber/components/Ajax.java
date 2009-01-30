package er.uber.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

import er.uber.model.Company;

public class Ajax extends UberComponent {
  public WODisplayGroup _companies;
  public Company _company;

  public Ajax(WOContext context) {
    super(context);
  }
}
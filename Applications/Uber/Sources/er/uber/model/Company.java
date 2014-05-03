package er.uber.model;

import org.apache.log4j.Logger;

import er.attachment.model.ERAttachment;

public class Company extends _Company {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Company.class);
  
  @Override
  public void setLogo(ERAttachment value) {
		ERAttachment attachment = logo();
		if (attachment != null) {
			attachment.delete();
		}
  	super.setLogo(value);
  }
}

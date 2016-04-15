package er.uber.model;

import er.attachment.model.ERAttachment;

public class Company extends _Company {
  @Override
  public void setLogo(ERAttachment value) {
		ERAttachment attachment = logo();
		if (attachment != null) {
			attachment.delete();
		}
  	super.setLogo(value);
  }
}

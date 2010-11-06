package com.secretpal.migrations;

import com.secretpal.SPUtilities;
import com.secretpal.model.SPPerson;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSValidation;

import er.extensions.eof.ERXEC;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.IERXPostMigration;

/**
 * I found a bunch of email addresses where people invited with "<mschrag@whatever.com>". This migration goes
 * and corrects those email addresses, which are no longer allowed.
 * 
 * @author mschrag
 */
public class SecretPal1 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
  public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable {
    for (SPPerson person : SPPerson.fetchAllSPPersons(editingContext)) {
      EOEditingContext nestedEditingContext = ERXEC.newEditingContext(editingContext);
      SPPerson nestedPerson = person.localInstanceIn(nestedEditingContext);
      nestedPerson.setEmailAddress(SPUtilities.cleanseEmailAddress(nestedPerson.emailAddress()));
      try {
        nestedEditingContext.saveChanges();
      }
      catch (NSValidation.ValidationException e) {
        // this probably means the person realized they screwed up and reinvited the person with the proper email address
        if (person.memberships().count() == 0) {
          person.delete();
        }
      }
    }
  }

  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // DO NOTHING
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // DO NOTHING
  }
}

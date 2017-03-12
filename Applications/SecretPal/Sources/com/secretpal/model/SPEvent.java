package com.secretpal.model;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class SPEvent extends _SPEvent {
	private static Logger log = LoggerFactory.getLogger(SPEvent.class);

	public boolean hasAssignedSecretPals() {
		return secretPals().count() > 0;
	}
	
	public boolean hasNotAssignedSecretPals() {
		return !hasAssignedSecretPals();
	}
	
	public void reassignSecretPals() {
		// MS: deleting all the existing pals means that this implementation can be a lot easier, but it was originally
		// written to NOT do this, which is why it's so weird and non-deterministic.
		deleteAllSecretPalsRelationships();

    NSArray<SPPerson> allPeople = SPMembership.PERSON.arrayValueInObject(group().memberships());
		NSArray<SPPerson> eligibleGivers = _eligibleGivers(allPeople);
		NSMutableArray<SPPerson> consumedReceivers = new NSMutableArray<>();
		
		log.info("{} eligible givers out of {} people", eligibleGivers.count(), allPeople.count());

    SecureRandom random = new SecureRandom();
		long a = System.currentTimeMillis();
		int numberOfAttempts = 0;
		int maxNumberOfAttempts = 100;
		boolean doneWithSecretPals = false;
		while (!doneWithSecretPals && numberOfAttempts < maxNumberOfAttempts) {
			log.info("attempt #{} of {}", numberOfAttempts, maxNumberOfAttempts);
			EOEditingContext nestedEditingContext = ERXEC.newEditingContext(editingContext());
			nestedEditingContext.lock();
			SPEvent localEvent = localInstanceIn(nestedEditingContext);
			try {
			  for (SPPerson selectedGiver : eligibleGivers) {
	        NSArray<SPPerson> eligibleReceivers = _eligibleReceiversForPerson(selectedGiver, allPeople, consumedReceivers);
	        if (eligibleReceivers.count() == 0) {
            throw new IllegalStateException("  Try again -- no receivers for " + selectedGiver.name());
	        }
          int selectedReceiverNum = Math.abs(random.nextInt()) % eligibleReceivers.count();
          SPPerson selectedReceiver = eligibleReceivers.objectAtIndex(selectedReceiverNum);
          log.info("   {}=>{}, {}, {}", selectedGiver.name(), selectedReceiver.name(), selectedGiver, selectedReceiver);
          SPSecretPal.createSPSecretPal(nestedEditingContext, localEvent, selectedGiver.localInstanceIn(nestedEditingContext), selectedReceiver.localInstanceIn(nestedEditingContext));
          consumedReceivers.addObject(selectedReceiver);
			  }
				nestedEditingContext.saveChanges();
				doneWithSecretPals = true;
			}
			catch (IllegalStateException e) {
				numberOfAttempts ++;
			}
			finally {
				nestedEditingContext.unlock();
				nestedEditingContext.dispose();
			}
		}
		log.info("done in {}ms", System.currentTimeMillis() - a);
		if (!doneWithSecretPals) {
			throw new IllegalStateException("Failed to assign secret pals after " + numberOfAttempts + " attempts.");
		}
	}

	public boolean canEdit(SPPerson currentPerson) {
		return currentPerson.admin().booleanValue() || ERXEOControlUtilities.eoEquals(group().owner(), currentPerson);
	}

	public NSArray<SPPerson> _eligibleGivers(NSArray<SPPerson> allPeople) {
    NSMutableArray<SPPerson> eligibleGivers = new NSMutableArray<>();
    for (SPPerson possibleGiver : allPeople) {
      NSArray<SPPerson> noNoPeople = SPNoNoPal.RECEIVER.arrayValueInObject(noNoPalsForPerson(possibleGiver));
      // If someone has NO possible receivers right at the start, just leave them out ... This is to support
      // defining babies that can't buy for anyone
      if (noNoPeople.count() != allPeople.count() - 1) {
        eligibleGivers.addObject(possibleGiver);
      }
    }
    return eligibleGivers;
	}
	
	public NSArray<SPPerson> _eligibleReceiversForPerson(SPPerson giver, NSArray<SPPerson> allPeople, NSArray<SPPerson> consumedReceivers) {
	  NSMutableArray<SPPerson> eligibleReceivers = new NSMutableArray<>();
    NSArray<SPPerson> noNoPeople = SPNoNoPal.RECEIVER.arrayValueInObject(noNoPalsForPerson(giver));
	  for (SPPerson possibleReceiver : allPeople) {
	    if (!giver.equals(possibleReceiver) && !consumedReceivers.containsObject(possibleReceiver) && !noNoPeople.containsObject(possibleReceiver)) {
	      eligibleReceivers.addObject(possibleReceiver);
	    }
	  }
	  return eligibleReceivers;
	}
	
  public NSArray<SPSecretPal> secretPalsForPerson(SPPerson person) {
    return SPSecretPal.GIVER.is(person).filtered(secretPals());
  }
  
  public NSArray<SPNoNoPal> noNoPalsForPerson(SPPerson person) {
    return SPNoNoPal.GIVER.is(person).filtered(noNoPals());
  }
  
  public NSArray<SPPerson> noNoPersonPossibilitiesForPerson(SPPerson person) {
    NSMutableArray<SPPerson> noNoPalPossibilities = new NSMutableArray<>();
    NSArray<SPPerson> noNoPals = SPNoNoPal.RECEIVER.arrayValueInObject(noNoPalsForPerson(person));
    for (SPMembership membership : group().memberships()) {
      if (!membership.person().equals(person) && !noNoPals.containsObject(membership.person())) {
        noNoPalPossibilities.addObject(membership.person());
      }
    }
    return noNoPalPossibilities;
  }

}

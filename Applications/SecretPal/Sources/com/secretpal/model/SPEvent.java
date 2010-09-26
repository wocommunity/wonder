package com.secretpal.model;

import java.security.SecureRandom;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class SPEvent extends _SPEvent {
	private static Logger log = Logger.getLogger(SPEvent.class);

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

		NSArray<SPSecretPal> secretPals = secretPals();
		NSArray<SPPerson> existingGivers = SPSecretPal.GIVER.arrayValueInObject(secretPals);
		NSArray<SPPerson> existingReceivers = SPSecretPal.RECEIVER.arrayValueInObject(secretPals);
		
		NSMutableArray<SPPerson> availableGivers = SPMembership.PERSON.arrayValueInObject(group().memberships()).mutableClone();
		NSMutableArray<SPPerson> availableReceivers = availableGivers.mutableClone();
		availableGivers.removeObjectsInArray(existingGivers);
		availableReceivers.removeObjectsInArray(existingReceivers);
		
		log.info(availableGivers.count() + " givers");
		log.info(availableReceivers.count() + " receivers");

		long a = System.currentTimeMillis();
		int numberOfAttempts = 0;
		int maxNumberOfAttempts = 100;
		boolean doneWithSecretPals = false;
		while (!doneWithSecretPals && numberOfAttempts < maxNumberOfAttempts) {
			log.info("attempt #" + numberOfAttempts);
			EOEditingContext nestedEditingContext = ERXEC.newEditingContext(editingContext());
			nestedEditingContext.lock();
			SPEvent localEvent = localInstanceIn(nestedEditingContext);
			try {
				SecureRandom random = new SecureRandom();
				for (SPPerson selectedGiver : availableGivers) {
					log.info(selectedGiver.name());
					boolean doneWithSecretPal = false;
					while (!doneWithSecretPal) {
						int selectedGiverNum = Math.abs(random.nextInt()) % availableReceivers.count();
						SPPerson selectedReceiver = availableReceivers.objectAtIndex(selectedGiverNum);
						if (ERXEOControlUtilities.eoEquals(selectedGiver, selectedReceiver)) {
							log.info("   can't assign " + selectedGiver.name() + " to self");
							if (availableReceivers.count() == 1) {
								throw new IllegalStateException("Try again");
							}
						}
						else {
							log.info("   " + selectedGiver.name() + "=>" + selectedReceiver.name());
							SPSecretPal.createSPSecretPal(nestedEditingContext, localEvent, selectedGiver.localInstanceIn(nestedEditingContext), selectedReceiver.localInstanceIn(nestedEditingContext));
							availableReceivers.removeObject(selectedReceiver);
							doneWithSecretPal = true;
						}
					}
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
		log.info("done in " + (System.currentTimeMillis() - a) + "ms");
		if (!doneWithSecretPals) {
			throw new IllegalStateException("Failed to assign secret pals after " + numberOfAttempts + " attempts.");
		}
	}

	public boolean canEdit(SPPerson currentPerson) {
		return currentPerson.admin().booleanValue() || ERXEOControlUtilities.eoEquals(group().owner(), currentPerson);
	}
}

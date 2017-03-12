package er.bugtracker;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.components.ERXLoremIpsumGenerator;
import er.extensions.foundation.ERXStringUtilities;

public class BTDataCreator {
	private static final Logger log = LoggerFactory.getLogger(BTDataCreator.class);

	EOEditingContext ec;

	public BTDataCreator(EOEditingContext editingContext) {
	  ec = editingContext;
	}
	
	/*
	 * delete from EO_PK_TABLE; delete from PEOPLE; delete from ERCPREFER;
	 * delete from BUG_TEST_ITEM; delete from COMPONENT; delete from Comment;
	 * delete from EO_PK_TABLE; delete from `RELEASE`; delete from REQUIREMENT;
	 * delete from REQ_TEST_ITEM; delete from TEST_ITEM;
	 */

	NSMutableArray<People> users = new NSMutableArray();
	NSMutableArray<Component> components = new NSMutableArray();
	NSMutableArray<Bug> bugs = new NSMutableArray();
	NSMutableArray<Requirement> requirements = new NSMutableArray();
	NSMutableArray<TestItem> testItems = new NSMutableArray();
	NSMutableArray<Priority> priorities = new NSMutableArray();
	NSMutableArray<TestItemState> testItemStates = new NSMutableArray();
	NSMutableArray<State> states = new NSMutableArray();
	NSMutableArray<Release> releases = new NSMutableArray();
	NSMutableArray<RequirementType> requirementTypes = new NSMutableArray();
	NSMutableArray<RequirementSubType> requirementSubTypes = new NSMutableArray();

	private int randomInt(int max) {
		return new Random().nextInt(max);
	}

	private Object randomObject(NSArray array) {
		return array.objectAtIndex(randomInt(array.count()));
	}

	private Priority randomPriority() {
		return (Priority) randomObject(priorities);
	}

	private People randomUser() {
		return (People) randomObject(users);
	}

	private Component randomComponent() {
		return (Component) randomObject(components);
	}

	private Bug randomBug() {
		return (Bug) randomObject(bugs);
	}

	private Requirement randomRequirement() {
		return (Requirement) randomObject(requirements);
	}

	private RequirementType randomRequirementType() {
		return (RequirementType) randomObject(requirementTypes);
	}

	private RequirementSubType randomRequirementSubType() {
		return (RequirementSubType) randomObject(requirementSubTypes);
	}

	private State randomState() {
		return (State) randomObject(states);
	}

	private TestItemState randomTestItemState() {
		return (TestItemState) randomObject(testItemStates);
	}

	private Release randomRelease() {
		return (Release) randomObject(releases);
	}

	private NSTimestamp randomTimestamp() {
		return new NSTimestamp().timestampByAddingGregorianUnits(0, 0, 0, -randomInt(24 * 1000), 0, 0);
	}

	private String randomWords(int size) {
		return ERXLoremIpsumGenerator.words(5, size / 7, size);
	}

	private String randomText(int size) {
		return ERXLoremIpsumGenerator.paragraphs(size);
	}

	private void addComments(Bug bug) {
		int maxComments = randomInt(20);
		int last = 0;
		for (int i = 0; i < maxComments; i++) {
			Comment comment = Comment.clazz.createAndInsertObject(ec);
			int hours = last + randomInt(48);
			comment.setDateSubmitted(bug.dateSubmitted().timestampByAddingGregorianUnits(0, 0, 0, hours, 0, 0));
			comment.setOriginator(randomUser());
			comment.setTextDescription(randomText(50));
			last = hours;
			comment.setBug(bug);
			bug.addToComments(comment);
			comment.validateForSave();
		}
	}

	private NSDictionary optionsWithPrimaryKeySupportDiabled(NSDictionary options) {
		NSMutableDictionary mutableOptions = options.mutableClone();
		mutableOptions.setObjectForKey("NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		return mutableOptions.immutableClone();
	}

	public void createDummyData() {
		priorities = Priority.clazz.allObjects(ec).mutableClone();
		states = State.clazz.allObjects(ec).mutableClone();
		testItemStates = TestItemState.clazz.allObjects(ec).mutableClone();
		requirementTypes = RequirementType.clazz.allObjects(ec).mutableClone();
		requirementSubTypes = RequirementSubType.clazz.allObjects(ec).mutableClone();

		int maxUsers = 20;
		log.info("Creating users: {}", maxUsers);

		for (int i = 100; i < 100 + maxUsers; i++) {
			People user = People.clazz.createAndInsertObject(ec);
			users.addObject(user);
			user.setLogin("user" + i);
			user.setName(ERXStringUtilities.capitalizeAllWords(randomWords(20)) + " " + i);
			user.setEmail("dummy@localhost");
			user.setPassword("user");
			user.setIsActive(i % 10 != 0);
			user.setIsAdmin(i % 5 != 0);
			user.setIsCustomerService(i % 3 != 0);
			user.setIsEngineering(i % 3 != 0 && !user.isAdmin());
		}
		log.info("Saving...");
		ec.saveChanges();

		log.info("Creating releases, frameworks and components");

		for (int i = 1; i < 10; i++) {
			Release release = Release.clazz.createAndInsertObject(ec);
			release.setName("Release R" + i / 2);
			if (i % 2 == 0) {
				release.setName("Release R" + i / 2 + ".1");
			}
			releases.addObject(release);
		}
		NSTimestamp dateDue = new NSTimestamp().timestampByAddingGregorianUnits(0, 5, 0, 0, 0, 0);
		for (int i = 8; i >= 0; i--) {
			Release release = releases.objectAtIndex(i);
			release.setDateDue(dateDue);
			dateDue = dateDue.timestampByAddingGregorianUnits(0, -(randomInt(2) + 1), 0, 0, 0, 0);
		}

		for (int i = 0; i < 10; i++) {
			Component component = Component.clazz.createAndInsertObject(ec);
			component.setOwner(randomUser());
			component.setTextDescription("Component " + i / 2);
			if (i % 2 == 1) {
				Component parent = components.lastObject();
				component.setParent(parent);
				component.setTextDescription("Component " + i / 2 + ".1");
			}
			components.addObject(component);
		}

		String names[] = new String[] { "ERDirectToWeb", "ERCoreBusinessLogic", "BTBusinessLogic", "BugTracker" };
		for (int i = 0; i < names.length; i++) {
			String string = names[i];
			Framework framework = Framework.clazz.createAndInsertObject(ec);
			framework.setName(string);
			framework.setOrdering(Integer.valueOf(i));
		}
		log.info("Saving...");
		ec.saveChanges();

		int maxItems = maxUsers * 10;

		log.info("Creating bugs: {}", maxItems);

		for (int i = 0; i < maxItems; i++) {
			People.clazz.setCurrentUser(randomUser());
			Bug bug = Bug.clazz.createAndInsertObject(ec);
			bugs.addObject(bug);
			bug.setDateSubmitted(randomTimestamp());
			bug.setDateModified(bug.dateSubmitted().timestampByAddingGregorianUnits(0, 0, 0, randomInt(24 * 1000), 0, 0));
			bug.setComponent(randomComponent());
			bug.setSubject(randomWords(50));
			bug.setTextDescription(randomText(3));
			bug.setOriginator(randomUser());
			bug.setOwner(randomUser());
			bug.setPreviousOwner(randomUser());
			bug.setPriority(randomPriority());
			bug.setState(randomState());
			bug.setIsFeatureRequest(i % 4 == 0);
			bug.setTargetRelease(randomRelease());
			addComments(bug);
		}

		log.info("Creating requirements: {}", maxItems);

		for (int i = 0; i < maxItems; i++) {
			People.clazz.setCurrentUser(randomUser());
			Requirement bug = (Requirement) Requirement.clazz.createAndInsertObject(ec);
			requirements.addObject(bug);
			bug.setDateSubmitted(randomTimestamp());
			bug.setDateModified(bug.dateSubmitted().timestampByAddingGregorianUnits(0, 0, 0, randomInt(24 * 100), 0, 0));
			bug.setComponent(randomComponent());
			bug.setSubject(randomWords(50));
			bug.setTextDescription(randomText(3));
			bug.setOriginator(randomUser());
			bug.setOwner(randomUser());
			bug.setPreviousOwner(randomUser());
			bug.setPriority(randomPriority());
			bug.setState(randomState());
			bug.setIsFeatureRequest(i % 4 == 0);
			bug.setTargetRelease(randomRelease());

			bug.setRequirementType(randomRequirementType());
			bug.setRequirementSubType(randomRequirementSubType());
			addComments(bug);
		}

		log.info("Creating test items: {}", maxItems * 2);

		for (int i = 0; i < maxItems * 2; i++) {
			People.clazz.setCurrentUser(randomUser());
			TestItem testItem = TestItem.clazz.createAndInsertObject(ec);
			testItems.addObject(testItem);
			TestItemState state = randomTestItemState();
			Bug bug = null;
			Component component = randomComponent();
			if (state == TestItemState.REQ) {
				bug = randomRequirement();
			} else if (state == TestItemState.BUG) {
				bug = randomBug();
			}
			testItem.setDateCreated(randomTimestamp());
			testItem.setTitle(randomWords(50));
			testItem.setTextDescription(randomText(3));
			testItem.setControlled(randomWords(50));
			testItem.setOwner(randomUser());
			testItem.setState(state);
			if (bug != null) {
				bug.addToTestItems(testItem);
				component = bug.component();
			}
			testItem.setComponent(component);
		}

		People user = People.clazz.createAndInsertObject(ec);
		user.setLogin("admin");
		user.setName("Administrator");
		user.setEmail("dummy@localhost");
		user.setPassword("admin");
		user.setIsActive(true);
		user.setIsAdmin(true);
		user.setIsCustomerService(false);
		user.setIsEngineering(true);

		log.info("Saving...");
		ec.saveChanges();
		log.info("Done");
	}
}

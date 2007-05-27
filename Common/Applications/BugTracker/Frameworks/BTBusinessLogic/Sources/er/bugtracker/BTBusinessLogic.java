/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Random;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXApplication;
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
import er.extensions.ERXFileUtilities;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.ERXJDBCUtilities;
import er.extensions.ERXLoremIpsumGenerator;
import er.extensions.ERXMainRunner;
import er.extensions.ERXProperties;
import er.extensions.ERXSQLHelper;
import er.extensions.ERXStringUtilities;
import er.prototypes.ERPrototypes;

public class BTBusinessLogic extends ERXFrameworkPrincipal {

    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class, ERPrototypes.class, ERCoreBusinessLogic.class};
    
    private static final Logger log = Logger.getLogger(BTBusinessLogic.class);
    
    static {
        setUpFrameworkPrincipalClass(BTBusinessLogic.class);
    }

    public static class DataCreator {
        EOEditingContext ec;
        
        public static void main(String[] args) {
            new DataCreator().create(args);
        }
        
        /*
delete from EO_PK_TABLE;
delete from PEOPLE;
delete from ERCPREFER;
delete from BUG_TEST_ITEM;
delete from COMPONENT;
delete from Comment;
delete from EO_PK_TABLE;
delete from `RELEASE`;
delete from REQUIREMENT;
delete from REQ_TEST_ITEM;
delete from TEST_ITEM;
         */
        
        NSMutableArray users = new NSMutableArray();
        NSMutableArray components = new NSMutableArray();
        NSMutableArray bugs = new NSMutableArray();
        NSMutableArray requirements = new NSMutableArray();
        NSMutableArray testItems = new NSMutableArray();
        NSMutableArray priorities = new NSMutableArray();
        NSMutableArray testItemStates = new NSMutableArray();
        NSMutableArray states = new NSMutableArray();
        NSMutableArray releases = new NSMutableArray();
        NSMutableArray requirementTypes = new NSMutableArray();
        NSMutableArray requirementSubTypes = new NSMutableArray();
               
        private int randomInt(int max) {
            return new Random().nextInt(max);
        }
        
        private Object randomObject(NSArray array) {
            return array.objectAtIndex(randomInt(array.count()));
        }
        
        private Priority randomPriority() {
            return (Priority)randomObject(priorities);
        }
        
        private People randomUser() {
            return (People)randomObject(users);
        }
        
        private Component randomComponent() {
            return (Component) randomObject(components);
        }
        
        private Bug randomBug() {
            return (Bug) randomObject(bugs);
        }
        
        private Requirement randomRequirement() {
            return (Requirement)randomObject(requirements);
        }
        
        private RequirementType randomRequirementType() {
            return (RequirementType)randomObject(requirementTypes);
        }
        
        private RequirementSubType randomRequirementSubType() {
            return (RequirementSubType)randomObject(requirementSubTypes);
        }
        
        private State randomState() {
            return (State)randomObject(states);
        }
        
        private TestItemState randomTestItemState() {
            return (TestItemState)randomObject(testItemStates);
        }

        private Release randomRelease() {
            return (Release)randomObject(releases);
        }
        
        private NSTimestamp randomTimestamp() {
            return new NSTimestamp().timestampByAddingGregorianUnits(
                    0, 0, 0, -randomInt(24*1000), 0, 0);
        }
        
        private String randomWords(int size) {
            return ERXLoremIpsumGenerator.words(5, size/7, size);
        }
        
        private String randomText(int size) {
            return ERXLoremIpsumGenerator.paragraphs(size);
        }
        
        private void addComments(Bug bug) {
            int maxComments = randomInt(20);
            int last = 0;
            for(int i = 0; i < maxComments; i++) {
                Comment comment = (Comment)Comment.clazz.createAndInsertObject(ec);
                int hours = last + randomInt(48);
                comment.setDateSubmitted(bug.dateSubmitted().timestampByAddingGregorianUnits(0, 0, 0, hours, 0, 0));
                comment.setOriginator(randomUser());
                comment.setTextDescription(randomText(5));
                last = hours;
                bug.addToComments(comment);
            }
        }

        public void create(String[] args) {
            ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                boolean dropTables = ERXProperties.booleanForKeyWithDefault("dropTables", false);
                createTables(dropTables);
                createDummyData();
            } finally {
                ec.unlock();
            }
        }

        private void createTables(boolean dropTables) {
            for (Enumeration e = EOModelGroup.defaultGroup().models().objectEnumerator(); e.hasMoreElements();) {
                EOModel eomodel = (EOModel) e.nextElement();
                EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, eomodel.name());
                dbc.lock();
                try {
                    EOAdaptorChannel channel = dbc.availableChannel().adaptorChannel();
                    EOSynchronizationFactory syncFactory = (EOSynchronizationFactory) channel.adaptorContext().adaptor().synchronizationFactory();
                    ERXSQLHelper helper = ERXSQLHelper.newSQLHelper(channel);
                    NSDictionary options = helper.defaultOptionDictionary(true, dropTables);
                    String sqlScript = syncFactory.schemaCreationScriptForEntities(eomodel.entities(), options);
                    log.info("Creating tables: " + eomodel.name());
                    ERXJDBCUtilities.executeUpdateScript(channel, sqlScript);
                    if(eomodel.name().equals("BugTracker")) {
                        InputStream is = ERXFileUtilities.inputStreamForResourceNamed("populate.sql", "BTBusinessLogic", null);
                        String sql = ERXFileUtilities.stringFromInputStream(is);
                        log.info("Populating: " + eomodel.name());
                        ERXJDBCUtilities.executeUpdateScript(channel, sql);
                        // re-read shared data, the first time around it probably wasn't there
                        // strictly speaking, we'd also need to do this for ERC too
                        BTBusinessLogic.sharedInstance().initializeSharedData();
                    }
                } catch (SQLException ex) {
                    log.error("Can't update: " + ex, ex);
                } catch (IOException ex) {
                    log.error("Can't read: " + ex, ex);
                } finally {
                    dbc.unlock();
                }
            }
            
        }
        
        private void createDummyData() {
            priorities = Priority.clazz.allObjects(ec).mutableClone();
            states = State.clazz.allObjects(ec).mutableClone();
            testItemStates = TestItemState.clazz.allObjects(ec).mutableClone();
            requirementTypes = RequirementType.clazz.allObjects(ec).mutableClone();
            requirementSubTypes = RequirementSubType.clazz.allObjects(ec).mutableClone();
            
            log.info("Creating users");
            
            for(int i = 100; i < 150; i++) {
                People user = (People)People.clazz.createAndInsertObject(ec);
                users.addObject(user);
                user.setLogin("user"+ i);
                user.setName(ERXStringUtilities.capitalizeAllWords(randomWords(20)) + " " + i);
                user.setEmail("dummy@localhost");
                user.setPassword("user");
                user.setIsActive(i % 10 != 0);
                user.setIsAdmin(i % 5 != 0);
                user.setIsCustomerService(i % 3 != 0);
                user.setIsEngineering(i % 3 != 0 && !user.isAdmin());
            }

            log.info("Creating releases, frameworks and components");
            
            for(int i = 1; i < 10; i++) {
                Release release = (Release) Release.clazz.createAndInsertObject(ec);
                release.setName("Release R" + i/2);
                if(i % 2 == 0) {
                    release.setName("Release R" + i/2 + ".1");
                }
                releases.addObject(release);
            }

            for(int i = 0; i < 10; i++) {
                Component component = (Component) Component.clazz.createAndInsertObject(ec);
                component.setOwner(randomUser());
                component.setTextDescription("Component " + i/2);
                if(i % 2 == 1) {
                    Component parent = (Component) components.lastObject();
                    parent.addToChildren(component);
                    component.setTextDescription("Component " + i/2 + ".1");
                }
                components.addObject(component);
            }

            String names[] = new String[]{"ERDirectToWeb", "ERCoreBusinessLogic", "BTBusinessLogic", "BugTracker"};
            for (int i = 0; i < names.length; i++) {
                String string = names[i];
                Framework framework = (Framework) Framework.clazz.createAndInsertObject(ec);
                framework.setName(string);
                framework.setOrdering(new Integer(i));
            }
            
            int MAX = 500;

            log.info("Creating bugs: "+ MAX);
            
            for(int i = 0; i < MAX; i++) {
                People.clazz.setCurrentUser(randomUser());
                Bug bug = (Bug) Bug.clazz.createAndInsertObject(ec);
                bugs.addObject(bug);
                bug.setDateSubmitted(randomTimestamp());
                bug.setDateModified(bug.dateSubmitted().timestampByAddingGregorianUnits(0, 0, 0, randomInt(24*1000), 0, 0));
                bug.setComponent(randomComponent());
                bug.setSubject(randomWords(50));
                bug.setTextDescription(randomText(3));
                bug.setOriginator(randomUser());
                bug.setOwner(randomUser());
                bug.setPreviousOwner(randomUser());
                bug.setTextDescription(randomText(3));
                bug.setPriority(randomPriority());
                bug.setState(randomState());
                bug.setIsFeatureRequest(i % 4 == 0);
                bug.setTargetRelease(randomRelease());

                addComments(bug);
            }

            log.info("Creating requirements: "+ MAX);
            
            for(int i = 0; i < MAX; i++) {
                People.clazz.setCurrentUser(randomUser());
                Requirement bug = (Requirement) Requirement.clazz.createAndInsertObject(ec);
                requirements.addObject(bug);
                bug.setDateSubmitted(randomTimestamp());
                bug.setDateModified(bug.dateSubmitted().timestampByAddingGregorianUnits(0, 0, 0, randomInt(24*100), 0, 0));
                bug.setComponent(randomComponent());
                bug.setSubject(randomWords(50));
                bug.setTextDescription(randomText(3));
                bug.setOriginator(randomUser());
                bug.setOwner(randomUser());
                bug.setPreviousOwner(randomUser());
                bug.setTextDescription(randomText(3));
                bug.setPriority(randomPriority());
                bug.setState(randomState());
                bug.setIsFeatureRequest(i % 4 == 0);
                bug.setTargetRelease(randomRelease());
                
                bug.setRequirementType(randomRequirementType());
                bug.setRequirementSubType(randomRequirementSubType());
                addComments(bug);
            }

            log.info("Creating test items: "+ MAX * 9);
            
            for(int i = 0; i < MAX * 9; i++) {
                People.clazz.setCurrentUser(randomUser());
                TestItem testItem = (TestItem) TestItem.clazz.createAndInsertObject(ec);
                testItems.addObject(testItem);
                TestItemState state = randomTestItemState();
                Bug bug = null;
                Component component = randomComponent();
                if(state == TestItemState.REQ) {
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
                if(bug != null) {
                    bug.addToTestItems(testItem);
                    component = bug.component();
                }
                testItem.setComponent(component);
            }

            People user = (People)People.clazz.createAndInsertObject(ec);
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
    
    static BTBusinessLogic sharedInstance;
    public static BTBusinessLogic sharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = (BTBusinessLogic)ERXFrameworkPrincipal.sharedInstance(BTBusinessLogic.class);
        }
        return sharedInstance;
    }

    public void finishInitialization() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EOModel model = EOModelGroup.defaultGroup().modelNamed("BugTracker");
            EOEntity release = model.entityNamed("Release");
            if(model.connectionDictionary().toString().toLowerCase().indexOf("mysql") >= 0) {
                release.setExternalName("`RELEASE`");
            }
            initializeSharedData();
            ERCoreBusinessLogic.sharedInstance().addPreferenceRelationshipToActorEntity("People", "id");
        } catch(JDBCAdaptorException ex) {
            if(!(ERXApplication.erxApplication() instanceof ERXMainRunner)) {
                throw ex;
            }
        } finally {
            ec.unlock();
        }
    }

    // Shared Data Init Point.  Keep alphabetical
    public void initializeSharedData() {
        State.clazz.initializeSharedData();
        Priority.clazz.initializeSharedData();
        TestItemState.clazz.initializeSharedData();
    }
}

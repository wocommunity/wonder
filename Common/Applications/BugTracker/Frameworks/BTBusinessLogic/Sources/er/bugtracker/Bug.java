// Bug.java
// 
package er.bugtracker;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXArrayUtilities;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;

public class Bug extends _Bug implements Markable {
    static final Logger log = Logger.getLogger(Bug.class);

    protected boolean _componentChanged;
    protected boolean _ownerChanged;

    public void init(EOEditingContext ec) {
        super.init(ec);
        setPriority(Priority.MEDIUM);
        setState(State.ANALYZE);
        setTargetRelease(Release.clazz.defaultRelease(ec));
        setIsRead(true);
        setIsFeatureRequest(false);
        setOriginator(People.clazz.currentUser(editingContext()));
        setOwner(People.clazz.currentUser(editingContext()));
        setDateSubmitted(new NSTimestamp());
        setDateModified(new NSTimestamp());
         setType("b");
    }

    public void markAsRead() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			People people = (People) ERCoreBusinessLogic.actor(ec);
			Bug copy = (Bug) ERXEOControlUtilities.localInstanceOfObject(ec, this);
			if(copy != null && !copy.isRead() && copy.owner().equals(people)) {
				copy.setIsRead(true);
				ec.saveChanges();
			}
		} finally {
			ec.unlock();
		}
	}
    
    public void markUnread() {
        setIsRead(false);
    }

    public void touch() {
        markUnread();
        setDateModified(new NSTimestamp());
    }

    // FIXME:(ak) now *what* is this supposed to do???
    public void setComponent(Component value) {
        willChange();
        Component oldComponent = component();
        super.setComponent(value);
        if (value!=null) {
            if (owner() == null) {
                setOwner(component().owner());
            } else if ((oldComponent==null) || (!(value.equals(oldComponent)))) {
                _componentChanged = true;
            }
        }
    }

    public void setOwner(People value) {
        willChange();
        People oldOwner = owner();
        super.setOwner(value);
        People currentUser = People.clazz.currentUser(editingContext());
        if ((value!=null) && (value!=currentUser) && (oldOwner==null ||
                                                     (!(value.equals(oldOwner))))) {
            _ownerChanged=true;
            if (oldOwner!=null) setPreviousOwner(oldOwner);
            touch();
        }
    }

	public void setState(State newState) {
        willChange();
        State oldState = state();
        if (newState==State.CLOSED && isFeatureRequest() && oldState==State.VERIFY)
            newState=State.DOCUMENT;
        super.setState(newState);
        if (newState==State.DOCUMENT && !_ownerChanged) {
            People documenter = People.clazz.defaultDocumenter(editingContext());
            if(documenter!=null) {
                setOwner(documenter);
                setIsRead(false);
            }
        }
        if (newState==State.VERIFY && !_ownerChanged) {
            People verifier = People.clazz.defaultVerifier(editingContext());
            if(verifier!=null) {
                setOwner(verifier);
            } else {
                setOwner(originator());
                touch();
            }
        }
	}

    public Object validateTargetReleaseForNewBugs() throws NSValidation.ValidationException {
        Release release = targetRelease();
        if (release != null) {
            if (!release.isOpen())
                throw new NSValidation.ValidationException("Sorry, the release <b>"+release.valueForKey("name")+"</b> is closed. Bugs/Requirements can only be attached to open releases" );
        }
        return null;
    }

    public void validateForInsert() {
        super.validateForInsert();
        validateTargetReleaseForNewBugs();
    }

    public void validateForUpdate() {
        if (_componentChanged && component()!=null && !_ownerChanged) {
            setOwner(component().owner());
        }
        _componentChanged=false;
        _ownerChanged=false;
        super.validateForUpdate();
        if(!(changesFromCommittedSnapshot().count() == 1 && changesFromCommittedSnapshot().allKeys().containsObject(Key.IS_READ))) {
            touch();
        }

    }

    public void validateForDelete () throws NSValidation.ValidationException {
        throw new NSValidation.ValidationException("Bugs can not be deleted; they can only be closed.");
    }

    // this key is used during mass updates by both the template EO and the real bugs
    private String _newText;

	public String newText() {
		return _newText;
	}

	public void setNewText(String newValue) {
		_newText = newValue;
		if (newValue != null && newValue.length() > 0) {
			Comment comment = (Comment) Comment.clazz.createAndInsertObject(editingContext());
			comment.setBug(this);
			addToComments(comment);
			
			String oldText = textDescription();

			if (oldText != null)
				oldText = oldText + "\n\n";
			else
				oldText = "";
			String newText = oldText + newValue;
			comment.setTextDescription(newText);
			touch();
		}
	}
	
	public NSArray sortedComments() {
		return ERXArrayUtilities.sortedArraySortedWithKey(comments(), Comment.Key.DATE_SUBMITTED);
	}

    public void didUpdate() {
        super.didUpdate();
        _newText=null;
    }
    
    
    // Class methods go here
    
    public static class BugClazz extends _BugClazz {
        
        protected EOQualifier qualifierForRelease(Release release) {
            if(release != null) {
                return new EOKeyValueQualifier(Key.TARGET_RELEASE, EOQualifier.QualifierOperatorEqual, release);
            }
            return null;
        }

        protected EOQualifier qualifierForState(State state) {
            if(state != null) {
                return new EOKeyValueQualifier(Key.STATE, EOQualifier.QualifierOperatorEqual, state);
            }
            return null;
        }

        protected EOQualifier qualifierForStates(State states[]) {
            return ERXEOControlUtilities.orQualifierForKeyPaths(
                    new NSArray(Key.STATE), EOQualifier.QualifierOperatorEqual, new NSArray(states)
            );
        }

        protected EOQualifier qualifierForOwner(People owner) {
            if(owner != null) {
                return new EOKeyValueQualifier(Key.OWNER, EOQualifier.QualifierOperatorEqual, owner);
            }
            return null;
        }

        protected EOQualifier qualifierForRead(boolean flag) {
            return new EOKeyValueQualifier(Key.IS_READ, EOQualifier.QualifierOperatorEqual, new Boolean(flag));
        }
        
        protected EOQualifier qualifierForPerson(People owner) {
            return ERXEOControlUtilities.andQualifier(
                    new EOKeyValueQualifier(Key.OWNER, EOQualifier.QualifierOperatorEqual, owner),
                    new EOKeyValueQualifier(Key.ORIGINATOR, EOQualifier.QualifierOperatorEqual, owner)
            );
        }
        
        protected EOQualifier negateQualifier(EOQualifier qualifier) {
            if(qualifier != null) {
                qualifier = new EONotQualifier(qualifier); 
            }
            return qualifier;
        }
        
        protected EOQualifier andQualifier(EOQualifier q1, EOQualifier q2) {
            return ERXEOControlUtilities.andQualifier(q1, q2);
        }
        
        protected EOFetchSpecification newFetchSpecification(EOQualifier qualifier) {
            return new EOFetchSpecification(entityName(), qualifier, null);
        }
        
        protected EOFetchSpecification newFetchSpecification(EOQualifier qualifier, NSArray sorting) {
            return new EOFetchSpecification(entityName(), qualifier, sorting);
        }

        public EOFetchSpecification fetchSpecificationForOwnedBugs(People people) {
            // owner, not(closed)
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForOwner(people), 
                            negateQualifier(qualifierForState(State.CLOSED))));
            return fs;
        }

        public NSArray bugsOwnedWithUser(EOEditingContext context, People people) {
            return context.objectsWithFetchSpecification(fetchSpecificationForOwnedBugs(people));
        }

        public NSArray unreadBugsWithUser(EOEditingContext context, People people) {
            // owner or originator, not(read)
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForPerson(people), 
                            qualifierForRead(false)));
            return context.objectsWithFetchSpecification(fs);
        }

        public NSArray bugsInBuildWithTargetRelease(EOEditingContext context, Release targetRelease) {
             // release, build
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForRelease(targetRelease), 
                            qualifierForState(State.BUILD)));
            return context.objectsWithFetchSpecification(fs);
        }

        public NSArray openBugsWithTargetRelease(EOEditingContext context, Release targetRelease) {
             // release, build
            EOFetchSpecification fs = newFetchSpecification(
                    andQualifier(
                            qualifierForRelease(targetRelease), 
                            negateQualifier(qualifierForState(State.CLOSED))));
            return context.objectsWithFetchSpecification(fs);
        }
     
        public NSArray findBugs(EOEditingContext ec, String string) {
            NSArray a=NSArray.componentsSeparatedByString(string," ");
            NSMutableArray quals=new NSMutableArray();
            for (Enumeration e=a.objectEnumerator(); e.hasMoreElements();) {
                String s=(String)e.nextElement();
                try {
                    Integer i=new Integer(s);
                    quals.addObject(new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, i));

                } catch (NumberFormatException ex) {}
            }
            EOOrQualifier or=new EOOrQualifier(quals);
            EODatabaseDataSource ds=newDatabaseDataSource(ec);
            EOFetchSpecification fs=newFetchSpecification(or,null);
            ds.setFetchSpecification(fs);
            NSArray bugs = ds.fetchObjects();
            return bugs;
        }
    }

    public static final BugClazz clazz = new BugClazz();

    public void close() {
        setState(State.CLOSED);
    }

	public void reopen() {
		setState(State.ANALYZE);
		setOwner(previousOwner());
	}

	public void rejectVerification() {
		setState(State.ANALYZE);
		setOwner(previousOwner());
	}

    public void moveToVerification() {
        setState(State.VERIFY);
    }

    public Number bugid() {
        return (Number) rawPrimaryKey();
    }
}



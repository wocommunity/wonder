// Bug.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXArrayUtilities;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXValueUtilities;

public class Bug extends _Bug implements Markable {
    static final Logger log = Logger.getLogger(Bug.class);

    protected boolean _componentChanged;
    protected boolean _ownerChanged;

    public void init(EOEditingContext ec) {
        super.init(ec);
        setPriority(Priority.MEDIUM);
        setState(State.ANALYZE);
        addToBothSidesOfTargetRelease(Release.clazz.defaultRelease(ec));
        setReadAsBoolean(true);
        setDateSubmitted(new NSTimestamp());
        setDateModified(new NSTimestamp());
        setFeatureRequest(Boolean.FALSE);
    }

	public void markAsRead() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			People people = (People) ERCoreBusinessLogic.actor(ec);
			Bug copy = (Bug) ERXEOControlUtilities.localInstanceOfObject(ec, this);
			if(copy != null && !copy.isRead() && copy.owner().equals(people)) {
				copy.setReadAsBoolean(true);
				ec.saveChanges();
			}
		} finally {
			ec.unlock();
		}
	}

	public void markReadBy(People reader) {
        if (owner() != null && owner().equals(localInstanceOf(reader)) && !isRead()) {
            setReadAsBoolean(true);
            editingContext().saveChanges();
        }
    }
    
    public void markUnread() {
        setReadAsBoolean(false);
    }

    public void touch() {
        markUnread();
        setDateModified(new NSTimestamp());
    }

    public void setReadAsBoolean(boolean read) {
        setRead(read ? "Y":"N");
    }
    public boolean isRead() {
        return "Y".equals(read());
    }

    // FIXME:(ak) now *what* is this supposed to do???
    public void setComponent(Component value) {
        willChange();
        Component oldComponent = component();
        super.setComponent(value);
        if (value!=null) {
            if (owner() == null) {
                addToBothSidesOfOwner(component().owner());
            } else if ((oldComponent==null) || (!(value.equals(oldComponent)))) {
                _componentChanged = true;
            }
        }
    }

    public void setOwner(People value) {
        willChange();
        People oldOwner = owner();
        super.setOwner(value);
        EOEnterpriseObject localOwner = ERCoreBusinessLogic.actor(editingContext());
        if ((value!=null) && (value!=localOwner) && (oldOwner==null ||
                                                     (!(value.equals(oldOwner))))) {
            _ownerChanged=true;
            if (oldOwner!=null) setPreviousOwner(oldOwner);
            touch();
        }
    }

    public boolean isFeatureRequest() {
		return ERXValueUtilities.booleanValue(featureRequest());
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
                addToBothSidesOfOwner(documenter);
                setReadAsBoolean(false);
            }
        }
        if (newState==State.VERIFY && !_ownerChanged) {
            People verifier = People.clazz.defaultVerifier(editingContext());
            if(verifier!=null) {
                addToBothSidesOfOwner(verifier);
            } else {
                addToBothSidesOfOwner(originator());
                touch();
            }
        }
	}

    public Object validateTargetReleaseForNewBugs() throws NSValidation.ValidationException {
        Release release = Release.clazz.targetRelease(editingContext());
        if (release != null) {
            if (!release.isOpenAsBoolean())
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
            addToBothSidesOfOwner(component().owner());
        }
        _componentChanged=false;
        _ownerChanged=false;
        super.validateForUpdate();
        if(!(changesFromCommittedSnapshot().count() == 1 && changesFromCommittedSnapshot().allKeys().containsObject("read"))) {
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
			addToBothSidesOfComments(comment);
			
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
	
	public NSArray comments() {
		return (NSArray)storedValueForKey("comments");
	}
	
    public void addToBothSidesOfComments(Comment object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "comments");
    }

    public void didUpdate() {
        super.didUpdate();
        _newText=null;
    }
    
    
    // Class methods go here
    
    public static class BugClazz extends _BugClazz {
        
    }

    public static final BugClazz clazz = new BugClazz();

    public void close() {
        setState(State.CLOSED);
    }
}



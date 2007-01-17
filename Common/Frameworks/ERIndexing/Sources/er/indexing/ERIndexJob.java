/**
 * 
 */
package er.indexing;

import org.apache.lucene.util.Parameter;

import com.webobjects.foundation.NSArray;

class ERIndexJob {
	
	protected static class Command extends Parameter {

		protected Command(String name) {
			super(name);
		}
		
		protected static Command CLEAR = new Command("CLEAR");
		protected static Command ADD = new Command("ADD");
		protected static Command DELETE = new Command("DELETE");
	} 
	
	
	private ERIndex _index;
	private Command _command;
	private NSArray _objects;

	public ERIndexJob(ERIndex index, Command command, NSArray objects) {
		_index = index;
		_command = command;
		_objects = objects;
	}

	public ERIndex index() {
		return _index;
	}

	public NSArray objects() {
		return _objects;
	}

	public Command command() {
		return _command;
	}
}
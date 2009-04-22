
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.foundation.NSArray;


public class Talent extends EOGenericRecord {
	
	// accessors
	public boolean isDirector() {
		NSArray moviesDirected = (NSArray) valueForKey("moviesDirected");
		return (moviesDirected != null && moviesDirected.count() > 0);
	}

}

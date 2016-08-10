package er.neo4jadaptor.utils.iteration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class FlattenedIteratorTest extends TestCase {
	public void test1_discoveredNPETest() {
		// it used to fail when internal iterator was not present, due to NullPointerException
		List<Iterator<Object>> iterators = new ArrayList<Iterator<Object>>();
		FlattenedIterator<Object> it = new FlattenedIterator<Object>(iterators.iterator());
		
		while (it.hasNext()) {
			it.next();
		}
	}
}

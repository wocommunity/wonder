import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXFileUtilities;

/**
 * Just some example data.
 */

public class ExampleData {
	static Logger log = Logger.getLogger(AutoCompleteExample.class);

	public ExampleData(String name) {
		this.name = name;
		this.value = name.length();
	}
	public String name;
	public int value;
	
	private static NSMutableArray examples;
	
	public static NSArray exampleValues() {
		// some sample data. if we don't find the file, just create random strings
		if(examples == null) {
			examples = new NSMutableArray();
			File f = new File("/usr/share/dict/words");
			if(f.exists()) {
				try {
					String words = ERXFileUtilities.stringFromFile(f);
					String splitWords[] = words.split("\\n");
					for (int i = 0; i < splitWords.length; i++) {
						String word = splitWords[i];
						// we don't want all words, that just takes too long
						if(word.length() > 0 && ((i % 20) == 0)) {
							ExampleData example = new ExampleData(word);
							examples.addObject(example);
						}
					}
				} catch (IOException e) {
					AutoCompleteExample.log.error("Can't read " + f + ": " + e, e);
				}
			}
			if(examples.count() == 0) {
				Random rand = new Random();

				for(int i = 0; i < 1000; i++) {
					String s = "";
					int max = 6 + rand.nextInt(6);
					for(int j = 0; j< max; j++) {
						int c = rand.nextInt(26);
						s += Character.toString((char) ('A' + c));
					}
					ExampleData example = new ExampleData(s);
					examples.addObject(example);
				}
			}
			EOSortOrdering.sortArrayUsingKeyOrderArray(examples, new NSArray(
					EOSortOrdering.sortOrderingWithKey("name", EOSortOrdering.CompareAscending)));

		}
		return examples;
	}
}
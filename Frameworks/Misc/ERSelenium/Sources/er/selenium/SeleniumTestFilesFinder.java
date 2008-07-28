package er.selenium;

import java.io.File;

import com.webobjects.foundation.NSArray;

public interface SeleniumTestFilesFinder {
	public NSArray<File> findTests(File root);
}

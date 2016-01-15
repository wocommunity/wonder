/*
 * Copyright (c) 2007 Design Maximum - http://www.designmaximum.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package er.selenium.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSMutableDictionary;

public class SeleniumImporterExporterFactory {
	private static final Logger log = LoggerFactory.getLogger(SeleniumImporterExporterFactory.class);
	private static SeleniumImporterExporterFactory _instance = null;
	
	public static SeleniumImporterExporterFactory instance() {
		if (_instance == null) {
			_instance = new SeleniumImporterExporterFactory();
		}
		
		return _instance;
	}
	
	/* key is file extension (.html), value is array (0 => importer, 1 => exporter) */
	protected static NSMutableDictionary _importersByExtensionMap = new NSMutableDictionary();
	protected static NSMutableDictionary _importersByNameMap = new NSMutableDictionary();
	protected static NSMutableDictionary _exportersByNameMap = new NSMutableDictionary();
	
	public void registerImporter(String extension, SeleniumTestImporter importer) {
		log.debug("Registering importer '{}' for extension '{}'", importer.name(), extension);
		_importersByExtensionMap.setObjectForKey(importer, extension);
		_importersByNameMap.setObjectForKey(importer, importer.name());
	}
	
	public void registerExporter(SeleniumTestExporter exporter) {
		log.debug("Registering exporter '{}'", exporter.name());
		_exportersByNameMap.setObjectForKey(exporter, exporter.name());
	}
	
	public SeleniumTestImporter findImporterByExtension(String extension) {
		return (SeleniumTestImporter)_importersByExtensionMap.objectForKey(extension);
	}
	
	public SeleniumTestImporter findImporterByName(String name) {
		return (SeleniumTestImporter)_importersByNameMap.objectForKey(name);
	}
	
	public SeleniumTestExporter findExporterByName(String name) {
		return (SeleniumTestExporter)_exportersByNameMap.objectForKey(name);
	}
}
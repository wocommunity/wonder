package er.extensions.virus;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * Virus scanner that uses ClamAV to check files and streams. Be sure that
 * ClamAV is installed on the system.
 * 
 * @property  er.extensions.virus.ERXClamAvVirusScanner set this property to
 *           the path of the <code>clamscan</code> executable. Defaults to
 *           <code>/usr/bin/clamscan</code>
 * 
 * @author darkv
 */
public class ERXClamAvVirusScanner extends ERXVirusScanner {
	private static final Logger log = LoggerFactory.getLogger(ERXClamAvVirusScanner.class);
	private static final String clamscan = ERXProperties.stringForKeyWithDefault(
			"er.extensions.virus.ERXClamAvVirusScanner", "/usr/bin/clamscan");
	private int status = -1;
	
	public ERXClamAvVirusScanner() {
		super();
	}

	public ERXClamAvVirusScanner(File file) {
		scan(file);
	}

	public ERXClamAvVirusScanner(InputStream inputStream) {
		scan(inputStream);
	}
	
	@Override
	public void scan(File file) {
		status = -1;
		if (file == null || !file.exists()) {
			return;
		}
		try {
			Process process = new ProcessBuilder(clamscan, file.getAbsolutePath()).start();
			status = process.waitFor();

			if (status > 1) {
				log.warn("Unexpected return code from ClamAV");
			}
		} catch (Exception e) {
			log.error("Could not check file {}.", file, e);
		}
	}

	@Override
	public void scan(InputStream inputStream) {
		status = -1;
		if (inputStream == null) {
			return;
		}
		try {
			Process process = new ProcessBuilder(clamscan, "-").start();
			ERXFileUtilities.writeInputStreamToOutputStream(inputStream, process.getOutputStream());
			status = process.waitFor();

			if (status > 1) {
				log.warn("Unexpected return code from ClamAV");
			}
		} catch (Exception e) {
			log.error("Could not scan input stream.", e);
		}
	}

	@Override
	public boolean isOk() {
		if (status == -1) {
			throw new IllegalStateException("No file nor stream was scanned yet!");
		}
		return status == 0;
	}
}

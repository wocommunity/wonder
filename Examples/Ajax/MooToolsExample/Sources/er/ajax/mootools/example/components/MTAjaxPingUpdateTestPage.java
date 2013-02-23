package er.ajax.mootools.example.components;

import java.util.Random;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

public class MTAjaxPingUpdateTestPage extends Main {
	
	private NSMutableArray<String> _randomStrings;
	public String _randomString;
	
    public MTAjaxPingUpdateTestPage(WOContext context) {
        super(context);
        RandomStringThread rst = new RandomStringThread();
        new Thread(rst).start();
    }

    public NSMutableArray<String> randomStrings() {
		if(_randomStrings == null) {
			_randomStrings = new NSMutableArray<String>();
		}
		return _randomStrings;
	}

	public void setRandomStrings(NSMutableArray<String> randomStrings) {
		_randomStrings = randomStrings;
	}

    public class RandomStringThread implements Runnable {

		public void run() {
			while(randomStrings().size() < 20) {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				randomStrings().addObject(_randomString(12));
			}
		}
    	
    }
    
	private String _randomString(int length) {
		StringBuffer buffer = new StringBuffer();
		Random random = new Random();
		char[] chars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 
				'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9'};
		for ( int i = 0; i < length; i++ ) {
			buffer.append(chars[random.nextInt(chars.length)]);
		}
		return buffer.toString();
	}	

}
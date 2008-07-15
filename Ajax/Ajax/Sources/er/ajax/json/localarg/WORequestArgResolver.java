package er.ajax.json.localarg;

import org.jabsorb.localarg.LocalArgResolveException;
import org.jabsorb.localarg.LocalArgResolver;

import com.webobjects.appserver.WORequest;

public class WORequestArgResolver implements LocalArgResolver {
	public Object resolveArg(Object obj) throws LocalArgResolveException {
		if (!(obj instanceof WORequest)) {
			throw new LocalArgResolveException("Invalid context.");
		}
		return obj;
	}

}

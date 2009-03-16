package er.ajax.json.localarg;

import org.jabsorb.localarg.LocalArgResolveException;
import org.jabsorb.localarg.LocalArgResolver;

import com.webobjects.appserver.WOResponse;

public class WOResponseArgResolver implements LocalArgResolver {
	public Object resolveArg(Object obj) throws LocalArgResolveException {
		if (!(obj instanceof WOResponse)) {
			throw new LocalArgResolveException("Invalid context.");
		}
		return obj;
	}

}

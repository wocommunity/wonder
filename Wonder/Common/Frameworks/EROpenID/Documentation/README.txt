===============================================
EROpenID
===============================================

EROpenID framework is based on the OpenID4Java project (http://code.google.com/p/openid4java/) at Google Code.

Connecting OpenID to your application requires a minimal amount of configuration, but a few properties are required.
Please review the Properties.sample file in this directory for an overview of the required and optional 
configuration Properties.

To provide further customization to the OpenID process, you will want to set a custom EROpenIDManager.Delegate onto
EROpenIDManager in your Application constructor.  A DefaultDelegate implementation is provided that you can 
extend as a starting point.  To do this, just call:

  EROpenIDManager.manager().setDelegate(new MyDelegate());

Finally, you should review the EROpenIDExample application in Project Wonder's Common/Examples folder to see an 
open id application in action.
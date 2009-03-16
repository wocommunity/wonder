ERCaptcha sits on top of the JCaptcha ( http://jcaptcha.sourceforge.net/ ) library.  Because it uses AWT classes
for dynamic image generation, you must set -Djava.awt.headless=true on your application in deployment if you 
do not run your applications as root.
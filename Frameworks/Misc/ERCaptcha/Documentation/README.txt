ERCaptcha sits on top of the JCaptcha ( http://jcaptcha.sourceforge.net/ ) library.  Because it uses AWT classes
for dynamic image generation, you must set -Djava.awt.headless=true on your application in deployment if you 
do not run your applications as root.

ERCaptcha は JCaptcha ( http://jcaptcha.sourceforge.net/ ) ライブラリーの上に位置します。
ダイナミック・イメージ生成の為に、 AWT クラスを使用している為アプリケーションを配布時で -Djava.awt.headless=true として実行する必要があります。
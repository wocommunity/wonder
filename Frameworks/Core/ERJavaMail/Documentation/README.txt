Odaiko MailDelivery Framework
-----------------------------


IMPORTANT NOTICE:
- - - - - - - - -

These files are protected under the GNU Lesser Public License.
You should read and understand the terms of this license before using it.
The article 15 in the LICENSE read this:

	  15. BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
	WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE
	LAW.  EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS
	AND/OR OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY 
	OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT 
	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
	FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS TO THE QUALITY AND 
	PERFORMANCE OF THE LIBRARY IS WITH YOU.  SHOULD THE LIBRARY PROVE 
	DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR 
	OR CORRECTION.



WHAT CAN I DO WITH IT ?
- - - - - - - - - - - -

This small framework is intended to send mails with the JavaMail API in conjunction to Apple WebObjects (http://www.apple.com/webobjects).  You can also use it without WebObjects but some (very little) porting is needed.  Two classes can't be used in this context: ERMailDeliveryWOComponentPlainText and ERMailDeliveryHTML.



ADVANTAGES
- - - - - -

This framework merely consists of 5 files that you may add to your project or compile as a separate framework.  Its originality comes from its ability to send mail with a separated thread.  Most of JavaMail functionalities are not hidden so it's easy to write subclasses that extend the use of these classes.

When you need to send multiple mails you don't create multiple instances of ERMailDelivery subclasses, you just create one instance and call the newMail () method to create a new message, this is useful for memory / performances issues.

I created these classes because there was a bug in WOMailDelivery in deployment mode that I cannot solve.  The other advantage is that the mails are sent within the process and JVM of the currently running application.


CONFIGURATION
- - - - - - -

Copy and customize the properties given in SampleConfiguration.txt under 'Support' group in the project files.

er.javamail.centralize = true
Centralize sends all emails to the er.javamail.adminEmail user.

er.javamail.debugEnabled = true
Determines whether or not email debugging is displayed.  This contains protocol-level debug information.

er.javamail.adminEmail = user@domain.com
The email address of the admin user to send centralized emails to.  This is a required property.

er.javamail.smtpHost = smtp.domain.com
The SMTP host name to use.  If this isn't set, mail.smtp.host will be checked and ultimately WOHost will be used.

er.javamail.senderQueue.size = 50
The number of messages that the sender queue can hold. Defaults to 50.

er.javamail.milliSecondsWaitIfSenderOverflowed = 6000
The number of milliseconds to wait if the sender queue is full. Default is 6000.

er.javamail.smtpAuth = true
Sets whether or not Authenticated SMTP is used to send outgoing mail.  If set, er.javamail.smtpUser MUST 
also be set (and preferably er.javamail.smtpPassword).

er.javamail.smtpUser = smtpusername
The username to use to login to the authenticated SMTP server.

er.javamail.smtpPassword = smtppassword
The password to use to login to the authenticated SMTP server.

er.javamail.XMailerHeader = 
The X-Mailer header to put into all outgoing mail messages. Defaults to nothing.

er.javamail.defaultEncoding = UTF-8
The default character encoding to use for message content.  Defaults to ???.

er.javamail.AllowEmailAddressPatterns = 
A comma-separated list of allowed email address patterns.  If set, then only addresses that match one of the allowed 
patterns will delivered to.  Pattern syntax is the same as EOQualifier's caseInsensitiveLike.

er.javamail.DenyEmailAddressPatterns =
A comma-separated list of disallowed email address patterns.  If set, then any email addresses that match a disallowed pattern 
will not be delivered to.  Pattern syntax is the same as EOQualifier's caseInsensitiveLike. The disallowed filter is processed 
last, so a disallowed pattern beats a allowed pattern.

CODE SAMPLE
- - - - - - 

// Create an instance of an ERMailDelivery subclass
ERMailDeliveryHTML mail = new ERMailDeliveryHTML ();

// Here ERMailDeliveryHTML needs a WOComponent to render the HTML text content.
mail.setComponent(mailPage);

// Here you create a new instance of the message
// You can loop over this fragment of code, not forgetting to use newMail ()
// before you set the attributes of the message.
try {
    mail.newMail();
    mail.setFromAddress(emailFrom);
    mail.setReplyToAddress(emailReplyTo);
    mail.setSubject(emailSubject);
    mail.setToAddresses(new NSArray (toEmailAddresses));
    // Send the mail.  There is an optional sendMail(boolean) that optionally blocks during the send.
    mail.sendMail();
} catch (Exception e) {
    // handle the exception ...
}


GOTCHAS
- - - -

Be careful of the WOContext that contains the component you are sending.  If you use
ERMailDeliveryHTML inside of the normal request-response loop with the default WOContext,
it is very likely that the next page that is sent to the user will be the emailed component
rather than the page you WANTED to send.  There are several possible workarounds for this.
One is to return a specific component rather than null from your action method.  I have
had better and more consistent success with the following code:

    WOContext context = (WOContext) context().clone();
    MyComponent component = (MyComponent) WOApplication.application().pageWithName(MyComponent.class.getName(), context);
    ERMailDeliveryHTML mail = new ERMailDeliveryHTML();
    mail.setComponent(component);
    ...

This seems to properly isolate the email to a clone of the current context rather than the
actual active context.  Your mileage may vary :)

CHANGES
- - - -
06/29/2005:
    Added support for authenticated SMTP and updated the documentation.
    
10/09/2002:
    Made API more consistent with Project Wonder.


09/19/2001:

    Packaged the framework into a WebObjects 5 framework.
    As long as ProjectBuilder for Mac OX X and ProjectBuilder for Windows are incompatible,
    the project can't be used under Windows, unless it is compiled under another platform.

    OFMaildelivery.setToAddress now takes an NSArray as argument. java.util.Vector is no
    longer used.


FEEDBACK
- - - - -

Don't hesitate to send {postcards, feedback, love letters} to Camille Troillard <tuscland@mac.com>


============ JAPANESE PART ============

設定定義
- - - - - - 

##　すべてのメールを er.javamail.adminEmail ユーザに送信します。(デバッグ中に便利)
@property er.javamail.centralize = true

##　必須項目："centralize" メールの送り先メール・アドレス
@property er.javamail.adminEmail = user@domain.com

## メール・デバッグ情報を表示するかどうか。プロトコル・レベル情報を含む
## SMTP 設定時に問題がある場合、これを true するといいのです。
@property er.javamail.debugEnabled = false

## SMTP 送信ホスト
## セットされててない場合には WOHost へ送信が行われる
@property er.javamail.smtpHost = smtp.domain.com

## SMTP 送信ポート
## セットされていない場合には 25 が使用されます。
@property # er.javamail.smtpPort = 25

## SMTP 送信に認証が必要かどうかをセットします。
## true の場合には下記のユーザとパスワード情報もセットする必要があります。
@property er.javamail.smtpAuth = true

## SMTP サーバへの認証ログインのユーザ名
@property er.javamail.smtpUser = smtpusername

## SMTP サーバへの認証ログインのパスワード
@property er.javamail.smtpPassword = smtppassword

## 送信キューが保持できるメッセージ・サイズ
## セットされていない場合には 50 が使用されます。
@property er.javamail.senderQueue.size = 50

## すべての送信メッセージに追加する X-Mailer header
## デフォルトは無し
@property # er.javamail.mailer.XMailerHeader = 

## メッセージ・コンテントに使用するデフォルト・エンコーディング
@property er.javamail.defaultEncoding = UTF-8

## 送信キューが一杯時に待つミリ秒。デフォルトは 6000
@property er.javamail.milliSecondsWaitIfSenderOverflowed = 6000

## 検証に使用する ERJavaMailメール・パタン。 null の場合には内部のパタンが使用される
@property # er.javamail.emailPattern = ^.*?@.*$

## ホワイト＆ブラック・リスト・メールアドレス・パタン
## 例えば、テスト中で@mycompany.com にしかメールを送信したい場合
## コンマ区切りされているメールアドレス・パタンのホワイト・リスト
## セットされている場合にはホワイト・リストに含まれるメールしか送信されません。
## パタン・シンタクスは EOQualifier caseInsensitiveLike 同様
## この場合には次の様に追加します：
@property # er.javamail.AllowEmailAddressPatterns=("*@mycompany.com", "somebody@mac.com")

## 送信したくないアドレスがある場合にはブラック・リストを使用できます。
## コンマ区切りされているメールアドレス・パタンのブラック・リスト
## セットされているアドレスへはメール送信されません。
## パタン・シンタクスは EOQualifier caseInsensitiveLike 同様
## ブラック・リスト・パタンは最後に処理されるので、ホワイト・リストよりも優先です。
@property # er.javamail.DenyEmailAddressPatterns=("*@baddomain.com", "badperson@mycompany.com")

## SMTP 又は SMTPS を使用するかどうか
## 基本的にはここで指定する必要がありません。 
@property # er.javamail.smtpProtocol
@property # mail.smtps.socketFactory.fallback

コード・サンプル
- - - - - - 

// ERMailDelivery クラスのインスタンス化
ERMailDeliveryHTML mail = new ERMailDeliveryHTML ();

// ERMailDeliveryHTML はレンダリングのために WOComponent を使用します
mail.setComponent(mailPage);

// メッセージの新インスタンスを作成します。
// メッセージのアトリビュートをセットする前に newMail() を忘れず
try {
	mail.newMail();
	mail.setFromAddress(emailFrom);
	mail.setReplyToAddress(emailReplyTo);
	mail.setSubject(emailSubject);
	mail.setToAddresses(new NSArray (toEmailAddresses));
	// Send the mail.  There is an optional sendMail(boolean) that optionally blocks during the send.
	mail.sendMail();
} catch (Exception e) {
	// handle the exception ...
}
 

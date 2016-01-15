package er.extensions.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * <div class="ja">
 * 	ネットワーク関連のツール類
 * 	特に TCP/IP
 * </div>
 */
public class ERXTcpIp {

	private static final Logger log = LoggerFactory.getLogger(ERXTcpIp.class);

	//********************************************************************
	//	プロパティー
	//********************************************************************

	/**
	 * <div class="ja">Internet Protocol バージョン 4 (IPv4) アドレスエラー値 -&gt; -1</div>
	 */
	public static final long INET4_IPADDRESS_ERROR_LONG	= -1;

	/**
	 * <div class="ja">
	 * 	Internet Protocol バージョン 4 (IPv4) アドレスを数字(long)にしたときの最小値
	 * 	0.0.0.0 = 0*256*256*256 + 0*256*256 + 0*256 + 0 = 0
	 * </div>
	 */
	public static final long INET4_IPADDRESS_MINIMUM_LONG = 0;

	/**
	 * <div class="ja">
	 * 	Internet Protocol バージョン 4 (IPv4) アドレスを数字(long)にしたときの最大値
	 * 	255.255.255.255 = 255*256*256*256 + 255*256*256 + 255*256 + 255 = 4294967295
	 * </div>
	 */
	public static final long INET4_IPADDRESS_MAXIMUM_LONG = 255L*256L*256L*256L + 255L*256L*256L + 255L*256L + 255L;

	/**
	 * <div class="ja">
	 * 	Internet Protocol バージョン 4 (IPv4) アドレス正規表現パターン
	 * 「0.0.0.0」
	 *  "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}"
	 * </div>
	 */
	public static final String	INET4_IPADDRESS_PATTERN_STR	= "([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})";
	public static final Pattern	INET4_IPADDRESS_PATTERN	= Pattern.compile(INET4_IPADDRESS_PATTERN_STR);

	/** 
	 * <div class="ja">INET : ローカル・アドレス</div>
	 */
	public static final String LOCAL_IP_ADDRESS = "127.0.0.1";

	/**
	 * <div class="ja">ET定数:. (ドット) </div>
	 */
	private static final String	_DOT = ".";

	/**
	 * <div class="ja">
	 * 	特殊記号やコントロールコードなど基本的な定数
	 *	『 _ 』：_ アンダーバー
	 * </div>
	 */
	public static final String UNDER_BAR = "_";

	/** 
	 * <div class="ja">NET定数:"000" IPアドレス数字フォーマット </div>
	 */
	private static final DecimalFormat _3DIGIT_FORMAT = new DecimalFormat("000");

	//********************************************************************
	//	Methods
	//********************************************************************

	/**
	 * <div class="ja">
	 * 	Internet Protocol バージョン 4 (IPv4) アドレス(文字列)をフォーマット。
	 *  入力値の各IP値(0〜255)の桁数を0で埋めた3文字にフォーマットする。
	 *　　入力値: 0.0.0.0
	 *　　出力値: 000.000.000.000
	 *
	 * 	@param aIPstr - IPアドレスの文字列形式は「0.0.0.0」〜「255.255.255.255」のみ。
	 * 
	 *  @return Stringフォーマット後の文字列。 null:IPアドレスが正しくないとき
	 * </div>
	 */
	public static String inet4IpAddressTo3digitFromat(String aIPstr){
		if(inet4IpAddressToLong(aIPstr) == INET4_IPADDRESS_ERROR_LONG) return null;
		String ipStr = null;

		if(ERXStringUtilities.stringIsNullOrEmpty(aIPstr))
			return ipStr;

		StringTokenizer tempStringTokenizer = new StringTokenizer(aIPstr,_DOT);
		long countToken = tempStringTokenizer.countTokens();
		if( countToken == 4 ){	// 4 digit時処理
			try{
				long ip1 = Long.parseLong(tempStringTokenizer.nextToken());
				long ip2 = Long.parseLong(tempStringTokenizer.nextToken());
				long ip3 = Long.parseLong(tempStringTokenizer.nextToken());
				long ip4 = Long.parseLong(tempStringTokenizer.nextToken());
				ipStr =		_3DIGIT_FORMAT.format(ip1) + _DOT
						+	_3DIGIT_FORMAT.format(ip2) + _DOT
						+	_3DIGIT_FORMAT.format(ip3) + _DOT
						+	_3DIGIT_FORMAT.format(ip4);
			} catch ( Exception e ){
				// 「null」値を返す
			}
		}
		return ipStr;
	}

	/**
	 * <div class="ja">
	 * 	Internet Protocol バージョン 4 (IPv4) アドレス(文字列)を数字(long)に変換。
	 *   各桁が「0〜255」の範囲外なら-1:IPアドレスの指定ミスを返す。
	 *
	 * 	@param aIPstr - IPアドレスの文字列形式は「0.0.0.0」〜「255.255.255.255」のみ。
	 * 
	 * 	@return long 変換後のIPアドレス数値。-1:IPアドレスの指定ミス。
	 * </div>
	 */
	public static long inet4IpAddressToLong(String aIPstr){
		long ip = INET4_IPADDRESS_ERROR_LONG;
		if(ERXStringUtilities.stringIsNullOrEmpty(aIPstr))
			return ip;

		StringTokenizer tempStringTokenizer = new StringTokenizer(aIPstr,_DOT);
		long countToken = tempStringTokenizer.countTokens();
		if( countToken == 4 ){	// 4 digit時処理
			try{
				long ip1 = Long.parseLong(tempStringTokenizer.nextToken());
				if((ip1 >= 0) && (ip1 <= 255)){
					long ip2 = Long.parseLong(tempStringTokenizer.nextToken());
					if((ip2 >= 0) && (ip2 <= 255)){
						long ip3 = Long.parseLong(tempStringTokenizer.nextToken());
						if((ip3 >= 0) && (ip3 <= 255)){
							long ip4 = Long.parseLong(tempStringTokenizer.nextToken());
							if((ip4 >= 0) && (ip4 <= 255)){
								ip = (ip1 * 256 * 256 * 256) + (ip2 * 256 * 256) +(ip3 * 256) +(ip4) ;
							}
						}
					}
				}
			} catch ( Exception e ){
				// 「INET4_IPADDRESS_ERROR_LONG」値を返す
			}
		}
		return (ip <= INET4_IPADDRESS_MAXIMUM_LONG) ? ip: INET4_IPADDRESS_ERROR_LONG;
	}

	/**
	 * <div class="ja">
	 * 	指定したInternet Protocol バージョン 4 (IPv4) アドレス(long)が範囲内かの判断。
	 *  判断ロジック
	 *   ((INET4_IPADDRESS_MINIMUM_LONG &lt;= ipl) &amp;&amp; (ipl &lt;= INET4_IPADDRESS_MAXIIMUM_LONG))
	 *
	 * 	@param ipl - IPアドレスの数値。
	 * 
	 * 	@return boolean	true:範囲内のIPアドレス
	 * </div>
	 */
	public static boolean isInet4IPAddressRange( long ipl ){
		return ((INET4_IPADDRESS_MINIMUM_LONG <= ipl) && (ipl <= INET4_IPADDRESS_MAXIMUM_LONG));
	}

	/**
	 * <div class="ja">
	 * 	指定したInternet Protocol バージョン 4 (IPv4) アドレス(文字列)が範囲内かの判断。
	 *  判断ロジック
	 *   ((INET4_IPADDRESS_MINIMUM_LONG &lt;= ipl) &amp;&amp; (ipl &lt;= INET4_IPADDRESS_MAXIIMUM_LONG))
	 *
	 * 	@param ipStr - IPアドレスの文字列。「0.0.0.0」〜「255.255.255.255」のみ。
	 * 	@return boolean true:範囲内のIPアドレス
	 * </div>
	 */
	public static boolean isInet4IPAddressRange( String ipStr ){
		return isInet4IPAddressRange(inet4IpAddressToLong(ipStr));
	}

	/**
	 * <div class="ja">
	 * 	指定したInternet Protocol バージョン 4 (IPv4) アドレス(long)が指定した範囲内かの判断。
	 *  判断ロジック
	 *   次の場合には直ぐに範囲外
	 *　　　ipStartが範囲外 or ipが範囲外 or ipが範囲外
	 *   3つの指定IPが範囲内の時に次の判断を実施
	 *   ((ipStart &lt;= ip) &amp;&amp; (ip &lt;= ipEnd))
	 *
	 * 	@param ipStart - IPアドレス-開始。
	 * 	@param ip - IPアドレス-比較。
	 * 	@param ipEnd - IPアドレス-終了。
	 * 
	 * 	@return boolean	true:範囲内のIPアドレス
	 * </div>
	 */
	public static boolean isInet4IPAddressWithinRange( long ipStart, long ip, long ipEnd ){
		if(!isInet4IPAddressRange(ipStart) || !isInet4IPAddressRange(ip) || !isInet4IPAddressRange(ip)) return false;
		return ((ipStart <= ip) && (ip <= ipEnd));
	}

	/**
	 * <div class="ja">
	 * 	指定したInternet Protocol バージョン 4 (IPv4) アドレス(文字列)が指定した範囲内かの判断。
	 *  判断ロジック
	 *   次の場合には直ぐに範囲外
	 *　　　ipStrStartが範囲外 or ipStrが範囲外 or ipEndが範囲外
	 *   3つの指定IPが範囲内の時に次の判断を実施
	 *   ((ipStrStart &lt;= ipStr) &amp;&amp; (ipStr &lt;= ipStrEnd))
	 *
	 * @param ipStrStart -	IPアドレスの文字列-開始。「0.0.0.0」〜「255.255.255.255」のみ。
	 * @param ipStr - IPアドレスの文字列-比較。「0.0.0.0」〜「255.255.255.255」のみ。
	 * @param ipStrEnd - IPアドレスの文字列-終了。「0.0.0.0」〜「255.255.255.255」のみ。
	 * 
	 * @return boolean true:範囲内のIPアドレス
	 * </div>
	 */
	public static boolean isInet4IPAddressWithinRange( String ipStrStart, String ipStr, String ipStrEnd ){
		long ipStart	= inet4IpAddressToLong(ipStrStart);
		long ip			= inet4IpAddressToLong(ipStr);
		long ipEnd		= inet4IpAddressToLong(ipStrEnd);
		return isInet4IPAddressWithinRange(ipStart, ip, ipEnd);
	}

	/**
	 * <div class="ja">
	 * 	マシンで設定されているIPリストを取得します。
	 * 	環境設定については： A10Properties. InIpRangeOperatorを参照
	 * 
	 * 	@return IP 配列が戻ります
	 * </div>
	 */
	public static NSArray<String> machineIpList() {

		// この関数は WOApplication がインスタンス化される前に実行されるので、ログ出力は標準ではない
		if(ERXArrayUtilities.arrayIsNullOrEmpty(_machineIpList)) {
			// プロパティー内でどの IP を使用するかどうかを読込みます。
			String machineIp = ERXProperties.stringFor2Keys("er.erxtensions.ERXTcpIp.UseThisIp", "wodka.a10.A10TcpIp.UseThisIp");

			try {
				// プロパティーがなければ、自動設定を行う
				if(ERXStringUtilities.stringIsNullOrEmpty(machineIp)){
					log.debug("MachineIp Automatic Mode");

					// マシンIPを得る
					if(ERXArrayUtilities.arrayIsNullOrEmpty(_machineIpList))
						_machineIpList = _machineIpList();

					// 処理不能 ??
					if(ERXArrayUtilities.arrayIsNullOrEmpty(_machineIpList)) {
						String noIpAndNoNetwork = ERXProperties.stringFor2Keys("er.erxtensions.ERXTcpIp.NoIpAndNoNetwork", "wodka.a10.A10TcpIp.NoIpAndNoNetwork");

						// No IP と No ネットワークも設定されていなければ、ローカル IP を使用する
						if(ERXStringUtilities.stringIsNullOrEmpty(noIpAndNoNetwork))
							noIpAndNoNetwork = LOCAL_IP_ADDRESS;

						// 使用する IP をセットします
						_machineIpList = new NSArray<String>( new String[] {noIpAndNoNetwork});

						log.warn("No IpAddress --- no network! use Address : {}", noIpAndNoNetwork);
					}
				} else {
					// 使用する IP をセットします
					_machineIpList = new NSArray<String>( new String[] {machineIp});
				}
			} catch (Exception e) {
				// ここでの処理失敗は致命的
				log.error( "getIpAddress error!!!" );

				_machineIpList = new NSArray<String>( new String[] {LOCAL_IP_ADDRESS});
			}

			if(log.isInfoEnabled())
				log.info( "MachineIp {} is in use.", ERXArrayUtilities.arrayToLogstring(_machineIpList));
		}

		return _machineIpList;
	}

	/** 
	 * <div class="ja">動作マシンが所有しているIPアドレス保持用変数：キャシュ用</div>
	 */
	private static NSArray<String> _machineIpList = NSArray.EmptyArray;

	/**
	 * <div class="ja">
	 *  ネットワークインアーフェースからIP一覧を得る
	 *  
	 * 	@return 動作マシンのネットワーク設定が所有しているIPアドレス配列
	 * 
	 * 	@exception Exception
	 * </div>
	 */
	private static NSArray<String> _machineIpList() throws Exception {
		// ワーク用
		NSMutableArray<String> workArray = new NSMutableArray<String>();

		// 全ネットワーク・インタフェース
		Enumeration<NetworkInterface> enNi = NetworkInterface.getNetworkInterfaces();
		while( enNi.hasMoreElements() ){
			NetworkInterface ni = enNi.nextElement();

			// 全 InetAddress
			Enumeration<InetAddress> enIp = ni.getInetAddresses();
			while( enIp.hasMoreElements() ){
				String ip = enIp.nextElement().getHostAddress();

				// IP アドレスとプロパティー内に保存されている優先順を合体する
				if( ( ip.indexOf( ":" ) < 0 )&&( !ip.equals( LOCAL_IP_ADDRESS ) )){

					String ipPri = ERXProperties.stringFor2Keys("er.erxtensions.ERXTcpIp.IpPriority" + ip, "wodka.a10.A10TcpIp.IpPriority" + ip);
					if(ERXStringUtilities.stringIsNullOrEmpty(ipPri)) {
						ipPri = "9999";
					} else {
						ipPri = ERXStringUtilities.trimZeroInFrontOfNumbers(ipPri);

						while( ipPri.length() < 4 ){ 
							ipPri = "0" + ipPri;
						}
					}

					workArray.addObject(ipPri + UNDER_BAR + ip);
				}
			}
		}

		// 優先順位順に並べ、IPのみ抜き出してArrayListにする
		workArray.sortUsingComparator(NSComparator.AscendingStringComparator);

		// 戻す配列の準備
		NSMutableArray<String> resultArray = new NSMutableArray<String>(workArray.count());

		for(String obj : workArray) {
			resultArray.addObject(obj.substring(5));
		}

		return resultArray.immutableClone();
	}

	/**
	 * <div class="ja">
	 * 	ドメイン又は ip リストを全件表示するドメイン又は ip リストに変換します
	 * 
	 * 	例：
	 * 	www.ksroom.com はそのままで戻る
	 * 	1.2.3.4 はそのままで戻る
	 * 	1.2.3.4-6 は (1.2.3.4, 1.2.3.5, 1.2.3.6) として戻る
	 * 
	 * 	@param data - NSArray&lt;String&gt; ドメイン又は ip リスト
	 * 
	 * 	@return 全件のドメイン又は ip リスト
	 * </div>
	 */
	public static NSArray<String> fullDomainIpList(NSArray<String> data) {
		NSMutableArray<String> results = new NSMutableArray<String>(data.count());
		for (String string : data) {
			if(ERXStringUtilities.stringIsNullOrEmpty(string)) {
				continue;
			}

			// 文字で始まるドメイン
			char firstLetter = string.charAt(0);  
			if(!StringUtils.isNumeric("" + firstLetter)) {
				results.addObject(string);

				// IP を調査し追加すること
				InetAddress addr;
				try {
					addr = InetAddress.getByName(string);
					if(addr != null)
						results.addObject(addr.getHostAddress());
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				}
				continue;
			} 

			// 数字で始まる普通のドメイン又は、IPアドレス
			if(!string.contains("-")) {
				results.addObject(string);
				continue;       
			}

			// 数字で始まる又は - を持つドメイン
			NSArray<String> bubuns = NSArray.componentsSeparatedByString(string, "-");
			if(bubuns.count() != 2) {
				// 複数の "-" がある
				results.addObject(string);
				continue;       
			}

			String startIP = ERXTcpIp.inet4IpAddressTo3digitFromat(bubuns.objectAtIndex(0));

			// 正しい IP ?
			if(ERXStringUtilities.stringIsNullOrEmpty(startIP)) {
				results.addObject(string);
				continue;          
			}

			// IP 追加処理
			long endIp = ERXValueUtilities.longValue(bubuns.objectAtIndex(1));

			StringTokenizer tempStringTokenizer = new StringTokenizer(startIP, ".");
			long countToken = tempStringTokenizer.countTokens();
			if(countToken == 4){  // 4 digit時処理
				try{
					long ip1 = Long.parseLong(tempStringTokenizer.nextToken());
					long ip2 = Long.parseLong(tempStringTokenizer.nextToken());
					long ip3 = Long.parseLong(tempStringTokenizer.nextToken());
					long ip4 = Long.parseLong(tempStringTokenizer.nextToken());

					for(long i = ip4; i <= endIp; i++) {
						String ipStr = "" + ip1 + _DOT + ip2 + _DOT +ip3 + _DOT + i;          
						results.addObject(ipStr);
					}
				} catch ( Exception e ){

				}
			}
		}
		return results.immutableClone();
	}
}

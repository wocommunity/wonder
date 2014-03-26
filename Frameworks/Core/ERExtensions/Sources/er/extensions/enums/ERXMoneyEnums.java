package er.extensions.enums;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.formatters.ERXDecimalFormatSymbols;

/**
 * http://en.wikipedia.org/wiki/ISO_4217
 * 
 * er.extensions.enums.ERXMoneyEnums
 */
public enum ERXMoneyEnums {

	/* A */
	AED("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "United Arab Emirates Dirham"), // TODO
	AFN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Afghan Afghani"), // TODO
	ALL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Albanian Lek"), // TODO
	AMD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Armenian Dram"), // TODO
	ANG("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Netherlands Antillean Guilder"), // TODO
	AOA("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Angolan Kwanza"), // TODO
	ARS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Argentine Peso"), // TODO
	AUD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Australian Dollar"), // TODO
	AWG("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Aruban Florin"), // TODO
	AZN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Azerbaijani Manat"), // TODO

	/* B */
	BAM("marka", "fening", "KM", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bosnia-Herzegovina Convertible Mark"),
	BBD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Barbadian Dollar"), // TODO
	BDT("taka", "poisha", "৳", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bangladeshi Taka"),
	BGN("lev", "stotinki", "лв", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bulgarian Lev"),
	BHD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bahraini Dinar"), // TODO
	BIF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Burundian Franc"), // TODO
	BMD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bermudan Dollar"), // TODO
	BND("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Brunei Dollar"), // TODO
	BOB("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bolivian Boliviano"), // TODO
	BRL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Brazilian Real"), // TODO
	BSD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bahamian Dollar"), // TODO
	BTN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Bhutanese Ngultrum"), // TODO
	BWP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Botswanan Pula"), // TODO
	BYR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Belarusian Ruble"), // TODO
	BZD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Belize Dollar"), // TODO

	/* C */
	CAD("dollar", "cent", "＄", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, 100, "Canadian Dollar"),
	CDF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Congolese Franc"), // TODO
	CHF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Swiss Franc"), // TODO
	CLF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Chilean Unit of Account (UF)"), // TODO
	CLP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Chilean Peso"), // TODO
	CNY("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Chinese Yuan"), // TODO
	COP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Colombian Peso"), // TODO
	CRC("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Costa Rican Colón"), // TODO
	CUP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Cuban Peso"), // TODO
	CVE("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Cape Verdean Escudo"), // TODO
	CZK("koruna", "haléř", "Kč", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Czech Republic Koruna"),

	/* D */
	DJF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Djiboutian Franc"), // TODO
	DKK("krone", "øre", "kr", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Danish Krone"),
	DOP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Dominican Peso"), // TODO
	DZD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Algerian Dinar"), // TODO

	/* E */
	EGP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Egyptian Pound"), // TODO
	ETB("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Ethiopian Birr"), // TODO
	EUR("euro", "cent", "€", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Euro"),

	/* F */
	FJD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Fijian Dollar"), // TODO
	FKP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Falkland Islands Pound"), // TODO

	/* G */
	GBP("pound", "penny", "£", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "British Pound Sterling"),
	GEL("lari", "tetri", "ლარი", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, 100, "Georgian Lari"),
	GHS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Ghanaian Cedi"), // TODO
	GIP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Gibraltar Pound"), // TODO
	GMD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Gambian Dalasi"), // TODO
	GNF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Guinean Franc"), // TODO
	GTQ("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Guatemalan Quetzal"), // TODO
	GYD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Guyanaese Dollar"), // TODO

	/* H */
	HKD("dollar", "cent", "$", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Hong Kong Dollar"),
	HNL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Honduran Lempira"), // TODO
	HRK("kuna", "lipa", "kn", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Croatian Kuna"),
	HTG("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Haitian Gourde"), // TODO
	HUF("forint", "fillér", "Ft", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Hungarian Forint"),

	/* I */
	IDR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Indonesian Rupiah"), // TODO
	ILS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Israeli New Sheqel"), // TODO
	INR("rupee", "paisa", "₹", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Indian Rupee"),
	IQD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Iraqi Dinar"), // TODO
	IRR("rial", "", "﷼", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, 1, "Iranian Rial"),
	ISK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Icelandic Króna"), // TODO

	/* J */
	JMD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Jamaican Dollar"), // TODO
	JOD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Jordanian Dinar"), // TODO
	JPY("yen", "", "￥", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, 1, "Japanese Yen"),

	/* K */
	KES("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Kenyan Shilling"), // TODO
	KGS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Kyrgystani Som"), // TODO
	KHR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Cambodian Riel"), // TODO
	KMF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Comorian Franc"), // TODO
	KPW("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "North Korean Won"), // TODO
	KRW("won", "", "₩", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 1, "South Korean Won"),
	KWD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Kuwaiti Dinar"), // TODO
	KZT("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Kazakhstani Tenge"), // TODO

	/* L */
	LAK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Laotian Kip"), // TODO
	LBP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Lebanese Pound"), // TODO
	LKR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Sri Lankan Rupee"), // TODO
	LRD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Liberian Dollar"), // TODO
	LSL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Lesotho Loti"), // TODO
	LTL("litas", "centas", "Lt", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Lithuanian Litas"),
	LVL("lats", "santīms", "Ls", "s", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Latvian Lats"),
	LYD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Libyan Dinar"), // TODO

	/* M */
	MAD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Moroccan Dirham"), // TODO
	MDL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Moldovan Leu"), // TODO
	MGA("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Malagasy Ariary"), // TODO
	MKD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Macedonian Denar"), // TODO
	MMK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Myanma Kyat"), // TODO
	MNT("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Mongolian Tugrik"), // TODO
	MOP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Macanese Pataca"), // TODO
	MRO("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Mauritanian Ouguiya"), // TODO
	MUR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Mauritian Rupee"), // TODO
	MVR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Maldivian Rufiyaa"), // TODO
	MWK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Malawian Kwacha"), // TODO
	MXN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Mexican Peso"), // TODO
	MYR("ringgit", "sen", "RM", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Malaysian Ringgit"),
	MZN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Mozambican Metical"), // TODO

	/* N */
	NAD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Namibian Dollar"), // TODO
	NGN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Nigerian Naira"), // TODO
	NIO("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Nicaraguan Córdoba"), // TODO
	NOK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Norwegian Krone"), // TODO
	NPR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Nepalese Rupee"), // TODO
	NZD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "New Zealand Dollar"), // TODO

	/* O */
	OMR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Omani Rial"), // TODO

	/* P */
	PAB("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Panamanian Balboa"), // TODO
	PEN("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Peruvian Nuevo Sol"), // TODO
	PGK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Papua New Guinean Kina"), // TODO
	PHP("peso", "sentimo", "₱", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Philippine Peso"),
	PKR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Pakistani Rupee"), // TODO
	PLN("złoty", "grosz", "zł", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Polish Zloty"),
	PYG("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Paraguayan Guarani"), // TODO

	/* Q */
	QAR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Qatari Rial"), // TODO

	/* R */
	RON("lei", "bani", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Romanian Leu"),
	RSD("dinar", "para", "РСД", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Serbian Dinar"),
	RUB("ruble", "kopek", "руб", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Russian Ruble"),
	RWF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Rwandan Franc"), // TODO

	/* S */
	SAR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Saudi Riyal"), // TODO
	SBD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Solomon Islands Dollar"), // TODO
	SCR("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Seychellois Rupee"), // TODO
	SDG("pound", "qirush", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Sudanese Pound"),
	SEK("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Swedish Krona"), // TODO
	SGD("dollar", "cent", "S$", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Singapore Dollar"),
	SHP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Saint Helena Pound"), // TODO
	SLL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Sierra Leonean Leone"), // TODO
	SOS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Somali Shilling"), // TODO
	SRD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Surinamese Dollar"), // TODO
	STD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "São Tomé and Príncipe Dobra"), // TODO
	SVC("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Salvadoran Colón"), // TODO
	SYP("pound", "piastre", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Syrian Pound"),
	SZL("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Swazi Lilangeni"), // TODO

	/* T */
	THB("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Thai Baht"), // TODO
	TJS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Tajikistani Somoni"), // TODO
	TMT("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Turkmenistani Manat"), // TODO
	TND("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Tunisian Dinar"), // TODO
	TOP("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Tongan Paʻanga"), // TODO
	TRY("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Turkish Lira"), // TODO
	TTD("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Trinidad and Tobago Dollar"), // TODO
	TWD("dollars", "cents", "＄", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, 100, "New Taiwan Dollar"),
	TZS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Tanzanian Shilling"), // TODO

	/* U */
	UAH("hryvnia", "kopiyka", "₴", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Ukrainian Hryvnia"),
	UGX("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Ugandan Shilling"), // TODO
	USD("dollar", "cent", "$", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "United States Dollar"),
	UYU("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Uruguayan Peso"), // TODO
	UZS("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Uzbekistan Som"), // TODO

	/* V */
	VEF("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Venezuelan Bolívar"), // TODO
	VND("dong", "hào", "₫", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Vietnamese Dong"),
	VUV("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Vanuatu Vatu"), // TODO

	/* W */
	WST("", "", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Samoan Tala"), // TODO

	/* X */
	// Cameroon,  Central African Republic,  Republic of the Congo,  Chad,  Equatorial Guinea,  Gabon
	XAF("franc", "centime", "FCFA", "c", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "CFA Franc BEAC"),
	// Anguilla,  Antigua and Barbuda,  Dominica,  Grenada,  Montserrat,  Saint Kitts and Nevis,  Saint Lucia,  Saint Vincent and the Grenadines
	XCD("dollar", "cent", "$", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "East Caribbean Dollar"),
	// Benin, Burkina Faso, Côte d'Ivoire, Guinea-Bissau, Mali, Niger, Senegal, Togo
	XOF("franc", "centime", "CFA", "c", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "CFA Franc BCEAO"),
	//  French Polynesia, New Caledonia, Wallis and Futuna
	XPF("franc", "centime", "F", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "CFP Franc"),

	/* Y */
	YER("rial", "fils", "", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Yemeni Rial"),
	@Deprecated /* use JPY*/
	YEN("yen", "", "￥", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, 1, "Japanese yen"),

	/* Z */
	ZAR("rand", "cent", "R", "c", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "South African Rand"),
	ZMK("kwacha", "ngwee", "ZK", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Zambian Kwacha"),
	ZWL("dollar", "cent", "$", "", ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA, ERXDecimalFormatSymbols.MONEY_SEPERATOR_DOT, 100, "Zimbabwean Dollar");

	//********************************************************************
	//  Constructor
	//********************************************************************

	ERXMoneyEnums(
			String unit_Name,
			String cent_Name, 
			String prefix_Symbol, 
			String suffix_Symbol, 
			char decimal_Point, 
			char group_Separator, 
			int scaleValue,
			String longUnitName) {
		this.unitName = unit_Name;
		this.centName = cent_Name;
		this.longname = longUnitName;
		this.prefixSymbol = prefix_Symbol;
		this.suffixSymbol = suffix_Symbol;
		this.decimal_point = decimal_Point;
		this.group_separator = group_Separator;
		this.scale = scaleValue;
		formatter = formatterCreator();
		simpleFormatter = simpleFormatterCreator();
	} 

	/** 
	 * Full Name for Localize
	 * 
	 * @return Full Name for Localize
	 */
	public String fullName() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(NSKeyValueCodingAdditions.KeyPathSeparator);
		sb.append(name());
		return sb.toString();
	}

	//********************************************************************
	//  Methods
	//********************************************************************

	public String longname() {
		return longname;
	}
	private final String longname;

	public String unitName() {
		return unitName;
	}
	private final String unitName;

	public String centName() {
		return centName;
	}
	private final String centName;

	public String prefixSymbol() {
		return prefixSymbol;
	}
	private final String prefixSymbol;

	public String suffixSymbol() {
		return suffixSymbol;
	}
	private final String suffixSymbol;

	public String decimal_point() {
		return String.valueOf(decimal_point);
	}
	private final char decimal_point; 

	public String group_separator() {
		return String.valueOf(group_separator);
	}
	private final char group_separator;

	public int scale() {
		return scale;
	}
	private final int scale;

	public DecimalFormat formatter() {
		return formatter;
	}
	private final DecimalFormat formatter;

	public DecimalFormat simpleFormatter() {
		return simpleFormatter;
	}
	private final DecimalFormat simpleFormatter;

	//********************************************************************
	//  Private Classes
	//********************************************************************

	private DecimalFormat simpleFormatterCreator() {
		String fms = creator();
		DecimalFormat formater = new DecimalFormat(fms);  

		DecimalFormatSymbols dfs = ERXDecimalFormatSymbols.decimalFormatSymbols(decimal_point());
		formater.setDecimalFormatSymbols(dfs);

		int i = log10(scale());
		formater.setMinimumFractionDigits(i);
		formater.setMaximumFractionDigits(i);

		return formater;
	}

	private DecimalFormat formatterCreator() {
		String fms = creator();
		DecimalFormat formater = new DecimalFormat(prefixSymbol() + fms + suffixSymbol());  

		DecimalFormatSymbols dfs = ERXDecimalFormatSymbols.decimalFormatSymbols(decimal_point());
		formater.setDecimalFormatSymbols(dfs);

		int i = log10(scale());
		formater.setMinimumFractionDigits(i);
		formater.setMaximumFractionDigits(i);

		return formater;
	}

	private String creator() {
		long whole = 99999999999990l;
		long divisors[] = { 1, 1000, 1000000, (long)1E9, (long)1E12,(long)1E15, (long)1E18};
		int group_no  = log10(whole) / 3;
		int group_val = (int)(whole / divisors[group_no]);

		String fms = "" + group_val; // Append leftmost 3-digits
		while (group_no > 0) { // For each remaining 3-digit group
			fms = fms + ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA; // Insert punctuation   
			whole -= group_val * divisors[group_no--]; // Compute new remainder
			group_val = (short)(whole/divisors[group_no]); // Get next 3-digit value
			if (group_val < 100)
				fms = fms + "0"; // Insert embedded 0's
			if (group_val <  10)
				fms = fms + "0"; //   as needed
			fms = fms + group_val;  // Append group value
		}
		return fms.replace("9", "#");
	}

	private static short log10(long x) {
		short result; // of decimal digits in an integer
		for (result=0; x>=10; result++, x/=10); // Decimal "shift" and count
		return result;
	}
}

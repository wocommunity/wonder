package your.app.ws.impl;

import javax.jws.WebService;

import your.app.ws.Calculator;

@WebService (endpointInterface = "your.app.ws.Calculator",
		targetNamespace = "http://ws.app.your/")

public class CalculatorImpl implements Calculator {

	@Override
	public int add(int arg0, int arg1) {
		return arg0 + arg1;
	}

}

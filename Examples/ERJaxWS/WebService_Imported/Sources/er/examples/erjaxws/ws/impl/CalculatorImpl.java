package er.examples.erjaxws.ws.impl;

import javax.jws.WebService;

import er.examples.erjaxws.ws.Calculator;

@WebService (endpointInterface = "er.examples.erjaxws.ws.Calculator",
		targetNamespace = "http://ws.erjaxws.examples.er/")

public class CalculatorImpl implements Calculator {

	@Override
	public int add(int arg0, int arg1) {
		return arg0 + arg1;
	}

}

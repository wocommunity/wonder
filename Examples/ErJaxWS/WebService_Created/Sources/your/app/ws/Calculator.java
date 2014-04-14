package your.app.ws;

import javax.jws.WebService;

@WebService

public class Calculator {

	public int add(int a, int b)
	{
		return a + b;
	}
}

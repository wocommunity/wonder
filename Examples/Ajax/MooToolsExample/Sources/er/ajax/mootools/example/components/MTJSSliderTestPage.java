package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOContext;

public class MTJSSliderTestPage extends Main {
    private int sliderValue;

	public MTJSSliderTestPage(WOContext context) {
        super(context);
    }

	/**
	 * @return the sliderValue
	 */
	public int sliderValue() {
		return sliderValue;
	}

	/**
	 * @param sliderValue the sliderValue to set
	 */
	public void setSliderValue(int sliderValue) {
		this.sliderValue = sliderValue;
	}
}
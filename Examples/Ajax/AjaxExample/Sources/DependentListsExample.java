import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class DependentListsExample extends WOComponent {
	private NSMutableArray<State> _states;
	public State _repetitionState;
	public County _repetitionCounty;
	public Street _repetitionStreet;

	private State _selectedState;
	private County _selectedCounty;
	private Street _selectedStreet;

	private State _selectedState2;
	private County _selectedCounty2;

	public String _address;
	
	
	public DependentListsExample(WOContext context) {
		super(context);
		_states = new NSMutableArray<State>();
		for (int stateNum = 0; stateNum < 30; stateNum++) {
			State state = new State("State " + stateNum);
			for (int countyNum = 0; countyNum < 30; countyNum++) {
				County county = new County(state, "State " + state.name() + " - County " + countyNum);
				for (int streetNum = 0; streetNum < 30; streetNum++) {
					Street street = new Street(county, "State " + state.name() + " - County " + countyNum + " - Street " + streetNum);
					county.addStreet(street);
				}
				state.addCounty(county);
			}
			_states.addObject(state);
		}
	}
	
	@Override
	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		super.takeValuesFromRequest(aRequest, aContext);
	}
	
	public void setSelectedCounty2(County selectedCounty2) {
		_selectedCounty2 = selectedCounty2;
	}
	
	public County selectedCounty2() {
		return _selectedCounty2;
	}
	
	public void setSelectedState2(State selectedState2) {
		if (_selectedState2 != selectedState2) {
			_selectedState2 = selectedState2;
			_selectedCounty2 = null;
		}
	}
	
	public State selectedState2() {
		return _selectedState2;
	}

	public void setSelectedState(State selectedState) {
		if (_selectedState != selectedState) {
			_selectedState = selectedState;
			_selectedCounty = null;
			_selectedStreet = null;
			System.out.println("DependentListsExample.setSelectedState: " + _selectedState);
		}
	}

	public State selectedState() {
		return _selectedState;
	}

	public void setSelectedCounty(County selectedCounty) {
		System.out.println("DependentListsExample.setSelectedCounty: selected county = " + selectedCounty);
		if (_selectedCounty != selectedCounty) {
			_selectedCounty = selectedCounty;
			_selectedStreet = null;
			System.out.println("DependentListsExample.setSelectedCounty: " + _selectedCounty);
		}
	}

	public County selectedCounty() {
		return _selectedCounty;
	}

	public void setSelectedStreet(Street selectedStreet) {
		_selectedStreet = selectedStreet;
		System.out.println("DependentListsExample.setSelectedStreet: " + _selectedStreet);
	}

	public Street selectedStreet() {
		return _selectedStreet;
	}

	public NSArray states() {
		return _states;
	}

	public static class State {

		private String _name;
		private NSMutableArray<County> _counties;

		public State(String name) {
			_name = name;
			_counties = new NSMutableArray<County>();
		}

		public String name() {
			return _name;
		}

		public void addCounty(County county) {
			_counties.addObject(county);
		}

		public NSArray counties() {
			return _counties;
		}
		
		@Override
		public String toString() {
			return "[State: name = " + _name + "]"; 
		}
	}

	public static class County {
		private State _state;
		private String _name;
		private NSMutableArray<Street> _streets;

		public County(State state, String name) {
			_state = state;
			_name = name;
			_streets = new NSMutableArray<Street>();
		}
		
		public State state() {
			return _state;
		}

		public String name() {
			return _name;
		}

		public void addStreet(Street street) {
			_streets.addObject(street);
		}

		public NSArray streets() {
			return _streets;
		}
		
		@Override
		public String toString() {
			return "[County: state = " + _state + "; name = " + _name + "]"; 
		}
	}

	public static class Street {
		private County _county;
		private String _name;

		public Street(County county, String name) {
			_county = county;
			_name = name;
		}
		
		public County county() {
			return _county;
		}

		public String name() {
			return _name;
		}
		
		@Override
		public String toString() {
			return "[Street: county = " + _county + "; name = " + _name + "]"; 
		}
	}
}

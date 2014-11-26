package er.chunk;

import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.LocalDate;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.chunk.EROldFart.ChuckHill.Beverage;

public interface EROldFart {

	public static final Fate FATE = new Fate();

	public LocalDate birthday();

	public NSMutableDictionary<String, Object> memories();

	public Object process(Object memory);

	public Object recallMemoryForStimulus(String stimulus);

	public void storeMemoryForStimulus(Object memory, String stimulus);

	public void drink(Beverage beverage);

	public Beverage lastBeverageConsumed();

	/**
	 * Default Implementation
	 * 
	 * @author Chuck's Mom
	 * @author The Milk Man
	 * 
	 * @appreciated
	 */
	public class ChuckHill implements EROldFart {
		private final NSMutableDictionary<String, Object> _memories = new NSMutableDictionary<String, Object>();
		private Beverage _lastBeverageConsumed;
		private LocalDate _birthday;

		public enum Beverage {
			Booze, Coffee, BloodOfYoungGoat
		}

		public Object recallMemoryForStimulus(String stimulus) {
			drink(Beverage.Booze);
			String memoryReferenceKey = PrefrontalCortex.memoryReferenceKeyForStimulus(stimulus);
			Object memory = memories().objectForKey(memoryReferenceKey);
			return memory;
		}

		public void storeMemoryForStimulus(Object memory, String stimulus) {
			boolean isAbleToStringTogetherEnoughNeurons = FATE.isAble();
			if (isAbleToStringTogetherEnoughNeurons) {
				Object processedMemory = process(memory);
				_memories.setObjectForKey(processedMemory, stimulus);
			}
			else {
				drink(Beverage.Booze);
			}
		}

		public Object process(Object memory) {
			final Object processedMemory;
			switch (lastBeverageConsumed()) {
			case Booze:
				processedMemory = NSKeyValueCoding.NullValue;
				drink(Beverage.Booze);
				break;

			case Coffee:
				processedMemory = memory;
				drink(Beverage.Booze);
				break;

			case BloodOfYoungGoat:
				processedMemory = memory;
				break;

			default:
				drink(Beverage.Booze);
				processedMemory = process(memory);
			}
			return processedMemory;
		}

		public NSMutableDictionary<String, Object> memories() {
			drink(Beverage.Booze);
			return _memories;
		}

		public Beverage lastBeverageConsumed() {
			return _lastBeverageConsumed;
		}

		public void drink(Beverage beverage) {
			_lastBeverageConsumed = beverage;
			drink(Beverage.Booze);
		}

		public LocalDate birthday() {
			if (_birthday == null) {
				_birthday = new LocalDate(1964, 05, 31);
			}
			return _birthday;
		}
	}

	class Fate extends Random {

		private static final long serialVersionUID = 50L;

		public int whim() {
			return nextInt();
		}

		public boolean isAble() {
			return nextBoolean();
		}
	}

	class PrefrontalCortex extends RandomStringUtils {

		public static String memoryReferenceKeyForStimulus(String stimulus) {
			final String referenceKey = PrefrontalCortex.whatWhoWhereHuh();
			return referenceKey;
		}

		public static String whatWhoWhereHuh() {
			return PrefrontalCortex.random(FATE.whim());
		}
	}
}

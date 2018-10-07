package org.ta4j.core.trading.rules;

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

/**
 * Indicator-rising-indicator rule.
 * </p>
 * Satisfied when the values of the {@link Indicator indicator} increase
 * within the barCount.
 */
public class IsRisingRule extends AbstractRule {

	/** The actual indicator */
	private final Indicator<Num> ref;
	/** The barCount */
	private final int barCount;
	/** The minimum required strenght of the rising */
	private double minStrenght;

	/**
	 * Constructor for strict rising.
	 * 
	 * @param ref the indicator
	 * @param barCount the time frame
	 */
	public IsRisingRule(Indicator<Num> ref, int barCount) {
		this(ref, barCount, 1);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param ref the indicator
	 * @param barCount the time frame
	 * @param minStrenght the minimum required rising strenght (between '0' and '1', e.g. '1' for strict rising)
	 */
	public IsRisingRule(Indicator<Num> ref, int barCount, double minStrenght) {
		this.ref = ref;
		this.barCount = barCount;
		this.minStrenght = minStrenght;
	}

	@Override
	public boolean isSatisfied(int index, TradingRecord tradingRecord) {
		if (minStrenght >= 1) {
			minStrenght = 0.99;
		}
		
		int count = 0;
		for (int i = Math.max(0, index - barCount + 1); i <= index; i++) {
			if (ref.getValue(i).isGreaterThan(ref.getValue(Math.max(0, i - 1)))) {
				count += 1;
			}
		}

		double ratio = count / (double) barCount;
		
		final boolean satisfied = ratio >= minStrenght;
		traceIsSatisfied(index, satisfied);
		return satisfied;
	}
}

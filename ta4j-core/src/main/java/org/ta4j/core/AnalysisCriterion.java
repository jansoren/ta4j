package org.ta4j.core;

import org.ta4j.core.num.Num;

import java.util.List;

/**
 * An analysis criterion.
 * </p>
 * Can be used to:
 * <ul>
 * 	<li>Analyze the performance of a {@link Strategy strategy}
 * 	<li>Compare several {@link Strategy strategies} together
 * </ul>
 */
public interface AnalysisCriterion {

    /**
     * @param series a time series, not null
     * @param trade a trade, not null
     * @return the criterion value for the trade
     */
    Num calculate(TimeSeries series, Trade trade);

    /**
     * @param series a time series, not null
     * @param tradingRecord a trading record, not null
     * @return the criterion value for the trades
     */
    Num calculate(TimeSeries series, TradingRecord tradingRecord);

    /**
     * @param manager the time series manager
     * @param strategies a list of strategies
     * @return the best strategy (among the provided ones) according to the criterion
     */
    default Strategy chooseBest(TimeSeriesManager manager, List<Strategy> strategies) {

        Strategy bestStrategy = strategies.get(0);
        Num bestCriterionValue = calculate(manager.getTimeSeries(), manager.run(bestStrategy));

        for (int i = 1; i < strategies.size(); i++) {
            Strategy currentStrategy = strategies.get(i);
            Num currentCriterionValue = calculate(manager.getTimeSeries(), manager.run(currentStrategy));

            if (betterThan(currentCriterionValue, bestCriterionValue)) {
                bestStrategy = currentStrategy;
                bestCriterionValue = currentCriterionValue;
            }
        }
        return bestStrategy;
    }

    /**
     * @param criterionValue1 the first value
     * @param criterionValue2 the second value
     * @return true if the first value is better than (according to the criterion) the second one, false otherwise
     */
    boolean betterThan(Num criterionValue1, Num criterionValue2);
}
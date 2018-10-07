package org.ta4j.core.indicators;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;

import static org.ta4j.core.num.NaN.NaN;


/**
 * Aroon down indicator.
 * </p>
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:aroon">chart_school:technical_indicators:aroon</a>
 */
public class AroonDownIndicator extends CachedIndicator<Num> {

    private final int barCount;

    private final LowestValueIndicator lowestMinPriceIndicator;
    private final Indicator<Num> minValueIndicator;
    private final Num hundred;

    /**
     * Constructor.
     * <p>
     * @param minValueIndicator the indicator for the maximum price (default {@link HighPriceIndicator})
     * @param barCount the time frame
     */
    public AroonDownIndicator(Indicator<Num> minValueIndicator, int barCount) {
        super(minValueIndicator);
        this.barCount = barCount;
        this.minValueIndicator = minValueIndicator;
        this.hundred = numOf(100);
        // + 1 needed for last possible iteration in loop
        lowestMinPriceIndicator = new LowestValueIndicator(minValueIndicator, barCount+1);
    }

    /**
     * Default Constructor that is using the maximum price
     * <p>
     * @param series the time series
     * @param barCount the time frame
     */
    public AroonDownIndicator(TimeSeries series, int barCount) {
        this(new LowPriceIndicator(series), barCount);
    }

    @Override
    protected Num calculate(int index) {
        if (getTimeSeries().getBar(index).getLowPrice().isNaN())
            return NaN;

        // Getting the number of bars since the lowest close price
        int endIndex = Math.max(0,index - barCount);
        int nbBars = 0;
        for (int i = index; i > endIndex; i--) {
            if (minValueIndicator.getValue(i).isEqual(lowestMinPriceIndicator.getValue(index))) {
                break;
            }
            nbBars++;
        }

        return numOf(barCount - nbBars).dividedBy(numOf(barCount)).multipliedBy(hundred);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" barCount: "+barCount;
    }
}

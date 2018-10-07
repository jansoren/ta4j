package org.ta4j.core.indicators.helpers;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;
/**
 * Open price indicator.
 * </p>
 */
public class OpenPriceIndicator extends CachedIndicator<Num> {

    public OpenPriceIndicator(TimeSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        return getTimeSeries().getBar(index).getOpenPrice();
    }
}
package org.ta4j.core.indicators.statistics;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import static org.ta4j.core.num.NaN.NaN;

/**
 * Simple linear regression indicator.
 * </p>
 * A moving (i.e. over the time frame) simple linear regression (least squares).
 * y = slope * x + intercept
 * See also: http://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html
 */
public class SimpleLinearRegressionIndicator extends CachedIndicator<Num> {

	 /**
	 * The type for the outcome of the {@link SimpleLinearRegressionIndicator}
	 */
	public enum SimpleLinearRegressionType {
		y, slope, intercept
	}

	private Indicator<Num> indicator;
	private int barCount;
	private Num slope;
	private Num intercept;
	private SimpleLinearRegressionType type;

	/**
	 * Constructor for the y-values of the formula (y = slope * x + intercept).
	 * 
	 * @param indicator the indicator for the x-values of the formula.
	 * @param barCount the time frame
	 */
	public SimpleLinearRegressionIndicator(Indicator<Num> indicator, int barCount) {
		this(indicator, barCount, SimpleLinearRegressionType.y);
	}

	/**
	 * Constructor.
	 * 
	 * @param indicator the indicator for the x-values of the formula.
	 * @param barCount the time frame
	 * @param type the type of the outcome value (y, slope, intercept)
	 */
	public SimpleLinearRegressionIndicator(Indicator<Num> indicator, int barCount,
			SimpleLinearRegressionType type) {
		super(indicator);
		this.indicator = indicator;
		this.barCount = barCount;
		this.type = type;
	}

    @Override
    protected Num calculate(int index) {
        final int startIndex = Math.max(0, index - barCount + 1);
        if (index - startIndex + 1 < 2) {
            // Not enough observations to compute a regression line
            return NaN;
        }
        calculateRegressionLine(startIndex, index);
        
        if (type == SimpleLinearRegressionType.slope) {
            return slope;
        }

        if (type == SimpleLinearRegressionType.intercept) {
            return intercept;
        }
      
        return slope.multipliedBy(numOf(index)).plus(intercept);
    }
    
    /**
     * Calculates the regression line.
     * @param startIndex the start index (inclusive) in the time series
     * @param endIndex the end index (inclusive) in the time series
     */
    private void calculateRegressionLine(int startIndex, int endIndex) {
        // First pass: compute xBar and yBar
        Num sumX = numOf(0);
        Num sumY = numOf(0);
        for (int i = startIndex; i <= endIndex; i++) {
            sumX = sumX.plus(numOf(i));
            sumY = sumY.plus(indicator.getValue(i));
        }
        Num nbObservations = numOf(endIndex - startIndex + 1);
        Num xBar = sumX.dividedBy(nbObservations);
        Num yBar = sumY.dividedBy(nbObservations);
        
        // Second pass: compute slope and intercept
        Num xxBar = numOf(0);
        Num xyBar = numOf(0);
        for (int i = startIndex; i <= endIndex; i++) {
            Num dX = numOf(i).minus(xBar);
            Num dY = indicator.getValue(i).minus(yBar);
            xxBar = xxBar.plus(dX.multipliedBy(dX));
            xyBar = xyBar.plus(dX.multipliedBy(dY));
        }
        
        slope = xyBar.dividedBy(xxBar);
        intercept = yBar.minus(slope.multipliedBy(xBar));
    }
}

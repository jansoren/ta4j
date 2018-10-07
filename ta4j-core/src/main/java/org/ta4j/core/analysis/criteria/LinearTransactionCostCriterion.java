package org.ta4j.core.analysis.criteria;

import org.ta4j.core.Order;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

/**
 * A linear transaction cost criterion.
 * </p>
 * That criterion calculate the transaction cost according to an initial traded amount
 * and a linear function defined by a and b (a * x + b).
 */
public class LinearTransactionCostCriterion extends AbstractAnalysisCriterion {

    private double initialAmount;

    private double a;
    private double b;

    private TotalProfitCriterion profit;

    /**
     * Constructor.
     * (a * x)
     * @param initialAmount the initially traded amount
     * @param a the a coefficient (e.g. 0.005 for 0.5% per {@link Order order})
     */
    public LinearTransactionCostCriterion(double initialAmount, double a) {
        this(initialAmount, a, 0);
    }

    /**
     * Constructor.
     * (a * x + b)
     * @param initialAmount the initially traded amount
     * @param a the a coefficient (e.g. 0.005 for 0.5% per {@link Order order})
     * @param b the b constant (e.g. 0.2 for $0.2 per {@link Order order})
     */
    public LinearTransactionCostCriterion(double initialAmount, double a, double b) {
        this.initialAmount = initialAmount;
        this.a = a;
        this.b = b;
        profit = new TotalProfitCriterion();
    }

    @Override
    public Num calculate(TimeSeries series, Trade trade) {
        return getTradeCost(series, trade, series.numOf(initialAmount));
    }

    @Override
    public Num calculate(TimeSeries series, TradingRecord tradingRecord) {
        Num totalCosts = series.numOf(0);
        Num tradedAmount = series.numOf(initialAmount);
        
        for (Trade trade : tradingRecord.getTrades()) {
            Num tradeCost = getTradeCost(series, trade, tradedAmount);
            totalCosts = totalCosts.plus(tradeCost);
            // To calculate the new traded amount:
            //    - Remove the cost of the *first* order
            //    - Multiply by the profit ratio
            //    - Remove the cost of the *second* order
            tradedAmount = tradedAmount.minus(getOrderCost(trade.getEntry(), tradedAmount));
            tradedAmount = tradedAmount.multipliedBy(profit.calculate(series, trade));
            tradedAmount = tradedAmount.minus(getOrderCost(trade.getExit(), tradedAmount));
        }
        
        // Special case: if the current trade is open
        Trade currentTrade = tradingRecord.getCurrentTrade();
        if (currentTrade.isOpened()) {
            totalCosts = totalCosts.plus(getOrderCost(currentTrade.getEntry(), tradedAmount));
        }
        
        return totalCosts;
    }

    @Override
    public boolean betterThan(Num criterionValue1, Num criterionValue2) {
        return criterionValue1.isLessThan(criterionValue2);
    }

    /**
     * @param order a trade order
     * @param tradedAmount the traded amount for the order
     * @return the absolute order cost
     */
    private Num getOrderCost(Order order, Num tradedAmount) {
        Num orderCost = tradedAmount.numOf(0);
        if (order != null) {
            return tradedAmount.numOf(a).multipliedBy(tradedAmount).plus(tradedAmount.numOf(b));
        }
        return orderCost;
    }

    /**
     * @param series the time series
     * @param trade a trade
     * @param initialAmount the initially traded amount for the trade
     * @return the absolute total cost of all orders in the trade
     */
    private Num getTradeCost(TimeSeries series, Trade trade, Num initialAmount) {
        Num totalTradeCost = series.numOf(0);
        if (trade != null) {
            if (trade.getEntry() != null) {
                totalTradeCost = getOrderCost(trade.getEntry(), initialAmount);
                if (trade.getExit() != null) {
                    // To calculate the new traded amount:
                    //    - Remove the cost of the first order
                    //    - Multiply by the profit ratio
                    Num newTradedAmount = initialAmount.minus(totalTradeCost).multipliedBy(profit.calculate(series, trade));
                    totalTradeCost = totalTradeCost.plus(getOrderCost(trade.getExit(), newTradedAmount));
                }
            }
        }
        return totalTradeCost;
    }
}

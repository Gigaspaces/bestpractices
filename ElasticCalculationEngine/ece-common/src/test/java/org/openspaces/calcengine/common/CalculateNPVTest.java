package org.openspaces.calcengine.common;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CalculateNPVTest {
    @Test
    public void testFormula() {
        double[] value = {0.0, 30000.0, 30000.0, 30000.0, 30000.0, 30000.0, 30000.0};
        double[] outlay = {100000.0, 5000, 5000, 5000, 5000, 5000, 5000};
        double npv = 0.0;
        double rate = 0.1;
        assertEquals(value.length, outlay.length);
        for (int idx = 0; idx < value.length; idx++) {
            double profit = value[idx] - outlay[idx];
            double presentValue = profit / Math.pow(1 + rate, idx);

            npv += presentValue;
            System.out.printf("Present value: %10.2f Net Present Value, Year %d: %10.2f%n",
                    presentValue, idx, npv);
        }
        assertEquals(npv, 8881.52, 0.01);
    }

    @Test
    public void testCalcNPV() {
        Trade trade = new Trade();
        trade.setCashFlowData(new double[]{-100000.0, 25000.0, 25000.0, 25000, 25000, 25000});
        CalculateNPVUtil.calculateNPV(10, trade);
        System.out.printf("Net present value after six years: %7.2f%n", trade.getNPV());
        assertEquals(trade.getNPV(), -5230.33, 0.01);
    }
}

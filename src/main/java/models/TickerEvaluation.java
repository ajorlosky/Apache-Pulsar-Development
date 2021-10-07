package models;

import java.util.function.Function;

//ticker function used as pulsar function: determines if user should buy, sell, or hold a specific stock
public class TickerEvaluation implements Function<StockTicker, String> {

	public String apply(StockTicker input) {
		
		double changeInPrice = input.getPrice() - input.getOpen();
		double percentChange = 100 * (changeInPrice / input.getOpen());
		String evaluation;
		
		// determines if user should sell, buy, or hold depending on the daily percent change in price
		if (percentChange > 5) {
			evaluation = "Profit greater than 5% - SELL";
		} else if (percentChange < -5) {
			evaluation = "Loss less than 5% - BUY";
		} else {
			evaluation = "Insignificant change in price - HOLD";
		}
		
		// returns a formatted string to the output topic
		return String.format("\n\nTicker Symbol: %s "
				+ "\nDaily Profit/Loss: %4.2f "
				+ "\nPercent Change in Price: %4.2f "
				+ "\n%s", 
				input.getName(), changeInPrice, 
				percentChange, evaluation);
				
	}
}

package models;

import org.apache.pulsar.functions.api.Function;
import java.util.HashMap;
import org.apache.pulsar.functions.api.Context;

// ticker function used as pulsar function: calculates profit or loss of individual ticker and all tickers on a topic
public class TickerEstimates implements Function<StockTicker, String>{

	private double totalProfit = 0;
	private HashMap<String, Double> tickers = new HashMap<String, Double>();

	// computes individual profit and total profit for ticker symbol and sends result to output topic
	public String process(StockTicker input, Context cxt) {
		double individualProfit = input.getPrice() - input.getOpen();
		
		/* 
		 * updates the total profit accurately by the change in price (since last message) 
		 * if the ticker was already sent to the topic
		 */
		if (tickers.containsKey(input.getName()))
			totalProfit -= tickers.get(input.getName());
		
		tickers.put(input.getName(), individualProfit);
		totalProfit += individualProfit;

		
		// format string for message sent to output topic 
		return String.format("\n\nInput Topic Name: %s "
				+ "\nOutput Topic Name: %s "
				+ "\nPulsar Function Name: %s"
				+ "\nTicker Payload Data: %s"
				+ "\nIndividual Profit/Loss of %s: $%4.2f "
				+ "\nTotal Profit/Loss of all tickers: $%4.2f\n", 
				cxt.getInputTopics().toString(), cxt.getOutputTopic(), 
				cxt.getFunctionName(), input.toString(), input.getName(),
				individualProfit, totalProfit);
	}
}

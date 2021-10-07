package utils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
//import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;
import java.text.SimpleDateFormat;
import models.StockTicker;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

public class YahooStockAPI {

	private static ArrayList<StockTicker> multipleTickers = new ArrayList<StockTicker>();
	private static List<String> tickerList = new ArrayList<String>();

	/*
	 * given a stock ticker symbol, # of years, and interval period, this method
	 * outputs an arrayList of ticker prices for that stock
	 */
	public static StockTicker loadStockTickerData(String stockName) throws IOException {

		/* parses each ticker symbol into a single stockTicker object. */
		String singleInstance;
		try {
			// generates the historical stock ticker data for the specific stock name
			StockQuote stock = YahooFinance.get(stockName.toUpperCase()).getQuote();
			
			singleInstance = "";
			singleInstance = convertDate(Calendar.getInstance().getTime());
			singleInstance += "," + stock.getSymbol();
			singleInstance += "," + stock.getPrice();
			singleInstance += "," + stock.getOpen().doubleValue();
			singleInstance += "," + stock.getPreviousClose().doubleValue();
			singleInstance += "," + stock.getDayHigh().doubleValue();
			singleInstance += "," + stock.getDayLow().doubleValue();
			singleInstance += "," + stock.getVolume().doubleValue();
		} catch (NullPointerException e) {
			return null;
		}

		return strToStockTicker(singleInstance);
	}

	/* returns array list for an array of multiple stock quotes */
	public static ArrayList<StockTicker> loadAllTickersThreaded() {

		YahooStockAPI lock = new YahooStockAPI();
		ArrayList<TickerThread> threadList = new ArrayList<TickerThread>();

		// creates a ticker thread for each symbol in the list
		for (String symbol : tickerList) {
			TickerThread ticker = new TickerThread(symbol, lock);
			ticker.start();
			threadList.add(ticker);
		}

		// uses thread.join to make sure all threads are complete before returning the
		// list of stock tickers
		for (TickerThread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return multipleTickers;
	}

	/* returns array list for an array of multiple stock quotes */
	public static ArrayList<StockTicker> loadAllTickers() throws IOException {

		StockTicker ticker = null;

		for (String symbol : tickerList) {
			/*
			 * adds the stock ticker objects of all stocks into one list
			 */
			try {
				ticker = loadStockTickerData(symbol);
			} catch (Exception e) {
				/* catches invalid ticker */
			} finally {
				multipleTickers.add(ticker);
			}

		}
		return multipleTickers;
	}

	// reads a list of ticker symbols from the file ticker-list.txt
	public static List<String> processTickerList() {

		try (BufferedReader br = Files.newBufferedReader(Paths.get("src/main/resources/datasets/ticker-list.txt"))) {
			tickerList = br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tickerList;
	}

	// writes table header, effectively clearing anything existing in the output
	// file
	public static void clearFile() throws IOException {

		// writes each message to an output file
		try (FileWriter pr = new FileWriter("src/main/resources/datasets/ticker-message-output.csv", false)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Date" + "," + "Ticker" + "," + "Price" + "," + "Open" + "," + "Previous Close" + "," + "High"
					+ "," + "Low" + "," + "Volume" + "\n");
			pr.write(sb.toString());
			pr.flush();
		}
	}

	// writes the stock-ticker value as a message to the output file
	public static void writeToFile(StockTicker value) throws IOException {
		// writes each message to an output file
		try (FileWriter pr = new FileWriter("src/main/resources/datasets/ticker-message-output.csv", true)) {
			if (value != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(value.getDate() + "," + value.getName() + "," + value.getPrice() + "," + value.getOpen() + ","
						+ value.getPrevClose() + "," + value.getHigh() + "," + value.getLow() + "," + value.getVolume()
						+ "\n");
				pr.append(sb.toString());
			}
			pr.flush();
		}
	}

	// adds an individual stockTicker payload to the list
	public void addToList(StockTicker payload) {
		multipleTickers.add(payload);
	}

	/* converts Calendar date into human readable form */
	private static String convertDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(date);
	}

	/*
	 * converts a string of stock ticker data into a stockTicker object, while
	 * initializing its fields
	 */
	public static StockTicker strToStockTicker(String str) {
		String[] tokens = str.split(",");
		String date = tokens[0];
		String name = tokens[1];
		double price = Double.parseDouble(tokens[2]);
		double open = Double.parseDouble(tokens[3]);
		double close = Double.parseDouble(tokens[4]);
		double high = Double.parseDouble(tokens[5]);
		double low = Double.parseDouble(tokens[6]);
		double volume = Double.parseDouble(tokens[7]);
		return new StockTicker(date, name, price, open, close, high, low, volume);
	}
}
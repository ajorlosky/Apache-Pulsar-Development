package utils;

import models.StockTicker;

public class TickerThread extends Thread {

	private String tickerSymbol;
	private YahooStockAPI lock;

	public TickerThread(String tickerSymbol, YahooStockAPI lock) {
		this.tickerSymbol = tickerSymbol;
		this.lock = lock;
	}

	// generates lock ticker data for each symbol using multithreading, but uses
	// lock to synchronize adding payload to the list
	public void run() {
		try {
			StockTicker payload = YahooStockAPI.loadStockTickerData(tickerSymbol);
			synchronized (lock) {
				lock.addToList(payload);
			}
		} catch (Exception e) {
			// catches invalid ticker
		}
		return;
	}
}

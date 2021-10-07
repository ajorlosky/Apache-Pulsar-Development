package models;

import java.util.Objects;

/* this class holds all of the data for an individual stock ticker, including
 * the date, company symbol name, open, close, high, and low prices, and volume
 */
public class StockTicker {
	private String date;
	private String name;
	private double price;
	private double open;
	private double prevClose;
	private double high;
	private double low;
	private double volume;
	
	public StockTicker ()
	{ }
	/* constructor for StockTicker */
	public StockTicker(String date, String name, double price, double open, double prevClose, double high, double low,
			double volume) {
		this.date = date;
		this.name = name;
		this.price = price;
		this.open = open;
		this.prevClose = prevClose;
		this.high = high;
		this.low = low;
		this.volume = volume;
	}

	/* getter and setter methods for each field */

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(double prevClose) {
		this.prevClose = prevClose;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	@Override
	// equals method for multiple StockTicker objects
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		StockTicker that = (StockTicker) o;
		return Objects.equals(date, that.date) && Objects.equals(name, that.name) && Objects.equals(price, that.price)
				&& Objects.equals(open, that.open) && Objects.equals(prevClose, that.prevClose)
				&& Objects.equals(high, that.high) && Objects.equals(low, that.low)
				&& Objects.equals(volume, that.volume);
	}

	@Override
	// generated hashCode for a stock ticker
	public int hashCode() {
		return Objects.hash(date, name, price, open, prevClose, high, low, volume);
	}

	/* toString method for the stock ticker data using data from each field */
	@Override
	public String toString() {
		return "StockTicker{" + "date=" + date + ", name='" + name + '\'' + ", price='" + price + '\'' + ", open="
				+ open + ", previous close=" + prevClose + ", high=" + high + ", low=" + low + ", volume=" + volume
				+ '}';
	}
}

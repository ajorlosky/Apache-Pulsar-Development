package models;

import java.util.regex.Pattern;
import org.apache.pulsar.functions.api.SerDe;

public class StockTickerSerde implements SerDe<StockTicker>{ 

	// deserializes message in bytes into a stock-ticker object
	public StockTicker deserialize(byte[] input) {
		String s = new String(input);
		String[] fields = s.split(Pattern.quote("|"));
		return new StockTicker(fields[0], fields[1], 
				Double.parseDouble(fields[2]), 
				Double.parseDouble(fields[3]),
				Double.parseDouble(fields[4]), 
				Double.parseDouble(fields[5]), 
				Double.parseDouble(fields[6]),
				Double.parseDouble(fields[7]));
	}

	// serializes a stock ticker object into a message bytes array
	public byte[] serialize(StockTicker ticker) {
		return String.format("%s|%s|%f|%f|%f|%f|%f|%f|%f", 
						ticker.getDate(), 
						ticker.getName(), 
						ticker.getPrice(),
						ticker.getOpen(), 
						ticker.getPrevClose(), 
						ticker.getHigh(), 
						ticker.getLow(), 
						ticker.getVolume()).getBytes();
	}
}
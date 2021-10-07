package producers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JOptionPane;
//import org.apache.pulsar.client.api.BatcherBuilder;
//import org.apache.pulsar.client.api.MessageRoutingMode;
//import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import interceptors.CustomAsyncProducerInterceptor;
import models.StockTicker;
import utils.ClientUtils;
import utils.YahooStockAPI;

public class SyncProducerUniqueTopics {
	public static void main(String[] args) throws IOException, InterruptedException {

		// 1. Instantiate pulsar client
		PulsarClient pulsarClient = ClientUtils.initPulsarClient();
		
		// enables user interaction with producer
		JOptionPane.showMessageDialog(null, "Press OK to Generate Live Stock Ticker Data!");
		System.out.println("Generating live stock ticker data...");

		// processes the list of tickers from the input text file
		ArrayList<String> symbols = new ArrayList<String>();
		symbols.addAll(YahooStockAPI.processTickerList());

		// clears the output text file used for jupyter notebooks
		YahooStockAPI.clearFile();

		// uses multiple threads to generate all of the live stock ticker objects for each symbol
		ArrayList<StockTicker> stockTickers = YahooStockAPI.loadAllTickersThreaded();

		String continuous, update;
		try (Scanner input = new Scanner(System.in)) {
			// ensures user customizes producer to either send messages continuously or over
			// a rate/time
			do {
				continuous = JOptionPane.showInputDialog("Would you like to send messages continuously? (yes or no): ");
			} while (!continuous.equals("yes") && !continuous.equals("no"));

			do {
				update = JOptionPane.showInputDialog("Would you like update payload in real-time? (yes or no): ");
			} while (!update.equals("yes") && !update.equals("no"));
		}

		CustomAsyncProducerInterceptor interceptor = new CustomAsyncProducerInterceptor();

		/*
		 * 2. Pulsar Producer: settings used for every producer prior to sending
		 *    messages to the topic. For each ticker symbol, a unique producer with a 
		 *    unique topic/producer name is created and stored into a hashmap with 
		 *    the key and topic name as the ticker symbol and the producer as the value.
		 */

		HashMap<String, Producer<StockTicker>> producers = new HashMap<String, Producer<StockTicker>>();

		for (String ticker : symbols) {
			producers.put(ticker, pulsarClient.newProducer(JSONSchema.of(StockTicker.class))
					.topic("persistent://investments/stocks/" + ticker)
					.producerName(ticker + " sync-producer")
					.intercept(interceptor)
					.enableBatching(true)
					// by default messages, messages are kept in an internal queue and sent to the
					// broker in batches
					// .batcherBuilder(BatcherBuilder.KEY_BASED)
					// use this to ensure tickers of same key end up in same batch
					// .messageRoutingMode(MessageRoutingMode.RoundRobinPartition)
					.blockIfQueueFull(true).maxPendingMessages(50000).create());
		}

		/* by modifying the above criteria, you can improve the producer performance */

		System.out.println("Running producer work loop...");

		/*
		 * 3. Initiates a producer work loop: sends every message in the list of stock
		 * tickers asynchronously to the topic using the JSON Schema for the Stock
		 * Ticker class.
		 */

		/*
		 * Option 1: Non-continuous - The producer is sending messages in a round-robin
		 * fashion in the ticker-list order for a preset amount of time.
		 */

		if (continuous.equals("no"))
			sendNoncontinuousMessages(producers, symbols, stockTickers, update);
		/*
		 * Option 2: Continuous - The producer is sending messages in a round-robin
		 * fashion in the ticker-list order continuously.
		 */

		else
			sendContinuousMessages(producers, symbols, stockTickers, update);

		JOptionPane.showMessageDialog(null, "Done sending messages!");

		// 4. Close all the resources after using pulsar client

		/*
		 * This is critical because no topic can be modified if there are consumers or
		 * producers still subscribed to a topic!
		 */

		System.out.println("Closing producer and pulsar client...");
		closeResources(producers, pulsarClient);
	}

	// produces message using ticker index, producer, and arraylist of stock tickers
	// (updates and returns tickerIndex after)
	public static int produceMessage(HashMap<String, Producer<StockTicker>> producers, ArrayList<String> symbols,
			ArrayList<StockTicker> stockTickers, String update, int tickerIndex) {

		// catches Exceptions to ensure tickers are generated in order
		// of arrayList
		try {

			// gets stock-ticker value and key for the current index
			StockTicker value = stockTickers.get(tickerIndex);
			String key = symbols.get(tickerIndex);

			// updates stock ticker data if requested
			if (update.equals("yes"))
				value = YahooStockAPI.loadStockTickerData(key);

			// publishes message to broker and writes to output file
			if (value != null) {
				producers.get(key).newMessage(JSONSchema.of(StockTicker.class)).key(key).value(value).send();
				YahooStockAPI.writeToFile(value);
			}

			// increments ticker to next index
			tickerIndex++;
		} catch (IndexOutOfBoundsException | IOException e) {
			tickerIndex = 0;
		}

		return tickerIndex;
	}

	// processing for not sending the messages continuously (for a period of time at
	// a specific rate)
	public static void sendNoncontinuousMessages(HashMap<String, Producer<StockTicker>> producer, ArrayList<String> symbols,
			ArrayList<StockTicker> stockTickers, String update) throws InterruptedException {

		// asks user for desired message rate and time of messages
		int messageRate = Integer.parseInt(JOptionPane.showInputDialog("Enter Message Rate (msgs/sec): "));
		int time = Integer.parseInt(JOptionPane.showInputDialog("Enter Time for Producer Loop (secs): "));
		System.out.println(
				"Messages sending for " + time + " seconds at a rate of " + messageRate + " messages per second.");

		// starts sending messages at first ticker in the list
		int tickerIndex = 0;
		long startTime = System.currentTimeMillis();
		long currentTime = startTime;
		time*=1000;

		// sends messages for the specified amount of seconds
		while (currentTime < startTime + time) {

			// produces message to broker
			tickerIndex = produceMessage(producer, symbols, stockTickers, update, tickerIndex);

			currentTime = System.currentTimeMillis();

			// main thread sleeps after each message sent, so that
			// the message is sent at the desired rate
			Thread.sleep((1000 / messageRate) - 3);
		}
	}

	// processing for sending the messages continuously to the the topic on pulsar
	// broker
	public static void sendContinuousMessages(HashMap<String, Producer<StockTicker>> producer, ArrayList<String> symbols,
			ArrayList<StockTicker> stockTickers, String update) {

		// starts a thread to send messages continuously until the user interrupts the
		// thread
		System.out.println("Messages sending continuously");
		Thread continuousMessages = new Thread() {
			public void run() {
				int tickerIndex = 0;
				while (true) {
					
					// produces message to broker and catches any Interrupted or I/O exceptions
					tickerIndex = produceMessage(producer, symbols, stockTickers, update, tickerIndex);

					// brief sleep between message sends
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						break;
					}
				}
				return;
			}
		};

		// user interrupts the thread when they want to stop sending messages to broker
		continuousMessages.start();
		JOptionPane.showMessageDialog(null, "Press OK to stop.");
		continuousMessages.interrupt();
	}

	// closes producer and pulsar client after using them
	public static void closeResources(HashMap<String, Producer<StockTicker>> producers, PulsarClient pulsarClient)
			throws PulsarClientException {
		for (Map.Entry<String, Producer<StockTicker>> elem : producers.entrySet()) {
			elem.getValue().close();
		}
		pulsarClient.close();
	}
}

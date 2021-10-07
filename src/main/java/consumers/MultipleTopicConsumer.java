package consumers;

import org.apache.pulsar.client.api.PulsarClientException;
import java.util.regex.Pattern;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import models.StockTicker;
import utils.ClientUtils;

public class MultipleTopicConsumer {
	
	public static void main(String[] args) throws PulsarClientException {

		/* initializes pulsar client */
		PulsarClient pulsarClient = ClientUtils.initPulsarClient();

		/* 
		 * Initialializes a consumer that subscribes to all topics in a namespace (see
		 * multi-topic subscriptions)
		 */
		Pattern allTopicsInNamespace = Pattern.compile("persistent://public/tickers/.*");
		Consumer<StockTicker> consumer = pulsarClient.newConsumer(JSONSchema.of(StockTicker.class))
				.topicsPattern(allTopicsInNamespace).subscriptionName("test-subs").subscribe();
		
		/*
		 * simple consumer has no specified subscription type, so it just acknowledges messages sent by the producer (default exclusive)
		 */
		int messageCount = 0;
		while (true) {
			Message<StockTicker> message = consumer.receive();
			messageCount += 1;
			System.out.println("Acked message [" + message.getMessageId() + "], Payload: " + message.getValue()
					+ " Total messages acked so far: " + messageCount);
			try {
				consumer.acknowledge(message);
			} catch (Exception e) {
				consumer.negativeAcknowledge(message);
				consumer.close();
				pulsarClient.close();
			}
		}

		/*
		 * for a more complicated pulsar client, we can change the AppConfig to have
		 * multiple topics or clusters.
		 */

	}
}

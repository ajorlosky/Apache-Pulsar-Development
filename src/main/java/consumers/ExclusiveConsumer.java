package consumers;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import config.AppConfig;
import models.StockTicker;
import utils.ClientUtils;

public class ExclusiveConsumer {

	/*
	 * Using pulsar exclusive consumer model, only one consumer is attached to the
	 * subscription. This is blocking, so if multiple consumers subscribe to the
	 * same subscription, an error occurs.
	 */
	public static void main(String[] args) throws PulsarClientException {

		/* initializes pulsar client */
		PulsarClient pulsarClient = ClientUtils.initPulsarClient();

		/* 
		 * Initialializes a consumer that subscribes to all topics in a namespace (see
		 * multi-topic subscriptions)
		 */

		/* initializes a simple consumer using the earliest position subscription */
		
		Consumer<StockTicker> consumer = pulsarClient.newConsumer(JSONSchema.of(StockTicker.class))
		  .topic(AppConfig.singleTopic).consumerName("stock-tickers-consumer")
		  .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
		  .subscriptionType(SubscriptionType.Exclusive).readCompacted(true)
		  .subscriptionName("test-subscription").subscribe();
		
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

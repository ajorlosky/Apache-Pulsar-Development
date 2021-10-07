package utils;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import models.StockTicker;

import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerThread extends Thread {

	private Consumer<StockTicker> consumer;
	private final AtomicInteger totalMessages = new AtomicInteger();
	private int delay = 0;
	private boolean isRunning = true;

	/*
	 * Constructor for the consumer thread of the for the JSON Schema in the Stock
	 * Ticker class. This class can be used to instantiate a consumer thread for any
	 * of the subscription types, however most useful for the Failover subscription
	 * type.
	 */
	public ConsumerThread(PulsarClient pulsarClient, String topicName, String subscriptionName, String consumerName,
			SubscriptionType subscriptionType) {

		/*
		 * instantiates a consumer for the provided pulsar client using the subscription
		 * name, subscription type, consumer name, and for the specific topic.
		 */

		try {
			this.consumer = pulsarClient.newConsumer(JSONSchema.of(StockTicker.class)).topic(topicName)
					.subscriptionName(subscriptionName).subscriptionType(subscriptionType).consumerName(consumerName)
					.subscriptionInitialPosition(SubscriptionInitialPosition.Earliest).subscribe();
			registerShutdownHook();
		} catch (PulsarClientException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Using multithreaded consumers, the consumer threads run synchronously to
	 * acknowledge messages sent by the producer faster.
	 */
	@Override
	public void run() {
		while (isRunning) {
			if (delay != 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Message<StockTicker> msg = null;
			try {
				msg = consumer.receive();
				if (msg != null) {
					consumer.acknowledge(msg);
					System.out.println("[" + consumer.getConsumerName() + "] Received message: " + msg.getValue());
					System.out.println("[" + consumer.getConsumerName() + "] Total messages received: "
							+ totalMessages.addAndGet(1));
				}
			} catch (PulsarClientException e) {
				consumer.negativeAcknowledge(msg);
			}
		}
		try {
			this.consumer.close();
			System.out.println(consumer.getConsumerName() + " disconnected");
		} catch (PulsarClientException e) {
			e.printStackTrace();
		}
	}

	// adds delay for the multithreaded consumers
	public void addDelay(int delay) {
		this.delay = delay;
	}

	// disconnects the consumer after a certain amount of time (see Failover
	// Consumer)
	// by setting isRunning to false
	public void disconnectConsumer() {
		this.isRunning = false;
	}

	/*
	 * shutdown hook used to close the all of the consumer threads after
	 * acknowledging all messages sent by the producer
	 */
	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("[" + consumer.getConsumerName() + "] Disconnecting..");
			try {
				consumer.close();
			} catch (PulsarClientException e) {
				e.printStackTrace();
			}
		}));
	}
}

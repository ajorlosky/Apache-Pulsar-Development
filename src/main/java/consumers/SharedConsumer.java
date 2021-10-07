package consumers;

import java.util.stream.IntStream;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import config.AppConfig;
import utils.ClientUtils;
import utils.ConsumerThread;

public class SharedConsumer {

	/*
	 * In the shared subscription mode, multiple consumers can attach to the same
	 * subscription.
	 */
	public static void main(String[] args) throws PulsarClientException {

		/* initializes pulsar client */
		PulsarClient pulsarClient = ClientUtils.initPulsarClient();

		/*
		 * When a consumer disconnects, all unacknowledged messages will be rescheduled
		 * for sending to remaining consumers. The messages are distributed in order by
		 * consumer in a round-robin fashion.
		 */
		IntStream.of(1, 2, 3, 4, 5).mapToObj(id -> {
			return new ConsumerThread(pulsarClient, AppConfig.singleTopic, "test-subscription", "consumer-" + id,
					SubscriptionType.Shared);
		}).forEach(Thread::start);

		/*
		 * for a more complicated pulsar client, we can change the AppConfig to have
		 * multiple topics or clusters.
		 */
	}

}

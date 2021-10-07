package consumers;

import java.util.stream.IntStream;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;
import config.AppConfig;
import utils.ClientUtils;
import utils.ConsumerThread;

public class KeysharedConsumer {

	/*
	 * In the KeyShared consumer subscription type, multiple consumers can attach to
	 * the same subscription. However, messages are distributed across consumers
	 * such that messages with same key are delivered to one consumer
	 */
	public static void main(String[] args) throws PulsarClientException {

		// initializes pulsar client
		PulsarClient pulsarClient = ClientUtils.initPulsarClient();

		/*
		 * In this example, there are 10 consumers (one for each stock ticker). Each
		 * will only received stock data for their specific stock, specified by the key
		 * value.
		 */
		
        IntStream.of(1, 2, 3, 4, 5).mapToObj(id -> {
            return new ConsumerThread(
                    pulsarClient, AppConfig.singleTopic,
                    "test-subscription",
                    "consumer-" + id,
                    SubscriptionType.Key_Shared);
        }).forEach(Thread::start);

		// for a more complicated pulsar client, we can change the AppConfig to have
		// multiple topics or clusters.
	}
}

package consumers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionType;
import config.AppConfig;
import utils.ClientUtils;
import utils.ConsumerThread;

public class FailoverConsumer {

	/*
	 * Using the failover consumer subscription type , multiple consumers attach to
	 * the same subscription, so when the master consumer disconnects, all messages
	 * are delivered to the next consumer in line
	 */
	public static void main(String[] args) throws PulsarClientException, InterruptedException {

		// initializes pulsar client
		PulsarClient pulsarClient = ClientUtils.initPulsarClient();

		/*
		 * In this failover example, there are a max of 3 consumers, who can only access
		 * a single topic through the consumer thread. In this subscription type, if one
		 * consumer access fails, the next consumer will proceed to acknowledge messages
		 * in the topic.
		 */
		List<ConsumerThread> consumerThreads = IntStream.of(1, 2, 3).mapToObj(id -> {
			ConsumerThread consumerThread = new ConsumerThread(pulsarClient, AppConfig.singleTopic, "test-subscription",
					"consumer-" + id, SubscriptionType.Failover);

			/* consumer threads are delayed so they proceed in order */
			consumerThread.addDelay(250);

			return consumerThread;
		}).collect(Collectors.toList());

		consumerThreads.forEach(Thread::start);

		/*
		 * Each consumer thread runs for 10 seconds acknowledging messages to
		 * demonstrate the behavior of the failover subscription type in the pulsar
		 * client
		 */
		for (int i = 1; i < consumerThreads.size(); i++) {
			Thread.sleep(10000);
			consumerThreads.get(i - 1).disconnectConsumer();
		}

		// for a more complicated pulsar client, we can change the AppConfig to have
		// multiple topics or clusters.
	}
}

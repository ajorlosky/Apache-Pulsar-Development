package interceptors;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.interceptor.ProducerInterceptor;

/* customizes the pulsar asynchronous interceptor interface perform a 
 * series of operations before producer sends the message */
public class CustomAsyncProducerInterceptor implements ProducerInterceptor {

	// increments the total messages atomically at each instance of this class
	private final AtomicInteger totalMessages = new AtomicInteger();
	private final long t1 = System.currentTimeMillis();

	@Override
	// closes the producer
	public void close() {
	}

	@Override
	// overrides default so messages are sent by the producer (sets to true)
	public boolean eligible(Message message) {
		return true;
	}

	@Override
	// triggered before each message is sent to the pulsar broker, returns the
	// message
	public Message beforeSend(Producer producer, Message message) {
		return message;
	}

	@Override
	// called each time an acknowledgement is returned from the broker for a sent
	// message
	public void onSendAcknowledgement(Producer producer, Message message, MessageId messageId, Throwable throwable) {
		if (throwable != null) {
			throw new RuntimeException(throwable.toString());
		}
		System.out.println("[" + producer.getProducerName() + "] Acked message - " + messageId + " - with payload: "
				+ new String(message.getData()));

		long t2 = System.currentTimeMillis();
		long totalTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(t2 - t1);
		System.out.printf("Total messages produced: %s in %s seconds.%n", totalMessages.getAndAdd(1), totalTimeSeconds);
	}

	// returns the total message count
	public AtomicInteger totalMessageCount() {
		return totalMessages;
	}

}

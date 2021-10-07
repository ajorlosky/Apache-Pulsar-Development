package consumers;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

import config.AppConfig;

public class MessageReader {

	public static void main(String[] args) throws PulsarClientException {
		/*
		 * Uses reader interface to process messages sent by producer
		 */

		PulsarClient pulsarClient = PulsarClient.builder().serviceUrl(AppConfig.SERVICE_URL).build();

		MessageReader reader = new MessageReader();
		reader.readMessages(pulsarClient);

		pulsarClient.close();
	}

	public void readMessages(PulsarClient pulsarClient) throws PulsarClientException {

		// declares a message reader and starts reading at the earliest/latest message of the specified topic
		Reader<byte[]> reader = pulsarClient.newReader().topic("persistent://investments/stocks/tickers-data")
				.readerName("my-reader").startMessageId(MessageId.latest).create();

		System.out.println("\nReading incoming messages...");

		Message<byte[]> message = reader.readNext();

		// outputs message read with ticker name, message id, and data
		while (message != null) {
			System.out
					.println("\nProducer name: " + message.getProducerName() + "\nMessage ID: " + message.getMessageId()
							+ "\nTicker name: " + message.getKey() + "\nTicker data: " + new String(message.getData()));
			message = reader.readNext();
		}
		
		// used for reader errors, so it saves the bytes of the message failure and restarts at that point
		// byte[] msgIdBytes = // some message byte array (message.getBytes() --> returns byte array of message data)
		// MessageId id = MessageId.fromByteArray(msgIdBytes); // specifies start point
		
		return;
	}
}

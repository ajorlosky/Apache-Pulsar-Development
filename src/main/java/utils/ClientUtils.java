package utils;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import config.AppConfig;

public class ClientUtils {

	// utility for initiating the pulsar client according to the service URL in the
	// config
	public static PulsarClient initPulsarClient() throws PulsarClientException {
		return PulsarClient.builder().serviceUrl(AppConfig.SERVICE_URL).build();
	}

}

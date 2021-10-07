package config;

public class AppConfig {

	/*
	 * Sets up the pulsar client information and service urls (localhost to run locally)
	 */
	public static final String SERVICE_HTTP_URL = "http://pulsar.evoservices.io:8080";
	public static final String SERVICE_URL = "pulsar://pulsar.evoservices.io:6650"; // multi-node cluster
	public static final String singleTopic = "persistent://investments/stocks/tickers"; //persistent://tenant/namespace/topic
}

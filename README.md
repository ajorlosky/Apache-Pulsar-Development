<h1> StockTickers in Pulsar</h1>

**Project Description:**
<p> In this project, I have included several files that enable a user to ingest StockTicker data from a Producer as messages into a Pulsar instance (Standalone, Multi-node cluster, and Multiple Clsuters). In addition, there are a variety of Consumer subscription types that enable different subscription types available in Apache Pulsar by simply running the subscription of choice in the consumers package. The generated payload data is formatted with JSON Schema for the StockTicker object, so that the key is a ticker symbol and message includes current stock information. Using this data streaming model, it should provide us with data to ingest into our own Pulsar instance, which can then be used to modify our Pulsar instance for potential customer needs. </p>

<hr>

**Package Descriptions:**
<ul>
    <li> 
        Config: includes an AppConfig file that specifies the http url, pulsar service url, and single topic name used for running a single Pulsar instance. 
    </li>
    <li> 
        Consumers: includes individual consumers for a Simple Consumer and for each subscription type (Exclusive, Failover, Keyshared, Shared) using the test-subscription name. These consumers are used in the Pulsar Client to acknowledge messages sent to the topic from the Producer. In addition, there is a message reader in this package that provides basic functionality for the Reader API in Pulsar.
    </li>
    <li> 
        Interceptors: defines various methods that intercept and potentially mutates the message received by the producer before they are sent to the broker.
    </li>
    <li> 
        Models: holds the data fields for an individual stock ticker, which includes date, name/symbol, current price, open price, previous close price, high price, low price, and trade volume. These values are all included in the message sent in the payload. In addition, StockTicker SerDe (Serialization/Deserialization) and the Pulsar function TickerEstiamtes.java are included in this package.
    </li>
    <li> 
        Producers: defines a synchronous and asynchronous producer used to send messages to the topic. PulsarClient defines a stock-ticker-producer of the JSON Schema to produce messages for all of the stock tickers in the specified list. Users interact with a UI display to generate stock ticker data for a given list, select continuous or user-selected rate/time of messaging, and choose whether to update stock-ticker data as it is running or not. Recently, new classes were added, so that multiple producers can send messages to unique topics for a specific ticker symbol.
    </li>
    <li> 
        Utilities: contains a variety of utilities for the pulsar client and stock ticker data processing to perform important tasks, such as initiating the pulsar client, defining/disconnecting a consumer thread, and loading the stock ticker data for an individual or group of symbols using the YahooFinanceAPI.
    </li>
    <li> 
        Resources: In src/main/resources, the datasets used for the ticker-lists are stored. In ticker-list.txt, a wider variety of symbols are used for data analysis purpose across the market, when update-ticker-list.txt has a select 10 tickers used to tested Key-shared/Shared subscription types and other Pulsar architecture. Additionally, the ticker-message-output.csv stores an organized format of message data utilized by the Jupyter Notebook. YahooStockAPI contains functions that demonstrate how these files are utilized by producers in the application.
    </li>
</ul>

<hr>

**Project Updates**
<ol>
    <li> 
        In this version of the StockTicker Project, producer classes can be used for testing of the multi-node cluster according to the criteria specified in Issue #16: Multi-node Cluster Testing. Using AsyncProducerUniqueTopics or SyncProducerUniqueTopics, a new producer is created on a unique topic for each stock-ticker symbol. Each of these producers will push messages to their respective topics all at the same or similar rate. To simulate both bundle splitting and offloading, changes were made to the multi-node cluster that were justified by the behavior in shown in Pulsar Manager. These results are further described in issue #16. 
    </li>
    <li>
        In addition, Jupyter Notebook of Pulsar Ticker Project.ipynb stores a jupyter notebook that performs some data analysis on the stock-ticker message data in Pulsar. Further development is needed to demonstrate Jupyter UI capabilities, including Pulsar SQL and Functions in Jupyter. These additions will be added in further updated versions. 
    </li>
    <li>
        Lastly, this version has been tested successfull with Pulsar Functions on the standalone cluster on sigma05.
        To simulate this, download the ticker-estimates.jar and foloow the Pulsar Functions instructions in Issue #17.
        The details of the Pulsar Function are located in models/Ticker-Estimates.java.
    </li>
</ol>

<hr>

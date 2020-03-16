# kafka-tutorial

Apache Kafka is a publish/subscribe messaging system often described as a “distributed commit log” or more recently as a “distributing streaming platform.”

### Message
- The unit of data within Kafka is called a message.
- It similar to a row or a record in a database. 
- A message is simply an array of bytes as far as Kafka is concerned, so the data contained within it does not have a specific format or meaning to Kafka.
- A message can have an optional bit of metadata, which is referred to as a **key**. The key is also a byte array and, as with the message, has no specific meaning to Kafka.

### Key
Keys are used when messages are to be written to partitions in a more controlled manner.

### Batch
- For efficiency, messages are written into Kafka in batches. 
- A batch is just a collection of messages, all of which are being produced to the same topic and partition. 
- An individual roundtrip across the network for each message would result in excessive overhead, and collecting messages together into a batch reduces this. 

### Schema
- Additional structure, or schema, be imposed on the message content so that it can be easily understood.
- Some common available options for message schema are
    - JSON
    - XML
    - Avro

### Topic
- Messages in Kafka are categorized into topics. 
- The closest analogies for a topic are a database table or a folder in a filesystem. 

### Partitition
- Topics are additionally broken down into a number of partitions. 
- A partition is a single log. 
- Messages are written to it in an append-only fashion, and are read in order from beginning to end. 
- Partitions are also the way that Kafka provides redundancy and scalability. Each partition can be hosted on a different server.
- Order of messages is guaranteed only within a partition(not across partitions).
- Once data is written in a partition it cannot be changed.   

### Producer
- Producers create new messages.
- A message will be produced to a specific topic.
- Producers already know to which broker and partition to write to. 
- By default, the producer does not care what partition a specific message is written to and will balance messages over all partitions of a topic evenly. 
- In some cases, the producer will direct messages to specific partitions. This is typically done using the message key and a partitioner that will generate a hash of the key and map it to a specific partition. This assures that all messages produced with a given key will get written to the same partition. 
- In case of broker failure, producers will automatically recover.
- Producers can choose to recieve acknowledgement of data writes.
    - **acks=0** : Producer won't wait for acknowledgement(possible data loss).
    - **acks=1** : Producer will wait for leader acknowledgement(limited data loss).
    - **acks="all"** : Leader + replicas acknowledgement(no data loss).
- Producers can choose to send key with the message.
- If key is null, data is sent to each partition in a round robbin fashion.
- If key is present, then all messages having key will always go to the same partition.

#### Producer retries
- In case of transient failures, developers are expected to handle failures otherwise the data will be lost.
- Example of transient failure
  - `NotEnoughReplicasException`
- There is a `retries` setting which defaults to 0 but can be set to a high number. e.g. - `Integer.MAX_VALUE`.
- In case of failure, by default there is a chance that messages will be sent out of order (if batch has failed to be sent).
- If you rely on key based ordering, this can be an issue.
- For this, you can set the setting `max.in.flight.requests.per.connection` which controls how many produce requests are made in parallel. Default setting is 5 but should be set to 1 if ordering of messages is necesssary (may impact throughput).

#### Idempotent producer
A producer can introduce duplicate messages due to network errors.
Here's how it can happen.

1. Producer makes a produce request to broker.
2. Broker commits the message and sends an ack.
3. Due to network error ack doesn't reaches the producer.
4. Since, producer didn't get ack, it retries the message.
5. If the network error goes away, the message is produced again and this leads to a duplicate record.

Idempotent producers are great to guarantee a stable and safe pipeline.
1. Producer sends an ID every time it produces a message.
2. If broker sees a duplicate ID, it doesn't commit the message again but it sends ack to the producer.
3. This prevents the message from being duplicated.

For a safe producer
1. Use idempotent producer, set `enable.idempotence` property of Kafka Producer to `true` => `acks="all"`, `retries=Integer.MAX_INT`, `max.in.flight.requests.per.connection=5` at producer level.
2. Set `min.insync.replicas=2` at broker/topic level.

### Consumer
- Consumers read messages.
- The consumer subscribes to one or more topics and reads the messages **in the order in which they were produced**.
- The consumer keeps track of which messages it has already consumed by keeping track of the **offset** of messages.
- The **offset** is another bit of metadata—an integer value that continually increases—that Kafka adds to each message as it is produced. Each message in a given partition has a unique offset.
- By storing the offset of the last consumed message for each partition, either in Zookeeper or in Kafka itself, a consumer can stop and restart without losing its place.
- An offset doesn't has any meaning on its own without partition.

### Consumer group
- Consumers work as part of a consumer group, which is one or more consumers that work together to consume a topic. 
- The group assures that each partition is only consumed by one member.
- The mapping of a consumer to a partition is often called ownership of the partition by the consumer.
- In this way, consumers can horizontally scale to consume topics with a large number of messages. Additionally, if a single consumer fails, the remaining members of the group will rebalance the partitions being consumed to take over for the missing member. 
- If you have more consumers than the number of partitions, some will be inactive.

### Consumer offsets
- Kafka stores the offsets at which a consumer group has been reading.
- The commited offsets live in a kafka topic named `__consumer_offsets`.
- When a consumer in a group has processed data, it should be committing offsets.
- If a consumer dies, it will be able to read from where it left off, thanks to the committed offsets.

### Delivery semantics for consumers
- Consumers choose when to commit offsets.
- There are 3 types of delivery semantics:
    1. **At most once**
        - Offsets are committed as soon as the message is received.
        - If the processing goes wrong, message will be lost (it won't be read again).
    2. **Atleast once**
        - Offsets are committed after message is processed.
        - If processing goes wrong message will be read again.
        - This can result in duplicate processing of messages. That's why we should make sure that processing of messages is idempotent (i.e. processing the messages again won't impact your system).
    3. **Exactly once**
        - Can be achieved for kafka to kafka workflows using Kafka Streams API.
        - For kafka to external system workflow use an idempotent consumer.

### Broker
- A single Kafka server is called a broker.
- The broker receives messages from producers, assigns offsets to them, and commits the messages to storage on disk.
- It also services consumers, responding to fetch requests for partitions and responding with the messages that have been committed to disk.

### Kafka cluster
- Kafka brokers are designed to operate as part of a cluster. 
- Each broker is identified by it's ID (integer).
- Within a cluster of brokers, one broker will also function as the cluster controller (elected automatically from the live members of the cluster). 
- The controller is responsible for administrative operations, including assigning partitions to brokers and monitoring for broker failures. 
-  A partition is owned by a single broker in the cluster, and that broker is called the leader of the partition. 
- A partition may be assigned to multiple brokers, which will result in the partition being replicated. This provides redundancy of messages in the partition, such that another broker can take over leadership if there is a broker failure.
- However, all consumers and producers operating on that partition must connect to the leader.

### Kafka broker discovery
- Every kafka broker is also called a bootstrap server.
- That means you only need to connect to one broker and you will be connected to the entire cluster.
- Each broker knows about all brokers, topics and partitions.

### Zookeeper
- Zookeeper manages brokers.
- It helps in performing leader election of partitions.
- It sends notifications to kafka in case of changes.
- Kafka can't work without zookeeper.
- By design, it operates with an odd number of servers (generally 3,5,7 ...)
- It has a leader(handles writes) and rest of them are followers(handles reads).
- Zookeeper doesn't store consumer offsets after Kafka > 0.10

### Replication factor
- A partition is replicated across the kafka cluster and the number of copies each partition has is given by its replication factor.
- At any time only one broker can be a leader of a given partition.
- Only that leader can receive and serve data for that partition.
- Other brokers will just synchronise the data.
- Therefore, each partition has one leader and multiple ISRs(in-sync replicas).
- With a replication factor of N, producers and consumers can tolerate up to N-1 brokers being down.

### Retention policy
- Retention is the durable storage of messages for some period of time.
- Kafka brokers are configured with a default retention setting for topics, either retaining messages for some period of time (e.g., 7 days) or until the topic reaches a certain size in bytes (e.g., 1 GB). Once these limits are reached, messages are expired and deleted.
- Topics can also be configured as **log compacted**, which means that Kafka will retain only the last message produced with a specific key. This can be useful for changelog-type data, where only the last update is interesting.

### Mirror maker
- When working with multiple datacenters in particular, it is often required that messages be copied between them. 
- The replication mechanisms within the Kafka clusters are designed only to work within a single cluster, not between multiple clusters.
- MirrorMaker is used for this purpose. At its core, MirrorMaker is simply a Kafka consumer and producer, linked together with a queue. Messages are consumed from one Kafka cluster and produced for another. 


## Useful kafka commands

To start kafka, follow the steps.

1. Download and extract Kafka binary from [here](https://kafka.apache.org/downloads).
2. Start zookeeper

``` bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

3. Start kafka server
``` bash
bin/kafka-server-start.sh config/server.properties
```

### Create a topic
``` bash
bin/kafka-topics.sh --create \
--bootstrap-server localhost:9092 \
--replication-factor 1 \
--partitions 1 \
--topic test
```

### List all topics
``` bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Describe a topic
``` bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 \
--topic test \
--describe
```

### Delete a topic
``` bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 \
--topic test \
--delete
```

### Produce records to a topic via console producer
``` bash
bin/kafka-console-producer.sh \
--broker-list localhost:9092 \
--topic test
```

### Consume records from a topic via console consumer
``` bash
bin/kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic test \
--from-beginning
```

### Consume records from a topic via console consumer as a part of consumer group
``` bash
bin/kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic test1 \
--group test1-cg-1
```

### Describe a consumer group
``` bash
bin/kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group test1-cg-1 \
--describe
```

We can see an output like this which shows all the partitions and their respective lag.

```
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
test2-cg-2      test2           1          3               3               0               -               -               -
test2-cg-2      test2           0          5               5               0               -               -               -
test2-cg-2      test2           2          3               3               0               -               -               -

```

### Resetting offsets

There are several strategies that can be used to reset offsets.

1. **to-datetime**
2. **by-period**
3. **to-earliest** - Shifts the current offset to the beginning of topic
4. **to-latest**
5. **shift-by** - Shifts the current offsets in all partitions by specified number of offsets, use positive number to shift offsets forward and negative to shift them backward.
6. **from-file**
7. **to-current** - Shifts the current offset to the end of topic

It is always good to perform a dry run before actually resetting the offsets. This can be done using flag `--dry-run` instead of `--execute` flag.

``` bash
bin/kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group test1-cg-1 \
--topic test1 \     
--reset-offsets \
--to-earliest \ # Use any one of the 7 strategies
--execute # Execute same command using dry-run first
```


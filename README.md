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

### Producer
- Producers create new messages.
- A message will be produced to a specific topic.
- By default, the producer does not care what partition a specific message is written to and will balance messages over all partitions of a topic evenly. 
- In some cases, the producer will direct messages to specific partitions. This is typically done using the message key and a partitioner that will generate a hash of the key and map it to a specific partition. This assures that all messages produced with a given key will get written to the same partition. 

### Consumer
- Consumers read messages.
- The consumer subscribes to one or more topics and reads the messages **in the order in which they were produced**.
- The consumer keeps track of which messages it has already consumed by keeping track of the **offset** of messages.
- The **offset** is another bit of metadata—an integer value that continually increases—that Kafka adds to each message as it is produced. Each message in a given partition has a unique offset.
- By storing the offset of the last consumed message for each partition, either in Zookeeper or in Kafka itself, a consumer can stop and restart without losing its place.

### Consumer group
- Consumers work as part of a consumer group, which is one or more consumers that work together to consume a topic. 
- The group assures that each partition is only consumed by one member.
- The mapping of a consumer to a partition is often called ownership of the partition by the consumer.
- In this way, consumers can horizontally scale to consume topics with a large number of messages. Additionally, if a single consumer fails, the remaining members of the group will rebalance the partitions being consumed to take over for the missing member. 

### Broker
- A single Kafka server is called a broker.
- The broker receives messages from producers, assigns offsets to them, and commits the messages to storage on disk.
- It also services consumers, responding to fetch requests for partitions and responding with the messages that have been committed to disk.

## Kafka cluster
- Kafka brokers are designed to operate as part of a cluster. 
- Within a cluster of brokers, one broker will also function as the cluster controller (elected automatically from the live members of the cluster). 
- The controller is responsible for administrative operations, including assigning partitions to brokers and monitoring for broker failures. 
-  A partition is owned by a single broker in the cluster, and that broker is called the leader of the partition. 
- A partition may be assigned to multiple brokers, which will result in the partition being replicated. This provides redundancy of messages in the partition, such that another broker can take over leadership if there is a broker failure.
- However, all consumers and producers operating on that partition must connect to the leader.

### Retention policy
- Retention is the durable storage of messages for some period of time.
- Kafka brokers are configured with a default retention setting for topics, either retaining messages for some period of time (e.g., 7 days) or until the topic reaches a certain size in bytes (e.g., 1 GB). Once these limits are reached, messages are expired and deleted.
- Topics can also be configured as **log compacted**, which means that Kafka will retain only the last message produced with a specific key. This can be useful for changelog-type data, where only the last update is interesting.

### Mirror maker
- When working with multiple datacenters in particular, it is often required that messages be copied between them. 
- The replication mechanisms within the Kafka clusters are designed only to work within a single cluster, not between multiple clusters.
- MirrorMaker is used for this purpose. At its core, MirrorMaker is simply a Kafka consumer and producer, linked together with a queue. Messages are consumed from one Kafka cluster and produced for another. 
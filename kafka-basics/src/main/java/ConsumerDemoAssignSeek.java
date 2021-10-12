import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/*
We do not have group id & we do not subscribe to a topic.
Assign and Seek mostly used to replay data or fetch a specific message
 */
public class ConsumerDemoAssignSeek {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class);

    public static void main(String[] args) {
        try {
            String bootstrapServers = "127.0.0.1:9092";

            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            //Kafka consumer
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

            //Assign
            String topic = "first_topic";
            TopicPartition partitionToReadFrom = new TopicPartition(topic, 0);
            consumer.assign(Collections.singleton(partitionToReadFrom));

            //Seek
            long offsetToReadFrom = 15L;
            consumer.seek(partitionToReadFrom, offsetToReadFrom);

            //Read from offset 15 and continue reading till 5 messages i.e offset 19
            int numberOfMessagesToRead = 5;
            boolean keepOnReading = true;
            int numberOfMessagesReadSoFar = 0;

            //poll() for new data
            while (keepOnReading) {
                //consumer.poll(100);//poll() is deprecated, new in kafka 2.0.0
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000));// timeout of 100 millis
                for (ConsumerRecord<String, String> record : records) {
                    numberOfMessagesReadSoFar += 1;
                    logger.info("Key: " + record.key() + ", Value: " + record.value());
                    logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());

                    if (numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
                        keepOnReading = false;
                        break;
                    }
                }
            }

            logger.info("exiting the application");
        } catch (Exception e) {

        }
    }
}

package com.github.onsvg.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemo {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

    public static void main(String[] args) {
        try {
            String bootstrapServers = "127.0.0.1:9092";
            String topic = "first_topic";

            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

            // Whatever is consumed from KAFKA is to be deserialized into real object from bytes.
            // Hence, deserializer tells what kind of data was sent to KAFKA.
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

            //set group id
            String groupId = "my-fourth-application";
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            //set auto offset reset value- earliest/latest/none
            //  earliest:       automatically reset the offset to the earliest offset
            //  latest:         automatically reset the offset to the latest offset
            //  none:           throw exception to the consumer if no previous offset is found for the consumer's group
            //  anything else:  throw exception to the consumer.
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            //subscribe consumer to our topic(s)
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
            //Subscribe to only one topic, hence used singleton
            //You may use Arrays.asList("first_topic","second_topic")
            consumer.subscribe(Arrays.asList(topic));

            //poll() for new data
            while (true) {
                //consumer.poll(100);//poll() is deprecated, new in kafka 2.0.0
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000));// timeout of 100 millis
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Key: " + record.key() + ", Value: " + record.value());
                    logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }
            }

        } catch (Exception e) {

        }
    }
}


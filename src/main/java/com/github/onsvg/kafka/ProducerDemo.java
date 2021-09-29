package com.github.onsvg.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemo {

    public static void main(String[] args) {
        try {
            String bootstrapServers = "127.0.0.1:9092";

            Properties properties = new Properties();
            properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

            // Whatever is sent to KAFKA is converted to bytes. Hence, serializer tells what kind of data is being sent to KAFKA.
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            // Create Kafka producer, here the key is of type String & value is of type String
            KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

            //create a record to be sent to KAFKA , first argument is topic_name & second is data to be pushed to KAFKA.
            ProducerRecord<String, String> record = new ProducerRecord<>("first_topic", "hello_world");

            //send the data
            producer.send(record);

            //flush the data- If we don't do flush() , the data may not be sent as, send() is asynchronous. Before the send
            // operation is finished, the program exits, this actually flushes the data what ever is produced.
            producer.flush();

            //flush & close the producer
            producer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

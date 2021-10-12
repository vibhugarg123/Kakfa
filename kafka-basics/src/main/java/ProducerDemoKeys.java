import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoKeys {
    public static Logger logger = LoggerFactory.getLogger(ProducerDemoKeys.class);

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

            for (int i = 0; i < 10; i++) {
                String topic = "first_topic";
                String value = "hello_world " + Integer.toString(i);
                String key = "id_" + Integer.toString(i);
                //create a record to be sent to KAFKA , first argument is topic_name & second is data to be pushed to KAFKA.
                //When you send key in send(), nevertheless number of times you run the code, for same key, it will go
                //to same partition, if number of partitions are constant.
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                logger.info("Key: " + key);

                //send the data
                producer.send(record, new Callback() {
                    //executes every time a record is successfully sent or an exception is thrown
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (null == e) {
                            logger.info("Received new metadata. \n" +
                                    "Topic: " + recordMetadata.topic() + "\n" +
                                    "Partition: " + recordMetadata.partition() + "\n" +
                                    "Offset: " + recordMetadata.offset() + "\n" +
                                    "Timestamp: " + recordMetadata.timestamp());
                        } else {
                            logger.error("er    ror while sending record", e);
                        }
                    }
                }).get();//block the send() to make it synchronous- don't do this in production!!
            }

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

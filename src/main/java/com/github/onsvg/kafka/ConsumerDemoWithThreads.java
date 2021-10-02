package com.github.onsvg.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/*
Output- when you stop the application
[Thread-2] INFO com.github.onsvg.kafka.ConsumerDemoWithThreads - Caught shutdown hook
[Thread-1] INFO com.github.onsvg.kafka.ConsumerRunnable - Received shutdown signal!
[main] INFO com.github.onsvg.kafka.ConsumerDemoWithThreads - application is closing
[Thread-2] INFO com.github.onsvg.kafka.ConsumerDemoWithThreads - Application has exited

When you stop the application-
1. ShutdownHook() will be called & shutdown() of runnable will be called
2. consumer.wakeup()-> WakeUpException->consumer.close()->CountDownLatch()
3. Current execution of main will complete
4. ShutdownHook() method will be completed.
 */

public class ConsumerDemoWithThreads {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);

    public static void main(String[] args) {
        new ConsumerDemoWithThreads().run();
    }

    private ConsumerDemoWithThreads() {

    }

    private void run() {
        try {
            String bootstrapServers = "127.0.0.1:9092";
            String topic = "first_topic";
            String groupId = "my-sixth-application";

            /*latch for dealing with multiple threads
            When we create an object of CountDownLatch, we specify the number of threads it should wait for,
            all such thread are required to do count down by calling CountDownLatch.countDown()
            once they are completed or ready to the job. As soon as count reaches zero, the waiting task starts running.
             */
            CountDownLatch latch = new CountDownLatch(1);
            logger.info("creating consumer thread");

            //create consumer runnable
            Runnable myConsumerRunnable = new ConsumerRunnable(
                    bootstrapServers,
                    groupId,
                    topic,
                    latch
            );

            // Let us create one thread and start it
            Thread myThread = new Thread(myConsumerRunnable);
            myThread.start();

            //add shutdown hook-  you will see that the shutdown hook is getting
            // called by the JVM when it finishes the execution of the main method.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Caught shutdown hook");
                ((ConsumerRunnable) myConsumerRunnable).shutdown();
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Application has exited");
            }));


            // wait until the application is over! The main task waits for one thread
            latch.await();
        } catch (InterruptedException e) {
            logger.error("application got interrupted", e);
        } catch (Exception e) {
            logger.error("error", e);
        } finally {
            logger.info("application is closing");
        }
    }
}

class ConsumerRunnable implements Runnable {

    public static final Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

    private CountDownLatch latch;
    private KafkaConsumer<String, String> consumer;

    public ConsumerRunnable(String bootstrapServers, String groupId, String topic, CountDownLatch latch) {
        this.latch = latch;
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Whatever is consumed from KAFKA is to be deserialized into real object from bytes.
        // Hence, deserializer tells what kind of data was sent to KAFKA.
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        //set group id
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        //set auto offset reset value- earliest/latest/none
        //  earliest:       automatically reset the offset to the earliest offset
        //  latest:         automatically reset the offset to the latest offset
        //  none:           throw exception to the consumer if no previous offset is found for the consumer's group
        //  anything else:  throw exception to the consumer.
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //subscribe consumer to our topic(s)
        this.consumer = new KafkaConsumer<>(properties);
        //Subscribe to only one topic, hence used singleton
        //You may use Arrays.asList("first_topic","second_topic")
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void run() {
        try {
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
        } catch (WakeupException e) {
            logger.info("Received shutdown signal!");
        } finally {
            consumer.close();
            //very important- tell our main code we are down with the consumer
            latch.countDown();
        }
    }

    public void shutdown() {
        //wakeup() special method to interrupt consumer.poll()
        //throws WakeUpException()
        consumer.wakeup();
    }
}
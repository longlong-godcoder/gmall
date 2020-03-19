package org.bigdata.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class MyKafkaProducer {

    private static KafkaProducer<String, String> kafkaProducer = null;

    private static Properties properties = null;

    static {

        properties = new Properties();

        properties.put("bootstrap.servers", "node-01:9092,node-02:9092,node-03:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            kafkaProducer = new KafkaProducer<String, String>(properties);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("=========kafkaProducer创建失败==========");
        }
    }

    public static void send(String topic, String msg){
        if (kafkaProducer == null) return;
        kafkaProducer.send(new ProducerRecord<>(topic, msg));
    }

}

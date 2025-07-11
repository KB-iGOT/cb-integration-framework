package com.igot.cb.producer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private Producer producer;

    @Test
    void testSend() {
        String topic = "test-topic";
        Object message = "test-message";

        // act
        producer.send(topic, message);

        // assert
        verify(kafkaTemplate, times(1)).send(topic, message);
    }
}

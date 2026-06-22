package com.igot.cb.producer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Producer Tests")
class ProducerTest {

    @InjectMocks
    private Producer producer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "integration-topic";
    private static final Object MESSAGE = Map.of("key", "value");

    @Test
    @DisplayName("send - valid topic and message - delegates to kafkaTemplate with exact args")
    void send_validTopicAndMessage_delegatesToKafkaTemplate() {
        producer.send(TOPIC, MESSAGE);

        verify(kafkaTemplate, times(1)).send(TOPIC, MESSAGE);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("send - null message - delegates to kafkaTemplate with null message")
    void send_nullMessage_delegatesToKafkaTemplate() {
        producer.send(TOPIC, null);

        verify(kafkaTemplate, times(1)).send(TOPIC, null);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("send - when kafkaTemplate throws RuntimeException - exception propagates to caller")
    void send_kafkaTemplateThrowsRuntimeException_propagatesToCaller() {
        doThrow(new RuntimeException("Kafka broker unavailable"))
                .when(kafkaTemplate).send(anyString(), any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> producer.send(TOPIC, MESSAGE));

        assertEquals("Kafka broker unavailable", ex.getMessage());
        verify(kafkaTemplate, times(1)).send(TOPIC, MESSAGE);
    }
}


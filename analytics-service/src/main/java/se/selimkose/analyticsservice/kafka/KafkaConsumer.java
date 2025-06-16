package se.selimkose.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics ="patient-events", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {

        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            //TODO business logic
            log.info("Received PatientEvent: {}", patientEvent.toString());
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse PatientEvent from byte array", e.getMessage());
        }

    }
}

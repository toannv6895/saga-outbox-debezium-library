package com.toannguyen.outbox.framework.event;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import com.toannguyen.outbox.framework.mapper.OutboxMapper;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@RequiredArgsConstructor
public class OutboxEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(OutboxEventHandler.class);

    private final EntityManager entityManager;
    private final OutboxMapper outboxMapper;

    @EventListener
    @Transactional(propagation = MANDATORY)
    public void on(ExportedEvent<String, JsonNode> event) {
        try (var session = entityManager.unwrap(Session.class)) {
            logger.info("An exported event was found for type {}", event.getType());
            var outbox = outboxMapper.toOutbox(event);
            session.persist(outbox);
        }
    }
}

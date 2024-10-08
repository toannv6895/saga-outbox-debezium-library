package com.toannguyen.outbox.framework.configuration;

import com.toannguyen.outbox.framework.event.OutboxEventHandler;
import com.toannguyen.outbox.framework.internal.SagaState;
import jakarta.persistence.EntityManager;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.toannguyen.outbox.framework.mapper.OutboxMapper;

@Configuration
@EntityScan("com.toannguyen")
public class OutboxConfiguration {
    @Bean
    public SagaState initSagaState() {
        return new SagaState();
    }

    @Bean
    OutboxEventHandler outboxEventHandler(EntityManager entityManager, OutboxMapper outboxMapper) {
        return new OutboxEventHandler(entityManager, outboxMapper);
    }

    @Bean
    public OutboxMapper outboxMapper() {
        return Mappers.getMapper(OutboxMapper.class);
    }
}
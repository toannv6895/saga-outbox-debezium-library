package com.toannguyen.outbox.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.toannguyen.outbox.framework.internal.SagaState;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SagaManager {

    private final ApplicationEventPublisher event;

    private final EntityManager entityManager;

    public <S extends SagaBase> S begin(Class<S> sagaType, JsonNode payload) {
        try {
            UUID sagaId = UUID.randomUUID();

            SagaState state = new SagaState();
            state.setId(sagaId);
            state.setType(sagaType.getAnnotation(Saga.class).type());
            state.setPayload(payload);
            state.setSagaStatus(SagaStatus.STARTED);
            state.setStepStatus(JsonNodeFactory.instance.objectNode());
            entityManager.persist(state);


            S saga = sagaType.getConstructor(ApplicationEventPublisher.class, SagaState.class, EntityManager.class).newInstance(event, state, entityManager);
            saga.advance();
            return saga;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <S extends SagaBase> S find(Class<S> sagaType, UUID sagaId) {
        SagaState state = entityManager.find(SagaState.class, sagaId);

        if (state == null) {
            return null;
        }

        try {
            return sagaType.getConstructor(ApplicationEventPublisher.class, SagaState.class, EntityManager.class).newInstance(event, state, entityManager);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

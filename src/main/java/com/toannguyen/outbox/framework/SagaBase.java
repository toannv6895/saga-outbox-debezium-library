package com.toannguyen.outbox.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.toannguyen.outbox.framework.event.SagaEvent;
import com.toannguyen.outbox.framework.internal.ConsumedMessage;
import com.toannguyen.outbox.framework.internal.SagaState;
import com.toannguyen.outbox.framework.internal.SagaStepStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.*;

public abstract class SagaBase {

    private static final Logger LOG = LoggerFactory.getLogger(SagaBase.class);

    private final ApplicationEventPublisher event;

    private final SagaState state;

    protected final EntityManager entityManager;

    protected SagaBase(ApplicationEventPublisher event, SagaState state, EntityManager entityManager) {
        this.event = event;
        this.state = state;
        this.entityManager = entityManager;
    }

    public final UUID getId() {
        return state.getId();
    }

    public final JsonNode getPayload() {
        return state.getPayload();
    }

    public final String getType() {
        return getClass().getAnnotation(Saga.class).type();
    }

    public final List<String> getStepIds() {
        return Arrays.asList(getClass().getAnnotation(Saga.class).stepIds());
    }

    public SagaStatus getStatus() {
        return entityManager.find(SagaState.class, getId()).getSagaStatus();
    }

    protected void onStepEvent(String type, SagaStepStatus status) {
        try {
            ObjectNode stepStatus = (ObjectNode) state.getStepStatus();
            stepStatus.put(type, status.name());

            if (status == SagaStepStatus.SUCCEEDED) {
                advance();
            }
            else if (status == SagaStepStatus.FAILED || status == SagaStepStatus.COMPENSATED) {
                goBack();
            }

            EnumSet<SagaStepStatus> allStatus = EnumSet.noneOf(SagaStepStatus.class);
            Iterator<String> fieldNames = stepStatus.fieldNames();
            while (fieldNames.hasNext()) {
                allStatus.add(SagaStepStatus.valueOf(stepStatus.get(fieldNames.next()).asText()));
            }

            state.setSagaStatus(getSagaStatus(allStatus));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract SagaStepMessage getStepMessage(String id);

    protected abstract SagaStepMessage getCompensatingStepMessage(String id);

    protected void processed(UUID eventId) {
        entityManager.persist(new ConsumedMessage(eventId, Instant.now()));
    }

    protected boolean alreadyProcessed(UUID eventId) {
        LOG.debug("Looking for event with id {} in message log", eventId);
        return entityManager.find(ConsumedMessage.class, eventId) != null;
    }

    private SagaStatus getSagaStatus(EnumSet<SagaStepStatus> stepStates) {
        if (containsOnly(stepStates, SagaStepStatus.SUCCEEDED)) {
            return SagaStatus.COMPLETED;
        }
        else if (containsOnly(stepStates, SagaStepStatus.STARTED, SagaStepStatus.SUCCEEDED)) {
            return SagaStatus.STARTED;
        }
        else if (containsOnly(stepStates, SagaStepStatus.FAILED, SagaStepStatus.COMPENSATED)) {
            return SagaStatus.ABORTED;
        }
        else {
            return SagaStatus.ABORTING;
        }
    }

    private boolean containsOnly(Collection<SagaStepStatus> stepStates, SagaStepStatus status) {
        for (SagaStepStatus sagaStepStatus : stepStates) {
            if (sagaStepStatus != status) {
                return false;
            }
        }

        return true;
    }

    private boolean containsOnly(Collection<SagaStepStatus> stepStates, SagaStepStatus status1, SagaStepStatus status2) {
        for (SagaStepStatus sagaStepStatus : stepStates) {
            if (sagaStepStatus != status1 && sagaStepStatus != status2) {
                return false;
            }
        }

        return true;
    }

    protected final void advance() throws JsonProcessingException {
        String nextStep = getNextStep();

        if (nextStep == null) {
            state.setCurrentStep(null);
            return;
        }

        SagaStepMessage stepEvent = getStepMessage(nextStep);
        event.publishEvent(new SagaEvent(getId(), stepEvent.type, stepEvent.eventType, stepEvent.payload));

        ObjectNode stepStatus = (ObjectNode) state.getStepStatus();
        stepStatus.put(nextStep, SagaStepStatus.STARTED.name());

        state.setCurrentStep(nextStep);
    }

    protected final void goBack() throws JsonProcessingException {
        String previousStep = getPreviousStep();

        if (previousStep == null) {
            state.setCurrentStep(null);
            return;
        }

        SagaStepMessage stepEvent = getCompensatingStepMessage(previousStep);
        event.publishEvent(new SagaEvent(getId(), stepEvent.type, stepEvent.eventType, stepEvent.payload));

        ObjectNode stepStatus = (ObjectNode) state.getStepStatus();
        stepStatus.put(previousStep, SagaStepStatus.COMPENSATING.name());

        state.setCurrentStep(previousStep);
    }

    protected String getCurrentStep() {
        return state.getCurrentStep();
    }

    private String getNextStep() {
        if (getCurrentStep() == null) {
            return getStepIds().get(0);
        }

        int idx = getStepIds().indexOf(getCurrentStep());

        if (idx == getStepIds().size() - 1) {
            return null;
        }

        return getStepIds().get(idx + 1);
    }

    private String getPreviousStep() {
        int idx = getStepIds().indexOf(getCurrentStep());

        if (idx == 0) {
            return null;
        }

        return getStepIds().get(idx - 1);
    }
}

package com.toannguyen.outbox.framework.internal;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import com.toannguyen.outbox.framework.SagaStatus;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "saga_state")
public class SagaState {

    @Id
    private UUID id;

    @Version
    private int version;

    private String type;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private JsonNode payload;

    private String currentStep;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private JsonNode stepStatus;

    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;
}

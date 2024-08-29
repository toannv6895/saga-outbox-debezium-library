package com.toannguyen.outbox.framework.outbox;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outboxevent")
@Getter
@Setter
public class Outbox implements Serializable {
    @Id
    private UUID id;
    private Instant timestamp;
    private String aggregateId;
    private String aggregateType;
    private String type;
    @Type(JsonType.class)
    private Object payload;
}

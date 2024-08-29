package com.toannguyen.outbox.framework.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.toannguyen.outbox.framework.event.ExportedEvent;
import com.toannguyen.outbox.framework.outbox.Outbox;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OutboxMapper {
    @Mapping(source = "payload", target = "payload")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    Outbox toOutbox(ExportedEvent<String, JsonNode> event);

    default Object jsonNodeToObject(JsonNode payload) {
        return payload;
    }
}


package com.toannguyen.outbox.framework.event;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BaseEvent<S, H> {
    private final UUID sagaId;
    private final UUID messageId;
    private final S status;
    private final H headers;

    public BaseEvent(UUID sagaId, UUID messageId, S status, H headers) {
        this.sagaId = sagaId;
        this.messageId = messageId;
        this.status = status;
        this.headers = headers;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sagaId=" + sagaId + ", messageId=" + messageId + ", status=" + status + "]";
    }
}

package com.toannguyen.outbox.framework.internal;

public enum SagaStepStatus {
    STARTED, FAILED, SUCCEEDED, COMPENSATING, COMPENSATED;
}

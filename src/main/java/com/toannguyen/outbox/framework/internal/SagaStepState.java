package com.toannguyen.outbox.framework.internal;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SagaStepState {

    public String type;
    public SagaStepStatus status;
}

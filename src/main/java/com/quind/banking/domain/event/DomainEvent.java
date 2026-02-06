package com.quind.banking.domain.event;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredOn();

    String getAggregateId();
}

package org.tailkeep.adapter.messaging.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The Kafka adapter is on unless {@code tailkeep.messaging.provider} names another broker. */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = "tailkeep.messaging", name = "provider", havingValue = "kafka", matchIfMissing = true)
public @interface ConditionalOnKafkaProvider {
}

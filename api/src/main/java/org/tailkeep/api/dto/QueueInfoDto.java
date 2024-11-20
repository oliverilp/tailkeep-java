package org.tailkeep.api.dto;

public record QueueInfoDto(
    long queue,
    long active,
    long finished
) {} 

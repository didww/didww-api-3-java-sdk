package com.didww.sdk.resource;

/**
 * Marker interface for resources that can be the entity of a Proof.
 * Supports polymorphic relationship - entity can be either Identity or Address.
 */
public interface ProofEntity {
    String getId();
}

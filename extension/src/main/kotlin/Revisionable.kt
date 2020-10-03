package io.holixon.axon.gateway.query

/**
 * Represents a revisionable model entity.
 */
interface Revisionable {
    /**
     * Revision value.
     */
    val revisionValue: RevisionValue
}

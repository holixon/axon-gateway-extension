package io.holixon.axon.gateway.example.approval.rest

import jakarta.validation.constraints.NotEmpty

/**
 * DTO.
 */
data class ApprovalRequestDto(
  @NotEmpty
  val subject: String,
  @NotEmpty
  val amount: String,
  @NotEmpty
  val currency: String = "EUR"
)
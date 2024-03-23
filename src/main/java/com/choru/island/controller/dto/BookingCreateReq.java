package com.choru.island.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookingCreateReq(
        @NotNull @NotBlank Long classId,
        @NotNull @NotBlank UUID uid
) {
}

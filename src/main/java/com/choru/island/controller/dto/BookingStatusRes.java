package com.choru.island.controller.dto;

public record BookingStatusRes(
        String name,
        int memberMaxCount,
        long memberCount
) {
}

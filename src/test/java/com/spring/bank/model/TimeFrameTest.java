package com.spring.bank.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeFrameTest {

    @Test
    public void when_apply_interval_on_days_should_subtract_days() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(now.minusDays(10)).isAfter(TimeFrame.DAYS.apply(11));
        assertThat(now.minusDays(12)).isBefore(TimeFrame.DAYS.apply(11));
    }

    @Test
    public void when_apply_interval_on_hours_should_subtract_hours() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(now).isAfter(TimeFrame.HOURS.apply(1));
        assertThat(now.minusMinutes(61)).isBefore(TimeFrame.HOURS.apply(1));
    }
}

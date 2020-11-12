package com.spring.bank.model;

import java.time.LocalDateTime;
import java.util.function.Function;

public enum TimeFrame {

    HOURS(interval -> LocalDateTime.now().minusHours(interval)),
    DAYS(interval -> LocalDateTime.now().minusDays(interval));

    private Function<Integer, LocalDateTime> localDateTimeFunction;

    TimeFrame(Function<Integer, LocalDateTime> localDateTimeFunction) {
        this.localDateTimeFunction = localDateTimeFunction;
    }

    public LocalDateTime apply(Integer interval) {
        return localDateTimeFunction.apply(interval);
    }
}

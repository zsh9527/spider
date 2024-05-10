package com.zsh.spider.enums;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 限制查询时间枚举
 */
@Getter
public enum LimitTimeEnum {
    DAY(1),
    WEEK(7),
    MONTH(30),
    YEAR(365);

    private final Integer diffDays;

    LimitTimeEnum(Integer diffDays) {
        this.diffDays = diffDays;
    }

    public Instant[] getStartAndEndTime() {
        Instant[] times = new Instant[2];
        var now = Instant.now();
        times[1] = now;
        times[0] = now.plus(-getDiffDays(), ChronoUnit.DAYS);
        return times;
    }

    public String getBingParam() {
        return switch (this) {
            case DAY ->  "ex1:\"ez1\"";
            case WEEK ->  "ex1:\"ez2\"";
            case MONTH ->  "ex1:\"ez3\"";
            case YEAR ->  {
                var now = Instant.now();
                var lastYear = now.plus(-getDiffDays(), ChronoUnit.DAYS);
                yield "ex1:\"ez5_" + lastYear.getEpochSecond() / 3600 / 24 + "_"  + now.getEpochSecond() / 3600 / 24 + "\"";
            }
        };
    }

    public String get360Param() {
        return switch (this) {
            case DAY ->  "d";
            case WEEK ->  "w";
            case MONTH ->  "m";
            case YEAR ->  "y";
        };
    }

    public String getSougouParam() {
        return switch (this) {
            case DAY ->  "inttime_day";
            case WEEK ->  "inttime_week";
            case MONTH ->  "inttime_month";
            case YEAR ->  "inttime_year";
        };
    }

    public String getChinaSoParam() {
        return switch (this) {
            case DAY ->  "24h";
            case WEEK ->  "1w";
            case MONTH ->  "1m";
            case YEAR ->  "1y";
        };
    }

    public String getYandexParam() {
        return switch (this) {
            case DAY ->  "77";
            case WEEK ->  "1";
            case MONTH ->  "2";
            case YEAR ->  "3";
        };
    }
}

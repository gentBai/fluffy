package com.halfhex.fluffy.security;

public class RateLimitRule {
    private final int requestsPerMinute;
    private final int requestsPerHour;
    private final int requestsPerDay;
    private final int burstSize;

    public RateLimitRule(int requestsPerMinute, int requestsPerHour, int requestsPerDay, int burstSize) {
        this.requestsPerMinute = requestsPerMinute;
        this.requestsPerHour = requestsPerHour;
        this.requestsPerDay = requestsPerDay;
        this.burstSize = burstSize;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public int getRequestsPerHour() {
        return requestsPerHour;
    }

    public int getRequestsPerDay() {
        return requestsPerDay;
    }

    public int getBurstSize() {
        return burstSize;
    }
}

package com.war.game.war_backend.controller.dto.response;

import java.lang.management.ManagementFactory;
import java.time.Instant;

public class HealthCheckResponse {
  private String status;
  private double uptime;
  private String timestamp;
  private long duration;

  public HealthCheckResponse(String status, double uptime, String timestamp, long duration) {
    this.status = status;
    this.uptime = uptime;
    this.timestamp = timestamp;
    this.duration = duration;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public double getUptime() {
    return uptime;
  }

  public void setUptime(double uptime) {
    this.uptime = uptime;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public static HealthCheckResponse ok(long duration) {
    return new HealthCheckResponse(
        "ok",
        ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0,
        Instant.now().toString(),
        duration);
  }

  public static HealthCheckResponse error(String message) {
    return new HealthCheckResponse(
        "error",
        ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0,
        Instant.now().toString(),
        -1);
  }
}

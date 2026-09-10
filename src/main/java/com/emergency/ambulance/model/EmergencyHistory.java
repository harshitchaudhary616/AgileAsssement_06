package com.emergency.ambulance.model;
import java.time.LocalDateTime;
public record EmergencyHistory(String emergencyId, String message, LocalDateTime timestamp) {}

package com.emergency.ambulance.model;

import com.emergency.ambulance.enums.*;
import java.time.LocalDateTime;

public class EmergencyRequest {
    private final String emergencyId;
    private final String patientId;
    private final String emergencyType;
    private final EmergencyPriority priority;
    private final String pickupLocation;
    private final String destinationHospital;
    private final double estimatedDistanceKm;
    private final LocalDateTime createdAt;
    private EmergencyStatus status = EmergencyStatus.WAITING;
    private String assignedAmbulanceId;
    private double estimatedArrivalMinutes;

    public EmergencyRequest(String emergencyId,String patientId,String emergencyType,EmergencyPriority priority,
                            String pickupLocation,String destinationHospital,double estimatedDistanceKm) {
        if(blank(emergencyId)||blank(patientId)||blank(emergencyType)||priority==null||blank(pickupLocation)
                ||blank(destinationHospital)||estimatedDistanceKm<0)
            throw new IllegalArgumentException("Invalid emergency request.");
        this.emergencyId=emergencyId.trim(); this.patientId=patientId.trim(); this.emergencyType=emergencyType.trim();
        this.priority=priority; this.pickupLocation=pickupLocation.trim(); this.destinationHospital=destinationHospital.trim();
        this.estimatedDistanceKm=estimatedDistanceKm; this.createdAt=LocalDateTime.now();
    }
    private boolean blank(String s){return s==null||s.trim().isEmpty();}
    public String getEmergencyId(){return emergencyId;}
    public String getPatientId(){return patientId;}
    public String getEmergencyType(){return emergencyType;}
    public EmergencyPriority getPriority(){return priority;}
    public String getPickupLocation(){return pickupLocation;}
    public String getDestinationHospital(){return destinationHospital;}
    public double getEstimatedDistanceKm(){return estimatedDistanceKm;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public EmergencyStatus getStatus(){return status;}
    public String getAssignedAmbulanceId(){return assignedAmbulanceId;}
    public double getEstimatedArrivalMinutes(){return estimatedArrivalMinutes;}
    public void allocate(String ambulanceId,double eta){status=EmergencyStatus.ALLOCATED;assignedAmbulanceId=ambulanceId;estimatedArrivalMinutes=eta;}
    public void start(){status=EmergencyStatus.ACTIVE;}
    public void complete(){status=EmergencyStatus.COMPLETED;}
}

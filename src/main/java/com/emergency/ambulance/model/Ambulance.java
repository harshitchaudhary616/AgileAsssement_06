package com.emergency.ambulance.model;

import com.emergency.ambulance.enums.*;
import com.emergency.ambulance.exception.InvalidStateTransitionException;

public class Ambulance {
    private final String ambulanceId;
    private final AmbulanceType type;
    private final Driver driver;
    private String currentLocation;
    private AmbulanceStatus status = AmbulanceStatus.AVAILABLE;
    private String activeEmergencyId;

    public Ambulance(String ambulanceId, AmbulanceType type, Driver driver, String currentLocation) {
        if (blank(ambulanceId) || type == null || driver == null || blank(currentLocation))
            throw new IllegalArgumentException("Invalid ambulance details.");
        this.ambulanceId=ambulanceId.trim();
        this.type=type;
        this.driver=driver;
        this.currentLocation=currentLocation.trim();
    }
    private boolean blank(String s){ return s==null || s.trim().isEmpty(); }
    public String getAmbulanceId(){ return ambulanceId; }
    public AmbulanceType getType(){ return type; }
    public Driver getDriver(){ return driver; }
    public String getCurrentLocation(){ return currentLocation; }
    public void setCurrentLocation(String location){ if(blank(location)) throw new IllegalArgumentException("Location required."); currentLocation=location.trim(); }
    public AmbulanceStatus getStatus(){ return status; }
    public String getActiveEmergencyId(){ return activeEmergencyId; }
    public boolean isAvailable(){ return status == AmbulanceStatus.AVAILABLE && activeEmergencyId == null; }

    public void assign(String emergencyId) {
        if (!isAvailable()) throw new IllegalStateException("Ambulance is not available.");
        if (blank(emergencyId)) throw new IllegalArgumentException("Emergency id required.");
        activeEmergencyId=emergencyId;
        status=AmbulanceStatus.DISPATCHED;
    }
    public void updateStatus(AmbulanceStatus next) {
        if(next==null) throw new IllegalArgumentException("Status required.");
        boolean valid = (status==AmbulanceStatus.DISPATCHED && next==AmbulanceStatus.EN_ROUTE)
            || (status==AmbulanceStatus.EN_ROUTE && next==AmbulanceStatus.PATIENT_PICKED_UP)
            || (status==AmbulanceStatus.PATIENT_PICKED_UP && next==AmbulanceStatus.HOSPITAL_ARRIVED);
        if(!valid) throw new InvalidStateTransitionException("Invalid ambulance transition: "+status+" -> "+next);
        status=next;
    }
    public void release() {
        if(status != AmbulanceStatus.HOSPITAL_ARRIVED)
            throw new InvalidStateTransitionException("Ambulance can be released only after hospital arrival.");
        status=AmbulanceStatus.AVAILABLE;
        activeEmergencyId=null;
    }
}

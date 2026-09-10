package com.emergency.ambulance.service;

import com.emergency.ambulance.enums.*;
import com.emergency.ambulance.exception.*;
import com.emergency.ambulance.model.*;
import java.time.LocalDateTime;
import java.util.*;

public class DispatchService {
    private final Map<String, Ambulance> ambulances = new HashMap<>();
    private final Map<String, EmergencyRequest> emergencies = new HashMap<>();
    private final List<EmergencyHistory> history = new ArrayList<>();
    private final PriorityQueue<EmergencyRequest> waitingQueue = new PriorityQueue<>(
        Comparator.comparingInt((EmergencyRequest e)->e.getPriority().getRank()).reversed()
                  .thenComparing(EmergencyRequest::getCreatedAt)
    );
    private static final double AVERAGE_SPEED_KMH = 40.0;

    public void registerAmbulance(Ambulance ambulance) {
        if(ambulance==null) throw new InvalidAmbulanceException("Ambulance cannot be null.");
        if(ambulances.putIfAbsent(ambulance.getAmbulanceId(), ambulance)!=null)
            throw new InvalidAmbulanceException("Duplicate ambulance id.");
    }
    public void submitEmergency(EmergencyRequest request) {
        if(request==null) throw new InvalidEmergencyException("Emergency cannot be null.");
        if(emergencies.putIfAbsent(request.getEmergencyId(),request)!=null)
            throw new InvalidEmergencyException("Duplicate emergency id.");
        Ambulance best=findBestAmbulance(request);
        if(best==null){ waitingQueue.offer(request); addHistory(request.getEmergencyId(),"Added to waiting queue"); }
        else allocate(request,best);
    }
    private Ambulance findBestAmbulance(EmergencyRequest request) {
        return ambulances.values().stream()
            .filter(Ambulance::isAvailable)
            .filter(a -> supports(a.getType(), request.getPriority()))
            .min(Comparator.comparingInt((Ambulance a)->typePenalty(a.getType(),request.getPriority()))
                 .thenComparingDouble(a->locationDistance(a.getCurrentLocation(),request.getPickupLocation())))
            .orElse(null);
    }
    private boolean supports(AmbulanceType type, EmergencyPriority priority) {
        return switch(priority) {
            case CRITICAL -> type==AmbulanceType.ICU;
            case HIGH -> type==AmbulanceType.ICU || type==AmbulanceType.ADVANCED_LIFE_SUPPORT;
            case MODERATE -> type!=null;
            case NORMAL -> type!=null;
        };
    }
    private int typePenalty(AmbulanceType type, EmergencyPriority p) {
        if(p==EmergencyPriority.CRITICAL) return type==AmbulanceType.ICU?0:100;
        if(p==EmergencyPriority.HIGH) return type==AmbulanceType.ADVANCED_LIFE_SUPPORT?0:(type==AmbulanceType.ICU?1:100);
        if(p==EmergencyPriority.MODERATE) return type==AmbulanceType.BASIC?0:1;
        return type==AmbulanceType.BASIC?0:1;
    }
    // Simple deterministic location metric for this assignment. Same location = 0; otherwise 1.
    private double locationDistance(String a,String b){ return a.equalsIgnoreCase(b)?0:1; }
    private void allocate(EmergencyRequest request, Ambulance ambulance) {
        ambulance.assign(request.getEmergencyId());
        double eta=(request.getEstimatedDistanceKm()/AVERAGE_SPEED_KMH)*60.0;
        request.allocate(ambulance.getAmbulanceId(), eta);
        addHistory(request.getEmergencyId(),"Ambulance "+ambulance.getAmbulanceId()+" allocated; ETA "+eta+" minutes");
    }
    public void updateAmbulanceStatus(String ambulanceId, AmbulanceStatus status) {
        Ambulance a=getAmbulance(ambulanceId);
        a.updateStatus(status);
        EmergencyRequest e=emergencies.get(a.getActiveEmergencyId());
        if(e!=null && status==AmbulanceStatus.EN_ROUTE) e.start();
        if(e!=null) addHistory(e.getEmergencyId(),"Ambulance status changed to "+status);
    }
    public void hospitalArrivedAndRelease(String ambulanceId) {
        Ambulance a=getAmbulance(ambulanceId);
        if(a.getStatus()!=AmbulanceStatus.HOSPITAL_ARRIVED)
            throw new InvalidStateTransitionException("Ambulance must be HOSPITAL_ARRIVED before release.");
        String id=a.getActiveEmergencyId();
        EmergencyRequest e=emergencies.get(id);
        if(e!=null){ e.complete(); addHistory(id,"Emergency completed"); }
        a.release();
        allocateWaitingEmergency();
    }
    private void allocateWaitingEmergency() {
        while(!waitingQueue.isEmpty()) {
            EmergencyRequest next=waitingQueue.peek();
            Ambulance best=findBestAmbulance(next);
            if(best==null) return;
            waitingQueue.poll();
            allocate(next,best);
        }
    }
    private Ambulance getAmbulance(String id) {
        Ambulance a=ambulances.get(id);
        if(a==null) throw new AmbulanceUnavailableException("Ambulance not found: "+id);
        return a;
    }
    private void addHistory(String id,String msg){ history.add(new EmergencyHistory(id,msg,LocalDateTime.now())); }
    public EmergencyRequest getEmergency(String id){
        EmergencyRequest e=emergencies.get(id);
        if(e==null) throw new EmergencyNotFoundException("Emergency not found: "+id);
        return e;
    }
    public int getWaitingCount(){return waitingQueue.size();}
    public List<EmergencyHistory> getHistory(){return List.copyOf(history);}
}

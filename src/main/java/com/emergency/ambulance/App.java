package com.emergency.ambulance;
import com.emergency.ambulance.enums.*;
import com.emergency.ambulance.model.*;
import com.emergency.ambulance.service.DispatchService;

public class App {
    public static void main(String[] args) {
        DispatchService service=new DispatchService();
        service.registerAmbulance(new Ambulance("AMB-101", AmbulanceType.ICU,
            new Driver("DRV-1","Ravi Kumar","9876543210"),"Central"));
        EmergencyRequest request=new EmergencyRequest("EMG-1","PAT-1","Cardiac Arrest",
            EmergencyPriority.CRITICAL,"Central","City Hospital",10);
        service.submitEmergency(request);
        System.out.println("Assigned ambulance: "+request.getAssignedAmbulanceId());
        System.out.println("ETA: "+request.getEstimatedArrivalMinutes()+" minutes");
    }
}

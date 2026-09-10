package com.emergency.ambulance;

import com.emergency.ambulance.enums.*;
import com.emergency.ambulance.exception.*;
import com.emergency.ambulance.model.*;
import com.emergency.ambulance.service.DispatchService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispatchServiceTest {

    private Ambulance ambulance(String id, AmbulanceType type) {
        return new Ambulance(
                id,
                type,
                new Driver("DRV-" + id, "Driver " + id, "9999999999"),
                "Central"
        );
    }

    private EmergencyRequest emergency(String id, EmergencyPriority priority) {
        return new EmergencyRequest(
                id,
                "PAT-" + id,
                "Medical Emergency",
                priority,
                "Central",
                "City Hospital",
                20
        );
    }

    // ==================== POSITIVE TEST CASES ====================

    @Test
    void positive_criticalEmergencyGetsIcuAmbulance() {
        DispatchService service = new DispatchService();

        service.registerAmbulance(ambulance("BASIC-1", AmbulanceType.BASIC));
        service.registerAmbulance(ambulance("ICU-1", AmbulanceType.ICU));

        EmergencyRequest request = emergency("EMG-1", EmergencyPriority.CRITICAL);
        service.submitEmergency(request);

        assertEquals("ICU-1", request.getAssignedAmbulanceId());
        assertEquals(EmergencyStatus.ALLOCATED, request.getStatus());
        assertEquals(30.0, request.getEstimatedArrivalMinutes());
    }

    @Test
    void positive_highEmergencyGetsAlsAmbulance() {
        DispatchService service = new DispatchService();

        service.registerAmbulance(ambulance("ALS-1", AmbulanceType.ADVANCED_LIFE_SUPPORT));

        EmergencyRequest request = emergency("EMG-2", EmergencyPriority.HIGH);
        service.submitEmergency(request);

        assertEquals("ALS-1", request.getAssignedAmbulanceId());
        assertEquals(EmergencyStatus.ALLOCATED, request.getStatus());
    }

    @Test
    void positive_emergencyWaitsWhenNoAmbulanceAvailable() {
        DispatchService service = new DispatchService();

        service.registerAmbulance(ambulance("ICU-1", AmbulanceType.ICU));

        EmergencyRequest first = emergency("EMG-1", EmergencyPriority.CRITICAL);
        EmergencyRequest second = emergency("EMG-2", EmergencyPriority.CRITICAL);

        service.submitEmergency(first);
        service.submitEmergency(second);

        assertEquals("ICU-1", first.getAssignedAmbulanceId());
        assertNull(second.getAssignedAmbulanceId());
        assertEquals(EmergencyStatus.WAITING, second.getStatus());
        assertEquals(1, service.getWaitingCount());
    }

    @Test
    void positive_waitingEmergencyAutomaticallyAllocatedAfterRelease() {
        DispatchService service = new DispatchService();

        service.registerAmbulance(ambulance("ICU-1", AmbulanceType.ICU));

        EmergencyRequest first = emergency("EMG-1", EmergencyPriority.CRITICAL);
        EmergencyRequest second = emergency("EMG-2", EmergencyPriority.CRITICAL);

        service.submitEmergency(first);
        service.submitEmergency(second);

        service.updateAmbulanceStatus("ICU-1", AmbulanceStatus.EN_ROUTE);
        service.updateAmbulanceStatus("ICU-1", AmbulanceStatus.PATIENT_PICKED_UP);
        service.updateAmbulanceStatus("ICU-1", AmbulanceStatus.HOSPITAL_ARRIVED);
        service.hospitalArrivedAndRelease("ICU-1");

        assertEquals("ICU-1", second.getAssignedAmbulanceId());
        assertEquals(EmergencyStatus.ALLOCATED, second.getStatus());
        assertEquals(0, service.getWaitingCount());
    }

    @Test
    void positive_historyIsRecorded() {
        DispatchService service = new DispatchService();
        service.registerAmbulance(ambulance("ICU-1", AmbulanceType.ICU));

        EmergencyRequest request = emergency("EMG-1", EmergencyPriority.CRITICAL);
        service.submitEmergency(request);

        assertFalse(service.getHistory().isEmpty());
    }

    // ==================== NEGATIVE TEST CASES ====================

    @Test
    void negative_invalidEmergencyDataThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new EmergencyRequest(
                        "",
                        "",
                        "",
                        null,
                        "",
                        "",
                        -5
                )
        );
    }

    @Test
    void negative_duplicateAmbulanceIdThrowsException() {
        DispatchService service = new DispatchService();

        service.registerAmbulance(ambulance("AMB-1", AmbulanceType.BASIC));

        assertThrows(InvalidAmbulanceException.class, () ->
                service.registerAmbulance(ambulance("AMB-1", AmbulanceType.ICU))
        );
    }

    @Test
    void negative_duplicateEmergencyIdThrowsException() {
        DispatchService service = new DispatchService();
        service.registerAmbulance(ambulance("ICU-1", AmbulanceType.ICU));

        service.submitEmergency(emergency("EMG-1", EmergencyPriority.CRITICAL));

        assertThrows(InvalidEmergencyException.class, () ->
                service.submitEmergency(emergency("EMG-1", EmergencyPriority.CRITICAL))
        );
    }

    @Test
    void negative_ambulanceCannotBeAssignedToTwoEmergencies() {
        Ambulance ambulance = ambulance("AMB-1", AmbulanceType.ICU);

        ambulance.assign("EMG-1");

        assertThrows(IllegalStateException.class, () ->
                ambulance.assign("EMG-2")
        );
    }

    @Test
    void negative_invalidAmbulanceStateTransitionThrowsException() {
        Ambulance ambulance = ambulance("AMB-1", AmbulanceType.BASIC);

        ambulance.assign("EMG-1");

        assertThrows(InvalidStateTransitionException.class, () ->
                ambulance.updateStatus(AmbulanceStatus.PATIENT_PICKED_UP)
        );
    }

    @Test
    void negative_unknownEmergencyThrowsException() {
        DispatchService service = new DispatchService();

        assertThrows(EmergencyNotFoundException.class, () ->
                service.getEmergency("UNKNOWN")
        );
    }
}

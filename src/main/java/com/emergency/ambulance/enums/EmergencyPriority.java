package com.emergency.ambulance.enums;
public enum EmergencyPriority {
    CRITICAL(4), HIGH(3), MODERATE(2), NORMAL(1);
    private final int rank;
    EmergencyPriority(int rank){ this.rank=rank; }
    public int getRank(){ return rank; }
}

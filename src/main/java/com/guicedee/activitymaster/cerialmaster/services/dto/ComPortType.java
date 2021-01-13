package com.guicedee.activitymaster.cerialmaster.services.dto;

import java.util.EnumSet;

public enum ComPortType {
    Scanner,
    ScannerType2,
    ScannerType3,
    ScannerType4,
    ScannerType5,
    ScannerType6,
    ScannerType7,
    ScannerType8,
    Device,
    Sim20,
    Sim20Type2,
    Lora;
    
    public static final EnumSet<ComPortType> scanners = EnumSet.of(Scanner, ScannerType2, ScannerType3, ScannerType4, ScannerType5, ScannerType6, ScannerType7, ScannerType8);
}

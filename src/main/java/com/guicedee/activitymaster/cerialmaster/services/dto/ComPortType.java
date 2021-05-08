package com.guicedee.activitymaster.cerialmaster.services.dto;

import java.util.EnumSet;

public enum ComPortType {
    Scanner,
    Device,
    Server;
    
    public static final EnumSet<ComPortType> graderServer = EnumSet.of(ComPortType.Server,ComPortType.Device);
    
    public static final EnumSet<ComPortType> scanners = EnumSet.of(Scanner);
}

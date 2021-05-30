package com.guicedee.activitymaster.cerialmaster.services.dto;

import lombok.Getter;

public enum ComPortStatus {
    GeneralException("mdi mdi-help-network-outline font-24 avatar-title d-flex"),
    Missing("mdi mdi-help-network font-24 avatar-title d-flex"),
    InUse("mdi mdi-help-network font-24 avatar-title d-flex"),
    Offline("mdi mdi-network-off font-24 avatar-title d-flex"),
    FileTransfer("mdi mdi-play-network font-24 avatar-title d-flex"),
    Simulation("mdi mdi-router-network font-24 avatar-title d-flex"),
    Silent("mdi mdi-security-network font-24 avatar-title d-flex"),
    Idle("mdi mdi-server-network font-24 avatar-title d-flex"),
    Logging("mdi mdi-upload-network font-24 avatar-title d-flex"),
    Running("mdi mdi-play-network font-24 avatar-title d-flex");
    
    @Getter
    private String icon;
    
    ComPortStatus(String icon)
    {
        this.icon = icon;
    }
}

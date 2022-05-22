package com.guicedee.activitymaster.cerialmaster.services.dto;

import java.util.EnumSet;

public enum ComPortStatus
{
	GeneralException("mdi mdi-help-network-outline font-24 avatar-title d-flex", "bg-danger", "text-danger"),
	Missing("mdi mdi-help-network font-24 avatar-title d-flex", "bg-danger", "text-danger"),
	InUse("mdi mdi-help-network font-24 avatar-title d-flex", "bg-danger", "text-danger"),
	Offline("mdi mdi-network-off font-24 avatar-title d-flex", "bg-secondary", "text-secondary"),
	OperationInProgress("mdi mdi-play-network font-24 avatar-title d-flex", "bg-purple", "text-purple"),
	FileTransfer("mdi mdi-play-network font-24 avatar-title d-flex", "bg-purple", "text-purple"),
	Simulation("mdi mdi-router-network font-24 avatar-title d-flex", "bg-purple", "text-purple"),
	Opening("mdi mdi-security-network font-24 avatar-title d-flex", "bg-info", "text-info"),
	Silent("mdi mdi-security-network font-24 avatar-title d-flex", "bg-warning", "text-warning"),
	Idle("mdi mdi-server-network font-24 avatar-title d-flex", "bg-warning", "text-warning"),
	Logging("mdi mdi-upload-network font-24 avatar-title d-flex", "bg-info", "text-info"),
	Running("mdi mdi-play-network font-24 avatar-title d-flex", "bg-success", "text-success");
	
	private String icon;
	
	private String backgroundClass;
	
	private String foregroundClass;
	
	public static final EnumSet<ComPortStatus> pauseOperations = EnumSet.of(GeneralException,Missing,InUse,Offline,OperationInProgress,FileTransfer,Opening) ;
	public static final EnumSet<ComPortStatus> portActive = EnumSet.of(Silent,Idle,Logging,Running,Simulation);
	public static final EnumSet<ComPortStatus> portOffline = EnumSet.of(GeneralException,Missing,InUse,Offline);
	
	ComPortStatus(String icon, String backgroundClass, String foregroundClass)
	{
		this.icon = icon;
		this.backgroundClass = backgroundClass;
		this.foregroundClass = foregroundClass;
	}
	
	public String getIcon()
	{
		return icon;
	}
	
	public ComPortStatus setIcon(String icon)
	{
		this.icon = icon;
		return this;
	}
	
	public String getBackgroundClass()
	{
		return backgroundClass;
	}
	
	public ComPortStatus setBackgroundClass(String backgroundClass)
	{
		this.backgroundClass = backgroundClass;
		return this;
	}
	
	public String getForegroundClass()
	{
		return foregroundClass;
	}
	
	public ComPortStatus setForegroundClass(String foregroundClass)
	{
		this.foregroundClass = foregroundClass;
		return this;
	}
}

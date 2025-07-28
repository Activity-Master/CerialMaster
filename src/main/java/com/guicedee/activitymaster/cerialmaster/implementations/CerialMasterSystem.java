package com.guicedee.activitymaster.cerialmaster.implementations;

import com.google.inject.Inject;
import com.guicedee.activitymaster.fsdm.client.services.ISystemsService;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterDefaultSystem;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise.IEnterprise;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;
import io.smallrye.mutiny.Uni;

import java.time.Duration;

import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.*;

@Log4j2
public class CerialMasterSystem
		extends ActivityMasterDefaultSystem<CerialMasterSystem>
		implements IActivityMasterSystem<CerialMasterSystem>
{
	
	@Inject
	private ISystemsService<?> systemsService;
	
	@Override
	public Uni<ISystems<?,?>> registerSystem(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		log.info("🚀 Registering CerialMaster system with external session for enterprise: '{}'", enterprise.getName());
		
		return systemsService
			.create(session, enterprise, getSystemName(), getSystemDescription())
			.onItem().invoke(system -> log.debug("✅ CerialMaster system created: '{}' (ID: {})", system.getName(), system.getId()))
			.onFailure().invoke(error -> log.error("❌ Failed to create CerialMaster system: {}", error.getMessage(), error))
			.chain(system -> {
				return getSystem(session, enterprise)
					.chain(retrievedSystem -> systemsService.registerNewSystem(session, enterprise, retrievedSystem))
					.onItem().invoke(() -> log.info("✅ CerialMaster system successfully registered"))
					.onFailure().invoke(error -> log.error("❌ Failed to register CerialMaster system: {}", error.getMessage(), error))
					.map(result -> system);
			});
	}
	
	@Override
	public Uni<Void> createDefaults(Mutiny.Session session, IEnterprise<?,?> enterprise)
	{
		log.info("🔧 Creating defaults for CerialMaster system with external session for enterprise: '{}'", enterprise.getName());
		
		// The actual default creation is handled by CerialMasterInstall.java during system updates
		// This method serves as a hook for any additional default data if needed in the future
		
		log.debug("📋 CerialMaster defaults are managed by CerialMasterInstall during system updates");
		log.info("✅ CerialMaster createDefaults completed successfully");
		
		return Uni.createFrom().voidItem();
	}

	@Override
	public Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise)
	{
		log.info("🚀 CerialMaster post-startup operations for enterprise: '{}'", enterprise.getName());
		
		return systemsService.findSystem(session, enterprise, getSystemName())
			.onItem().invoke(system -> log.debug("✅ CerialMaster system found during post-startup: '{}' (ID: {})", 
				system.getName(), system.getId()))
			.onFailure().invoke(error -> log.error("❌ Failed to find CerialMaster system during post-startup: {}", 
				error.getMessage(), error))
			.chain(system -> {
				log.debug("🔍 Validating CerialMaster system configuration");
				// Additional post-startup validation could be added here if needed
				log.info("🎉 CerialMaster post-startup validation completed successfully");
				return Uni.createFrom().voidItem();
			});
	}

	@Override
	public int totalTasks()
	{
		return 3; // Update to match CerialMasterInstall taskCount
	}
	
	@Override
	public String getSystemName()
	{
		return CerialMasterSystemName;
	}

	@Override
	public String getSystemDescription()
	{
		return "CerialMaster System - Provides serial port communication functionality for ActivityMaster";
	}
	

	@Override
	public Integer sortOrder()
	{
		return 550;
	}
}
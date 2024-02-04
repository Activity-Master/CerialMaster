import com.guicedee.activitymaster.cerialmaster.implementations.*;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;

module com.guicedee.activitymaster.cerialmaster {
	
	exports com.guicedee.activitymaster.cerialmaster.implementations;
	exports com.guicedee.activitymaster.cerialmaster.services;
	exports com.guicedee.activitymaster.cerialmaster.services.enumerations;
	exports com.guicedee.activitymaster.cerialmaster;
	exports com.guicedee.activitymaster.cerialmaster.services.exceptions;
	
	requires transitive com.neuronrobotics.nrjavaserial;
	
	requires org.apache.logging.log4j.core;
	requires com.guicedee.guicedinjection;
	requires com.guicedee.guicedpersistence;

	requires com.google.guice;
	
	requires com.guicedee.activitymaster.fsdm.client;
	requires com.entityassist;
	requires static lombok;
	requires com.guicedee.activitymaster.cerialmaster.client;
	
	provides com.guicedee.guicedinjection.interfaces.IGuiceModule with CerialMasterModule;
	provides IActivityMasterSystem with CerialMasterSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with CerialMasterGuiceConfig;
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with CerialMasterInclusionModule;
	provides IGuicePostStartup with CerialMasterPostStartup;
	
	opens com.guicedee.activitymaster.cerialmaster to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.implementations to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.services to com.google.guice,com.fasterxml.jackson.databind;
	
	opens com.guicedee.activitymaster.cerialmaster.services.enumerations to com.google.guice,com.fasterxml.jackson.databind;
}

import com.guicedee.activitymaster.cerialmaster.implementations.*;
import com.guicedee.activitymaster.cerialmaster.services.IReceiveMessage;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;

module com.guicedee.activitymaster.cerialmaster {

	exports com.guicedee.activitymaster.cerialmaster.services;
	exports com.guicedee.activitymaster.cerialmaster.services.enumerations;
	exports com.guicedee.activitymaster.cerialmaster.services.dto;
	exports com.guicedee.activitymaster.cerialmaster;
	exports com.guicedee.activitymaster.cerialmaster.services.exceptions;
	
	requires static lombok;
	requires transitive com.neuronrobotics.nrjavaserial;
	requires com.guicedee.guicedhazelcast.hibernate;
	
	requires com.jwebmp.plugins.quickforms.annotations;

	requires com.guicedee.guicedinjection;

	requires com.google.guice;
	
	requires static com.guicedee.guicedhazelcast;
	requires com.guicedee.activitymaster.fsdm.client;
	
	provides com.guicedee.guicedinjection.interfaces.IGuiceModule with CerialMasterModule;
	provides IActivityMasterSystem with CerialMasterSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with CerialMasterGuiceConfig;
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with CerialMasterInclusionModule;
	
	opens com.guicedee.activitymaster.cerialmaster to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.implementations to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.services to com.google.guice,com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.cerialmaster.services.dto to com.google.guice,com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.cerialmaster.services.enumerations to com.google.guice,com.fasterxml.jackson.databind;
	
	
	uses IReceiveMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.IErrorReceiveMessage;
	uses com.guicedee.activitymaster.cerialmaster.services.ITerminalReceiveMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.ICleanReceivedMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.IComPortStatusChanged;

}

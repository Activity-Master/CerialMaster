import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterGuiceConfig;
import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterInclusionModule;
import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterModule;
import com.guicedee.activitymaster.cerialmaster.implementations.CerialMasterSystem;
import com.guicedee.activitymaster.cerialmaster.services.IReceiveMessage;

module com.guicedee.activitymaster.cerialmaster {

	exports com.guicedee.activitymaster.cerialmaster.services;
	exports com.guicedee.activitymaster.cerialmaster.services.dto;
	exports com.guicedee.activitymaster.cerialmaster;
	
	requires static lombok;
	requires transitive com.neuronrobotics.nrjavaserial;
	requires com.guicedee.guicedhazelcast.hibernate;

	requires com.guicedee.guicedinjection;
	requires com.guicedee.activitymaster.core;
	
	requires com.google.guice;
	
	requires static com.guicedee.guicedhazelcast;
	
    provides com.guicedee.guicedinjection.interfaces.IGuiceModule with CerialMasterModule;
	provides com.guicedee.activitymaster.core.services.IActivityMasterSystem with CerialMasterSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with CerialMasterGuiceConfig;
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with CerialMasterInclusionModule;
	
	opens com.guicedee.activitymaster.cerialmaster to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.implementations to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.services to com.google.guice;

	uses IReceiveMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.IErrorReceiveMessage;
	uses com.guicedee.activitymaster.cerialmaster.services.ITerminalReceiveMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.ICleanReceivedMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.IComPortStatusChanged;

}

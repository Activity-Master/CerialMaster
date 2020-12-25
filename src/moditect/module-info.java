import com.guicedee.activitymaster.cerialmaster.implementations.CerialKillerGuiceConfig;
import com.guicedee.activitymaster.cerialmaster.implementations.CerialKillerInclusionModule;
import com.guicedee.activitymaster.cerialmaster.services.IReceiveMessage;

module com.guicedee.activitymaster.cerialmaster {

	exports com.guicedee.activitymaster.cerialmaster.services;
	exports com.guicedee.activitymaster.cerialmaster.services.dto;
	exports com.guicedee.activitymaster.cerialmaster;
	
	exports gnu.io.NRSerialPort;
	
	requires static lombok;

	requires com.guicedee.guicedinjection;

	requires com.guicedee.activitymaster.core;
	requires com.google.guice;
	
	requires static com.guicedee.guicedhazelcast;
    requires static nrjavaserial;

    provides com.guicedee.guicedinjection.interfaces.IGuiceModule with com.guicedee.activitymaster.cerialmaster.implementations.CerialKillerModule;
	provides com.guicedee.activitymaster.core.services.IActivityMasterSystem with com.guicedee.activitymaster.cerialmaster.implementations.CerialKillerSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with CerialKillerGuiceConfig;
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with CerialKillerInclusionModule;
	
	opens com.guicedee.activitymaster.cerialmaster to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.implementations to com.google.guice;
	opens com.guicedee.activitymaster.cerialmaster.services to com.google.guice;

	uses IReceiveMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.IErrorReceiveMessage;
	uses com.guicedee.activitymaster.cerialmaster.services.ITerminalReceiveMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.ICleanReceivedMessage;
    uses com.guicedee.activitymaster.cerialmaster.services.IComPortStatusChanged;

}

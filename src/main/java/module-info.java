import com.guicedee.activitymaster.cerialmaster.implementations.*;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;

module com.guicedee.activitymaster.cerialmaster {

  exports com.guicedee.activitymaster.cerialmaster.implementations;
  exports com.guicedee.activitymaster.cerialmaster.services;
  exports com.guicedee.activitymaster.cerialmaster.services.enumerations;
  exports com.guicedee.activitymaster.cerialmaster;

  requires org.apache.logging.log4j.core;
  requires com.google.guice;
  requires transitive com.guicedee.vertxpersistence;

  requires transitive com.guicedee.activitymaster.fsdm.client;
  requires transitive com.guicedee.activitymaster.fsdm;
  requires com.entityassist;
  requires static lombok;
  requires com.guicedee.activitymaster.cerialmaster.client;
  requires com.guicedee.cerial;
  requires io.vertx.core;

  provides com.guicedee.guicedinjection.interfaces.IGuiceModule with CerialMasterModule;
  provides IActivityMasterSystem with CerialMasterSystem;
  provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with CerialMasterGuiceConfig;
  provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with CerialMasterInclusionModule;

  opens com.guicedee.activitymaster.cerialmaster to com.google.guice;
  opens com.guicedee.activitymaster.cerialmaster.implementations to com.google.guice;
  opens com.guicedee.activitymaster.cerialmaster.services to com.google.guice, com.fasterxml.jackson.databind;

  opens com.guicedee.activitymaster.cerialmaster.services.enumerations to com.google.guice, com.fasterxml.jackson.databind;
}

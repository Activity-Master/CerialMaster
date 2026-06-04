import com.guicedee.activitymaster.cerialmaster.implementations.*;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.client.services.lifecycle.IGuiceConfigurator;
import com.guicedee.client.services.lifecycle.IGuiceModule;
import com.guicedee.client.services.config.IGuiceScanModuleInclusions;

module com.guicedee.activitymaster.cerialmaster {

  exports com.guicedee.activitymaster.cerialmaster.implementations;
  exports com.guicedee.activitymaster.cerialmaster.services;
  exports com.guicedee.activitymaster.cerialmaster.services.enumerations;
  exports com.guicedee.activitymaster.cerialmaster;
  exports com.guicedee.activitymaster.cerialmaster.rest;

  requires org.apache.logging.log4j.core;
  requires org.apache.logging.log4j;
  requires com.google.guice;
  requires transitive com.guicedee.persistence;

  requires transitive com.guicedee.activitymaster.fsdm.client;
  requires transitive com.guicedee.activitymaster.fsdm;
  requires com.entityassist;
  requires static lombok;
  requires com.guicedee.activitymaster.cerialmaster.client;
  requires com.guicedee.cerial;
  requires io.vertx.core;
  requires com.guicedee.rest.client;

  requires com.guicedee.rest;
  requires com.guicedee.vertx;
  requires com.guicedee.vertx.graphql;
  requires com.graphqljava;
  requires jakarta.ws.rs;
  requires com.fasterxml.jackson.databind;
  requires io.smallrye.mutiny;
  requires org.hibernate.reactive;

  provides IGuiceModule with CerialMasterModule;
  provides IActivityMasterSystem with CerialMasterSystem;
  provides IGuiceConfigurator with CerialMasterGuiceConfig;
  provides IGuiceScanModuleInclusions with CerialMasterInclusionModule;
  provides com.guicedee.vertx.graphql.services.IGraphQLSchemaProvider
      with com.guicedee.activitymaster.cerialmaster.implementations.graphql.CerialMasterGraphQLSchemaProvider;

  opens com.guicedee.activitymaster.cerialmaster to com.google.guice;
  opens com.guicedee.activitymaster.cerialmaster.implementations to com.google.guice;
  opens com.guicedee.activitymaster.cerialmaster.implementations.graphql to com.google.guice;
  opens com.guicedee.activitymaster.cerialmaster.services to com.google.guice, com.fasterxml.jackson.databind;

  opens com.guicedee.activitymaster.cerialmaster.services.enumerations to com.google.guice, com.fasterxml.jackson.databind;
  opens com.guicedee.activitymaster.cerialmaster.rest to com.google.guice, com.guicedee.rest, com.fasterxml.jackson.databind, org.hibernate.reactive, net.bytebuddy;
}

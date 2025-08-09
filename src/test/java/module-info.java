import com.guicedee.activitymaster.cerialmaster.test.PostgreSQLTestDBModule;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;

open module cerial.master.tests {
  requires transitive com.entityassist;
  requires transitive com.guicedee.vertxpersistence;

  requires org.junit.jupiter.api;
  requires junit;


  requires jakarta.xml.bind;
  requires jakarta.persistence;


  requires transitive org.hibernate.reactive;
  requires io.smallrye.mutiny;
  requires com.google.guice;
  requires static lombok;

  requires org.testcontainers;
  requires io.vertx.sql.client.pg;
  requires io.vertx.sql.client;

  requires com.guicedee.activitymaster.fsdm;
  requires com.guicedee.activitymaster.cerialmaster.client;
  requires com.guicedee.activitymaster.cerialmaster;

  provides IGuiceModule with PostgreSQLTestDBModule;


}
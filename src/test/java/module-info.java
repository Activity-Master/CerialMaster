open module cerial.master.tests {
  requires transitive org.junit.jupiter.api;
  requires transitive com.guicedee.activitymaster.cerialmaster.client;
  requires transitive com.guicedee.cerial;
  requires transitive com.guicedee.guicedinjection;
  requires transitive com.guicedee.activitymaster.fsdm.client;
  requires transitive com.guicedee.activitymaster.fsdm;
  requires static lombok;
  requires org.testcontainers;
  requires com.guicedee.activitymaster.cerialmaster;

  exports com.guicedee.activitymaster.cerialmaster.test;
  exports com.guicedee.activitymaster.cerialmaster.testimpl;

  provides com.guicedee.activitymaster.cerialmaster.client.services.IComPortStatusChanged
      with com.guicedee.activitymaster.cerialmaster.testimpl.TestStatusListener;
}

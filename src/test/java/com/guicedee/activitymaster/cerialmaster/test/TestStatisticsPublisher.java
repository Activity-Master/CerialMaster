package com.guicedee.activitymaster.cerialmaster.test;

import com.guicedee.activitymaster.cerialmaster.client.AggregateProgress;
import com.guicedee.activitymaster.cerialmaster.client.SenderSnapshot;
import com.guicedee.vertx.VertxEventDefinition;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.Message;

public class TestStatisticsPublisher
{

  @VertxEventDefinition("server-task-updates")
  public Uni<Void> receiveStatistics(Message<AggregateProgress> statistics)
  {
    return Uni.createFrom()
               .nullItem();
  }

  @VertxEventDefinition("sender-1-tasks")
  public Uni<Void> receiveServer1Statistics(Message<SenderSnapshot> statistics)
  {
    return Uni.createFrom()
               .nullItem();
  }

  @VertxEventDefinition("sender-2-tasks")
  public Uni<Void> receiveServer2Statistics(Message<SenderSnapshot> statistics)
  {
    return Uni.createFrom()
               .nullItem();
  }

  @VertxEventDefinition("sender-3-tasks")
  public Uni<Void> receiveServer3Statistics(Message<SenderSnapshot> statistics)
  {
    return Uni.createFrom()
               .nullItem();
  }

  @VertxEventDefinition("sender-4-tasks")
  public Uni<Void> receiveServer4Statistics(Message<SenderSnapshot> statistics)
  {
    return Uni.createFrom()
               .nullItem();
  }

}

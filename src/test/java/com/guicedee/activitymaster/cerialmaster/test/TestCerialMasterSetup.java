package com.guicedee.activitymaster.cerialmaster.test;

import com.google.inject.Key;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService;
import com.guicedee.activitymaster.fsdm.client.services.IEnterpriseService;
import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.cerial.enumerations.ComPortType;
import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import lombok.extern.log4j.Log4j2;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.CerialMasterSystemName;
import static com.guicedee.activitymaster.fsdm.DefaultEnterprise.TestEnterprise;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystem;
import static com.guicedee.activitymaster.fsdm.client.services.IActivityMasterService.getISystemToken;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class TestCerialMasterSetup
{

  protected Mutiny.SessionFactory sessionFactory;

  @BeforeAll
  public void setup()
  {
    // Initialize the Guice context
    //IGuiceContext.registerModule(new PostgreSQLTestDBModule());
    ActivityMasterConfiguration.get()
        .setApplicationEnterpriseName(TestEnterprise.name());
    IGuiceContext.instance()
        .inject();

    log.info("Loading DB Configuration / PersistService from Guice");
    sessionFactory = IGuiceContext.get(Key.get(Mutiny.SessionFactory.class));
    assertNotNull(sessionFactory, "SessionFactory should not be null");
  }


  @AfterAll
  public void afterAll()
  {
    // JtaPersistService ps = (JtaPersistService) IGuiceContext.get(Key.get(PersistService.class, Names.named("ActivityMaster-Test")));
    //ps.stop();
  }


  @Test
  public void testPostgreSQLConnects()
  {
    var result =
        sessionFactory.withSession(session -> {
              // Persist the entity
              return session.withTransaction(tx -> {
                log.info("Session: " + session);
                return Uni.createFrom()
                           .voidItem();
              });
            })
            .await()
            .atMost(Duration.of(50L, ChronoUnit.SECONDS))
        ;
  }

  @Nested
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  public class TestEnterprise
  {

    @Test
    @Order(1)
    public void testEnterpriseInstallation()
    {
      sessionFactory.withSession(session -> {
            // Persist the entity
            return session.withTransaction(tx -> {

                  IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
                  var enterprise = enterpriseService.get();
                  enterprise.setName(TestEnterprise.name());
                  enterprise.setDescription("Enterprise Entity for Testing");

                  return enterpriseService.createNewEnterprise(session, enterprise);
                })
                       .chain(result -> {
                         System.out.println("Enterprise Created - " + result.getName() + " / " + result.getId());
                         return Uni.createFrom()
                                    .item(result);
                       });
          })
          .await()
          .atMost(Duration.of(2, ChronoUnit.MINUTES))
      ;

    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EnterpriseSetup
    {
      @Test
      @Order(2)
      public void testEnterpriseUpdates()
      {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        var updates = sessionFactory.withTransaction(session -> {
          return enterpriseService.getEnterprise(session, TestEnterprise.name())
                     .chain(enterprise -> {
                       return enterpriseService.loadUpdates(session, enterprise)
                                  .onFailure()
                                  .invoke(a -> log.fatal("Cannot load updates", a));
                     });
        });
        updates.onFailure()
            .invoke(a -> log.fatal("Cannot load updates", a));
        updates.onItem()
            .invoke(a -> log.info("loaded updates"));
        updates.await()
            .atMost(Duration.ofMinutes(1))
        ;
      }

      @Test
      @Order(1)
      public void testStartNewEnterprise()
      {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        var updates = sessionFactory.withTransaction(session -> {
          return enterpriseService.getEnterprise(session, TestEnterprise.name())
                     .chain(enterprise -> {
                       return enterpriseService.startNewEnterprise(session, TestEnterprise.name(), "admin", "!@adminadmin")
                                  .onFailure()
                                  .invoke(a -> log.fatal("Cannot load updates", a));
                     });
        });
        updates.onFailure()
            .invoke(a -> log.fatal("Cannot load updates", a));
        updates.onItem()
            .invoke(a -> log.info("loaded updates"));
        updates.await()
            .atMost(Duration.ofMinutes(1))
        ;
      }


      @Test
      @Order(3)
      public void testCerialPort()
      {
        IEnterpriseService<?> enterpriseService = IGuiceContext.get(IEnterpriseService.class);
        var updates = sessionFactory.withTransaction(session -> {
          return enterpriseService.getEnterprise(session, TestEnterprise.name())
                     .chain(enterprise -> {
                           return getISystem(session, CerialMasterSystemName, enterprise)
                                      .chain(system -> {
                                        return getISystemToken(session, CerialMasterSystemName, enterprise)
                                                   .chain(identityToken -> {
                                                     ICerialMasterService<?> cerialMasterService = IGuiceContext.get(ICerialMasterService.class);
                                                     return cerialMasterService.listComPorts()
                                                                .onFailure()
                                                                .invoke(a -> log.fatal("Cannot list Cerial Ports", a))
                                                                .chain(ports -> {
                                                                  if (ports.isEmpty())
                                                                  {
                                                                    log.warn("No Cerial Ports found. Please check your system configuration.");
                                                                    return Uni.createFrom()
                                                                               .failure(new RuntimeException("No Cerial Ports found"));
                                                                  }
                                                                  else
                                                                  {
                                                                    ports.forEach(port -> log.info("Found Cerial Port: " + port));
                                                                    if (ports.contains("COM20"))
                                                                    {
                                                                      var comPort = new ComPortConnection<>(20, ComPortType.Device);
                                                                      log.info("COM20 is available for use.");
                                                                      return cerialMasterService.addOrUpdateConnection(session, comPort, system, identityToken)
                                                                                 .onFailure()
                                                                                 .recoverWithUni(throwable -> {
                                                                                   log.error("Failed to add or update COM20 connection", throwable);
                                                                                   return cerialMasterService.findComPortConnection(session, comPort, system, identityToken)
                                                                                              .chain(a -> {
                                                                                                return cerialMasterService.listAvailableComPorts(session, enterprise)
                                                                                                           .invoke(b -> log.info("Available Cerial Ports: " + b))
                                                                                                           .replaceWith(comPort);
                                                                                              });
                                                                                 })
                                                                                 .map(a -> a);
                                                                    }
                                                                    else
                                                                    {
                                                                      log.warn("COM20 is not available. Please ensure it is connected and recognized by the system.");
                                                                      return Uni.createFrom()
                                                                                 .failure(new RuntimeException("COM20 is not available"));
                                                                    }
                                                                  }
                                                                });
                                                   });
                                      });
                         }
                     );
        });
        updates.onFailure()
            .invoke(a -> log.fatal("Cannot load updates", a));
        updates.onItem()
            .invoke(a -> log.info("loaded updates"));
        updates.await()
            .atMost(Duration.ofMinutes(1))
        ;
      }
    }
  }


}
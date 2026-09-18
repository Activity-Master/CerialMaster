package com.guicedee.activitymaster.cerialmaster.test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.guicedee.activitymaster.cerialmaster.CerialMasterService;
import com.guicedee.activitymaster.cerialmaster.client.ComPortConnection;
import com.guicedee.activitymaster.fsdm.client.services.IResourceItemService;
import com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems.ISystems;
import com.guicedee.activitymaster.fsdm.client.services.rest.RestClients;
import com.guicedee.activitymaster.fsdm.client.services.rest.resourceitems.*;
import com.guicedee.cerial.enumerations.ComPortType;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.scopes.CallScoper;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.time.Duration;
import java.util.*;

import static com.guicedee.activitymaster.cerialmaster.client.services.ICerialMasterService.CerialMasterSystemName;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialMasterClassifications.*;
import static com.guicedee.activitymaster.cerialmaster.services.enumerations.CerialResourceItemTypes.SerialConnectionPort;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CerialMasterRestDelegationTest {
    private final RestClients rest = mock(RestClients.class);
    private final Mutiny.StatelessSession session = mock(Mutiny.StatelessSession.class);
    private final ISystems<?, ?> system = mock(ISystems.class);
    private final CerialMasterService service = new CerialMasterService();
    private ComPortConnection<?> connection;
    private MockedStatic<IGuiceContext> context;

    @BeforeEach void setup() {
        context = mockStatic(IGuiceContext.class);
        context.when(() -> IGuiceContext.get(CallScoper.class)).thenReturn(mock(CallScoper.class));
        Guice.createInjector(new AbstractModule() {
            @Override protected void configure() {
                bind(RestClients.class).toProvider(() -> rest);
                bind(new TypeLiteral<IResourceItemService<?>>() {}).toInstance(mock(IResourceItemService.class));
            }
        }).injectMembers(service);
        when(system.getName()).thenReturn("RegistrationCaller");
        connection = new ComPortConnection<>(23, ComPortType.Server);
    }

    @AfterEach void teardown() {
        try { verifyNoInteractions(session); } finally { context.close(); }
    }

    private static <T> T await(Uni<T> result) {
        return result.await().atMost(Duration.ofSeconds(2));
    }

    private ResourceItemDTO resource(Map<String, String> values) {
        ResourceItemDTO item = new ResourceItemDTO();
        item.resourceItemId = UUID.randomUUID();
        item.classifications = values;
        return item;
    }

    private void search(List<ResourceItemDTO> results) {
        when(rest.searchResourceItems(eq("RegistrationCaller"), any())).thenReturn(Uni.createFrom().item(results));
    }

    @Test void lookupHydratesSettingsUnderCallerIdentity() {
        var item = resource(Map.of(ComPortNumber.toString(), "23", ComPortDeviceType.toString(), "Server",
                ComPortStatus.toString(), "Silent", BaudRate.toString(), "19200", BufferSize.toString(), "8192",
                DataBits.toString(), "8", StopBits.toString(), "1", Parity.toString(), "0"));
        search(List.of(item));
        assertSame(connection, await(service.findComPortConnection(session, connection, system)));
        assertEquals(item.resourceItemId, connection.getId());
        assertEquals(19200, connection.getBaudRate().toInt());
        assertEquals(8192, connection.getBufferSize());
        assertEquals(ComPortType.Server, connection.getComPortType());
        verify(rest).searchResourceItems(eq("RegistrationCaller"), argThat(q ->
                SerialConnectionPort.toString().equals(q.resourceItemType) && "23".equals(q.classificationValue)
                        && ComPortNumber.toString().equals(q.classificationName)
                        && q.includes.contains(ResourceItemDataIncludes.Classifications) && q.maxResults == 1));
        verifyNoMoreInteractions(rest);
    }

    @Test void missingLookupFailsWithoutCreating() {
        search(List.of());
        assertThrows(NoSuchElementException.class, () -> await(service.findComPortConnection(session, connection, system)));
        verify(rest, never()).createResourceItem(anyString(), any());
    }

    @Test void newConnectionCreatesAllClassifications() {
        search(List.of());
        var created = resource(Map.of());
        when(rest.createResourceItem(eq("RegistrationCaller"), any())).thenReturn(Uni.createFrom().item(created));
        assertSame(connection, await(service.addOrUpdateConnection(session, connection, system)));
        assertEquals(created.resourceItemId, connection.getId());
        verify(rest).createResourceItem(eq("RegistrationCaller"), argThat(q ->
                SerialConnectionPort.toString().equals(q.type) && "23".equals(q.dataValue)
                        && q.classifications.size() == 9 && "23".equals(q.classifications.get(ComPortNumber.toString()))));
        verify(rest, never()).updateResourceItem(anyString(), any());
    }

    @Test void existingConnectionUpdatesSameResource() {
        var existing = resource(Map.of());
        search(List.of(existing));
        when(rest.updateResourceItem(eq("RegistrationCaller"), any())).thenReturn(Uni.createFrom().item(existing));
        assertSame(connection, await(service.addOrUpdateConnection(session, connection, system)));
        assertEquals(existing.resourceItemId, connection.getId());
        verify(rest).updateResourceItem(eq("RegistrationCaller"), argThat(q ->
                existing.resourceItemId.equals(q.resourceItemId) && q.classifications.addOrUpdate.size() == 9));
        verify(rest, never()).createResourceItem(anyString(), any());
    }

    @Test void statusUpdateWritesOnlyStatus() {
        var existing = resource(Map.of());
        search(List.of(existing));
        when(rest.updateResourceItem(eq("RegistrationCaller"), any())).thenReturn(Uni.createFrom().item(existing));
        assertSame(connection, await(service.updateStatus(session, connection, system)));
        verify(rest).updateResourceItem(eq("RegistrationCaller"), argThat(q ->
                existing.resourceItemId.equals(q.resourceItemId) && q.classifications.addOrUpdate.equals(
                        Map.of(ComPortStatus.toString(), connection.getComPortStatus().toString()))));
    }

    @Test void missingStatusResourceDoesNotWrite() {
        search(List.of());
        assertSame(connection, await(service.updateStatus(session, connection, system)));
        verify(rest, never()).updateResourceItem(anyString(), any());
    }

    @Test void registeredPortsUseCerialIdentityAndSortNames() {
        when(rest.searchResourceItems(eq(CerialMasterSystemName), any())).thenReturn(Uni.createFrom().item(List.of(
                resource(Map.of(ComPortNumber.toString(), "40")), resource(Map.of()),
                resource(Map.of(ComPortNumber.toString(), "23")))));
        assertEquals(List.of("COM23", "COM40"), await(service.listRegisteredComPorts(session, null)));
        verify(rest).searchResourceItems(eq(CerialMasterSystemName), argThat(q ->
                q.classificationValue == null && q.includes.contains(ResourceItemDataIncludes.Classifications)));
    }

    @Test void lookupWithoutCallerUsesCerialIdentity() {
        when(rest.searchResourceItems(eq(CerialMasterSystemName), any()))
                .thenReturn(Uni.createFrom().item(List.of(resource(Map.of()))));
        assertSame(connection, await(service.findComPortConnection(null, connection, null)));
        verify(rest).searchResourceItems(eq(CerialMasterSystemName), any());
    }

    @Test void searchFailurePropagatesWithoutCreating() {
        var failure = new IllegalStateException("REST unavailable");
        when(rest.searchResourceItems(eq("RegistrationCaller"), any())).thenReturn(Uni.createFrom().failure(failure));
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> await(service.addOrUpdateConnection(session, connection, system))));
        verify(rest, never()).createResourceItem(anyString(), any());
    }
}

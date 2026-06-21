package com.guicedee.activitymaster.cerialmaster.test;

import com.guicedee.activitymaster.fsdm.client.services.administration.ActivityMasterConfiguration;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.utils.LogUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.guicedee.activitymaster.fsdm.DefaultEnterprise.TestEnterprise;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the optional cerial module contributes its {@code CerialMasterRestService} surface to
 * the merged OpenAPI 3.1 document produced by the GuicedEE OpenAPI scanner.
 *
 * <p>The cerial module is discovered purely by being on the class/module path. Once present, the
 * scanner must merge its Swagger-annotated COM-port endpoints — grouped under the
 * {@code Cerial Master} tag declared on {@code CerialMasterRestService} (and catalogued on the
 * application-level {@code @OpenAPIDefinition}) — into the same {@code /openapi.json} document as the
 * core FSDM resources, without disturbing the global info block or the existing FSDM tags/paths.</p>
 *
 * <p>Run explicitly (the module's hardware/DB-dependent suite is skipped by default):</p>
 * <pre>mvn -Dcerial.skip.tests=false -Dtest=TestCerialMasterOpenApi test</pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCerialMasterOpenApi
{
    private static final String CERIAL_TAG = "Cerial Master";
    private static final String CERIAL_SEGMENT = "/cerial/";

    private OpenAPI openAPI;

    @BeforeAll
    public void setup()
    {
        LogUtils.addConsoleLogger(Level.INFO);
        ActivityMasterConfiguration.get()
                .setApplicationEnterpriseName(TestEnterprise.name());
        IGuiceContext.instance();

        openAPI = IGuiceContext.get(OpenAPI.class);
        assertNotNull(openAPI, "The merged OpenAPI model should be provided by the GuicedEE OpenAPI module");
    }

    @Test
    public void globalInfoFromOpenApiDefinitionIsPresent()
    {
        assertNotNull(openAPI.getInfo(), "Merged document must retain the global @OpenAPIDefinition info block");
        org.junit.jupiter.api.Assertions.assertEquals("ActivityMaster FSDM API", openAPI.getInfo().getTitle(),
                "Global title should come from ActivityMasterOpenApiConfiguration");
    }

    @Test
    public void cerialMasterTagIsMerged()
    {
        assertNotNull(openAPI.getTags(), "The merged document should carry the tag catalogue");
        Set<String> tagNames = openAPI.getTags()
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        assertTrue(tagNames.contains(CERIAL_TAG),
                "The 'Cerial Master' tag should be merged into the document, but was: " + tagNames);
    }

    @Test
    public void cerialPathsAreScannedAndMerged()
    {
        assertNotNull(openAPI.getPaths(), "The merged document should expose scanned JAX-RS paths");
        Set<String> paths = openAPI.getPaths().keySet();

        // GET    {enterprise}/cerial/{requestingSystemName}/comport/{comPort}  — find / update
        // GET    {enterprise}/cerial/{requestingSystemName}/comports           — list
        // POST   {enterprise}/cerial/{requestingSystemName}/comport            — create
        assertPathPresent(paths, "/comport/{comPort}");
        assertPathPresent(paths, "/comports");
        assertPathPresent(paths, "/comport");
    }

    @Test
    public void existingFsdmPathsRemainAfterMerge()
    {
        Set<String> paths = openAPI.getPaths().keySet();

        // The cerial contribution must not displace the core FSDM resources.
        boolean fsdmPresent = paths.stream().anyMatch(p -> p.contains("/resource-item/"));
        assertTrue(fsdmPresent,
                "Core FSDM paths (e.g. /resource-item/) must remain alongside the merged cerial paths: " + paths);
    }

    @Test
    public void cerialEnterpriseAndSystemScopingTemplatesArePreserved()
    {
        Set<String> cerialPaths = openAPI.getPaths().keySet()
                .stream()
                .filter(p -> p.contains(CERIAL_SEGMENT))
                .collect(Collectors.toSet());

        assertFalse(cerialPaths.isEmpty(), "At least one '/cerial/' path should have been scanned");
        boolean allScoped = cerialPaths.stream()
                .allMatch(p -> p.contains("{enterprise}") && p.contains("{requestingSystemName}"));
        assertTrue(allScoped,
                "Cerial paths must keep the {enterprise} and {requestingSystemName} scoping templates: " + cerialPaths);
    }

    @Test
    public void cerialOperationsAreTaggedWithCerialMaster()
    {
        List<Operation> operations = openAPI.getPaths()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().contains(CERIAL_SEGMENT))
                .map(java.util.Map.Entry::getValue)
                .flatMap(item -> item.readOperations().stream())
                .toList();

        assertFalse(operations.isEmpty(),
                "Expected at least one operation for paths containing '" + CERIAL_SEGMENT + "'");

        boolean allTagged = operations.stream()
                .allMatch(op -> op.getTags() != null && op.getTags().contains(CERIAL_TAG));
        assertTrue(allTagged,
                "All operations under '" + CERIAL_SEGMENT + "' should be tagged '" + CERIAL_TAG + "'");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void assertPathPresent(Set<String> paths, String suffix)
    {
        boolean present = paths.stream()
                .anyMatch(p -> p.contains(CERIAL_SEGMENT) && p.endsWith(suffix));
        assertTrue(present,
                "Expected a merged cerial path ending in '" + suffix + "', but was: " + paths);
    }
}


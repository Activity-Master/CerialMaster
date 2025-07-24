# Activity Master Reactive Migration Guide

## Overview

This guide outlines the structure and patterns for implementing reactive programming in the Activity Master system using SmallRye Mutiny. The migration focuses on converting synchronous operations to reactive streams for better scalability and non-blocking behavior.

## 🔧 Technology Stack

**ActivityMaster uses a specialized technology stack that differs from mainstream frameworks:**

- 🎯 **Dependency Injection**: Google Guice with GuicedEE wrapper (NOT Spring or Quarkus)
- 🏗️ **Architecture Pattern**: CRTP (Curiously Recurring Template Pattern) is fundamental to all structures
- 🗄️ **Query Builder**: EntityAssist for criteria query building (NOT JPA Criteria API)
- ⚡ **Reactive Framework**: SmallRye Mutiny for non-blocking operations
- 💾 **Database Access**: Hibernate Reactive with Mutiny integration
- 🔄 **Session Management**: External applications manage `Mutiny.Session` lifecycle

### Key Technology Differences

#### Dependency Injection with GuicedEE
```java
// ✅ CORRECT: GuicedEE/Guice injection patterns
@Inject
private IExampleService<?> exampleService;

// Service retrieval through GuiceContext
IActiveFlagService<?> acService = IGuiceContext.get(IActiveFlagService.class);

// Named injection for specific systems
@Inject
@Named(ActivityMasterSystemName)
private ISystems<?, ?> activityMasterSystem;
```

#### CRTP Pattern Implementation
```java
// ✅ CORRECT: CRTP is fundamental to ActivityMaster structures
public class ExampleSystem 
        extends ActivityMasterDefaultSystem<ExampleSystem>  // CRTP here
        implements IActivityMasterSystem<ExampleSystem>     // CRTP here
{
    // The class extends and implements using itself as the type parameter
    // This provides compile-time type safety and method chaining
}

public class ExampleService implements IExampleService<ExampleService>  // CRTP pattern
{
    // Service implements interface with itself as type parameter
    // Enables fluent APIs and type-safe method chaining
}
```

#### EntityAssist Query Building
```java
// ✅ CORRECT: EntityAssist criteria builder pattern
return new Example().builder(session)  // EntityAssist builder
    .withName(name)                     // Fluent query building
    .withSystem(system)                 // Type-safe criteria
    .inActiveRange()                    // Pre-built common filters
    .inDateRange()                      // Date range filtering
    .get()                              // Execute query
    .onItem().invoke(example -> log.debug("✅ Found example: '{}'", example.getName()))
    .onFailure().invoke(error -> log.error("💥 Query failed: {}", error.getMessage(), error));

// ❌ INCORRECT: Don't use JPA Criteria API
// CriteriaBuilder cb = entityManager.getCriteriaBuilder(); // NOT used in ActivityMaster
```

### Framework Integration Notes
- **No Spring Boot**: ActivityMaster does not use Spring Boot or Spring Framework
- **No Quarkus**: While SmallRye Mutiny originated from Quarkus, ActivityMaster uses it independently
- **GuicedEE**: Custom Guice wrapper providing enhanced dependency injection capabilities
- **CRTP Everywhere**: The Curiously Recurring Template Pattern is used throughout for type safety
- **EntityAssist**: Custom query builder providing fluent, type-safe database queries

## ⚠️ Important: ActivityMaster as a Library

**ActivityMaster is designed as a library to be consumed by external applications**. Key session management principles:

- 🏛️ **External Session Management**: Sessions (`Mutiny.Session`) are **always** created and managed by the consuming applications
- 📚 **Library Pattern**: ActivityMaster provides services and systems that accept sessions as parameters
- 🔄 **Session Passing**: Sessions must be passed as the **first parameter** in all methods that require database access
- ⚠️ **Exception**: Only the `TimeSystem` creates its own stateless session internally for time-related operations
- 🧪 **Testing**: Test classes also use the main ActivityMaster library and must provide their own session management

### Session Parameter Pattern
```java
// ✅ CORRECT: Session as first parameter
public Uni<IExample<?, ?>> create(Mutiny.Session session, String name, String description, ISystems<?, ?> system, UUID... identityToken)
{
    // Implementation...
}

// ✅ CORRECT: All service methods follow this pattern
public Uni<List<IExample<?, ?>>> findBySystem(Mutiny.Session session, ISystems<?, ?> system, UUID... identityToken)
{
    // Implementation...
}

// ❌ INCORRECT: Never create sessions inside ActivityMaster (except TimeSystem)
public Uni<IExample<?, ?>> create(String name, String description, ISystems<?, ?> system)
{
    // DON'T DO THIS - session should come from caller
    Mutiny.Session session = sessionFactory.openSession();
    // ...
}
```

### Consumer Application Session Management
```java
// Example of how consuming applications should manage sessions
public class MyApplicationService
{
    @Inject
    private Mutiny.SessionFactory sessionFactory;
    
    @Inject
    private IExampleService activityMasterExampleService;
    
    public Uni<IExample<?, ?>> createExample(String name, String description, ISystems<?, ?> system)
    {
        log.info("🚀 Application creating example through ActivityMaster library");
        
        return sessionFactory.withSession(session ->
        {
            log.debug("📋 Session created by application, passing to ActivityMaster");
            
            // Pass session as first parameter to ActivityMaster service
            return activityMasterExampleService.create(session, name, description, system);
        })
        .onItem().invoke(result -> log.info("✅ Example created successfully: {}", result.getName()))
        .onFailure().invoke(error -> log.error("❌ Failed to create example: {}", error.getMessage(), error));
    }
}
```

### Test Session Management
```java
// Example of session management in tests
public class ExampleServiceTest
{
    @Inject
    private Mutiny.SessionFactory sessionFactory;
    
    @Inject
    private IExampleService exampleService;
    
    @Test
    public void testCreateExample()
    {
        // Test must provide its own session
        sessionFactory.withSession(session ->
        {
            log.debug("🧪 Test session created, testing ActivityMaster service");
            
            return exampleService.create(session, "Test Example", "Test Description", mockSystem)
                .onItem().invoke(result ->
                {
                    assertNotNull(result);
                    assertEquals("Test Example", result.getName());
                });
        })
        .await().atMost(Duration.ofSeconds(30));
    }
}
```

## Core Architecture Patterns

### 1. System Structure

All systems follow a consistent pattern extending `ActivityMasterDefaultSystem<T>` and implementing `IActivityMasterSystem<T>`:

```java
@Log4j2
public class ExampleSystem 
        extends ActivityMasterDefaultSystem<ExampleSystem>
        implements IActivityMasterSystem<ExampleSystem>
{
    @Inject
    private ISystemsService<?> systemsService;
    
    @Inject 
    private IExampleService<?> exampleService;
    
    // All methods receive session as first parameter
    @Override
    public ISystems<?, ?> registerSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise)
    {
        log.info("🚀 Registering system with externally provided session");
        // Implementation...
    }
}
```

### 2. Required System Methods

Every system must implement these core methods:

#### Core Registration Methods
```java
@Override
public ISystems<?, ?> registerSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise)
{
    log.info("🚀 Registering system: '{}' with external session", getSystemName());
    
    // Create the system and register it
    ISystems<?, ?> iSystems = systemsService
        .create(session, enterprise, getSystemName(), getSystemDescription())
        .await().atMost(Duration.ofMinutes(1));
        
    getSystem(session, enterprise).chain(system ->
    {
        return systemsService.registerNewSystem(session, enterprise, system);
    }).await().atMost(Duration.ofMinutes(1));
    
    return iSystems;
}

@Override
public void createDefaults(Mutiny.Session session, IEnterprise<?, ?> enterprise)
{
    log.info("🔧 Creating defaults with external session for: '{}'", getSystemName());
    // Create default data, classifications, and relationships
    // Use reactive patterns with .await() for synchronous interface compatibility
}

@Override
public Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise)
{
    log.info("🚀 Post-startup operations with external session for: '{}'", getSystemName());
    // Post-startup reactive operations
    // This method is fully reactive and returns Uni<Void>
    return systemsService.findSystem(session, enterprise, getSystemName())
        .chain(system ->
        {
            log.debug("✅ System found during post-startup: '{}'", system.getName());
            // Perform post-startup operations
            return Uni.createFrom().voidItem();
        });
}
```

#### System Metadata Methods
```java
@Override
public String getSystemName()
{
    return "YourSystemName"; // Use constants from service interfaces
}

@Override
public String getSystemDescription()
{
    return "Description of your system's purpose";
}

@Override
public Integer sortOrder()
{
    return Integer.MIN_VALUE + 10; // Determines system creation order
}

@Override
public int totalTasks()
{
    return 5; // For progress tracking
}
```

## 3. Reactive Programming Patterns

### 4. Reactive Programming Patterns

#### Enhanced Logging for Service Operations

All service operations should include comprehensive logging with visual indicators and appropriate log levels:

```java
private Uni<Void> createExampleData(Mutiny.Session session, IEnterprise<?, ?> enterprise)
{
    log.info("🚀 Starting example data creation for enterprise: '{}' with external session", enterprise.getName());
    logProgress("Example System", "Creating Example Data");
    
    return IActivityMasterService.getISystem(ActivityMasterSystemName, enterprise)
        .onItem().invoke(activityMasterSystem -> log.debug("✅ Retrieved ActivityMaster system: '{}'", activityMasterSystem.getName()))
        .onFailure().invoke(error -> log.error("❌ Failed to retrieve ActivityMaster system: {}", error.getMessage(), error))
        .chain(activityMasterSystem ->
        {
            log.debug("📋 Preparing parallel example creation operations");
            
            // Create a list of operations to run in parallel
            List<Uni<?>> operations = new ArrayList<>();
            
            // Add operations to the list with detailed logging
            // Note: session is passed through to all operations
            operations.add(
                exampleService.createExample(session, "Example1", "First example description", activityMasterSystem)
                    .onItem().invoke(result -> log.debug("✅ Created Example1 successfully"))
                    .onFailure().invoke(error -> log.error("❌ Failed to create Example1: {}", error.getMessage(), error))
            );
            operations.add(
                exampleService.createExample(session, "Example2", "Second example description", activityMasterSystem)
                    .onItem().invoke(result -> log.debug("✅ Created Example2 successfully"))
                    .onFailure().invoke(error -> log.error("❌ Failed to create Example2: {}", error.getMessage(), error))
            );
            
            log.info("🔄 Running {} example creation operations in parallel", operations.size());
            
            // Run all operations in parallel
            return Uni.combine()
                .all()
                .unis(operations)
                .discardItems()
                .onItem().invoke(() -> log.info("🎉 All example creation operations completed successfully"))
                .onFailure()
                .invoke(error -> log.error("💥 One or more example creation operations failed: {}", error.getMessage(), error))
                .map(result ->
                {
                    log.debug("📤 Returning void result from createExampleData");
                    return null; // Convert to Void
                });
        });
}
```

#### Pattern: Chain Operations with Uni and patterns for implementing reactive programming in the Activity Master system using SmallRye Mutiny. The migration focuses on converting synchronous operations to reactive streams for better scalability and non-blocking behavior.

## Core Architecture Patterns

### 1. System Structure

All systems follow a consistent pattern extending `ActivityMasterDefaultSystem<T>` and implementing `IActivityMasterSystem<T>`:

```java
@Log4j2
public class ExampleSystem 
        extends ActivityMasterDefaultSystem<ExampleSystem>
        implements IActivityMasterSystem<ExampleSystem>
{
    @Inject
    private ISystemsService<?> systemsService;
    
    @Inject 
    private IExampleService<?> exampleService;
    
    // Required methods implementation
}
```

### 2. Required System Methods

Every system must implement these core methods:

#### Core Registration Methods
```java
@Override
public ISystems<?, ?> registerSystem(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    // Create the system and register it
    ISystems<?, ?> iSystems = systemsService
        .create(session, enterprise, getSystemName(), getSystemDescription())
        .await().atMost(Duration.ofMinutes(1));
        
    getSystem(session, enterprise).chain(system -> {
        return systemsService.registerNewSystem(session, enterprise, system);
    }).await().atMost(Duration.ofMinutes(1));
    
    return iSystems;
}

@Override
public void createDefaults(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    // Create default data, classifications, and relationships
    // Use reactive patterns with .await() for synchronous interface compatibility
}

@Override
public Uni<Void> postStartup(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    // Post-startup reactive operations
    // This method is fully reactive and returns Uni<Void>
    return systemsService.findSystem(session, enterprise, getSystemName())
        .chain(system -> {
            // Perform post-startup operations
            return Uni.createFrom().voidItem();
        });
}
```

#### System Metadata Methods
```java
@Override
public String getSystemName() {
    return "YourSystemName"; // Use constants from service interfaces
}

@Override
public String getSystemDescription() {
    return "Description of your system's purpose";
}

@Override
public Integer sortOrder() {
    return Integer.MIN_VALUE + 10; // Determines system creation order
}

@Override
public int totalTasks() {
    return 5; // For progress tracking
}
```

## 3. Reactive Programming Patterns

### Service Operations

#### Pattern: Chain Operations with Uni
```java
private Uni<Void> createExampleData(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    logProgress("Example System", "Creating Example Data");
    
    return IActivityMasterService.getISystem(ActivityMasterSystemName, enterprise)
        .chain(activityMasterSystem -> {
            log.debug("Got system: {}", activityMasterSystem.getName());
            
            // Create a list of operations to run in parallel
            List<Uni<?>> operations = new ArrayList<>();
            
            // Add operations to the list
            operations.add(service.createExample(session, "Example1", "Description", activityMasterSystem));
            operations.add(service.createExample(session, "Example2", "Description", activityMasterSystem));
            
            log.info("Running {} example creation operations in parallel", operations.size());
            
            // Run all operations in parallel
            return Uni.combine()
                .all()
                .unis(operations)
                .discardItems()
                .onFailure()
                .invoke(error -> log.error("Error creating examples: {}", error.getMessage(), error))
                .map(result -> null); // Convert to Void
        });
}
```

#### Pattern: Sequential Operations with Comprehensive Logging
```java
private Uni<Void> createSequentialData(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    log.info("📋 Starting sequential data creation process");
    
    // Create parent first, then children
    return service.createParent(session, "Parent", activityMasterSystem)
        .onItem().invoke(parent -> log.info("✅ Parent '{}' created successfully with ID: {}", parent.getName(), parent.getId()))
        .onFailure().invoke(error -> log.error("❌ Failed to create parent: {}", error.getMessage(), error))
        .chain(parent -> {
            log.info("👶 Creating children for parent: '{}'", parent.getName());
            
            // Now create children with parent reference
            List<Uni<?>> childOperations = new ArrayList<>();
            
            childOperations.add(
                service.createChild(session, "Child1", parent, activityMasterSystem)
                    .onItem().invoke(child -> log.debug("✅ Child1 created for parent '{}'", parent.getName()))
                    .onFailure().invoke(error -> log.error("❌ Failed to create Child1: {}", error.getMessage(), error))
            );
            childOperations.add(
                service.createChild(session, "Child2", parent, activityMasterSystem)
                    .onItem().invoke(child -> log.debug("✅ Child2 created for parent '{}'", parent.getName()))
                    .onFailure().invoke(error -> log.error("❌ Failed to create Child2: {}", error.getMessage(), error))
            );
            
            log.info("🔄 Processing {} child creation operations", childOperations.size());
            
            return Uni.combine().all().unis(childOperations).discardItems()
                .onItem().invoke(() -> log.info("🎉 All children created successfully for parent '{}'", parent.getName()))
                .onFailure().invoke(error -> log.error("💥 Failed to create some children: {}", error.getMessage(), error));
        })
        .map(result -> {
            log.info("🏁 Sequential data creation completed");
            return null;
        });
}
```

### 4. Synchronous Interface Compatibility

When implementing synchronous interfaces with reactive internals:

```java
@Override
public void createDefaults(Mutiny.Session session, IEnterprise<?, ?> enterprise) {
    log.info("Starting createDefaults for Example System");
    
    // Get the ActivityMaster system reactively but await the result
    ISystems<?, ?> activityMasterSystem = IActivityMasterService.getISystem(ActivityMasterSystemName, enterprise)
        .await().atMost(Duration.ofMinutes(1));
    
    logProgress("Example System", "Creating classifications...");
    
    // Create operations in parallel
    List<Uni<?>> operations = new ArrayList<>();
    operations.add(classificationService.create(session, "Example", "Description", activityMasterSystem));
    
    // Wait for completion in synchronous method
    Uni.combine().all().unis(operations)
        .discardItems()
        .await().atMost(Duration.ofMinutes(1));
}
```

## 5. Service Layer Patterns

### Service Implementation Structure with CRTP and GuicedEE

All services follow the CRTP pattern and use GuicedEE for dependency injection:

```java
@Log4j2
public class ExampleService implements IExampleService<ExampleService> // CRTP: service implements itself as type parameter
{
    @Override
    public Uni<IExample<?, ?>> create(Mutiny.Session session, String name, String description, ISystems<?, ?> system, UUID... identityToken)
    {
        log.info("🚀 Creating new example: '{}' for system: '{}' using GuicedEE/Guice", name, system.getName());
        log.debug("📝 Example details - Name: '{}', Description: '{}', System ID: {}, Session: {}", 
            name, description, system.getId(), systemToken);
        
        Example example = new Example();
        example.setName(name);
        example.setDescription(description);
        example.setSystemID(system);
        
        log.debug("📋 Example entity prepared, retrieving active flag for enterprise: {}", system.getEnterpriseID().getName());
        
        // GuicedEE service retrieval pattern
        IActiveFlagService<?> acService = IGuiceContext.get(IActiveFlagService.class);
        
        return acService.getActiveFlag(session, system.getEnterpriseID())
            .onItem().invoke(activeFlag -> log.debug("✅ Active flag retrieved: {}", activeFlag.getId()))
            .onFailure().invoke(error -> log.error("❌ Failed to retrieve active flag for enterprise '{}': {}", 
                system.getEnterpriseID().getName(), error.getMessage(), error))
            .chain(activeFlag ->
            {
                example.setActiveFlagID(activeFlag);
                log.debug("🔗 Active flag linked to example");
                
                log.info("💾 Persisting example '{}' to database using external session", name);
                
                // Persist the entity using the provided session
                return session.persist(example).replaceWith(Uni.createFrom().item(example))
                    .onItem().invoke(persisted -> log.info("✅ Example '{}' successfully persisted with ID: {}", 
                        name, persisted.getId()))
                    .onFailure().invoke(error -> log.error("❌ Failed to persist example '{}': {}", 
                        name, error.getMessage(), error))
                    .chain(persisted ->
                    {
                        log.debug("🔐 Starting security creation for example '{}' in background", name);
                        
                        // Start createDefaultSecurity in parallel without waiting
                        example.createDefaultSecurity(system, identityToken)
                            .subscribe().with(
                                result -> log.info("🛡️ Security setup completed successfully for example '{}'", name),
                                error -> log.warn("⚠️ Security creation failed for example '{}' but continuing operation: {}", 
                                    name, error.getMessage(), error)
                            );
                        
                        log.info("🎉 Example '{}' creation completed successfully", name);
                        return Uni.createFrom().item(persisted);
                    });
            });
    }
    
    @Override
    public Uni<IExample<?, ?>> findByName(Mutiny.Session session, String name, ISystems<?, ?> system, UUID... identityToken)
    {
        log.debug("🔍 Searching for example with name: '{}' in system: '{}' using EntityAssist", name, system.getName());
        
        // EntityAssist query builder pattern (NOT JPA Criteria API)
        return new Example().builder(session)  // EntityAssist fluent builder
            .withName(name)                     // Type-safe criteria
            .withSystem(system)                 // System filtering
            .inActiveRange()                    // Pre-built active filter
            .inDateRange()                      // Pre-built date filter
            .get()                              // Execute query
            .onItem().invoke(example ->
            {
                if (example != null)
                {
                    log.debug("✅ Found example: '{}' with ID: {}", name, example.getId());
                }
                else
                {
                    log.debug("❌ No example found with name: '{}'", name);
                }
            })
            .onFailure().invoke(error -> log.error("💥 EntityAssist query failed for example '{}': {}", 
                name, error.getMessage(), error));
    }
    
    @Override
    public Uni<Boolean> delete(Mutiny.Session session, IExample<?, ?> example, ISystems<?, ?> system, UUID... identityToken)
    {
        log.info("🗑️ Deleting example: '{}' (ID: {}) using external session", example.getName(), example.getId());
        
        return example.delete(session, system, identityToken)
            .onItem().invoke(success ->
            {
                if (success)
                {
                    log.info("✅ Example '{}' successfully deleted", example.getName());
                }
                else
                {
                    log.warn("⚠️ Example '{}' deletion returned false", example.getName());
                }
            })
            .onFailure().invoke(error -> log.error("❌ Failed to delete example '{}': {}", 
                example.getName(), error.getMessage(), error));
    }
}
```

## 6. Update Pattern Structure

### System Updates using @SortedUpdate
```java
@SortedUpdate(sortOrder = -100, taskCount = 3)
@Log4j2
public class ExampleBaseSetup implements ISystemUpdate {
    
    @Inject
    private IClassificationService<?> service;
    
    @Inject
    @Named(ActivityMasterSystemName)
    private ISystems<?, ?> activityMasterSystem;
    
    @Override
    public Uni<Boolean> update(IEnterprise<?, ?> enterprise) {
        log.info("Starting parallel creation of example classifications");
        logProgress("Example System", "Creating Base...");
        
        // Create base classification first
        return service.create(session, ExampleClassifications.BaseExample, activityMasterSystem)
            .chain(baseClassification -> {
                // Create child classifications in parallel
                List<Uni<?>> operations = new ArrayList<>();
                
                operations.add(service.create(session, ExampleClassifications.Child1, activityMasterSystem, ExampleClassifications.BaseExample));
                operations.add(service.create(session, ExampleClassifications.Child2, activityMasterSystem, ExampleClassifications.BaseExample));
                
                log.info("Running {} classification creation operations in parallel", operations.size());
                
                return Uni.combine()
                    .all()
                    .unis(operations)
                    .discardItems()
                    .onFailure()
                    .invoke(error -> log.error("Error creating classifications: {}", error.getMessage(), error))
                    .map(result -> true); // Return Boolean for ISystemUpdate
            });
    }
}
```

### 7. Enhanced Error Handling and Logging Patterns

#### Comprehensive Logging Levels and Icons

Use appropriate log levels with visual indicators throughout the service layer:

```java
// INFO level - Major operations, user-visible actions
log.info("🚀 Starting system initialization for '{}'", systemName);
log.info("✅ System '{}' successfully registered", systemName);
log.info("🎉 All operations completed successfully");

// DEBUG level - Detailed flow information, internal state
log.debug("📋 Preparing database query for entity: {}", entityType);
log.debug("🔗 Linking entity {} to parent {}", childId, parentId);
log.debug("📤 Returning result: {}", result);

// WARN level - Recoverable issues, background operation failures
log.warn("⚠️ Background security creation failed but continuing: {}", error.getMessage());
log.warn("🔄 Retrying operation after failure: {}", operationName);

// ERROR level - Critical failures, operation-stopping errors
log.error("❌ Failed to persist entity '{}': {}", entityName, error.getMessage(), error);
log.error("💥 Critical system failure in {}: {}", componentName, error.getMessage(), error);

// TRACE level - Very detailed debugging (when enabled)
if (log.isTraceEnabled()) {
    log.trace("🔍 Detailed entity state: {}", entity.toString());
    log.trace("📊 Performance metrics: operation took {}ms", duration);
}
```

#### Standard Error Handling with Enhanced Logging
```java
return someReactiveOperation()
    .onItem().invoke(result -> log.debug("✅ Operation completed successfully: {}", result))
    .onFailure().invoke(error -> {
        log.error("💥 Operation failed: {}", error.getMessage(), error);
        // Additional context logging
        log.debug("🔍 Failure context - Operation: {}, Parameters: {}", operationName, parameters);
    })
    .onFailure().recoverWithItem(() -> {
        log.warn("🔄 Recovering from failure with default value");
        return defaultValue;
    });
```

#### Service-Level Performance and Progress Logging
```java
@Override
public Uni<List<IExample<?, ?>>> processLargeDataset(Mutiny.Session session, List<DataItem> items, ISystems<?, ?> system) {
    long startTime = System.currentTimeMillis();
    log.info("🚀 Starting large dataset processing: {} items", items.size());
    
    return Uni.createFrom().item(items)
        .onItem().invoke(() -> log.debug("📋 Dataset processing initialized"))
        .chain(itemList -> {
            List<Uni<IExample<?, ?>>> operations = new ArrayList<>();
            
            for (int i = 0; i < itemList.size(); i++) {
                final int index = i;
                final DataItem item = itemList.get(i);
                
                operations.add(
                    processItem(session, item, system)
                        .onItem().invoke(result -> {
                            if ((index + 1) % 100 == 0) { // Progress every 100 items
                                log.info("📊 Progress: {}/{} items processed", index + 1, itemList.size());
                                logProgress("Data Processing", String.format("Processed %d/%d items", index + 1, itemList.size()));
                            }
                        })
                        .onFailure().invoke(error -> log.warn("⚠️ Failed to process item {}: {}", index, error.getMessage()))
                );
            }
            
            return Uni.combine().all().unis(operations).asTuple()
                .onItem().invoke(results -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("🎉 Dataset processing completed in {}ms. Processed: {}, Failed: {}", 
                        duration, 
                        results.asList().size(), 
                        items.size() - results.asList().size());
                })
                .map(results -> results.asList());
        });
}
``` 

### Background Operations with Detailed Logging
```java
// Start operation in background without blocking main flow
log.debug("🔄 Starting background operation: {}", operationName);

backgroundOperation
    .onItem().invoke(result -> log.info("🌟 Background operation '{}' completed successfully: {}", operationName, result))
    .onFailure().invoke(error -> log.warn("⚠️ Background operation '{}' failed: {}", operationName, error.getMessage(), error))
    .subscribe().with(
        result -> {
            log.debug("✅ Background operation '{}' processing completed", operationName);
        },
        error -> {
            log.warn("🔥 Background operation '{}' subscription failed: {}", operationName, error.getMessage(), error);
        }
    );

log.debug("📤 Main operation continuing while '{}' runs in background", operationName);
```

#### Service Health and Monitoring Logging
```java
@Override
public Uni<Boolean> healthCheck(Mutiny.Session session) {
    log.debug("🏥 Performing service health check");
    long startTime = System.currentTimeMillis();
    
    return performHealthCheckOperations(session)
        .onItem().invoke(isHealthy -> {
            long duration = System.currentTimeMillis() - startTime;
            if (isHealthy) {
                log.info("💚 Service health check passed in {}ms", duration);
            } else {
                log.warn("💛 Service health check failed in {}ms", duration);
            }
        })
        .onFailure().invoke(error -> {
            long duration = System.currentTimeMillis() - startTime;
            log.error("💔 Service health check error in {}ms: {}", duration, error.getMessage(), error);
        });
}
```

## 8. Important Migration Principles

### DO's
- ✅ **Always pass `Mutiny.Session` as the first parameter** in all methods that need database access
- ✅ **Let consuming applications manage session lifecycle** - never create sessions inside ActivityMaster (except TimeSystem)
- ✅ Use `Uni.createFrom().item()` for wrapping values
- ✅ Use `.chain()` for sequential operations that depend on previous results
- ✅ Use `Uni.combine().all().unis()` for parallel operations
- ✅ **Always implement comprehensive logging with appropriate levels and visual icons**
- ✅ **Use structured logging with context information (entity names, IDs, session identifiers)**
- ✅ **Log performance metrics for operations that take significant time**
- ✅ Always use `.onFailure().invoke()` for error logging with detailed context
- ✅ Use `.await().atMost(Duration.ofMinutes(1))` only in synchronous interface methods
- ✅ Return `Uni<Void>` from reactive methods using `.map(result -> null)`
- ✅ Use `logProgress()` for user feedback during long operations
- ✅ **Include progress logging for batch operations (every N items processed)**
- ✅ **Log both start and completion of major operations with timing information**
- ✅ **Include session identifiers in debug logs for traceability**

### DON'Ts
- ❌ **Never create `Mutiny.Session` instances inside ActivityMaster services** (except TimeSystem)
- ❌ **Don't manage session lifecycle within ActivityMaster** - that's the consumer's responsibility
- ❌ Don't mix blocking and reactive operations unnecessarily
- ❌ Don't use `.await()` in fully reactive methods
- ❌ Don't forget error handling in reactive chains
- ❌ Don't create deeply nested reactive chains (use helper methods)
- ❌ Don't ignore background operation failures completely
- ❌ **Don't use generic log messages without context (avoid "Operation failed")**
- ❌ **Don't log sensitive information (passwords, tokens) even at TRACE level**
- ❌ **Don't use INFO level for every small operation (reserve for major milestones)**
- ❌ **Don't forget to log both success and failure cases**

### Enhanced Logging Standards
- 🚀 **Startup/Initialization operations** - Use rocket icon
- ✅ **Successful operations** - Use checkmark icon  
- ❌ **Failed operations** - Use X icon
- ⚠️ **Warnings/Recoverable issues** - Use warning icon
- 🔄 **Retry/Background operations** - Use rotation icon
- 📋 **Data preparation/setup** - Use clipboard icon
- 💾 **Database operations** - Use disk icon
- 🔗 **Linking/Association operations** - Use link icon
- 🎉 **Major completion milestones** - Use celebration icon
- 💥 **Critical failures** - Use explosion icon
- 🔍 **Debug/Investigation** - Use magnifying glass icon
- 📊 **Performance/Progress metrics** - Use chart icon
- 🏥 **Health checks** - Use hospital icon
- 🛡️ **Security operations** - Use shield icon
- 🏛️ **Session management** - Use building icon
- 📚 **Library operations** - Use books icon

### Migration Notes
- **TimeSystem**: Still needs migration - currently uses synchronous patterns and is the ONLY system that creates its own sessions
- **Session Management**: Always use externally provided `Mutiny.Session` for database operations
- **Technology Stack**: Use GuicedEE/Guice for DI (NOT Spring/Quarkus), EntityAssist for queries (NOT JPA Criteria), CRTP patterns throughout
- **Service Injection**: Use `@Inject` for GuicedEE dependency injection of services
- **Query Building**: Use EntityAssist fluent builders, not JPA Criteria API
- **CRTP Implementation**: All services and systems must implement CRTP pattern for type safety
- **Progress Tracking**: Use `logProgress()` for UI feedback during operations
- **Security**: Default security creation often runs in background to avoid blocking
- **Library Pattern**: ActivityMaster is consumed as a library, not a standalone application

## 9. Common Patterns

### Creating Hierarchical Data with Enhanced Logging
```java
// Parent -> Children pattern with comprehensive logging
return createParent(session, parentData, system)
    .onItem().invoke(parent -> log.info("✅ Parent '{}' created with ID: {}", parent.getName(), parent.getId()))
    .onFailure().invoke(error -> log.error("❌ Failed to create parent '{}': {}", parentData.getName(), error.getMessage(), error))
    .chain(parent -> {
        log.info("👶 Creating {} children for parent '{}'", childrenData.size(), parent.getName());
        
        List<Uni<?>> childOperations = new ArrayList<>();
        for (int i = 0; i < childrenData.size(); i++) {
            final int index = i;
            final ChildData childData = childrenData.get(i);
            
            childOperations.add(
                createChild(session, childData, parent, system)
                    .onItem().invoke(child -> log.debug("✅ Child {}/{} '{}' created for parent '{}'", 
                        index + 1, childrenData.size(), child.getName(), parent.getName()))
                    .onFailure().invoke(error -> log.error("❌ Failed to create child {}/{} '{}': {}", 
                        index + 1, childrenData.size(), childData.getName(), error.getMessage(), error))
            );
        }
        
        return Uni.combine().all().unis(childOperations).discardItems()
            .onItem().invoke(() -> log.info("🎉 All {} children created successfully for parent '{}'", 
                childrenData.size(), parent.getName()))
            .onFailure().invoke(error -> log.error("💥 Failed to create some children for parent '{}': {}", 
                parent.getName(), error.getMessage(), error));
    })
    .map(result -> {
        log.debug("📤 Hierarchical data creation completed");
        return null;
    });
```

### Service Discovery Pattern with Logging
```java
// Getting system references with detailed logging
log.debug("🔍 Retrieving ActivityMaster system for enterprise: '{}'", enterprise.getName());

ISystems<?, ?> activityMasterSystem = IActivityMasterService.getISystem(ActivityMasterSystemName, enterprise)
    .onItem().invoke(system -> log.debug("✅ ActivityMaster system retrieved: '{}' (ID: {})", 
        system.getName(), system.getId()))
    .onFailure().invoke(error -> log.error("❌ Failed to retrieve ActivityMaster system: {}", 
        error.getMessage(), error))
    .await().atMost(Duration.ofMinutes(1));

log.debug("📋 Using ActivityMaster system for subsequent operations");
```

### Parallel Classification Creation with Progress Logging
```java
// Create multiple classifications simultaneously with progress tracking
log.info("🚀 Starting parallel creation of {} classifications", classifications.size());

List<Uni<?>> operations = new ArrayList<>();
for (int i = 0; i < classifications.size(); i++) {
    final int index = i;
    final ClassificationData classification = classifications.get(i);
    
    operations.add(
        service.create(session, classification, system, parent)
            .onItem().invoke(result -> {
                log.debug("✅ Classification {}/{} '{}' created successfully", 
                    index + 1, classifications.size(), classification.getName());
                
                // Log progress every 10 items or at completion
                if ((index + 1) % 10 == 0 || (index + 1) == classifications.size()) {
                    log.info("📊 Classification progress: {}/{} completed", index + 1, classifications.size());
                    logProgress("Classifications", String.format("Created %d/%d classifications", 
                        index + 1, classifications.size()));
                }
            })
            .onFailure().invoke(error -> log.error("❌ Failed to create classification {}/{} '{}': {}", 
                index + 1, classifications.size(), classification.getName(), error.getMessage(), error))
    );
}

return Uni.combine()
    .all()
    .unis(operations)
    .discardItems()
    .onItem().invoke(() -> log.info("🎉 All {} classifications created successfully", classifications.size()))
    .onFailure().invoke(error -> log.error("💥 Failed to create some classifications: {}", error.getMessage(), error))
    .map(result -> null);
```

## 10. Best Practices Summary

1. **Library Architecture**: Remember ActivityMaster is a library - sessions come from consuming applications
2. **Technology Stack Adherence**: Use GuicedEE/Guice (NOT Spring/Quarkus), EntityAssist (NOT JPA Criteria), and CRTP patterns throughout
3. **Session Management**: Always pass `Mutiny.Session` as the first parameter, never create sessions internally (except TimeSystem)
4. **CRTP Implementation**: All services and systems must follow the Curiously Recurring Template Pattern for type safety
5. **Query Building**: Use EntityAssist fluent builders for all database queries, avoiding JPA Criteria API
6. **Dependency Injection**: Use GuicedEE patterns with `@Inject` and `IGuiceContext.get()` for service retrieval
7. **Comprehensive Logging**: Implement extensive logging with visual icons, appropriate levels, and structured context
   - Use emoji icons for visual clarity in logs
   - Include timing information for performance monitoring
   - Log both success and failure scenarios
   - Provide progress updates for long-running operations
   - Include entity names, IDs, session identifiers, and operation context in log messages
8. **Error Handling**: Always handle failures gracefully with detailed error logging including session context
9. **Performance**: Use parallel operations where possible and log performance metrics
10. **Progress Tracking**: Provide user feedback for long-running operations with `logProgress()`
11. **Security**: Handle security creation asynchronously to avoid blocking
12. **Testing**: Ensure reactive chains are properly tested with proper session management
13. **Documentation**: Document complex reactive flows and session passing patterns
14. **Monitoring**: Include health check logging and service monitoring capabilities with session tracking
15. **Debugging**: Use appropriate log levels (TRACE for detailed debugging, DEBUG for flow, INFO for milestones, WARN for recoverable issues, ERROR for failures)
16. **Consistency**: Follow the established patterns across all systems and maintain consistent session parameter ordering

This guide ensures consistent implementation across the Activity Master system using GuicedEE, EntityAssist, and CRTP patterns, providing a clear migration path from synchronous to reactive programming while maintaining proper session management as a library component.

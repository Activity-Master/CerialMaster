# ActivityMaster Logging Rules and Standards

## Overview

This document establishes comprehensive logging standards for the ActivityMaster library. All service operations should implement extensive logging with visual indicators, appropriate log levels, and structured context information to ensure excellent debugging, monitoring, and operational visibility.

## Core Logging Principles

### 1. Library Context Awareness
- 🏛️ **Session Tracking**: Always include session identifiers in debug logs for traceability across the library boundary
- 📚 **Library Operations**: Log when external applications interact with ActivityMaster services
- 🔄 **Session Passing**: Document session flow from consuming applications through ActivityMaster services

### 2. Visual Clarity with Emoji Icons
Use consistent emoji icons throughout all logging to provide instant visual recognition of operation types and outcomes.

### 3. Structured Context Information
Include relevant context in all log messages:
- Entity names and IDs
- System names and identifiers
- Session identifiers (for debugging)
- Operation parameters
- Performance metrics
- Progress indicators

## Logging Level Guidelines

### INFO Level - Major Operations and User-Visible Actions
Use for significant milestones and operations that users or administrators need to track:

```java
// System operations
log.info("🚀 Starting system initialization for '{}' with external session", systemName);
log.info("✅ System '{}' successfully registered", systemName);
log.info("🎉 All operations completed successfully");

// Service operations
log.info("🚀 Creating new example: '{}' for system: '{}' with external session", name, system.getName());
log.info("✅ Example '{}' successfully persisted with ID: {}", name, persisted.getId());
log.info("🎉 Example '{}' creation completed successfully", name);

// Library interactions
log.info("🚀 Application creating example through ActivityMaster library");
log.info("✅ Example created successfully: {}", result.getName());

// Progress milestones
log.info("📊 Progress: {}/{} items processed", processed, total);
log.info("🎉 Dataset processing completed in {}ms. Processed: {}, Failed: {}", duration, success, failed);
```

### DEBUG Level - Detailed Flow Information and Internal State
Use for detailed execution flow, internal state changes, and session tracking:

```java
// Flow tracking with session context
log.debug("📋 Preparing database query for entity: {} using session: {}", entityType, session.hashCode());
log.debug("🔗 Linking entity {} to parent {} with external session", childId, parentId);
log.debug("📤 Returning result: {} from session: {}", result, session.hashCode());

// Library boundary operations
log.debug("📋 Session created by application, passing to ActivityMaster");
log.debug("🧪 Test session created, testing ActivityMaster service");

// Internal state and preparation
log.debug("📋 Example entity prepared, retrieving active flag for enterprise: {}", enterpriseName);
log.debug("✅ Active flag retrieved: {}", activeFlag.getId());
log.debug("🔗 Active flag linked to example");

// Detailed operation context
log.debug("📝 Example details - Name: '{}', Description: '{}', System ID: {}, Session: {}", 
    name, description, system.getId(), session.hashCode());
```

### WARN Level - Recoverable Issues and Background Operation Failures
Use for non-critical issues that don't stop operations but should be noted:

```java
// Background operation failures
log.warn("⚠️ Background security creation failed but continuing: {}", error.getMessage());
log.warn("⚠️ Security creation failed for example '{}' but continuing operation: {}", 
    name, error.getMessage(), error);

// Retry scenarios
log.warn("🔄 Retrying operation after failure: {}", operationName);
log.warn("🔄 Recovering from failure with default value for session {}", session.hashCode());

// Unexpected but non-critical results
log.warn("⚠️ Example '{}' deletion returned false", example.getName());
log.warn("💛 Service health check failed in {}ms", duration);

// Background subscription issues
log.warn("🔥 Background operation '{}' subscription failed: {}", operationName, error.getMessage(), error);
log.warn("⚠️ Failed to process item {}: {}", index, error.getMessage());
```

### ERROR Level - Critical Failures and Operation-Stopping Errors
Use for serious errors that prevent operations from completing successfully:

```java
// Critical operation failures with session context
log.error("❌ Failed to persist entity '{}' with session {}: {}", entityName, session.hashCode(), error.getMessage(), error);
log.error("💥 Critical system failure in {}: {}", componentName, error.getMessage(), error);

// Library interaction failures
log.error("❌ Failed to create example: {}", error.getMessage(), error);
log.error("❌ Failed to retrieve ActivityMaster system with session {}: {}", 
    session.hashCode(), error.getMessage(), error);

// Service operation failures
log.error("❌ Failed to retrieve active flag for enterprise '{}': {}", 
    system.getEnterpriseID().getName(), error.getMessage(), error);
log.error("💥 One or more example creation operations failed: {}", error.getMessage(), error);

// Health check failures
log.error("💔 Service health check error in {}ms: {}", duration, error.getMessage(), error);

// Additional context for debugging
log.error("💥 Operation failed with session {}: {}", session.hashCode(), error.getMessage(), error);
log.debug("🔍 Failure context - Operation: {}, Parameters: {}, Session: {}", operationName, parameters, session.hashCode());
```

### TRACE Level - Very Detailed Debugging (When Enabled)
Use conditionally for extremely detailed debugging information:

```java
// Detailed entity state with session tracking
if (log.isTraceEnabled()) 
{
    log.trace("🔍 Detailed entity state with session {}: {}", session.hashCode(), entity.toString());
    log.trace("📊 Performance metrics: operation took {}ms with session {}", duration, session.hashCode());
}
```

## Visual Icon Standards

### Core Operation Icons
- 🚀 **Startup/Initialization operations** - Use rocket icon
- ✅ **Successful operations** - Use checkmark icon  
- ❌ **Failed operations** - Use X icon
- ⚠️ **Warnings/Recoverable issues** - Use warning icon
- 🔄 **Retry/Background operations** - Use rotation icon
- 🎉 **Major completion milestones** - Use celebration icon
- 💥 **Critical failures** - Use explosion icon

### Data and Processing Icons
- 📋 **Data preparation/setup** - Use clipboard icon
- 💾 **Database operations** - Use disk icon
- 🔗 **Linking/Association operations** - Use link icon
- 🔍 **Debug/Investigation** - Use magnifying glass icon
- 📊 **Performance/Progress metrics** - Use chart icon
- 📤 **Return/Output operations** - Use outbox icon

### System and Security Icons
- 🏥 **Health checks** - Use hospital icon
- 🛡️ **Security operations** - Use shield icon
- 🏛️ **Session management** - Use building icon
- 📚 **Library operations** - Use books icon
- 🔐 **Security/Authentication** - Use lock icon

### Lifecycle and Flow Icons
- 👶 **Child/Dependent creation** - Use baby icon
- 🗑️ **Deletion operations** - Use trash icon
- 🌟 **Background completion** - Use star icon
- 💚 **Health check success** - Use green heart icon
- 💛 **Health check warning** - Use yellow heart icon
- 💔 **Health check failure** - Use broken heart icon
- 🔥 **Subscription/Event failures** - Use fire icon

## Enhanced Logging Patterns

### Service Operations with Comprehensive Logging

```java
@Override
public Uni<IExample<?, ?>> create(Mutiny.Session session, String name, String description, ISystems<?, ?> system, UUID... identityToken)
{
    log.info("🚀 Creating new example: '{}' for system: '{}' with external session", name, system.getName());
    log.debug("📝 Example details - Name: '{}', Description: '{}', System ID: {}, Session: {}", 
        name, description, system.getId(), session.hashCode());
    
    Example example = new Example();
    example.setName(name);
    example.setDescription(description);
    example.setSystemID(system);
    
    log.debug("📋 Example entity prepared, retrieving active flag for enterprise: {}", system.getEnterpriseID().getName());
    
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
```

### Parallel Operations with Progress Tracking

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
            
            List<Uni<?>> operations = new ArrayList<>();
            
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

### Large Dataset Processing with Progress Logging

```java
@Override
public Uni<List<IExample<?, ?>>> processLargeDataset(Mutiny.Session session, List<DataItem> items, ISystems<?, ?> system)
{
    long startTime = System.currentTimeMillis();
    log.info("🚀 Starting large dataset processing: {} items", items.size());
    
    return Uni.createFrom().item(items)
        .onItem().invoke(() -> log.debug("📋 Dataset processing initialized"))
        .chain(itemList ->
        {
            List<Uni<IExample<?, ?>>> operations = new ArrayList<>();
            
            for (int i = 0; i < itemList.size(); i++)
            {
                final int index = i;
                final DataItem item = itemList.get(i);
                
                operations.add(
                    processItem(session, item, system)
                        .onItem().invoke(result ->
                        {
                            if ((index + 1) % 100 == 0) // Progress every 100 items
                            {
                                log.info("📊 Progress: {}/{} items processed", index + 1, itemList.size());
                                logProgress("Data Processing", String.format("Processed %d/%d items", index + 1, itemList.size()));
                            }
                        })
                        .onFailure().invoke(error -> log.warn("⚠️ Failed to process item {}: {}", index, error.getMessage()))
                );
            }
            
            return Uni.combine().all().unis(operations).asTuple()
                .onItem().invoke(results ->
                {
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
log.debug("🔄 Starting background operation: {} with session {}", operationName, session.hashCode());

backgroundOperation
    .onItem().invoke(result -> log.info("🌟 Background operation '{}' completed successfully: {}", operationName, result))
    .onFailure().invoke(error -> log.warn("⚠️ Background operation '{}' failed: {}", operationName, error.getMessage(), error))
    .subscribe().with(
        result -> log.debug("✅ Background operation '{}' processing completed", operationName),
        error -> log.warn("🔥 Background operation '{}' subscription failed: {}", operationName, error.getMessage(), error)
    );

log.debug("📤 Main operation continuing while '{}' runs in background", operationName);
```

### Standard Error Handling with Enhanced Logging

```java
return someReactiveOperation(session, parameters)
    .onItem().invoke(result -> log.debug("✅ Operation completed successfully with session {}: {}", session.hashCode(), result))
    .onFailure().invoke(error ->
    {
        log.error("💥 Operation failed with session {}: {}", session.hashCode(), error.getMessage(), error);
        // Additional context logging
        log.debug("🔍 Failure context - Operation: {}, Parameters: {}, Session: {}", operationName, parameters, session.hashCode());
    })
    .onFailure().recoverWithItem(() ->
    {
        log.warn("🔄 Recovering from failure with default value for session {}", session.hashCode());
        return defaultValue;
    });
```

### Health Check and Monitoring Logging

```java
@Override
public Uni<Boolean> healthCheck(Mutiny.Session session)
{
    log.debug("🏥 Performing service health check");
    long startTime = System.currentTimeMillis();
    
    return performHealthCheckOperations(session)
        .onItem().invoke(isHealthy ->
        {
            long duration = System.currentTimeMillis() - startTime;
            if (isHealthy)
            {
                log.info("💚 Service health check passed in {}ms", duration);
            }
            else
            {
                log.warn("💛 Service health check failed in {}ms", duration);
            }
        })
        .onFailure().invoke(error ->
        {
            long duration = System.currentTimeMillis() - startTime;
            log.error("💔 Service health check error in {}ms: {}", duration, error.getMessage(), error);
        });
}
```

## Library-Specific Logging Patterns

### Consumer Application Session Management

```java
public class MyApplicationService
{
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

### Test Logging Patterns

```java
public class ExampleServiceTest
{
    @Test
    public void testCreateExample()
    {
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

## Logging Standards and Best Practices

### DO's
- ✅ **Always include session identifiers in debug logs** for traceability across library boundaries
- ✅ **Use structured logging with context information** (entity names, IDs, session identifiers)
- ✅ **Log performance metrics for operations that take significant time**
- ✅ **Include progress logging for batch operations** (every N items processed)
- ✅ **Log both start and completion of major operations** with timing information
- ✅ **Use consistent emoji icons** for visual clarity and instant recognition
- ✅ **Provide detailed context in error messages** including operation parameters
- ✅ **Log library boundary interactions** to track external application usage
- ✅ **Include operation timing** for performance monitoring and optimization

### DON'Ts
- ❌ **Don't use generic log messages without context** (avoid "Operation failed")
- ❌ **Don't log sensitive information** (passwords, tokens) even at TRACE level
- ❌ **Don't use INFO level for every small operation** (reserve for major milestones)
- ❌ **Don't forget to log both success and failure cases**
- ❌ **Don't ignore session context** in debug logging
- ❌ **Don't mix logging levels inappropriately** (follow the guidelines strictly)
- ❌ **Don't log without emoji icons** (maintain visual consistency)

## Implementation Guidelines

### 1. Consistent Message Format
```java
// Pattern: [Icon] [Action] [Entity] [Context] [Additional Info]
log.info("🚀 Creating new example: '{}' for system: '{}' with external session", name, system.getName());
log.debug("📝 Example details - Name: '{}', Description: '{}', System ID: {}, Session: {}", 
    name, description, system.getId(), session.hashCode());
```

### 2. Session Tracking
Always include session hash codes in debug logs for traceability:
```java
log.debug("💾 Persisting example '{}' to database using session {}", name, session.hashCode());
log.debug("✅ Operation completed successfully with session {}: {}", session.hashCode(), result);
```

### 3. Progress Tracking
For operations processing multiple items:
```java
if ((index + 1) % progressInterval == 0 || (index + 1) == total)
{
    log.info("📊 Progress: {}/{} items processed", index + 1, total);
    logProgress("Operation Name", String.format("Processed %d/%d items", index + 1, total));
}
```

### 4. Error Context
Provide comprehensive error context:
```java
.onFailure().invoke(error ->
{
    log.error("💥 Operation failed with session {}: {}", session.hashCode(), error.getMessage(), error);
    log.debug("🔍 Failure context - Operation: {}, Parameters: {}, Session: {}", 
        operationName, parameters, session.hashCode());
})
```

This comprehensive logging standard ensures excellent visibility into ActivityMaster library operations, facilitating debugging, monitoring, and operational excellence while maintaining consistency across all components.

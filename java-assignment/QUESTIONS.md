# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Standardize on Domain-Driven Database Access
--------------------------------------------
The current codebase exhibits a hybrid data access layer, mixing domain-based and stereotype-based implementation strategies. 
To establish consistency and align with official Quarkus community best practices, we should refactor toward a unified domain-based (Active Record/Panache Entity) model, despite the my traditional Spring Boot background.

Introduce a Dedicated Service Layer
-----------------------------------
The project currently lacks traditional, decoupled service layers. 
Introducing an explicit service layer will cleanly separate business logic from HTTP endpoints and data access, significantly improving code maintainability, testability, and reuse.

Adopt MapStruct for Type-Safe Mapping
-------------------------------------
Data transformation between entities, DTOs, and API payloads is currently handled manually. 
Implementing an automated mapping library like MapStruct will eliminate boilerplate code, enforce structural safety, and improve overall code quality.

Visibility and Accessor Patterns:
---------------------------------
The absence of traditional getters/setters and the use of public visibility in the data models initially felt unconventional coming from a standard Hibernate/Spring background. 
If this is public-field pattern is the idiomatic standard for Quarkus Panache (which automatically generates accessors at compile time), I happily adopt it to keep our entities lean and idiomatic.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
I recommend standardizing all endpoints around the OpenAPI specification. 
Adopting a uniform OpenAPI model establishes a clear, standardized API contract that guarantees architectural consistency and eliminates manual integration errors. 
Furthermore, it serves as a central blueprint that drastically improves collaboration, allowing frontend teams, product managers, and other stakeholders to easily review and align on API changes before development begins.

OpenAPI-Driven
--------------
Pros:
 - Single source of truth:  Interactive docs like Swagger UI or Redoc are always up to date.
 - Automatic documentation: Client and server code generation ensures both sides match perfectly.
 - Parallel development: Frontend or mobile teams can mock the API using the YAML file while the backend is being built.

Cons:
 - Rigid workflow: Making a small change requires updating the YAML first, regenerating code, and then implementing the logic.
 - Tooling overhead: Code generators can sometimes produce messy, unidiomatic, or overly complex code.
 - Steep learning curve: Writing and maintaining massive OpenAPI YAML files by hand can be tedious.
 
Code-First
----------
Pros:
 - High velocity: Developers can build, iterate, and ship endpoints incredibly fast without moving between files.
 - Idiomatic code: The codebase remains clean, readable, and written exactly how your team prefers, without generator artifacts.
 - Maximum flexibility: You have total control over how requests are handled, validated, and structured.

Cons:
 - Documentation drift: API documentation often becomes outdated because developers forget to update manual docs or code annotations.
 - Client-server mismatch: It is easy to accidentally change a field type or route name in the code, breaking frontend or external clients.
 - Maximum flexibility: You have total control over how requests are handled, validated, and structured.

Summary
-------
With usage of AI the cons are more than manageable and I would favour Open-API usage.

```
----

3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Targeted and Pragmatic Coverage
-------------------------------
While achieving 100% test coverage is ideal, the immediate focus should be on validating the primary happy paths and critical edge cases. 
Code coverage metrics reveal untested gaps, but they do not automatically guarantee that tests are meaningful or effective.
 
Integration-First Focus
-----------------------
Prioritization will be placed on integration tests to ensure different system components interact flawlessly. 
Because simple coverage metrics often miss cross-component failures, integration testing will catch the issues that isolated unit tests overlook.Robust, 
 
Maintainable Test Code
----------------------
Code should be written robustly (similar to the Locations implementation) so tests are written once and rarely require modification. 
Test suites should only change when core business logic changes, avoiding constant, brittle updates.
 
Continuous Monitoring
---------------------
Code coverage tools will be utilized to track testing metrics over time and quickly pinpoint areas of the codebase that require additional test cases.
 
Automated CI/CD Guardrails
--------------------------
Strict guardrails will be integrated directly into the CI/CD pipeline. 
These checks will prevent code from being merged if it decreases overall test coverage or introduces new bugs.
```
----

NOTES:

```txt
- I have found on the home page "an Product" should be "a Product" - typo
- I have implemented the bounus task as a separate feature based on my best understanding
- Would like to say thank you for the opportunity to work on this assignment. It was a great experience and I learned a lot about Quarkus, Panache. Can't wait to join!
```
----


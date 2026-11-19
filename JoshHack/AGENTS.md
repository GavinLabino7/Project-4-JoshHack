# JoshHack Agent Guidance

## Build and Test Environment

Build with the Maven Wrapper and Java 17:

- Unit tests: `./mvnw test`
- Full build: `./mvnw verify`
- Windows wrapper: `mvnw.cmd`

## Completion Quality Checks

Before work is complete, run JUnit/Surefire tests and SpotBugs through Maven verification.

For mutation testing, run PIT for the `game.*` target classes:

- `./mvnw pitest:mutationCoverage`
- Windows wrapper: `mvnw.cmd pitest:mutationCoverage`

Review or address relevant generated quality findings.

## Behavior and Test Integrity

- Preserve existing behavior unless a change is explicitly requested.
- Do not weaken, remove, skip, or disable tests merely to make the build pass.

## Review and Delivery

- Clearly identify production-code changes in the final summary so humans can review them separately from test or documentation changes.
- Do not push or merge changes automatically; wait for explicit user approval.

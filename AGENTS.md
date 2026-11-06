# AGENTS.md

This file provides guidance to AI coding agents when working with code in this repository.

## Project Overview

Sedatools is a set of Java tools to build and manipulate SEDA (NF Z44-022) SIPs for the VITAM archival system.

- **Language/Build**: Java 21, Maven multi-module (`pom.xml` at the root)
- **GUIs**: Swing (Resip, MailExtract)
- **Tests**: JUnit 5 + AssertJ

## Modules

- `sedalib/` - SEDA library (SIP construction and manipulation)
- `sedalib-samples/` - Usage examples of `sedalib`
- `javalibpst/` - Embedded fork of java-libpst (Outlook PST reading)
- `mailextractlib/` - Mail container extraction library (IMAP, Thunderbird, PST, MSG, mbox, eml)
- `mailextract/` - Mail extraction application (CLI + GUI), based on `mailextractlib`
- `resip/` - SIP creation and manipulation application (CLI + GUI)
- `testsipgenerator/` - Command line tool generating test SIPs
- `packaging/` - Distribution packaging

## Build and Test Commands

```bash
# Full build with tests
mvn clean install

# Build without running tests
mvn clean install -DskipTests

# Build only one module and the modules it depends on
mvn install -pl resip -am

# Run the tests of a single module / a single test class
mvn test -pl sedalib
mvn test -pl sedalib -Dtest=MyClassTest
```

- Some tests depend on the time zone: CI runs them with `-Duser.timezone=Europe/Paris`. Do the same if date-related tests fail locally.
- CI (`.github/workflows/`): `build.yml` runs `mvn verify`, `lint.yml` runs `mvn license:check spotless:check`.

## Code Style

- **Formatting**: Spotless with the Prettier Java plugin (`printWidth` 120, `tabWidth` 4), configured in the root `pom.xml`.
  - Do NOT hand-format Java code or hand-prune imports: run `mvn spotless:apply` (on the touched modules, e.g. `-pl resip`) before committing.
  - Check with `mvn spotless:check`.
- **Imports**: ordered by Spotless (other imports, then `javax|java`, then static imports). Unused imports are removed automatically. No wildcard imports.
- **License headers**: every Java file under `src/main/java` and `src/test/java` must start with the header from `LICENSE.txt`.
  - Add it with `mvn license:format` and check it with `mvn license:check`.
- **Language**: code, identifiers, comments and Javadoc in English.
- **Logging**: use SLF4J (`LoggerFactory.getLogger(...)`) in new code. Never use `System.out` / `printStackTrace` for diagnostics.
- **Tests**:
  - JUnit 5 (`org.junit.jupiter`) and AssertJ (`assertThat`) for assertions.
  - New test methods are named `shouldDoSomething` in camelCase, with a given / when / then structure.
  - Test resources go in the module's `src/test/resources`.
  - Tests must not depend on the local environment (time zone, locale, absolute paths, network).
- **Dependencies**: versions are declared as properties in the root `pom.xml` (`<properties>` and `<dependencyManagement>`). Do not hardcode versions in module poms.
- Keep changes focused: do not reformat or refactor unrelated code in the same commit.

## Commit Message Convention

- Messages must be in **English**, short, in the imperative mood and lowercase after the prefix.
- Every commit must reference a **Tuleap ticket** with its type:
  - User story: `Story #XXXXX: <description>`
    (e.g. `Story #16471: update droid to 6.9.14`)
  - Bug fix: `Bug #XXXXX: <description>`
    (e.g. `Bug #16844: move the metadata filter reset to the compliance check thread`)
  - Several bugs: `Bugs #XXXXX #YYYYY: <description>`
- Optional scope after the ticket when useful: `Story #15307 ci: add license and linter checks`.
- One logical change per commit. Formatting-only changes (`spotless:apply`, `license:format`) on untouched code go in a separate commit.
- Version bumps use: `Set up version from X.Y.Z to X.Y.Z-SNAPSHOT`.

## Branches and Pull Requests

- Branch names: `story_XXXXX_short_description` or `bug_XXXXX_short_description`.
- PRs target `master` and must pass the `Build & Tests` and `Lint` workflows.
- The PR title follows the same format as commit messages.

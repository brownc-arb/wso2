# alrayan-utils

## Requirements

* Java 8
* Direct internet access / Apache Maven proxy configured

### Changing the required Java version

Java 8 is required by default. To move the project to Java 11, change the parent POM artifactId and
the following two properties in your `pom.xml`:

```xml
<parent>
  <groupId>com.github.gantsign.parent</groupId>
  <artifactId>java-parent</artifactId>
  ...
</parent>
...
<properties>
  ...
  <java.require.version>[11,12)</java.require.version>
  <java.target.version>11</java.target.version>
  ...
</properties>
```

## Building

This project is built using [Maven Wrapper](https://github.com/takari/maven-wrapper).

### Building Java only (no Docker image)

#### Linux/macOS

```bash
./mvnw install
```

#### Windows

```bash
mvnw.cmd install
```

## Security scanning

This project includes the [SpotBugs Maven Plugin](https://spotbugs.github.io/spotbugs-maven-plugin)
with the [Find Security Bugs](https://find-sec-bugs.github.io) plugin for performing static analysis
on your code. The static analysis can be quite time consuming so it's not run by default.

To run the security scan run the following from the project root:

#### Linux/macOS

```bash
./mvnw clean install
./mvnw spotbugs:spotbugs -P find-sec-bugs
```

#### Windows

```bash
mvnw.cmd clean install
mvnw.cmd spotbugs:spotbugs -P find-sec-bugs
```

To view the results run the following from each Maven module directory:

#### Linux/macOS

```bash
./mvnw spotbugs:gui -P find-sec-bugs
```

#### Windows

```bash
mvnw.cmd spotbugs:gui -P find-sec-bugs
```

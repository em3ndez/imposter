# Imposter: A scriptable, multipurpose mock server [![CI](https://github.com/outofcoffee/imposter/actions/workflows/ci.yaml/badge.svg)](https://github.com/outofcoffee/imposter/actions/workflows/ci.yaml)

Reliable, scriptable and extensible mock server for REST APIs, OpenAPI (and Swagger) specifications, Salesforce and HBase APIs.

Run standalone mock servers, or embed mocks within your tests (supports JVM and Node.js). Dynamic responses can be scripted using JavaScript, Groovy or Java.

## Features

* run standalone mocks in place of real systems
* turn an OpenAPI/Swagger file into a mock API for dev or QA (use it before the real API is built)
* decouple your integration tests from the cloud/back-end systems and take control of your dependencies
* validate your API requests against an OpenAPI specification
* capture data and retrieve later, or use in templates to for conditional responses

Send dynamic responses:

- Provide mock responses using static files or customise behaviour based on characteristics of the request.
- Power users can control mock responses with JavaScript or Java/Groovy script engines.
- Advanced users can write their own plugins in a JVM language of their choice.

*****
## Getting started

The quickest way to get up and running is to use the free cloud-hosted service at **[mocks.cloud](https://www.mocks.cloud)**

## User documentation

**[Read the user documentation here](https://docs.imposter.sh/)**

## Tutorials

* [Mocking APIs with Swagger and Imposter](https://medium.com/@outofcoffee/mocking-apis-with-swagger-and-imposter-3694bd1733c0)
* [Mocking REST APIs with Imposter](https://medium.com/@outofcoffee/mocking-apis-with-imposter-53bd908632e5)

*****

# Plugins

Imposter provides specialised mocks for the following scenarios:

* **[OpenAPI](docs/openapi_plugin.md)** - Support for [OpenAPI](https://github.com/OAI/OpenAPI-Specification) (and Swagger) API specifications.
* **[REST](docs/rest_plugin.md)** - Mocks RESTful or plain HTTP APIs.
* **[HBase](docs/hbase_plugin.md)** - Basic HBase mock implementation.
* **[SFDC (Salesforce) plugin](docs/sfdc_plugin.md)** - Basic Salesforce mock implementation.

These use a plugin system, so you can also create your own plugins, using any JVM language.

# Example

```shell
$ imposter up

Starting server on port 8080...
Parsing configuration file: someapi-config.yaml
...
Mock server is up and running
```

Your mock server is now running! Here Imposter provides HTTP responses to simulate an API that accepts users and returns a dynamic response containing the user ID from the request.

```shell
$ curl -v -X PUT http://localhost:8080/users/alice

HTTP/1.1 201 Created
Content-Type: application/json

{ "userName": "alice" }
```

This is a trivial example, which you can extend with conditional logic, request validation, data capture and much more... 

# How to run Imposter

There are many ways to run Imposter.

### Standalone mock server

1. Using the command line client - see [Imposter CLI](./docs/run_imposter_cli.md)
2. As a Docker container - see [Imposter Docker container](./docs/run_imposter_docker.md)
3. As a JAR file on the JVM - see [Imposter JAR file](./docs/run_imposter_jar.md)

### Embedded in tests

4. Embedded within your **Java/Kotlin/Scala/JVM** unit tests - see [JVM bindings](./docs/embed_jvm.md)
5. Embedded within your **JavaScript/Node.js** unit tests - see [JavaScript bindings](https://github.com/gatehill/imposter-js)

# Recent changes and Roadmap
  
For recent changes see the [Changelog](CHANGELOG.md), or view the [Roadmap](docs/roadmap.md).

# Contributing

* Pull requests are welcome.
* PRs should target the `develop` branch.

# Author

Pete Cornish (outofcoffee@gmail.com)

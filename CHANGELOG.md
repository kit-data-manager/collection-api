# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Smart Collections for rule-based organization of members depending on member attributs, e.g., dataType
- Added support for PIDs in the form *prefix/suffix* to be used as collection and member ids

### Changed
- Update to org.springframework.boot 2.7.3
- Update to io.spring.dependency-management 1.0.13.RELEASE
- Update to io.freefair.lombok6.5.1
- Update to io.freefair.maven-publish-java 6.5.1
- Update to com.github.kt3k.coveralls 2.12.0"
- Update to org.owasp.dependencycheck 7.2.1
- Update to org.asciidoctor.jvm.convert 3.3.2
- Update to researchgate.release 3.0.2
- Update to springdoc-openapi-ui 1.6.9
- Update to springdoc-openapi-data-rest 1.6.9
- Update to springdoc-openapi-webmvc-core 1.6.9
- Update to keycloak 19.0.0
- Update to spring-messaging 5.3.23
- Update to spring-security-web 5.7.3
- Update to spring-security-config 5.7.3
- Update to spring-cloud-starter-config 3.1.4
- Update to spring-cloud-starter-netflix-eureka-client 3.1.4
- Update to repo-core 1.0.4
- Update to nimbus-jose-jwt 9.25.1
- Update to jjwt-api 0.11.5
- Update to jjwt-impl 0.11.5
- Update to jjwt-jackson 0.11.5
- Update to commons-collections4 4.4
- Update to httpclient 4.5.13
- Update to postgresql 42.4.1
- Update to h2 2.1.214

### Security
- Changed handling of collection- and member-id in the way, the both MUST be provided URLEncoded 

### Removed


## [1.2.0] - 2021-09-23
### Added
- Graphical collection editor

### Changed
- Internal changes now require JDK 11

### Fixed
- Issue with identifiers containing slashes

## [1.1.0] - 2020-12-15
### Changed
- Change of messaging property names including documentation
- Update to service-base 0.2.0
- Update to generic-message-consumer 0.2.0

### Fixed
- Some properties, e.g. collection members and internal identifiers, are now properly omitted while being returned by the controller
- Truncating service-assigned times to milliseconds for compatibility reasons

## [1.0] -  2020-09-29
### Added
- First public version

### Changed
- none

### Removed
- none

### Deprecated
- none

### Fixed
- none

### Security
- none

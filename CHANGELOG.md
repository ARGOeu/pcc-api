# Changelog

------------------------------------------------------------------------

All notable changes to this project will be documented in this file.

According to [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), the `Unreleased` section serves the following purposes:

- People can see what changes they might expect in upcoming releases.
- At release time, you can move the `Unreleased` section changes into a new release version section.

## Types of changes

------------------------------------------------------------------------

- `Added` for new features.
- `Changed` for changes in existing functionality.
- `Removed` for now removed features.
- `Fixed` for any bug fixes.
- `Security` in case of vulnerabilities.
- `Deprecated` for soon-to-be removed features.

## Unreleased

### Added

- [#3](https://github.com/ARGOeu/pcc-api/pull/3) PCC-1 / PCC-4 Providers support
- [#4](https://github.com/ARGOeu/pcc-api/pull/4) PCC-6 EOSC-Portal Schedule Job
- [#5](https://github.com/ARGOeu/pcc-api/pull/5) PCC-7 Support list services API call
- [#6](https://github.com/ARGOeu/pcc-api/pull/6) PCC-9 Fetch a particular Domain
- [#7](https://github.com/ARGOeu/pcc-api/pull/7) PCC-5 Support list domains action to API
- [#9](https://github.com/ARGOeu/pcc-api/pull/9) PCC-12 Introduce OpenAPI specification
- [#10](https://github.com/ARGOeu/pcc-api/pull/10) PCC-11 Support list one service
- [#11](https://github.com/ARGOeu/pcc-api/pull/11) PCC-13 Describe the structure of Providers /get/{id} request using OpenAPI 3.0
- [#12](https://github.com/ARGOeu/pcc-api/pull/12) PCC-14 Describe /providers endpoint using OpenAPI 3.0
- [#13](https://github.com/ARGOeu/pcc-api/pull/13) PCC-15 Describe Domain Endpoint through the OpenAPI v3
- [#15](https://github.com/ARGOeu/pcc-api/pull/15) PCC-17 Database in the project - Flyway
- [#16](https://github.com/ARGOeu/pcc-api/pull/16) PCC-18 Describe Service Endpoint through the OpenAPI v3
- [#17](https://github.com/ARGOeu/pcc-api/pull/17) PCC-19 Support create prefix API call
- [#19](https://github.com/ARGOeu/pcc-api/pull/19) PCC-21 Delete a particular prefix
- [#20](https://github.com/ARGOeu/pcc-api/pull/20) PCC-22 Prefix List Endpoint
- [#21](https://github.com/ARGOeu/pcc-api/pull/21) PCC-23 Support list specific Prefix action
- [#22](https://github.com/ARGOeu/pcc-api/pull/22) PCC-24 Enable global exception handling
- [#23](https://github.com/ARGOeu/pcc-api/pull/23) PCC-25 Support PUT on /prefixes to update the entity
- [#24](https://github.com/ARGOeu/pcc-api/pull/24) PCC-20 Support HTTP PATCH on /prefixes
- [#25](https://github.com/ARGOeu/pcc-api/pull/25) PCC-26 Enable CORS in devel mode
- [#26](https://github.com/ARGOeu/pcc-api/pull/26) PCC-8 Formatter & Validator
- [#27](https://github.com/ARGOeu/pcc-api/pull/27) PCC-36 Independent local docker DB set up
- [#29](https://github.com/ARGOeu/pcc-api/pull/29) PCC-56 API Call - Reverse Look up
- [#30](https://github.com/ARGOeu/pcc-api/pull/30) PCC-57 Extend prefix model with lookup service type information
- [#31](https://github.com/ARGOeu/pcc-api/pull/31) PCC-58 Add support for pagination in reverse look up API call
- [#33](https://github.com/ARGOeu/pcc-api/pull/33) PCC-68 Support CHECKSUM filter
- [#34](https://github.com/ARGOeu/pcc-api/pull/34) PCC-69 Support 10320/LOC filter
- [#39](https://github.com/ARGOeu/pcc-api/pull/39) PCC-92 Introduce PIDProbe daemon
- [#42](https://github.com/ARGOeu/pcc-api/pull/42) PCC-105 Add contact to prefix
- [#45](https://github.com/ARGOeu/pcc-api/pull/45) PCC-104 Add contract-enddate to prefix
- [#46](https://github.com/ARGOeu/pcc-api/pull/46) PCC-107 Add resolvable in the prefix
- [#48](https://github.com/ARGOeu/pcc-api/pull/48) PCC-103 Add a contract-type to prefix
- [#56](https://github.com/ARGOeu/pcc-api/pull/56) PCC-122 Add /{id}/statistics endpoint
- [#59](https://github.com/ARGOeu/pcc-api/pull/59) PCC-113 [API] Set statistics to a Prefix manually
- [#71](https://github.com/ARGOeu/pcc-api/pull/71) PCC-158 Add Pagination to GET Prefixes
- [#79](https://github.com/ARGOeu/pcc-api/pull/79) PCC-161 Run resolvable script on a new PID in a prefix
- [#86](https://github.com/ARGOeu/pcc-api/pull/86) PCC-170 Add batching query when resolving handles
- [#89](https://github.com/ARGOeu/pcc-api/pull/89) PCC-144 Implement Keycloak authentication for API security
- [#90](https://github.com/ARGOeu/pcc-api/pull/90) PCC-174 [API] Create a Handle
- [#92](https://github.com/ARGOeu/pcc-api/pull/92) PCC-176 [API] Retrieve a Handle


### Changed

- [#14](https://github.com/ARGOeu/pcc-api/pull/14) PCC-16 Update Swagger properties
- [#36](https://github.com/ARGOeu/pcc-api/pull/36) PCC-88 Update README.md
- [#49](https://github.com/ARGOeu/pcc-api/pull/49) PCC-99 Refactor lookup_service_type
- [#55](https://github.com/ARGOeu/pcc-api/pull/55) PCC-120 Make HRLSConnector singleton
- [#60](https://github.com/ARGOeu/pcc-api/pull/60) PCC-141 Remove required fields from API
- [#62](https://github.com/ARGOeu/pcc-api/pull/62) PCC-142 Change prefix required fields
- [#64](https://github.com/ARGOeu/pcc-api/pull/64) PCC-142 Change prefix required fields
- [#69](https://github.com/ARGOeu/pcc-api/pull/69) PCC-159 Change enum fields of prefixes to be stored in a table
- [#80](https://github.com/ARGOeu/pcc-api/pull/80) PCC-161 Run resolvable script on a new PID in a prefix: replace daemons jar execution with a thread worker
- [#81](https://github.com/ARGOeu/pcc-api/pull/81) PCC-169 Upgrade PCC API
- [#88](https://github.com/ARGOeu/pcc-api/pull/88) PCC-171 Update project documentation after Quarkus 3.27 upgrade

### Fixed

- [#28](https://github.com/ARGOeu/pcc-api/pull/28) PCC-44 Fix ignored prefix status parameter on HTTP PATCH
- [#38](https://github.com/ARGOeu/pcc-api/pull/38) PCC-97 Appropriate error on Prefix Update
- [#57](https://github.com/ARGOeu/pcc-api/pull/57) PCC-124 Fix issue regarding contract_type and lookupservice_type
- [#74](https://github.com/ARGOeu/pcc-api/pull/74) PCC-165 Handle missing domains cause of failure of EOSC marketplace URL


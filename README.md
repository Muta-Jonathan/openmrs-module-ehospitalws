openmrs-module-ehospitalws
==========================

Description
-----------

eHospital Web Services is an OpenMRS module that exposes additional REST web
services required by the eHospital project that are not available natively
through the OpenMRS FHIR or Web Services modules.

It provides:

- **Patient and OPD reporting endpoints** – client lists and outpatient (OPD)
  visit / revisit summaries over a date range.
- **Patient observation export** – aggregates a patient's observations into a
  single payload (used to feed an LLM).
- **Patient flags** – computed clinical flags for a patient.
- **LLM message logging** – persists and retrieves messages generated for
  patients.
- **SMS appointment reminders** – schedules and sends appointment reminder
  messages, including two daily scheduled tasks.
- **Procedure management** – a `Procedure` / `ProcedureOrder` model with REST
  resources, migrated from the openmrs-module-procedures module.

Requirements
------------

- **OpenMRS Platform** 2.6.0 or higher (`require_version` in `config.xml`).
- **Java 8** and **Maven 3.x**.
- Required OpenMRS module: **webservices.rest**.
- Aware of (optional): **legacyui**.

Building from Source
--------------------

Use Maven to compile and package the module:

    mvn clean package

The `.omod` file will be produced in `omod/target/` (e.g.
`omod/target/ehospitalws-<version>.omod`).

Installation
------------

1. Build the module to produce the `.omod` file (see above).
2. Use the OpenMRS **Administration > Manage Modules** screen to upload and
   install the `.omod` file.

If web uploads are disabled (configurable via a runtime property), drop the
`.omod` into the `~/.OpenMRS/modules` folder (where `~/.OpenMRS` is the
application data directory the running OpenMRS instance is using) and restart
OpenMRS/Tomcat. The module will be loaded and started on restart.

Configuration
-------------

The SMS and appointment-reminder features read the following keys from the
OpenMRS runtime properties file (`openmrs-runtime.properties`). If a key is
missing the module falls back to a placeholder default, so these **must** be
set for SMS delivery to work:

| Property            | Purpose                                   |
| ------------------- | ----------------------------------------- |
| `sms.api.url`       | SMS gateway endpoint URL                  |
| `sms.api.key`       | SMS gateway API key                       |
| `sms.partner.id`    | SMS gateway partner ID                    |
| `sms.shortcode`     | SMS sender short code                     |
| `admin.username`    | Username used for internal API calls      |
| `admin.password`    | Password used for internal API calls      |

The standard OpenMRS `connection.*` properties (driver, URL, username,
password) are also read for the module's direct database access.

REST API
--------

All endpoints are served under the OpenMRS web-services path:

    /openmrs/ws/rest/v1/ehospital

| Method | Path                          | Description                                   |
| ------ | ----------------------------- | --------------------------------------------- |
| GET    | `/allClients`                 | Patient list for a date range                 |
| GET    | `/outPatientClients`          | Outpatient client list for a date range       |
| GET    | `/opdVisits`                  | OPD visits for a date range                   |
| GET    | `/opdRevisits`                | OPD revisits for a date range                 |
| GET    | `/{type}`                     | OPD summary by category type                  |
| GET    | `/patient/obs`                | Aggregated observations for a patient         |
| GET    | `/patient/encounter`          | Patient encounter data (LLM)                  |
| GET    | `/patient/flags`              | Computed patient flags                        |
| GET    | `/forms`                      | Available forms                               |
| POST   | `/message/save`               | Save an LLM-generated message for a patient   |
| POST   | `/message/send`               | Send the latest message to a patient          |
| GET    | `/messages/patient`           | Latest message(s) for a patient               |
| GET    | `/messages/all`               | All logged messages                           |
| GET    | `/scheduled-messages`         | Scheduled appointment messages                |
| POST   | `/smsAppointmentReminder`     | Schedule appointment reminder messages        |
| POST   | `/sendAppointmentReminder`    | Send scheduled appointment reminders          |

Date-range endpoints accept `startDate` and `endDate` request parameters.

Scheduled Tasks
---------------

Two Spring `@Scheduled` cron jobs run daily in the `Africa/Nairobi` timezone:

- **17:10 EAT** – schedule appointment reminder messages for the next day.
- **17:30 EAT** – send the messages scheduled for the current day.

Database
--------

The module adds custom tables for `LLMMessages` and `ScheduledMessage`, and a
Hibernate mapping for `ProcedureOrder` (`ProcedureOrder.hbm.xml`). These are
created/updated automatically when the module starts.

Releasing
---------

Releases are automated via GitHub Actions:

- **Snapshot deploy** – snapshot artifacts are deployed when changes are merged.
- **Maven release** – publishing a GitHub Release triggers
  `.github/workflows/release.yml`, which runs `mvn release:prepare
  release:perform` and deploys the release artifacts to the configured Maven
  repository (Repsy).

The release workflow checks out the `main` branch and pushes the version-bump
commits and release tag back to it, so cut releases from `main`.

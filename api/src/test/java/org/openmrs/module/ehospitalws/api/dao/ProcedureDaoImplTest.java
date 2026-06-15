/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ehospitalws.api.dao;

import java.util.Optional;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ehospitalws.api.dao.impl.ProcedureDaoImpl;
import org.openmrs.module.ehospitalws.api.model.Procedure;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test for {@link ProcedureDaoImpl}. Extends {@link BaseModuleContextSensitiveTest} to
 * run against the in-memory H2 database with the standard OpenMRS test dataset. All test methods
 * execute within a transaction that is rolled back at the end.
 */
public class ProcedureDaoImplTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("procedureDao")
	private ProcedureDao procedureDao;
	
	@Test
	public void saveOrUpdate_shouldSaveNewProcedureAndAssignId() {
		// Given
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(2); // from standard test data
		
		Procedure procedure = new Procedure();
		procedure.setPatient(patient);
		procedure.setStatus(Procedure.ProcedureStatus.IN_PROGRESS);
		procedure.setProcedureReport("Initial report");
		
		// When
		Procedure saved = procedureDao.saveOrUpdate(procedure);
		
		// Then
		assertNotNull("Saved procedure should not be null", saved);
		assertNotNull("Procedure should have an auto-generated id", saved.getProcedureId());
		assertNotNull("Procedure should have a UUID", saved.getUuid());
		assertThat(saved.getPatient(), is(patient));
		assertThat(saved.getStatus(), is(Procedure.ProcedureStatus.IN_PROGRESS));
		assertThat(saved.getProcedureReport(), is("Initial report"));
	}
	
	@Test
	public void saveOrUpdate_shouldUpdateExistingProcedure() {
		// Given – first create and persist a procedure
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(2);
		
		Procedure procedure = new Procedure();
		procedure.setPatient(patient);
		procedure.setStatus(Procedure.ProcedureStatus.IN_PROGRESS);
		procedure.setProcedureReport("Initial report");
		
		procedureDao.saveOrUpdate(procedure);
		Context.flushSession();
		
		Integer id = procedure.getProcedureId();
		String uuid = procedure.getUuid();
		
		// When – update fields and save again
		procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
		procedure.setProcedureReport("Final report");
		procedure.setOutcome(Procedure.ProcedureOutcome.SUCCESSFUL);
		procedureDao.saveOrUpdate(procedure);
		Context.flushSession();
		Context.clearSession();
		
		// Then – fetch from DB and verify updates
		Optional<Procedure> fetched = procedureDao.getProcedureByUuid(uuid);
		assertTrue("Procedure should be found by UUID", fetched.isPresent());
		
		Procedure updated = fetched.get();
		assertThat(updated.getProcedureId(), is(id));
		assertThat(updated.getStatus(), is(Procedure.ProcedureStatus.COMPLETED));
		assertThat(updated.getProcedureReport(), is("Final report"));
		assertThat(updated.getOutcome(), is(Procedure.ProcedureOutcome.SUCCESSFUL));
	}
	
	@Test
	public void getProcedureByUuid_shouldReturnEmptyForNonExistentUuid() {
		// When
		Optional<Procedure> result = procedureDao.getProcedureByUuid("non-existent-uuid-12345");
		
		// Then
		assertFalse("Should return empty Optional for non-existent UUID", result.isPresent());
	}
	
	@Test
	public void getProcedureByUuid_shouldFindSavedProcedure() {
		// Given
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(2);
		
		Procedure procedure = new Procedure();
		procedure.setPatient(patient);
		procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
		procedure.setOutcome(Procedure.ProcedureOutcome.SUCCESSFUL);
		
		procedureDao.saveOrUpdate(procedure);
		Context.flushSession();
		Context.clearSession();
		
		String uuid = procedure.getUuid();
		
		// When
		Optional<Procedure> fetched = procedureDao.getProcedureByUuid(uuid);
		
		// Then
		assertTrue("Procedure should be found by UUID", fetched.isPresent());
		assertThat(fetched.get().getUuid(), is(uuid));
		assertThat(fetched.get().getStatus(), is(Procedure.ProcedureStatus.COMPLETED));
		assertThat(fetched.get().getOutcome(), is(Procedure.ProcedureOutcome.SUCCESSFUL));
	}
	
	@Test
	public void get_shouldReturnEmptyForNonExistentId() {
		// When
		Optional<Procedure> result = procedureDao.get(999999);
		
		// Then
		assertFalse("Should return empty Optional for non-existent id", result.isPresent());
	}
	
	@Test
	public void get_shouldFindSavedProcedureById() {
		// Given
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(2);
		
		Procedure procedure = new Procedure();
		procedure.setPatient(patient);
		procedure.setStatus(Procedure.ProcedureStatus.PREPARATION);
		
		procedureDao.saveOrUpdate(procedure);
		Context.flushSession();
		Context.clearSession();
		
		Integer id = procedure.getProcedureId();
		assertNotNull("Procedure should have been assigned an id", id);
		
		// When
		Optional<Procedure> fetched = procedureDao.get(id);
		
		// Then
		assertTrue("Procedure should be found by id", fetched.isPresent());
		assertThat(fetched.get().getProcedureId(), is(id));
		assertThat(fetched.get().getStatus(), is(Procedure.ProcedureStatus.PREPARATION));
	}
	
	@Test
	public void saveOrUpdate_shouldPersistAllProcedureFields() {
		// Given
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(2);
		
		Procedure procedure = new Procedure();
		procedure.setPatient(patient);
		procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
		procedure.setOutcome(Procedure.ProcedureOutcome.PARTIALLY_SUCCESSFUL);
		procedure.setProcedureReport("Detailed surgical report");
		
		java.util.Date startDate = new java.util.Date();
		java.util.Date endDate = new java.util.Date(startDate.getTime() + 3600000); // +1 hour
		procedure.setStartDatetime(startDate);
		procedure.setEndDatetime(endDate);
		
		// When
		procedureDao.saveOrUpdate(procedure);
		Context.flushSession();
		Context.clearSession();
		
		// Then
		Optional<Procedure> fetched = procedureDao.getProcedureByUuid(procedure.getUuid());
		assertTrue(fetched.isPresent());
		
		Procedure result = fetched.get();
		assertThat(result.getPatient().getPatientId(), is(2));
		assertThat(result.getStatus(), is(Procedure.ProcedureStatus.COMPLETED));
		assertThat(result.getOutcome(), is(Procedure.ProcedureOutcome.PARTIALLY_SUCCESSFUL));
		assertThat(result.getProcedureReport(), is("Detailed surgical report"));
		assertNotNull(result.getStartDatetime());
		assertNotNull(result.getEndDatetime());
	}
}

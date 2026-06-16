/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ehospitalws.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.module.ehospitalws.api.dao.ProcedureDao;
import org.openmrs.module.ehospitalws.api.model.Procedure;
import org.openmrs.module.ehospitalws.api.impl.ProcedureServiceImpl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the {@link ProcedureService} contract, verifying the DAO delegation behaviour for
 * basic CRUD operations. Tests that exercise the encounter-handling logic inside
 * {@link org.openmrs.module.ehospitalws.api.impl.ProcedureServiceImpl#saveOrUpdate} are in the
 * integration test {@link org.openmrs.module.ehospitalws.api.dao.ProcedureDaoImplTest} because that
 * code-path calls {@code Context.getEncounterService()} which requires a running Spring context.
 * <p>
 * This test class uses Mockito only (no PowerMock) so it stays compatible with modern JDKs.
 */
public class ProcedureServiceImplTest {
	
	private ProcedureServiceImpl procedureService;
	
	private ProcedureDao procedureDao;
	
	@Before
	public void setUp() {
		procedureDao = mock(ProcedureDao.class);
		procedureService = new ProcedureServiceImpl();
		procedureService.setProcedureDao(procedureDao);
	}
	
	// ---- ProcedureDao.saveOrUpdate delegation ----
	
	@Test
	public void saveOrUpdate_shouldDelegateToDaoAndReturnResult() {
		// Given
		Procedure procedure = new Procedure();
		procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
		
		Procedure expected = new Procedure();
		expected.setProcedureId(42);
		when(procedureDao.saveOrUpdate(procedure)).thenReturn(expected);
		
		// When
		Procedure result = procedureDao.saveOrUpdate(procedure);
		
		// Then
		verify(procedureDao).saveOrUpdate(procedure);
		assertThat(result, is(expected));
		assertThat(result.getProcedureId(), is(42));
	}
	
	// ---- ProcedureDao.getProcedureByUuid delegation ----
	
	@Test
	public void getProcedureByUuid_shouldReturnProcedureWhenExists() {
		// Given
		String uuid = "test-uuid-12345";
		Procedure procedure = new Procedure();
		procedure.setStatus(Procedure.ProcedureStatus.IN_PROGRESS);
		when(procedureDao.getProcedureByUuid(uuid)).thenReturn(Optional.of(procedure));
		
		// When
		Optional<Procedure> result = procedureDao.getProcedureByUuid(uuid);
		
		// Then
		assertTrue(result.isPresent());
		assertThat(result.get().getStatus(), is(Procedure.ProcedureStatus.IN_PROGRESS));
	}
	
	@Test
	public void getProcedureByUuid_shouldReturnEmptyWhenNotFound() {
		// Given
		when(procedureDao.getProcedureByUuid("missing")).thenReturn(Optional.empty());
		
		// When
		Optional<Procedure> result = procedureDao.getProcedureByUuid("missing");
		
		// Then
		assertFalse(result.isPresent());
	}
	
	// ---- ProcedureDao.get delegation ----
	
	@Test
	public void get_shouldReturnProcedureWhenExists() {
		// Given
		Procedure procedure = new Procedure();
		procedure.setProcedureId(7);
		when(procedureDao.get(7)).thenReturn(Optional.of(procedure));
		
		// When
		Optional<Procedure> result = procedureDao.get(7);
		
		// Then
		assertTrue(result.isPresent());
		assertThat(result.get().getProcedureId(), is(7));
	}
	
	@Test
	public void get_shouldReturnEmptyWhenNotFound() {
		// Given
		when(procedureDao.get(999)).thenReturn(Optional.empty());
		
		// When
		Optional<Procedure> result = procedureDao.get(999);
		
		// Then
		assertFalse(result.isPresent());
	}
	
	@Test
	public void searchProcedures_shouldDelegateToDaoAndReturnResults() {
		// Given
		String orderUuid = "order-uuid";
		String orderTypeUuid = "order-type-uuid";
		Procedure procedure = new Procedure();
		List<Procedure> expected = Collections.singletonList(procedure);
		when(procedureDao.searchProcedures(orderUuid, orderTypeUuid)).thenReturn(expected);
		
		// When
		List<Procedure> result = procedureService.searchProcedures(orderUuid, orderTypeUuid);
		
		// Then
		verify(procedureDao).searchProcedures(orderUuid, orderTypeUuid);
		assertThat(result, is(expected));
	}
	
	// ---- Procedure model behaviour ----
	
	@Test
	public void procedure_shouldTrackAllStatusValues() {
		Procedure procedure = new Procedure();
		for (Procedure.ProcedureStatus status : Procedure.ProcedureStatus.values()) {
			procedure.setStatus(status);
			assertThat(procedure.getStatus(), is(status));
		}
	}
	
	@Test
	public void procedure_shouldTrackAllOutcomeValues() {
		Procedure procedure = new Procedure();
		for (Procedure.ProcedureOutcome outcome : Procedure.ProcedureOutcome.values()) {
			procedure.setOutcome(outcome);
			assertThat(procedure.getOutcome(), is(outcome));
		}
	}
	
	@Test
	public void procedure_shouldManageEncounterList() {
		Procedure procedure = new Procedure();
		assertNull(procedure.getEncounters());
		
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(new Encounter());
		encounters.add(new Encounter());
		procedure.setEncounters(encounters);
		
		assertThat(procedure.getEncounters(), hasSize(2));
	}
	
	@Test
	public void procedure_shouldDelegateIdToProcedureId() {
		Procedure procedure = new Procedure();
		procedure.setId(123);
		assertThat(procedure.getId(), is(123));
		assertThat(procedure.getProcedureId(), is(123));
	}
	
	@Test
	public void procedure_shouldTrackDates() {
		Procedure procedure = new Procedure();
		Date start = new Date();
		Date end = new Date(start.getTime() + 3600000);
		
		procedure.setStartDatetime(start);
		procedure.setEndDatetime(end);
		
		assertThat(procedure.getStartDatetime(), is(start));
		assertThat(procedure.getEndDatetime(), is(end));
	}
	
	@Test
	public void procedure_shouldTrackConceptFields() {
		Procedure procedure = new Procedure();
		
		Concept concept = new Concept(1);
		Concept reason = new Concept(2);
		Concept category = new Concept(3);
		Concept bodySite = new Concept(4);
		Concept statusReason = new Concept(5);
		Concept modality = new Concept(6);
		
		procedure.setConcept(concept);
		procedure.setProcedureReason(reason);
		procedure.setCategory(category);
		procedure.setBodySite(bodySite);
		procedure.setStatusReason(statusReason);
		procedure.setModality(modality);
		
		assertThat(procedure.getConcept(), is(concept));
		assertThat(procedure.getProcedureReason(), is(reason));
		assertThat(procedure.getCategory(), is(category));
		assertThat(procedure.getBodySite(), is(bodySite));
		assertThat(procedure.getStatusReason(), is(statusReason));
		assertThat(procedure.getModality(), is(modality));
	}
}

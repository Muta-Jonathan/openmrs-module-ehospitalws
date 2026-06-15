package org.openmrs.module.ehospitalws.api.dao;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

import org.openmrs.module.ehospitalws.api.model.Procedure;

public interface ProcedureDao {
	
	Optional<Procedure> get(@NotNull int id);
	
	Optional<Procedure> getProcedureByUuid(@NotNull String uuid);
	
	List<Procedure> searchProcedures(String orderUuid, String orderTypeUuid);
	
	Procedure saveOrUpdate(@NotNull Procedure procedure);
}

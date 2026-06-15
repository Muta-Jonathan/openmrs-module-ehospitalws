package org.openmrs.module.ehospitalws.api.dao.impl;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.ehospitalws.api.dao.ProcedureDao;
import org.openmrs.module.ehospitalws.api.model.Procedure;

public class ProcedureDaoImpl implements ProcedureDao {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public Optional<Procedure> get(int id) {
		Procedure procedure = (Procedure) getCurrentSession().get(Procedure.class, id);
		return Optional.ofNullable(procedure);
	}
	
	@Override
	public Optional<Procedure> getProcedureByUuid(String uuid) {
		Criteria criteria = getCurrentSession().createCriteria(Procedure.class);
		return Optional.ofNullable((Procedure) criteria.add(eq("uuid", uuid)).uniqueResult());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Procedure> searchProcedures(String orderUuid, String orderTypeUuid) {
		boolean hasOrderUuid = hasText(orderUuid);
		boolean hasOrderTypeUuid = hasText(orderTypeUuid);
		
		if (!hasOrderUuid && !hasOrderTypeUuid) {
			return Collections.emptyList();
		}
		
		Criteria criteria = getCurrentSession().createCriteria(Procedure.class);
		criteria.add(eq("voided", false));
		criteria.createAlias("procedureOrder", "procedureOrder");
		
		if (hasOrderUuid) {
			criteria.add(eq("procedureOrder.uuid", orderUuid));
		}
		
		if (hasOrderTypeUuid) {
			criteria.createAlias("procedureOrder.orderType", "orderType");
			criteria.add(eq("orderType.uuid", orderTypeUuid));
		}
		
		return criteria.list();
	}
	
	@Override
	public Procedure saveOrUpdate(Procedure procedure) {
		getCurrentSession().saveOrUpdate(procedure);
		return procedure;
	}
	
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}
}

package org.openmrs.module.ehospitalws.api.model;

import java.lang.reflect.Field;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

public class ProcedureOrderTest {
	
	@Test
	public void getNumberOfRepeats_shouldReturnParsedIntegerWhenValidString() throws Exception {
		ProcedureOrder order = new ProcedureOrder();
		setInternalState(order, "numberOfRepeats", "42");
		assertThat(order.getNumberOfRepeats(), is(42));
	}
	
	@Test
	public void getNumberOfRepeats_shouldReturnSuperWhenNullString() throws Exception {
		ProcedureOrder order = new ProcedureOrder();
		order.setNumberOfRepeats(10);
		// Simulate XStream leaving the local string field null but populating super
		setInternalState(order, "numberOfRepeats", null);
		assertThat(order.getNumberOfRepeats(), is(10));
	}
	
	@Test
	public void getNumberOfRepeats_shouldReturnSuperWhenEmptyString() throws Exception {
		ProcedureOrder order = new ProcedureOrder();
		order.setNumberOfRepeats(10);
		setInternalState(order, "numberOfRepeats", "");
		assertThat(order.getNumberOfRepeats(), is(10));
	}
	
	@Test
	public void getNumberOfRepeats_shouldReturnSuperWhenWhitespace() throws Exception {
		ProcedureOrder order = new ProcedureOrder();
		order.setNumberOfRepeats(10);
		setInternalState(order, "numberOfRepeats", "   ");
		assertThat(order.getNumberOfRepeats(), is(10));
	}
	
	@Test
	public void getNumberOfRepeats_shouldReturnSuperWhenNonNumeric() throws Exception {
		ProcedureOrder order = new ProcedureOrder();
		order.setNumberOfRepeats(10);
		setInternalState(order, "numberOfRepeats", "abc");
		assertThat(order.getNumberOfRepeats(), is(10));
	}
	
	@Test
	public void setNumberOfRepeats_shouldSetStringAndSuper() {
		ProcedureOrder order = new ProcedureOrder();
		order.setNumberOfRepeats(5);
		assertThat(order.getNumberOfRepeats(), is(5));
	}
	
	@Test
	public void setNumberOfRepeats_shouldHandleNull() {
		ProcedureOrder order = new ProcedureOrder();
		order.setNumberOfRepeats(null);
		assertNull(order.getNumberOfRepeats());
	}
	
	private void setInternalState(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}

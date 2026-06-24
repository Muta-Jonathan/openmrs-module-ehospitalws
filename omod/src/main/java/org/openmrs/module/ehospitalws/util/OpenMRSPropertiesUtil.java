package org.openmrs.module.ehospitalws.util;

import org.openmrs.api.context.Context;

import java.util.Properties;

public class OpenMRSPropertiesUtil {

	public static Properties getProperties() {
		return Context.getRuntimeProperties();
	}

	/**
	 * Fetches an optional runtime property, returning the supplied default (trimmed) when the property
	 * is absent or blank. Use this only for values that have a sensible fallback.
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = getProperties().getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return value.trim();
	}

	/**
	 * Fetches a required runtime property and fails loudly when it is missing or blank, rather than
	 * silently falling back to a placeholder. Use this for credentials and gateway settings that the
	 * module cannot function correctly without.
	 *
	 * @throws IllegalStateException if the property is not set or is blank
	 */
	public static String getRequiredProperty(String key) {
		String value = getProperties().getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Missing required OpenMRS runtime property '" + key
			        + "'. Set it in openmrs-runtime.properties.");
		}
		return value.trim();
	}
}

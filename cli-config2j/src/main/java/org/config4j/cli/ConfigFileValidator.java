package org.config4j.cli;

import java.io.File;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ConfigFileValidator implements IParameterValidator {

	public void validate(final String name, final String value) throws ParameterException {
		File file = new File(value);

		if (!file.exists() && !file.canRead()) {
			throw new ParameterException("File pointed in parameter \"" + name + "\" with value \"" + value
			        + "\" can not be read. Is it a file? Does it exists?");
		}
	}

}

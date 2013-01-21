package org.config4j.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class PackageNameValidator implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		if (!Config2JUtil.isValidPackageName(value)) {
			throw new ParameterException("A value of '" + name + "' is not a valid Java package name.");
		}
	}

}

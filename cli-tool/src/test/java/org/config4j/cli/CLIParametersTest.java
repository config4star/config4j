package org.config4j.cli;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

@Slf4j
@RunWith(JUnitParamsRunner.class)
public class CLIParametersTest {
	private static final String VALID_FILE = "src/test/resources/org/config4j/DefaultSecurity.cfg";

	private static final String PROGRAM_NAME = "config4j-cli-tool";

	private static final String CLASSNAME = "DummyClass";

	private CLIParameters params;

	private JCommander comm;

	private final List<String> defaults = new ArrayList<String>();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void getInstance() {
		params = new CLIParameters(PROGRAM_NAME);
		comm = new JCommander(params);

		defaults.add("-cfg");
		defaults.add(VALID_FILE);
		defaults.add("-class");
		defaults.add(CLASSNAME);
	}

	@Test
	public void testDefaults() throws IOException {
		new JCommander(params).parseWithoutValidation("");

		assertEquals("FALSE should be a default value for help screen", false, params.isHelp());
		assertEquals("FALSE should be a default value for making class public", false, params.isClassPublic());
		assertEquals("FALSE should be a default value for creating a singleton class", false, params.isWantSingleton());
		assertEquals("TRUE should be a default value for generating schema", true, params.isWantSchema());
	}

	@Test
	public void testMissingConfigFile() throws ParameterException {
		// Desired exceptin and description
		thrown.expect(ParameterException.class);
		thrown.expectMessage("File pointed in parameter");

		// Actual test
		final String[] argv = { "-cfg", "conf/configuration-that-will-not-exist.xml" };
		comm.parse(argv);
	}

	@Test
	public void testValidMinimalConfiguration() {
		// Parsing the arguments
		comm.parse(defaults.toArray(new String[4]));

		assertEquals("Config file should be a file", true, params.getCfgFile().isFile());
		assertEquals("A class name does not match what was expected", CLASSNAME, params.getClassName());
	}

	@Test
	@Parameters({ "-h", "-?", "--help" })
	public void testHelp(String parameterLine) {
		log.info("Checking a help parameter in the form of: {}", parameterLine);
		comm.parse(parameterLine);
		comm.setProgramName(params.getProgName());

		// The help flag was kinda expected
		assertEquals("A help flag was expected", true, params.isHelp());

		// Let's check the defaults anyway... just to be sure...
		assertEquals("FALSE should be a default value for making class public", false, params.isClassPublic());
		assertEquals("FALSE should be a default value for creating a singleton class", false, params.isWantSingleton());
		assertEquals("TRUE should be a default value for generating schema", true, params.isWantSchema());

		final StringBuilder stringBuilder = new StringBuilder();
		comm.usage(stringBuilder);

		log.debug("A help message after processing: {}{}", System.lineSeparator(), stringBuilder.toString());
		stringBuilder.toString().matches("(?s)^Usage\\:\\s" + PROGRAM_NAME + "\\s+\\[options\\]\\s+Options\\:\\s+.*");
		// TODO This may not be the greatest way to handle this...
	}

	@Test
	@Parameters({ 
		"-public -singleton -noschema, true, true, true", 
		"-singleton -noschema, false, true, true",
	    "-public -noschema, true, false, true", 
	    "-public -singleton, true, true, true" 
	})
	public void testBooleans(String argv, boolean isPublic, boolean isSingleton, boolean isNoSchema) {
		defaults.addAll(Arrays.asList(argv.split("\\s+")));

		comm.parse(defaults.toArray(new String[0]));
		assertEquals(isPublic, params.isClassPublic());
		assertEquals(isSingleton, params.isWantSingleton());
		assertEquals(isNoSchema, params.isWantSchema());
	}
}

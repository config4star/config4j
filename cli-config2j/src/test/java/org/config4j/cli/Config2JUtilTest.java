package org.config4j.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class Config2JUtilTest {
	private static final String OUTPUTDIR = "data";
	private static final String PACKAGE_NAME = "org.config4j";
	private static final String CLASS_NAME = "DefaultSecurity";
	private static final String CONFIG_FILE = "src/test/resources/org/config4j/DefaultSecurity.cfg";

	@BeforeClass
	public static void removeOldStuff() {
		File testOutputDir = new File(OUTPUTDIR);
		if (testOutputDir.exists()) {
			testOutputDir.delete();
		}
	}

	@Parameters({ "org.config4j, true", "org/config4j, true", "or%g.Config, false" })
	@Test
	public void testIsValidPackageName(String packageName, boolean shouldValidate) {
		assertEquals(shouldValidate, Config2JUtil.isValidPackageName(packageName));
	}

	@Test
	public void goodTest() throws IOException {
		Config2JUtil util = new Config2JUtil("org.config4j.Config2JNoCheck");
		String[] args = { "-cfg", CONFIG_FILE, "-singleton", "-class", CLASS_NAME, "-package", PACKAGE_NAME, "-outputdir", OUTPUTDIR };
		assertTrue(util.parseCmdLineArgs(args));

		assertEquals(new File(CONFIG_FILE), util.getParams().getCfgFile());
		assertTrue(util.getParams().isWantSingleton());
		assertEquals(CLASS_NAME, util.getParams().getClassName());
		assertEquals(PACKAGE_NAME, util.getParams().getPackageName());
		assertEquals(OUTPUTDIR, util.getParams().getOutputDir());

		assertTrue(util.generateJavaClass(null));

		File javaFileName = new File(OUTPUTDIR + System.getProperty("file.separator") + util.getParams().getClassName() + ".java");
		assertTrue(javaFileName.exists());

		String rightFile = FileUtils.readFileToString(new File("src/test/resources/org/config4j/DefaultSecurity.java"));
		String createdFile = FileUtils.readFileToString(javaFileName);

		assertTrue(rightFile.contentEquals(createdFile));
	}

}

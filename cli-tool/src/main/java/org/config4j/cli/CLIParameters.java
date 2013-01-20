package org.config4j.cli;

import java.io.File;

import lombok.Data;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

@Data
@Parameters(resourceBundle = "cli.CLIParameters")
public class CLIParameters {
	private final String progName;

	@Parameter(names = { "-h", "-?", "--help" }, descriptionKey = "help", help = true)
	private boolean help;

	@Parameter(names = { "-cfg", "--configFile" }, converter = FileConverter.class, validateWith = ConfigFileValidator.class, descriptionKey = "config", required = true)
	private File cfgFile;

	@Parameter(names = { "-class", "--className" }, descriptionKey = "class", required = true)
	private String className;

	@Parameter(names = { "-schemaOverrideCfg" }, descriptionKey = "schemaOverrideCfg")
	private String schemaOverrideCfg;

	@Parameter(names = { "-schemaOverrideScope" }, descriptionKey = "schemaOverrideScope")
	private String schemaOverrideScope;

	@Parameter(names = { "-public" }, descriptionKey = "public")
	private boolean classPublic = false;

	@Parameter(names = { "-singleton" }, descriptionKey = "singleton")
	private boolean wantSingleton = false;

	@Parameter(names = { "-noschema" }, descriptionKey = "noschema")
	private boolean wantSchema = true;

	@Parameter(names = { "-package" }, validateWith = PackageNameValidator.class, descriptionKey = "package")
	private String packageName;

	@Parameter(names = { "-outputdir" }, descriptionKey = "outputdir")
	private String outputDir;
}

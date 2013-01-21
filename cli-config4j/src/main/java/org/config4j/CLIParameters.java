package org.config4j;

import lombok.Data;

import com.beust.jcommander.Parameter;

@Data
public class CLIParameters {
	@Parameter(names = { "-h", "-?", "/?", "/h", "--help" }, descriptionKey = "help", help = true)
	private boolean help;

	@Parameter(names = { "-cfg", "--configSource" }, description = "configSource", required = true)
	private String cfgSource;

	private String cmd;
	private String[] filterPatterns;
	private int forceMode;
	private boolean isRecursive;
	private String name;
	private String schemaName;
	private String schemaSource;
	private String scope;
	private String secScope;
	private String secSource;
	private int types;
	private boolean wantDiagnostics;
	private boolean wantExpandedUidNames;

}

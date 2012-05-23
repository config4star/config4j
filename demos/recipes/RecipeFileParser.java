//----------------------------------------------------------------------
// Copyright 2011 Ciaran McHale.
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions.
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.  
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
// BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
// ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//----------------------------------------------------------------------

import java.util.ArrayList;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;


public class RecipeFileParser
{
	public RecipeFileParser()
	{
		cfg = null;
		parseCalled = false; // set to 'true' after a successful parse().
	}


	public void parse(String recipeFilename, String scope)
											throws RecipeFileParserException
	{
		SchemaValidator		sv = new SchemaValidator();
		String				filter;
		String[]			schema = new String[] {
										"uid-recipe = scope",
										"uid-recipe.ingredients = list[string]",
										"uid-recipe.name = string",
										"uid-recipe.uid-step = string",
						};

		this.cfg = Configuration.create();
		this.scope = scope;
		filter = Configuration.mergeNames(scope, "uid-recipe");
		try {
			cfg.parse(recipeFilename);
			sv.parseSchema(schema);
			sv.validate(cfg, scope, "");
			recipeScopeNames = cfg.listFullyScopedNames(scope, "",
										Configuration.CFG_SCOPE, false, filter);
		} catch(ConfigurationException ex) {
			throw new RecipeFileParserException(ex.getMessage());
		}
		parseCalled = true;
	}


	//--------
	// Operations to query information about recipes
	//--------

	public String[] listRecipeScopes()
	{
		return recipeScopeNames;
	}


	String getRecipeName(String recipeScope) throws RecipeFileParserException
	{
		try {
			return cfg.lookupString(recipeScope, "name");
		} catch(ConfigurationException ex) {
			throw new RecipeFileParserException(ex.getMessage());
		}
	}


	public String[] getRecipeIngredients(String recipeScope)
											throws RecipeFileParserException
	{
		try {
			return cfg.lookupList(recipeScope, "ingredients");
		} catch(ConfigurationException ex) {
			throw new RecipeFileParserException(ex.getMessage());
		}
	}


	public String[] getRecipeSteps(String recipeScope)
											throws RecipeFileParserException
	{
		int						i;
		String[]				stepNames;
		String					str;
		ArrayList				result = new ArrayList();

		try {
			stepNames = cfg.listLocallyScopedNames(recipeScope, "",
										  Configuration.CFG_STRING, false,
										  "uid-step");
			for (i = 0; i < stepNames.length; i++) {
				str = cfg.lookupString(recipeScope, stepNames[i]);
				result.add(str);
			}
		} catch(ConfigurationException ex) {
			throw new RecipeFileParserException(ex.getMessage());
		}

		return (String[])result.toArray(new String[0]);
	}

	//--------
	// Instance variables
	//--------
	private Configuration		cfg;
	private String				scope;
	private boolean				parseCalled;
	private String[]			recipeScopeNames;

}

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


public class Demo
{
	private static String	recipeFilename;   // set by parseCommandLineArgs()
	private static String	scope;            // set by parseCommandLineArgs()


	public static void main(String[] args)
	{
		RecipeFileParser		parser = new RecipeFileParser();
		int						i;
		int						j;
		String[]				recipeScopes;
		String[]				ingredients;
		String[]				steps;
		String					name;

		parseCommandLineArgs(args);
		//--------
		// Parse and error-check a file containing recipes.
		//--------
		try {
			parser.parse(recipeFilename, scope);
		} catch(RecipeFileParserException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}

		//--------
		// Print information about the recipes.
		//--------
		recipeScopes = parser.listRecipeScopes();
		System.out.println("There are " + recipeScopes.length + " recipes");
		for (i = 0; i < recipeScopes.length; i++) {
			name = parser.getRecipeName(recipeScopes[i]);
			ingredients = parser.getRecipeIngredients(recipeScopes[i]);
			steps = parser.getRecipeSteps(recipeScopes[i]);
			System.out.println("\nRecipe \"" + name + "\":");
			System.out.println("\tThis recipe has " + ingredients.length
					+ " ingredients:");
			for (j = 0; j < ingredients.length; j++) {
				System.out.println("\t\t\"" + ingredients[j] + "\"");
			}
			System.out.println("\tThis recipe has " + steps.length + " steps:");
			for (j = 0; j < steps.length; j++) {
				System.out.println("\t\t\"" + steps[j] + "\"");
			}
		}
	}


	private static void parseCommandLineArgs(String[] args)
	{
		int					i;
		String				arg;

		recipeFilename = "";
		scope = "";

		for (i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equals("-h")) {
				usage();
			} else if (arg.equals("-recipes")) {
				if (i == args.length - 1) { usage(); }
				recipeFilename = args[i+1];
				i++;
			} else if (arg.equals("-scope")) {
				if (i == args.length - 1) { usage(); }
				scope = args[i+1];
				i++;
			} else {
				System.err.println("Unrecognised option '" + arg + "'\n");
				usage();
			}
		}
		if (recipeFilename.equals("")) {
			System.err.println("\nYou must specify '-recipes <source>'");
			usage();
		}
	}


	private static void usage()
	{
		String	className = Demo.class.getName();
		System.err.println("\n"
			+ "usage: java " + className + " <options> -recipes <source>\n"
			+ "\n"
			+ "The <options> can be:\n"
			+ "  -h             Print this usage statement\n"
			+ "  -scope         Configuration scope\n"
			+ "\n"
			+ "A configuration <source> can be one of the following:\n"
			+ "  file.cfg       A configuration file\n"
			+ "  file#file.cfg  A configuration file\n"
			+ "  exec#<command> Output from executing the specified command\n"
			+ "\n");
		System.exit(1);
	}

}

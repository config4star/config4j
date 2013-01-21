package org.config4j;

//----------------------------------------------------------------------
// WARNING: this file was generated by org.config4j.Config2JNoCheck.
// DO NOT EDIT.
//----------------------------------------------------------------------


class DefaultSecurity
{
    private DefaultSecurity()
    {
        str = new StringBuffer();
        str.append("#-----------------------------------------------");
        str.append("------------------------" + CR);
        str.append("# Default security checks for Config4*" + CR);
        str.append("# ------------------------------------" + CR);
        str.append("#" + CR);
        str.append("# allow_patterns is a list of wildcarded command");
        str.append(" lines that are allowed" + CR);
        str.append("# to be executed (if the command resides in a di");
        str.append("rectory listed in" + CR);
        str.append("# trusted_directories)." + CR);
        str.append("#" + CR);
        str.append("# deny_patterns is a list of wildcarded command ");
        str.append("lines that are" + CR);
        str.append("# disallowed." + CR);
        str.append("#" + CR);
        str.append("# The wildcarded command-lines can contain \"*\", ");
        str.append("which denotes zero or" + CR);
        str.append("# more characters." + CR);
        str.append("#" + CR);
        str.append("# trusted_directories is a list of directories i");
        str.append("n which the commands" + CR);
        str.append("# listed in allow_patterns must be found." + CR);
        str.append("#-----------------------------------------------");
        str.append("------------------------" + CR);
        str.append("" + CR);
        str.append("" + CR);
        str.append("@if (osType() == \"unix\") {" + CR);
        str.append("\t#--------" + CR);
        str.append("\t# Allow only a few commands that might be usefu");
        str.append("l." + CR);
        str.append("\t#--------" + CR);
        str.append("\tallow_patterns = [" + CR);
        str.append("\t\t\"curl *\"," + CR);
        str.append("\t\t\"hostname\"," + CR);
        str.append("\t\t\"uname\"," + CR);
        str.append("\t\t\"uname *\"," + CR);
        str.append("\t\t\"ifconfig\"" + CR);
        str.append("\t];" + CR);
        str.append("" + CR);
        str.append("\t#--------" + CR);
        str.append("\t# Disallow `...` (nested commands) and piped co");
        str.append("mmands since we" + CR);
        str.append("\t# have no idea what they might contain." + CR);
        str.append("\t#--------" + CR);
        str.append("\tdeny_patterns = [\"*`*\", \"*|*\", \"*>*\"];" + CR);
        str.append("\ttrusted_directories = [\"/bin\", \"/usr/bin\", \"/us");
        str.append("r/local/bin\"," + CR);
        str.append("\t\t\t\t\"/sbin\", \"/usr/sbin\"];" + CR);
        str.append("} @elseIf (osType() == \"windows\") {" + CR);
        str.append("\t#--------" + CR);
        str.append("\t# Cygwin (or some other collection of UNIX-like");
        str.append(" tools) might" + CR);
        str.append("\t# be installed on a Windows machine so the secu");
        str.append("rity" + CR);
        str.append("\t# configuration is written accordingly." + CR);
        str.append("\t# The main difference from the UNIX settings is");
        str.append(" that" + CR);
        str.append("\t# \"deny_patterns\" forbids the use of both UNIX ");
        str.append("and Windows-style" + CR);
        str.append("\t# environment variables, that is, $NAME and %NA");
        str.append("ME%." + CR);
        str.append("\t#--------" + CR);
        str.append("\tallow_patterns = [" + CR);
        str.append("\t\t\"curl *\"," + CR);
        str.append("\t\t\"hostname\"," + CR);
        str.append("\t\t\"uname\"," + CR);
        str.append("\t\t\"uname *\"," + CR);
        str.append("\t\t\"ipconfig\"" + CR);
        str.append("\t];" + CR);
        str.append("\tdeny_patterns = [\"*`*\", \"*|*\", \"*>*\"];" + CR);
        str.append("\ttrusted_directories = [getenv(\"SYSTEMROOT\") + \"");
        str.append("\\system32\"];" + CR);
        str.append("} @else {" + CR);
        str.append("\t#--------" + CR);
        str.append("\t# If we don't know what operating system we are");
        str.append(" running on then" + CR);
        str.append("\t# don't trust anything." + CR);
        str.append("\t#--------" + CR);
        str.append("\tallow_patterns = [];" + CR);
        str.append("\tdeny_patterns = [\"*\"];" + CR);
        str.append("\ttrusted_directories = [];" + CR);
        str.append("};" + CR);
        str.append("");
    }

    public static String   getString() { return singleton.str.toString(); }

    private StringBuffer str;
    private static final String CR = System.getProperty("line.separator");
    private static DefaultSecurity singleton = new DefaultSecurity();
}

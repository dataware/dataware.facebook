package datasphere.shims.facebook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JIDValidator{
	 
	  private static Pattern pattern;
	  private static Matcher matcher;

	  private static final String EMAIL_PATTERN = 
		  "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@" +
		  "[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	  private JIDValidator(){}

	  /**
	   * Validate the address via the regex
	   * @param jid the address being validated
	   * @return true if the address is valid, false otherwise
	   */
	  public static boolean validate( final String jid ) {
		  pattern = Pattern.compile( EMAIL_PATTERN );
		  matcher = pattern.matcher( jid );
		  return matcher.matches();
	  }
}
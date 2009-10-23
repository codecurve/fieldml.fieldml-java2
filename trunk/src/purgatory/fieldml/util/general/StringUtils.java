package purgatory.fieldml.util.general;

/**
 * General utility methods for strings and char arrays.
 *
 */
public class StringUtils {

	/**
	 * Fill charArray with the first k chars from stringValue, where:
	 * n = charArray.length
	 * m = stringValue.length()
	 * k = min(n,m)
	 * 
	 * If there is still space in charArray, a 0 value is inserted after the k chars (i.e. to "null terminate" the "string").
	 * The remaining length of charArray will be unchanged.
	 * 
	 */
	public static void stringToChars(char[] charArray, String stringValue) {
		final int lengthToCopy = Math.min(charArray.length, stringValue.length());
		stringValue.getChars(0, lengthToCopy, charArray, 0);
		if(lengthToCopy < charArray.length) {
			charArray[lengthToCopy]='\0';
		}
		//TODO: There is an assumption that only the lower 8 bits of each char are used, i.e. these are actually going to only be US-ASCII chars.
	}

}

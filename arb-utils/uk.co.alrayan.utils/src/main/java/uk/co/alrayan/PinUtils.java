package uk.co.alrayan;

public class PinUtils {

  private static final int ABS_PIN_LENGTH = 6;
  private static final int MAX_DUPS = 4;
  private static final int pinLength = ABS_PIN_LENGTH;

  public static int checkPin(String pinCode) {
    int retVal = 0;

    // do lots of checks on the PIN

    if (pinCode.length() != pinLength) {
      retVal = -1;
    } else {

      // check too many of the same digits
      for (int j = 0; j < pinCode.length(); j++) {
        char nextChar = pinCode.charAt(j);

        long count = pinCode.chars().filter(ch -> ch == nextChar).count();
        if (count >= MAX_DUPS)
          retVal = -2;
          break;
      }

      
    }

    if (retVal == 0) {

      // check for a sequence
      int sequp = 0;
      int seqdown = 0;
      for (int i = 0; i < pinLength - 1; i++) {
        int val1 = java.lang.Character.getNumericValue(pinCode.charAt(i));
        int val2 = java.lang.Character.getNumericValue(pinCode.charAt(i + 1));

        if (val2 == val1 + 1) sequp++;

        if (val2 == val1 - 1) seqdown++;
      }
      if (sequp > 4 || seqdown > 4) retVal = -3;
    }

    return retVal;
  }
}

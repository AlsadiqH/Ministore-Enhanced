package variableValidator;

public class VariableValidator {
    public static boolean validIntForStock(String input) {
        try {
            int value = Integer.parseInt(input);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
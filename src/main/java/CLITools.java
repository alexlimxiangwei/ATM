public abstract class CLITools {
    public String adjustSpacing(String input){
        int numSpaces;
        String spaces = "";
        if (input.length() < 19){
            numSpaces = 19 - input.length();
            for (int x = 0; x < numSpaces; x++){
                spaces += " ";
            }
            return input + spaces;
        }

        return input;
    }
}

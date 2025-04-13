package query_parser;

class Lexer {
    public Lexer(String input) {
        this.input = input;
    }

    private final String input;
    private int index = 0;

    public Token nextToken() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) index++;
        if (index >= input.length()) return new Token(Token.Type.END, "");

        char c = input.charAt(index);
        if (Character.isDigit(c)) {
            int start = index;
            while (index < input.length() && Character.isDigit(input.charAt(index))) index++;
            return new Token(Token.Type.NUMBER, input.substring(start, index));
        }
        if (c == '(') {
            index++;
            return new Token(Token.Type.LPAREN, "(");
        }
        if (c == ')') {
            index++;
            return new Token(Token.Type.RPAREN, ")");
        }
        if (c == ',') {
            index++;
            return new Token(Token.Type.COMMA, ",");
        }
        if (input.startsWith("AND", index)) {
            index += 3;
            return new Token(Token.Type.AND, "AND");
        }
        if (input.startsWith("OR", index)) {
            index += 2;
            return new Token(Token.Type.OR, "OR");
        }
        if (input.startsWith("NOT", index)) {
            index += 3;
            return new Token(Token.Type.NOT, "NOT");
        }
        if (input.startsWith("WITHIN", index)) {
            index += 6;
            return new Token(Token.Type.WITHIN, "WITHIN");
        }

        if (c == '\'' || c == '"') {
            int start = ++index;
            while (index < input.length() && input.charAt(index) != c) index++;
            if (index < input.length()) {
                String value = input.substring(start, index);
                index++;
                if(c == '\'')
                    return new Token(Token.Type.WORD, value);
                else
                    return new Token(Token.Type.PHRASE, value);
            }
        }

        throw new RuntimeException("Unexpected token at position " + index);
    }
}

package Realizations.QueryParsers;

import QuerySystem.Dictionary;
import QuerySystem.QueryParser;
import QuerySystem.QueryResult;

public class PhrasalBooleanRetrQueryParser implements QueryParser {

    static Dictionary dict;

    private static class Token {

        Token(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        enum Type { WORD, PHRASE, NUMBER, AND, OR, NOT, LPAREN, RPAREN, WITHIN, COMMA, END }
        Type type;
        String value;
    }

    private static class Lexer {
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

    private static class Parser {
        public Parser(String query) {
            this.lexer = new Lexer(query);
            this.currentToken = lexer.nextToken();
        }

        private final Lexer lexer;
        private Token currentToken;

        private void match(Token.Type expected) {
            if (currentToken.type == expected) {
                currentToken = lexer.nextToken();
            } else {
                throw new RuntimeException("Syntax error: expected " + expected + " but found " + currentToken.type);
            }
        }

        public QueryResult parse() {
            QueryResult res = parseExpr();
            if (currentToken.type != Token.Type.END) {
                throw new RuntimeException("Unexpected token at end of input");
            }
            return res;
        }

        private QueryResult parseExpr() {
            QueryResult res;
            switch (currentToken.type) {
                case NOT:
                    match(Token.Type.NOT);
                    res = parseExpr();
                    res.not();
                    break;
                case LPAREN:
                    match(Token.Type.LPAREN);
                    res = parseExpr();
                    match(Token.Type.RPAREN);
                    break;
                case WORD:
                    res = dict.findWord(currentToken.value);
                    match(Token.Type.WORD);
                    break;
                case PHRASE:
                    res = dict.findPhrase(currentToken.value);
                    match(Token.Type.PHRASE);
                    break;
                case WITHIN:
                    match(Token.Type.WITHIN);
                    int n = Integer.parseInt(currentToken.value);
                    match(Token.Type.NUMBER);
                    String word1 = currentToken.value;
                    match(Token.Type.WORD);
                    match(Token.Type.COMMA);
                    String word2 = currentToken.value;
                    match(Token.Type.WORD);
                    res = dict.findWordsWithin(word1, word2, n);
                    break;
                default:
                    throw new RuntimeException("Unexpected token: " + currentToken.type);
            }

            switch (currentToken.type) {
                case AND:
                    match(Token.Type.AND);
                    assert res != null;
                    res.and(parseExpr());
                    return res;
                case OR:
                    match(Token.Type.OR);
                    assert res != null;
                    res.or(parseExpr());
                    return res;
                default:
                    return res;
            }
        }
    }

    @Override
    public QueryResult parse(Dictionary dict, String query) {
        PhrasalBooleanRetrQueryParser.dict = dict;
        Parser parser = new Parser(query);
        return parser.parse();
    }
}
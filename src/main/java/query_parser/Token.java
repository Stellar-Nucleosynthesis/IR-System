package query_parser;

class Token {

    Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    enum Type { WORD, PHRASE, NUMBER, AND, OR, NOT, LPAREN, RPAREN, WITHIN, COMMA, END }
    Type type;
    String value;
}
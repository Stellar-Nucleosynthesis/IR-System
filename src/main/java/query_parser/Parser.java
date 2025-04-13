package query_parser;

import retrieval_system.RetrievalEngine;
import retrieval_results.RetrievalResult;
import postings.Posting;

class Parser<T extends RetrievalResult<T, P>, P extends Posting<P>> {
    public Parser(RetrievalEngine<T, P> engine, String query) {
        this.engine = engine;
        this.lexer = new Lexer(query);
        this.currentToken = lexer.nextToken();
    }

    private final RetrievalEngine<T, P> engine;
    private final Lexer lexer;
    private Token currentToken;

    private void match(Token.Type expected) {
        if (currentToken.type == expected) {
            currentToken = lexer.nextToken();
        } else {
            throw new RuntimeException("Syntax error: expected " + expected + " but found " + currentToken.type);
        }
    }

    public T parse() {
        T res = parseExpr();
        if (currentToken.type != Token.Type.END) {
            throw new RuntimeException("Unexpected token at end of input");
        }
        return res;
    }

    private T parseExpr() {
        T res;
        switch (currentToken.type) {
            case NOT:
                match(Token.Type.NOT);
                res = parseExpr();
                T all = engine.retrieveAll();
                all.subtract(res);
                res = all;
                break;
            case LPAREN:
                match(Token.Type.LPAREN);
                res = parseExpr();
                match(Token.Type.RPAREN);
                break;
            case PHRASE:
                res = engine.retrieve(currentToken.value);
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
                res = engine.retrieve(word1, word2, n);
                break;
            default:
                throw new RuntimeException("Unexpected token: " + currentToken.type);
        }

        switch (currentToken.type) {
            case AND:
                match(Token.Type.AND);
                assert res != null;
                res.intersect(parseExpr());
                return res;
            case OR:
                match(Token.Type.OR);
                assert res != null;
                res.merge(parseExpr());
                return res;
            default:
                return res;
        }
    }
}
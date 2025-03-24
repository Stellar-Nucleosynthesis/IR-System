package realizations.query_parsers;

import query_system.QueryEngine;
import query_system.QueryParser;
import query_system.QueryResult;

import java.util.LinkedList;

public class BooleanRetrQueryParser implements QueryParser {
    @Override
    public QueryResult parse(QueryEngine dict, String query) {
        String[] tokens = query.split(" ");
        LinkedList<QueryResult> valStack = new LinkedList<>();
        LinkedList<String> opStack = new LinkedList<>();
        for(String token : tokens){
            switch(token){
                case "AND":
                    while(!opStack.isEmpty()){
                        if(opStack.peek().equals("AND")){
                            opStack.pop();
                            assert valStack.peek() != null;
                            valStack.peek().and(valStack.pop());
                        }
                    }
                    opStack.push("AND");
                    break;
                case "OR":
                    while(!opStack.isEmpty()){
                        if(opStack.peek().equals("AND")){
                            opStack.pop();
                            assert valStack.peek() != null;
                            valStack.peek().and(valStack.pop());
                        }
                        assert opStack.peek() != null;
                        if(opStack.peek().equals("OR")){
                            opStack.pop();
                            assert valStack.peek() != null;
                            valStack.peek().or(valStack.pop());
                        }
                    }
                    opStack.push("OR");
                    break;
                case "NOT":
                    opStack.push("NOT");
                    break;
                default:
                    if(opStack.peek() != null && opStack.peek().equals("NOT")){
                        opStack.pop();
                        assert valStack.peek() != null;
                        valStack.peek().not();
                    } else {
                        valStack.push(dict.findWord(token));
                    }
                    break;
            }
        }
        return valStack.isEmpty() ? null : valStack.peek();
    }
}

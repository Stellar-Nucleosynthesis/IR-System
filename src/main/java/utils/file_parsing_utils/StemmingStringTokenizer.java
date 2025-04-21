package utils.file_parsing_utils;

import opennlp.tools.stemmer.PorterStemmer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StemmingStringTokenizer {
    public List<String> tokenize(String line) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9-'`]+");
        Matcher matcher = pattern.matcher(line);
        while(matcher.find()) {
            String word = normalize(matcher.group());
            if(!word.isEmpty()){
                result.add(word);
            }
        }
        return result;
    }

    private final PorterStemmer stemmer = new PorterStemmer();

    public String normalize(String word){
        word = word.toLowerCase().replaceAll("[-`']", "");
        return stemmer.stem(word);
    }
}

package FileParsingUtils;

import opennlp.tools.stemmer.PorterStemmer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StemmingStringTokenizer {
    public static List<String> tokenize(String line) {
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

    public static String normalize(String word){
        PorterStemmer stemmer = new PorterStemmer();
        word = word.toLowerCase().replaceAll("[-`']", "");
        return stemmer.stem(word);
    }
}

package Realizations.Dictionaries.TolerantDictionaries;

public class JokerQueryFilter {
    public static boolean matches(String term, String query) {
        return term.matches(query.replace("*", ".*"));
    }
}

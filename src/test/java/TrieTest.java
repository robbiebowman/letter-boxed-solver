import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrieTest {

    private static Trie dictionary;

    @BeforeAll
    public static void setUp() {
        try {
            URI uri = Objects.requireNonNull(TrieTest.class.getResource("words.txt")).toURI();
            List<String> strings = Files.readAllLines(Paths.get(uri)).stream().filter(w -> {
                var cs = w.toCharArray();
                for (char c : cs) {
                    if (c < 'a' || c > 'z') {
                        return false;
                    }
                }
                if (w.length() < 3) {
                    return false;
                }
                return true;
            }).toList();
            dictionary = new Trie();
            for (String word : strings) {
                dictionary.insert(word);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testChildWordRetrieval() {
        var node = dictionary.getNodeOrNull("test");
        var words = dictionary.getAllChildWords(node, "test");
        assertTrue(words.contains("testing"));
        assertTrue(words.contains("testers"));
        assertTrue(words.contains("testament"));
    }
}

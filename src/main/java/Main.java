import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Main {

    public static void main(String[] args) {
        var solver = new Solver(List.of("yfu", "lnr", "gxb", "mai"), 6);
        var answer = solver.getShortestAnswer();
        var words = answer.words().stream().map(w -> w.getText()).toList();
        System.out.println(words);
    }
}

record Coordinate(int sideIndex, int charIndex) {
}

record Word(char[][] sides, List<Coordinate> coordinates) {
    String getText() {
        return coordinates.stream()
                .map(coord -> Character.toString(sides[coord.sideIndex()][coord.charIndex()]))
                .reduce((acc, e) -> acc + e)
                .get();
    }
}

record Answer(List<Word> words) {
}

class Solver {

    Solver(List<String> puzzleSides, int limit) {
        this.limit = limit;
        try {
            URI uri = Objects.requireNonNull(getClass().getResource("words.txt")).toURI();
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
            this.dictionary = new Trie();
            for (String word : strings) {
                this.dictionary.insert(word);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        puzzle = puzzleSides.stream().map(String::toCharArray).toArray(char[][]::new);
    }

    Answer getShortestAnswer() {
        var startingAnswers = new HashSet<Answer>();
        for (int y = 0; y < puzzle.length; y++) {
            var chars = puzzle[y];
            for (int x = 0; x < chars.length; x++) {
                startingAnswers.add(new Answer(List.of(
                        new Word(puzzle, List.of(
                                new Coordinate(y, x)
                        )))
                ));
            }
        }
        var answers = startingAnswers.stream().flatMap(s -> getLegalContinuations(s).stream()).collect(Collectors.toSet());
        while (answers.size() > 0) {
            var completeAnswers = answers.stream().filter(this::isCompleteAnswer).toList();
            if (!completeAnswers.isEmpty()) {
                List<Word> words = completeAnswers.stream().min(Comparator.comparingInt(a -> a.words().size())).get().words();
                return new Answer(words);
            }
            answers = answers.stream().flatMap(s -> getLegalContinuations(s).stream()).collect(Collectors.toSet());
        }
        return null;
    }

    private boolean wordAddsNewLetter(List<Word> existingWords, Word newWord) {
        var existingString = existingWords.stream().map(Word::getText).reduce((s, s2) -> s + s2).toString();
        // Create a set to store characters from the first string
        Set<Character> baseChars = new HashSet<>();
        for (char c : existingString.toCharArray()) {
            baseChars.add(c);
        }
        for (char c : newWord.getText().toCharArray()) {
            if (!baseChars.contains(c)) {
                return true;
            }
        }
        return false;
    }

    private PriorityQueue<Answer> getLegalContinuations(Answer answer) {
        if (answer.words().size() > limit) {
            return new PriorityQueue<>();
        }
        Word currentWord = answer.words().get(answer.words().size() - 1);
        var currentPosition = currentWord.coordinates().get(currentWord.coordinates().size() - 1);
        Comparator<Answer> comparator = Comparator.comparingInt(a -> -a.words().size());
        var legalMoves = new PriorityQueue<>(comparator);
        for (int y = 0; y < puzzle.length; y++) {
            if (currentPosition.sideIndex() == y) {
                continue;
            }
            var chars = puzzle[y];
            for (int x = 0; x < chars.length; x++) {
                var newWord = new Word(puzzle, concat(currentWord.coordinates(), new Coordinate(y, x)));
                String wordText = newWord.getText();
                if (canBecomeRealWord(wordText)) {
                    List<Word> newWords = concat(answer.words().subList(0, answer.words().size() - 1), newWord);
                    legalMoves.add(new Answer(newWords));
                }
                if (isRealWord(wordText)) {
                    var previousWords = answer.words().subList(0, answer.words().size() - 1);
                    if (wordAddsNewLetter(previousWords, newWord)) {
                        List<Word> newWords = concat(previousWords, newWord);
                        legalMoves.add(new Answer(concat(newWords, new Word(puzzle, List.of(new Coordinate(y, x))))));
                    }
                }
            }
        }
        return legalMoves;
    }

    boolean isCompleteAnswer(Answer answer) {
        var coordinates = answer.words().stream().flatMap(i -> i.coordinates().stream()).toList();
        for (int y = 0; y < puzzle.length; y++) {
            var chars = puzzle[y];
            for (int x = 0; x < chars.length; x++) {
                if (!coordinates.contains(new Coordinate(y, x))) {
                    return false;
                }
            }
        }

        Word finalWord = answer.words().get(answer.words().size() - 1);
        return isRealWord(finalWord.getText());
    }

    private boolean canBecomeRealWord(String wordText) {
        return dictionary.startsWith(wordText);
    }

    private boolean isRealWord(String wordText) {
        return dictionary.contains(wordText);
    }

    private <T> List<T> concat(List<T> a, List<T> b) {
        return Stream.concat(a.stream(), b.stream()).collect(toList());
    }

    private <T> List<T> concat(List<T> as, T a) {
        return Stream.concat(as.stream(), Stream.of(a)).collect(toList());
    }

    private final char[][] puzzle;

    private final int limit;

    private final Trie dictionary;


}
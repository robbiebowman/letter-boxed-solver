package com.robbiebowman

import com.robbiebowman.Answer
import com.robbiebowman.Coordinate
import com.robbiebowman.Trie
import com.robbiebowman.Word
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class Solver(puzzleSides: List<String>, private val limit: Int) {
    private val comparator: Comparator<Answer> = Comparator.comparingInt { a: Answer -> a.words.size }

    fun getShortestAnswer(): Answer? {
        val startingAnswers = HashSet<Answer>()
        for (y in puzzle.indices) {
            val chars = puzzle[y]
            for (x in chars.indices) {
                startingAnswers.add(
                    Answer(listOf(Word(puzzle, listOf(Coordinate(y, x)))))
                )
            }
        }
        var answers = startingAnswers.flatMap { s: Answer ->
            getLegalContinuations(
                s
            )
        }.let { xs -> PriorityQueue(comparator).also { it.addAll(xs) } }
        while (!answers.isEmpty()) {
            val completeAnswers = answers.filter { answer: Answer -> isCompleteAnswer(answer) }
            if (completeAnswers.isNotEmpty()) {
                val words = completeAnswers.minBy { it.words.size }.words
                return Answer(words)
            }
            answers.groupBy { a: Answer ->
                a.words.stream().map { obj: Word -> obj.text }
                    .collect(Collectors.joining())
            }
            answers = answers.flatMap { s: Answer ->
                getLegalContinuations(
                    s
                )
            }.let { xs -> PriorityQueue(comparator).also { it.addAll(xs) } }
        }
        return null
    }

    private fun getUniqueCharacters(word: String): Set<Char> {
        return word.toSet()
    }

    private fun wordAddsNewLetter(existingWords: List<Word>, newWord: Word): Boolean {
        val existingString =
            existingWords.stream().map { obj: Word -> obj.getText() }.reduce { s: String, s2: String -> s + s2 }
                .toString()
        // Create a set to store characters from the first string
        val baseChars: MutableSet<Char> = HashSet()
        for (c in existingString.toCharArray()) {
            baseChars.add(c)
        }
        for (c in newWord.getText().toCharArray()) {
            if (!baseChars.contains(c)) {
                return true
            }
        }
        return false
    }

    fun getLegalContinuations(answer: Answer): PriorityQueue<Answer> {
        if (answer.words.size > limit) {
            return PriorityQueue()
        }
        val currentWord = answer.words[answer.words.size - 1]
        val currentPosition = currentWord.coordinates[currentWord.coordinates.size - 1]
        val legalMoves = PriorityQueue(comparator)
        for (y in puzzle.indices) {
            if (currentPosition.sideIndex == y) {
                continue
            }
            val chars = puzzle[y]
            for (x in chars.indices) {
                val newWord = Word(puzzle, concat(currentWord.coordinates, Coordinate(y, x)))
                val wordText = newWord.getText()
                if (canBecomeRealWord(wordText)) {
                    val newWords = concat(answer.words.subList(0, answer.words.size - 1), newWord)
                    legalMoves.add(Answer(newWords))
                }
                if (isRealWord(wordText)) {
                    val previousWords: List<Word> = answer.words.subList(0, answer.words.size - 1)
                    if (wordAddsNewLetter(previousWords, newWord)) {
                        val newWords = concat(previousWords, newWord)
                        legalMoves.add(Answer(concat(newWords, Word(puzzle, java.util.List.of(Coordinate(y, x))))))
                    }
                }
            }
        }
        return legalMoves
    }

    private fun isCompleteAnswer(answer: Answer): Boolean {
        val coordinates = answer.words.stream().flatMap { i: Word -> i.coordinates.stream() }.toList()
        for (y in puzzle.indices) {
            val chars = puzzle[y]
            for (x in chars.indices) {
                if (!coordinates.contains(Coordinate(y, x))) {
                    return false
                }
            }
        }

        val finalWord = answer.words[answer.words.size - 1]
        return isRealWord(finalWord.getText())
    }

    private fun canBecomeRealWord(wordText: String): Boolean {
        return dictionary.startsWith(wordText)
    }

    private fun isRealWord(wordText: String): Boolean {
        return dictionary.contains(wordText)
    }

    private fun <T> concat(a: List<T>, b: List<T>): List<T> {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toList())
    }

    private fun <T> concat(`as`: List<T>, a: T): List<T> {
        return Stream.concat(`as`.stream(), Stream.of(a)).collect(Collectors.toList())
    }

    private var puzzle: Array<out CharArray>

    private val dictionary: Trie = Trie()

    init {
        try {
            val uri = Objects.requireNonNull(javaClass.getResource("/words.txt")).toURI()
            val strings = Files.readAllLines(Paths.get(uri)).stream().filter { w: String ->
                val cs = w.toCharArray()
                for (c in cs) {
                    if (c < 'a' || c > 'z') {
                        return@filter false
                    }
                }
                if (w.length < 3) {
                    return@filter false
                }
                true
            }.toList()
            for (word in strings) {
                dictionary.insert(word)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        puzzle = puzzleSides.map { it.toCharArray() }.toTypedArray()
    }
}
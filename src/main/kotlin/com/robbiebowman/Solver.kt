package com.robbiebowman

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class Solver(puzzleSides: List<String>, private val limit: Int) {

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
        val answers = startingAnswers.flatMap { s: Answer ->
            getLegalContinuations(s)
        }.let { ans ->
            PriorityQueue<Answer>(
                compareBy(
                    { it.words.size },
                    { it.words.flatMap { it.text.toSet() }.toSet().size * -1 })
            ).also { pq ->
                pq.addAll(ans)
            }
        }
        while (answers.isNotEmpty()) {
            val completeAnswers = answers.filter { answer: Answer -> isCompleteAnswer(answer) }
            if (completeAnswers.isNotEmpty()) {
                val words = completeAnswers.minBy { it.words.size }.words
                return Answer(words)
            }
            val bestAns = answers.poll()
            val newAnswers = getLegalContinuations(bestAns).filter { a -> answers.none { a.isStrictSubsetOf(it) } }
            answers.addAll(newAnswers)
        }
        return null
    }

    private fun Answer.isStrictSubsetOf(other: Answer): Boolean {
        if (this.words.size == 1) return false
        val thisLockedInWords = this.words.dropLast(1)
        val otherLockedInWords = other.words.dropLast(1)
        val thisChars = thisLockedInWords.flatMap { it.text.toSet() }.toSet()
        val otherChars = otherLockedInWords.flatMap { it.text.toSet() }.toSet()
        val thisStarter = this.words.last().text.first()
        val otherStarter = other.words.last().text.first()
        val isSubset = thisChars.subtract(otherChars).isEmpty() && thisStarter == otherStarter
        return isSubset && other.words.last().text.startsWith(this.words.last().text)
    }

    private fun wordAddsNewLetter(existingWords: List<Word>, newWord: Word): Boolean {
        val existingChars = existingWords.joinToString { it.text }.toSet()
        return newWord.text.toSet().subtract(existingChars).isNotEmpty()
    }

    private fun getLegalContinuations(answer: Answer): Set<Answer> {
        if (answer.words.size > limit) {
            return emptySet()
        }
        val currentWord = answer.words.last()
        val currentPosition = currentWord.coordinates.last()
        val legalMoves = mutableSetOf<Answer>()
        for (y in puzzle.indices) {
            if (currentPosition.sideIndex == y) {
                continue
            }
            val chars = puzzle[y]
            for (x in chars.indices) {
                val newWord = Word(puzzle, currentWord.coordinates.plus(Coordinate(y, x)))
                if (canBecomeRealWord(newWord.text)) {
                    val newWords = answer.words.dropLast(1).plus(newWord)
                    legalMoves.add(Answer(newWords))
                }
                if (isRealWord(newWord.text)) {
                    val previousWords: List<Word> = answer.words.subList(0, answer.words.size - 1)
                    if (wordAddsNewLetter(previousWords, newWord)) {
                        val newWords = previousWords.plus(newWord)
                        legalMoves.add(Answer(newWords.plus(Word(puzzle, listOf(Coordinate(y, x))))))
                    }
                }
            }
        }
        return legalMoves
    }

    private fun isCompleteAnswer(answer: Answer): Boolean {
        val coordinates = answer.words.flatMap { i: Word -> i.coordinates }.toSet()
        puzzle.forEachIndexed { y, rows ->
            rows.indices.forEach { x ->
                if (!coordinates.contains(Coordinate(y, x))) {
                    return@isCompleteAnswer false
                }
            }
        }
        val finalWord = answer.words.last()
        return isRealWord(finalWord.text)
    }

    private fun canBecomeRealWord(wordText: String): Boolean {
        return dictionary.startsWith(wordText)
    }

    private fun isRealWord(wordText: String): Boolean {
        return dictionary.contains(wordText)
    }

    private val puzzle: Array<out CharArray>

    private val dictionary: Trie = Trie()

    init {
        val uri = javaClass.getResource("/words.txt")?.toURI() ?: throw Exception("Couldn't get resource.")
        val strings = Files.readAllLines(Paths.get(uri)).filter { w ->
            w.all { it in 'a'..'z' } && w.length > 2
        }
        strings.forEach(dictionary::insert)
        puzzle = puzzleSides.map { it.toCharArray() }.toTypedArray()
    }
}
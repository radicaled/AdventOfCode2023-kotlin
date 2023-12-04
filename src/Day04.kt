import java.lang.IllegalStateException

fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf {
            val card = parseCardRow(it)
            card.calculateWorth()
        }        
    }

    fun part2(input: List<String>): Int {
        val cards = input.map { parseCardRow(it) }
        val wonCardIds = ArrayDeque<Int>()        
        
        // Prime the queue by processing all the original cards first.
        cards.forEach { card ->
            val cardIds = getWonCardIds(card)
            wonCardIds.addAll(cardIds)
        }
        
        // This value is how many cards we have, total. This is going to include the original set.
        var cardCount = cards.size + wonCardIds.size
        
        // Next we're going to iterate over all of our won cards as a queue, with freshly won cards pushed to the back of the queue.
        var nextCardId = wonCardIds.removeFirstOrNull()
        
        // To prevent unbounded processing, make sure we limit iterations to 10 million at max.
        // You know I'm going to make a logic bug here on one of these passes. Just wait.
        var currentIterations = 0
        val maxIterationsAllowed = 10_000_000
        
        while (nextCardId != null) {
            if (currentIterations >= maxIterationsAllowed) throw IllegalStateException("Surpassed ${maxIterationsAllowed} iterations!")
            currentIterations++
            
            val card = cards.find { it.id == nextCardId }
            if (card == null) throw IllegalStateException("Could not find a card with ID ${nextCardId}")
            
            val cardIds = getWonCardIds(card)
            wonCardIds.addAll(cardIds)
            
            nextCardId = wonCardIds.removeFirstOrNull()
            cardCount += cardIds.size
        }
        
        return cardCount
    }

    // test if implementation meets criteria from the description, like:
    testSolution(part1(listOf("Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53")), 8)
    testSolution(part1(listOf("Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19")), 2)
    testSolution(part1(listOf("Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1")), 2)
    testSolution(part1(listOf("Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83")), 1)
    testSolution(part1(listOf("Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36")), 0)
    testSolution(part1(listOf("Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11")), 0)
    
    fun cardCheck(card: Card, expectedWins: List<Int>) {
        val cardIds = getWonCardIds(card)
        if (cardIds != expectedWins) {
            throw IllegalStateException("Expected card ${card.id}'s ${cardIds} to match ${expectedWins}")
        }
    }
    
    cardCheck(Card(id = 1, numbers = listOf(41,48,83,86,17), potentialWinningNumbers = listOf(83, 86,  6, 31, 17,  9, 48, 53)), listOf(2, 3, 4, 5))
    cardCheck(Card(id = 2, numbers = listOf(13,32,20,16,61), potentialWinningNumbers = listOf(61, 30, 68, 82, 17, 32, 24, 19)), listOf(3, 4))
    cardCheck(Card(id = 3, numbers = listOf(1, 21, 53,  59, 44), potentialWinningNumbers = listOf(69, 82, 63, 72, 16, 21, 14,  1)), listOf(4, 5))
    cardCheck(Card(id = 4, numbers = listOf(41, 92, 73,  84, 69), potentialWinningNumbers = listOf(59, 84, 76, 51, 58,  5, 54, 83)), listOf(5))
    cardCheck(Card(id = 5, numbers = listOf(87, 83, 26,  28, 32), potentialWinningNumbers = listOf(88, 30, 70, 12, 93, 22, 82, 36)), listOf())
    cardCheck(Card(id = 6, numbers = listOf(31, 18, 13,  56, 72), potentialWinningNumbers = listOf(74, 77, 10, 23, 35, 67, 36, 11)), listOf())    
    
    testSolution(part1(readInput("Day04_test_p1")), 13)
    testSolution(part2(readInput("Day04_test_p2")), 30)    

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}

fun getWonCardIds(card: Card): List<Int> {
    val wonCardIds = mutableListOf<Int>()
    val cardsWon = card.getWinningNumbers().size
    val startingCardId = card.id + 1
    val endingCardId = startingCardId + cardsWon
    wonCardIds.addAll(startingCardId..<endingCardId)

//    println("Card ${card.id} won ${cardsWon} scratchCards: ${startingCardId..endingCardId}")
    
    return wonCardIds
}

val cardMatcher = Regex("Card\\s+(\\d+):")
data class Card(val id: Int, val numbers: List<Int>, val potentialWinningNumbers: List<Int>) {
    fun getWinningNumbers(): Set<Int> {
        return numbers.intersect(potentialWinningNumbers)
    }
    
    // "The first match makes the card worth one point and each match after the first doubles the point value of that card."
    fun calculateWorth(): Int {
        val winningNumbers = getWinningNumbers()
        
        if (winningNumbers.size <= 1) return winningNumbers.size        
        
        var accumulator = 0
        
        // I'm bad at math so let's do this the old fashioned way.
        winningNumbers.forEach {
            accumulator = if (accumulator == 0) 1 else accumulator * 2
        }
        
        return accumulator
    }
}
fun parseCardRow(row: String): Card {
    val cardId = cardMatcher.find(row).let {
        if (it != null) {
            it.groupValues[1].toInt()
        } else {
            throw IllegalStateException("No ID found for \"${row}\"")
        }        
    }
    
    val parts = row.split(':', '|')
    
    val cardNumbers = parts[1]
        .split(' ')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.trim().toInt() }
    
    val winningNumbers = parts[2].trim()
        .split(' ')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.toInt() }
    
    return Card(
        id = cardId,
        numbers = cardNumbers,
        potentialWinningNumbers = winningNumbers,        
    )
}

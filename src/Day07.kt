fun main() {
    fun part1(input: List<String>): Long {
        val hands = parseHandsAndBids(input)
        val sortedHands = sortHandsStrongestToWeakest(hands)

        return sortedHands.reversed().mapIndexed { index, hand ->
            // 1-based ranks
            val rank = index + 1
            hand.bid * rank
        }.sum()
    }

    fun part2(input: List<String>): Long {
        val hands = parseHandsAndBids(input, jokersWild = true)
        val sortedHands = sortHandsStrongestToWeakest(hands, jokersWild = true)

        return sortedHands.reversed().mapIndexed { index, hand ->
            // 1-based ranks
            val rank = index + 1
            hand.bid * rank
        }.sum()
    }

    testParseHandsAndBids()
    testHandType()
    testHandTypeWithJokersWild()
    testHandSorting()
    testHandSortingWithJokersWild()

    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day07_test_p1")), 6440L)
    testSolution(part2(readInput("Day07_test_p1")), 5905L)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}

private enum class HandType {
    FiveOfAKind,
    FourOfAKind,
    FullHouse,
    ThreeOfAKind,
    TwoPair,
    OnePair,
    HighCard,
}

private data class Hand(val cards: String, val bid: Long, val jokersWild: Boolean) {
    val handType: HandType

    companion object {
        private val fiveOfAKindPattern = listOf(5)
        private val fourOfAKindPattern = listOf(4, 1)
        private val fullHousePattern = listOf(3, 2)
        private val threeOfAKindPattern = listOf(3, 1, 1)
        private val twoPairPattern = listOf(2, 2, 1)
        private val onePairPattern = listOf(2, 1, 1, 1)
        private val highCardPattern = listOf(1, 1, 1, 1, 1)

        fun getHandType(cards: String, jokersWild: Boolean = false): HandType {
            val instances = mutableMapOf<Char, Int>()

            cards.forEach {
                val count = instances.getOrPut(it) { 0 }
                instances[it] = count + 1
            }

            val cardPattern = instances.values.toMutableList()
            cardPattern.sortBy { it }
            cardPattern.reverse()

            if (jokersWild) {
                val jCount = instances.getOrDefault('J', 0)
                // Jokers exist and aren't the only card in the hand (EG, "JJJJJ")
                if (jCount > 0 && cardPattern.size > 1) {
                    cardPattern.remove(jCount)
                    cardPattern[0] = cardPattern[0] + jCount

                }
            }

            return when (cardPattern) {
                fiveOfAKindPattern -> HandType.FiveOfAKind
                fourOfAKindPattern -> HandType.FourOfAKind
                fullHousePattern -> HandType.FullHouse
                threeOfAKindPattern -> HandType.ThreeOfAKind
                twoPairPattern -> HandType.TwoPair
                onePairPattern -> HandType.OnePair
                highCardPattern -> HandType.HighCard
                else -> throw IllegalStateException("Could not match card pattern for ${cards}")
            }
        }
    }
    init {
        handType = Hand.getHandType(cards, jokersWild)
    }
}

private fun parseHandsAndBids(input: List<String>, jokersWild: Boolean = false): List<Hand> {
    return input.map { row ->
        val parts = row.split(' ').map { it.trim() }
        val hand = parts[0]
        val bid = parts[1].toLong()

        Hand(cards = hand, bid = bid, jokersWild = jokersWild)
    }
}

private fun sortHandsStrongestToWeakest(hands: List<Hand>, jokersWild: Boolean = false): List<Hand> {
    // Map strongest to weakest
    fun <T> powerMap(vararg elements: T): Map<T, Int> {
        val map = mutableMapOf<T, Int>()
        val pairs = elements.reversed().mapIndexed { index, element ->
            Pair(element, index)
        }
        pairs.forEach { (key, value) -> map[key] = value }
        return map
    }

    val cardPower = if (jokersWild) {
        powerMap('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J')
    } else {
        powerMap('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')
    }

    val handPower = powerMap(HandType.FiveOfAKind, HandType.FourOfAKind, HandType.FullHouse, HandType.ThreeOfAKind, HandType.TwoPair, HandType.OnePair, HandType.HighCard)
    val sortedHands = hands.toMutableList()

    sortedHands.sortWith { a, b ->
        val aHandPower = handPower.getValue(a.handType)
        val bHandPower = handPower.getValue(b.handType)

        val diff = aHandPower - bHandPower

        if (diff == 0) {
            // Zip the cards up, then return the first diff of card power that isn't a 0.
            // If they're all 0 (same card), then just return 0.
            val newDiff = b.cards.zip(a.cards).map { (bCard, aCard) ->
                val aCardPower = cardPower.getValue(aCard)
                val bCardPower = cardPower.getValue(bCard)

                aCardPower - bCardPower
            }.firstOrNull { it != 0 }
            newDiff ?: 0
        } else {
            diff
        }
    }
    // Make sure results are strongest => weakest instead of weakest => strongest
    return sortedHands.reversed()
}

private fun testParseHandsAndBids() {
    val hands = parseHandsAndBids(readInput("Day07_test_p1"))

    check(hands[0].cards == "32T3K")
    check(hands[0].bid == 765L)

    check(hands[1].cards == "T55J5")
    check(hands[1].bid == 684L)

    check(hands[2].cards == "KK677")
    check(hands[2].bid == 28L)

    check(hands[3].cards == "KTJJT")
    check(hands[3].bid == 220L)

    check(hands[4].cards == "QQQJA")
    check(hands[4].bid == 483L)
}

private fun testHandType() {
    check(Hand.getHandType("AAAAA") == HandType.FiveOfAKind)
    check(Hand.getHandType("AA8AA") == HandType.FourOfAKind)
    check(Hand.getHandType("23332") == HandType.FullHouse)
    check(Hand.getHandType("TTT98") == HandType.ThreeOfAKind)
    check(Hand.getHandType("23432") == HandType.TwoPair)
    check(Hand.getHandType("A23A4") == HandType.OnePair)
    check(Hand.getHandType("23456") == HandType.HighCard)
}

private fun testHandTypeWithJokersWild() {
    fun testHandType(cards: String, expectedHandType: HandType) {
        val handType = Hand.getHandType(cards, jokersWild = true)
        check(handType == expectedHandType) {
            println("For ${cards} expected hand type ${expectedHandType} but got ${handType} instead")
        }
    }

    // From example
    testHandType("32T3K", HandType.OnePair)
    testHandType("KK677", HandType.TwoPair)
    testHandType("T55J5", HandType.FourOfAKind)
    testHandType("KTJJT", HandType.FourOfAKind)
    testHandType("KTJJT", HandType.FourOfAKind)

    testHandType("AAAAA", HandType.FiveOfAKind)
    testHandType("AAAAJ", HandType.FiveOfAKind)
    testHandType("J2222", HandType.FiveOfAKind)
    testHandType("JJJJJ", HandType.FiveOfAKind)
    testHandType("AAAAA", HandType.FiveOfAKind)
    testHandType("AAJJ2", HandType.FourOfAKind)
    testHandType("23456", HandType.HighCard)

}

private fun testHandSorting() {
    val hands = parseHandsAndBids(readInput("Day07_test_p1"))
    val sorted = sortHandsStrongestToWeakest(hands)

    check(sorted[0].cards == "QQQJA")
    check(sorted[1].cards == "T55J5")
    check(sorted[2].cards == "KK677")
    check(sorted[3].cards == "KTJJT")
    check(sorted[4].cards == "32T3K")
}

private fun testHandSortingWithJokersWild() {
    val hands = parseHandsAndBids(readInput("Day07_test_p1"), jokersWild = true)

    // Reverse so we get weakest to strongest
    val sorted1 = sortHandsStrongestToWeakest(hands, jokersWild = true).reversed()

    check(sorted1[0].cards == "32T3K")
    check(sorted1[1].cards == "KK677")
    check(sorted1[2].cards == "T55J5")
    check(sorted1[3].cards == "QQQJA")
    check(sorted1[4].cards == "KTJJT")


    // Tests strongest to weakest
    fun testSort(expectedCardOrder: List<String>) {
        var shuffled = expectedCardOrder.shuffled()
        for (i in 0..10) {
            shuffled = shuffled.shuffled()
        }
        val shuffledHands = shuffled.map { Hand(cards = it, bid = 1L, jokersWild = true)}
        val sortedHands = sortHandsStrongestToWeakest(shuffledHands, jokersWild = true)

        for ((hand, expectedCards) in sortedHands.zip(expectedCardOrder)) {
            check(hand.cards == expectedCards) {
                println("${hand.cards} did not match ${expectedCards}")
            }
        }
    }
    testSort(listOf(
        "AAAAA",
        "AAAAJ",
        "AAAJT",
        "AAJTT",
        "AAJT9",
        "AJT95",
    ))

    testSort(listOf(
        "AAAAA",
        "AAAAJ",
        "J2222",
        "JJJJJ",
        "AAJJ2",
        "23456",
    ))

    testSort(listOf(
        "QQQQ2",
        "JKKK2",
    ))

    testSort(listOf(
        "TJTTT",
        "TJTTJ",
        "J66J6",
    ))

    testSort(listOf(
        "AJAJA",
        "KJKJK",
        "QQQJQ",
        "QQQJJ",
        "QJJJJ",
        "TJTTT",
        "TJTTJ",
        "99J99",
        "JAAAA",
        "JKKKK",
        "J88J8",
        "J8JJJ",
        "J777J",
        "J66J6",
        "JJJJJ",
    ))

}
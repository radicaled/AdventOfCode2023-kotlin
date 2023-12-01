fun main() {
    fun part1(input: List<String>): Int {
        val regex = Regex("\\d")
        val finalValue = input.map {
            val result = regex.findAll(it)
            val stringifiedInt = "${result.first().value}${result.last().value}"
            stringifiedInt.toInt()
        }.sum()

        return finalValue
    }

    fun part2(input: List<String>): Int {
        val extractor = Extractor()

        return input.map {
            val numbers = extractor.extract(it)
            val stringifiedInt = "${numbers.first()}${numbers.last()}"
            stringifiedInt.toInt()
        }.sum()
    }

    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day01_test_p1")), 142)
    testSolution(part2(readInput("Day01_test_p2")), 281)
    testSolution(part2(listOf("seven3oneightp")), 78)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}

// I probably could have fixed the regex but my 'Fu ain't strong enough.
// Need to handle this problem:
// seven3oneeightp
// Regex extracts "seven", "3", "one"
// See that "eight" is missing at the end?
class Extractor {
    val transliteration = mapOf(
        "1" to 1,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "6" to 6,
        "7" to 7,
        "8" to 8,
        "9" to 9,

        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
    )

    fun extract(input: String): List<Int> {
        val results = mutableListOf<Int>()
        input.forEachIndexed { index, char ->        
            transliteration.keys.forEach { key ->
                val endIndex = index + (key.length - 1) // subtract 1 for an inclusive value

                // Impossible match, key length exceeds input size
                if (endIndex >= input.length) return@forEach

                val potentialMatch = input.slice(index..endIndex)
                if (potentialMatch == key) {
                    val mappedValue = transliteration.getValue(potentialMatch)
                    results.add(mappedValue)
                    return@forEach // early exit, no other matches possible
                }
            }
        }

        return results;
    }
}
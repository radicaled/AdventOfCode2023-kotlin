fun main() {
    fun part1(input: List<String>): Long {
        val races = parseRaceInfo(input)

        return races.map {
            val results = findWaysToBeatRecord(it)
            results.size.toLong()
        }.reduce { a, b ->
            (a * b)
        }
    }

    fun part2(input: List<String>): Long {
        val races = parseRaceInfo2(input)
        return races.map {
            val results = findWaysToBeatRecord(it)
            results.size.toLong()
        }.reduce { a, b ->
            (a * b)
        }
    }

    testRaceParsing()
    testRaceParsing2()
    testRaceValues()


    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day06_test_p1")), 288)
    testSolution(part2(readInput("Day06_test_p2")), 71503)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}

data class Race(val distanceToBeat: Long, val raceTime: Long)
data class RaceResult(val distance: Long, val timeCharging: Long)

fun parseRaceInfo(input: List<String>): List<Race> {
    val digit = Regex("\\d+")
    val times = input[0].split(' ').map { it.trim() }.filter { it.matches(digit) }.map { it.toLong() }
    val distances = input[1].split(' ').map { it.trim() }.filter { it.matches(digit) }.map { it.toLong() }

    return times.indices.map {
        Race(distanceToBeat = distances[it], raceTime = times[it])
    }
}

fun parseRaceInfo2(input: List<String>): List<Race> {
    val digit = Regex("\\d+")
    val times = input[0].split(':')
        .map { it.trim().replace(Regex("\\s+"), "") }
        .filter { it.matches(digit) }
        .map { it.toLong() }
    val distances = input[1].split(':')
        .map { it.trim().replace(Regex("\\s+"), "") }
        .filter { it.matches(digit) }
        .map { it.toLong() }

    return times.indices.map {
        Race(distanceToBeat = distances[it], raceTime = times[it])
    }
}

fun findWaysToBeatRecord(race: Race): List<RaceResult> {
    // Brute force it
    return (0..race.raceTime).map { ms ->
        val stepsPerSecond = ms
        val timeLeft = race.raceTime - ms
        // distance travelled
        RaceResult(
            distance = stepsPerSecond * timeLeft,
            timeCharging = ms
        )

    }.filter {
        it.distance > race.distanceToBeat
    }
}

private fun testRaceParsing() {
    val lines = readInput("Day06_test_p1")
    val races = parseRaceInfo(lines)

    check(races[0].raceTime == 7L)
    check(races[0].distanceToBeat == 9L)

    check(races[1].raceTime == 15L)
    check(races[1].distanceToBeat == 40L)

    check(races[2].raceTime == 30L)
    check(races[2].distanceToBeat == 200L)
}

private fun testRaceParsing2() {
    val lines = readInput("Day06_test_p2")
    val races = parseRaceInfo2(lines)

    check(races[0].raceTime == 71530L)
    check(races[0].distanceToBeat == 940200L)
}

private fun testRaceValues() {
    val lines = readInput("Day06_test_p1")
    val races = parseRaceInfo(lines)

    val race1WinningValues = listOf(2L, 3L, 4L, 5L)
    val race2WinningValues = listOf(4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)
    val race3WinningValues = listOf(11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L)

    fun checkRace(testValues: List<Long>, results: List<RaceResult>) {
        val charges = results.map { it.timeCharging }
        check(testValues == charges) {
            println("${testValues} vs ${charges}")
        }
    }

    checkRace(race1WinningValues, findWaysToBeatRecord(races[0]))
    checkRace(race2WinningValues, findWaysToBeatRecord(races[1]))
    checkRace(race3WinningValues, findWaysToBeatRecord(races[2]))
}
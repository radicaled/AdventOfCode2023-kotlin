import kotlin.time.measureTime

val bm = Benchmark()

val timePerSeedRef = "timePerSeed".intern()
val totalTimeRef = "totalTime".intern()
val timePerSDMRef = "timePerSDM".intern()
val timePerAlmanacSectionRef = "timePerAlmanacSection".intern()

fun main() {
    fun part1(input: List<String>): Long {
        val almanac = Almanac.parseAlmanac(input)
        var minLocation = Long.MAX_VALUE
        almanac.seeds.forEach {            
            minLocation = minOf(minLocation, almanac.findSeedLocation(it))
        }        
        
        return minLocation
    }

    fun part2(input: List<String>): Long {
        val almanac = Almanac.parseAlmanac(input)
        var minLocation = Long.MAX_VALUE

        val pairs = getSeedPairs(almanac.seeds)

        bm.enabled = false
        println("Benchmarking enabled: ${bm.enabled}")
        // Too many iterations here -- there's a very specific way of solving this puzzle I haven't found yet.
        pairs.forEachIndexed { index, it ->
            val (start, length) = it
            val seedRange = getSeedRange(start, length)

            println("Seed range #${index + 1} of ${pairs.size}: ${"%,d".format(length)} seeds")

            val sanity = measureTime {
                bm.measure(totalTimeRef) {
                    for (seed in seedRange) {
                        bm.measure(timePerSeedRef) {
                            minLocation = minOf(minLocation, almanac.findSeedLocation(seed))
                        }
                    }
                }
            }

            println("TIME (sanity check) ${sanity.inWholeMilliseconds}ms")
            if (bm.enabled) {
                bm.printResults()
            }
        }
        println("FINAL RESULT")
        return minLocation
    }

    testSourceDestinationMap()
    testParsing()
    testSeedPairs()
    testSeedRange()
    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day05_test_p1")), 35)
    testSolution(part2(readInput("Day05_test_p1")), 46)
    
    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}

fun getSeedPairs(input: List<Long>): List<Pair<Long, Long>> {
    val pairs = mutableListOf<Pair<Long, Long>>()
    for (i in input.indices step 2) {
        pairs.add(Pair(input[i], input[i + 1]))
    }

    return pairs
}

fun testSeedPairs() {
    val seeds = listOf(79L, 14L, 55L, 13L)
    val results = getSeedPairs(seeds)

    check(results.size == 2)
    check(results[0].first == 79L)
    check(results[0].second == 14L)
    check(results[1].first == 55L)
    check(results[1].second == 13L)
}

fun getSeedRange(seedStart: Long, seedLength: Long): LongRange {
    return seedStart..<seedStart + seedLength
}

fun testSeedRange() {
    val range = getSeedRange(79L, 14L)

    check(range.first == 79L)
    check(range.last == 92L)
}

class AlmanacSection(val from: String, val to: String) {
    private val maps = mutableListOf<SourceDestinationMap>()
    private var mapArray: Array<SourceDestinationMap> = arrayOf()

    fun addMap(map: SourceDestinationMap) {
        maps.add(map)
        maps.sortBy { it.source }

        mapArray = maps.toTypedArray()
    }

    fun mapInput(input: Long): Long {
        // Skip iteration if we have SURPASSED THE LIMIT
        if (input > maps.last().sourceEnd) return input

        val result = bm.measure(timePerAlmanacSectionRef) {

            mapArray.asSequence().filter {
                it.contains(input)
            }.firstNotNullOfOrNull { it.mapInput(input) }
        }

        return result ?: input
    }
}

data class SourceDestinationMap(val source: Long, val destination: Long, val rangeLength: Long) {
    val sourceEnd: Long = source + (rangeLength - 1)

    fun contains(input: Long): Boolean {
        return input in source..sourceEnd
    }
    
    fun mapInput(input: Long): Long? {
        return bm.measure(timePerSDMRef) {
            if (!contains(input)) return@measure null

            val offset = input - source

            destination + offset
        }
    }
}

fun testSourceDestinationMap() {
    val sdm = SourceDestinationMap(destination = 50, source = 98, rangeLength = 2)
    val output1 = sdm.mapInput(98)
    val output2 = sdm.mapInput(99)

    check(output1 == 50L) {
        println("${output1} does not == 50")
    }
    
    check(output2 == 51L) {
        println("${output1} does not == 51")
    }

    // Checking for overlaps.
    val sdm1 = SourceDestinationMap(destination =  45, source = 77, rangeLength = 22)
    val sdm2 = SourceDestinationMap(destination =  81, source = 45, rangeLength = 19)
    val sdm3 = SourceDestinationMap(destination =  68, source = 64, rangeLength = 13)

    val overlapCheckList = listOf(sdm1, sdm2, sdm3)

    overlapCheckList.forEach { sdm ->
        val overlap = overlapCheckList
            .filter { it != sdm }
            .filter { sdm.sourceEnd == it.source }
            .firstNotNullOfOrNull { Pair(sdm, it) }

        if (overlap != null) {
            val (a, b) = overlap
            throw IllegalStateException("${a} (${a.source}, ${a.sourceEnd}) overlapped with ${b} (${b.source}, ${b.sourceEnd})")
        }

    }

    val overlappingSdm = overlapCheckList.firstNotNullOfOrNull { sdm ->
        overlapCheckList
            .filter { it != sdm }
            .filter { sdm.sourceEnd == it.source }
            .firstNotNullOfOrNull { Pair(sdm, it) }
    }
    if (overlappingSdm != null) {
        assert(false) {
            println("Overlap detected: ${overlappingSdm.first} <=> ${overlappingSdm.second}")
        }
    }
}

class Almanac(val seeds: List<Long>, sections: List<AlmanacSection>) {
    val sections: List<AlmanacSection>
    
    companion object {
        private val seedsRegex = Regex("^seeds:")
        private val headerRegex = Regex("^(\\w+)-to-(\\w+)\\s+map:")
        private val mapRegex = Regex("^(\\d+)")
        
        fun parseAlmanac(input: List<String>): Almanac {
            val seeds = mutableListOf<Long>()
            val sections = mutableListOf<AlmanacSection>()
            
            var currentSection: AlmanacSection? = null
            for (line in input) {
                val seedMatch = seedsRegex.find(line)
                val headerMatch = headerRegex.find(line)
                val mapMatch = mapRegex.find(line)
                
                if (seedMatch != null) {
                    val newSeeds = line.split(' ')
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .filter { it.matches(Regex("^\\d+$")) }
                        .map { it.toLong() }
                    
                    seeds.addAll(newSeeds)
                } else if (headerMatch != null) {
                    val from = headerMatch.groupValues[1]
                    val to = headerMatch.groupValues[2]
                    
                    currentSection = AlmanacSection(from = from, to = to)
                    sections.add(currentSection)
                } else if (mapMatch != null) {
                    if (currentSection == null) throw IllegalStateException("Encountered a map without a corresponding section!")
                    val values = line.split(' ')
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { it.toLong() }
                    val destination = values[0]
                    val source = values[1]
                    val rangeLength = values[2]
                    
                    val sdm = SourceDestinationMap(destination = destination, source = source, rangeLength = rangeLength)
                    currentSection.addMap(sdm)
                }
            }
            
            val almanac = Almanac(seeds, sections)
            
            return almanac            
        }
    }
    
    init {
        // Gonna make sure the sections are sorted from start to finish
        val sortedSections = mutableListOf<AlmanacSection>()

        val seedSection = sections.first { it.from == "seed" }
        sortedSections.add(seedSection)

        var previousSection: AlmanacSection? = seedSection
        while (previousSection != null) {
            val section = sections.find { it.from == previousSection?.to }

            if (section != null) {
                sortedSections.add(section)
            }

            previousSection = section
        }
        
        this.sections = sortedSections
    }

    fun findSeedLocation(seed: Long): Long {
        var tableValue = seed
        for (section in sections) {
            tableValue = section.mapInput(tableValue)
            if (seed == 82L) {
                println("Seed ${seed}: ${section.to} is ${tableValue}")
            }
        }

        return tableValue
    }
}

fun testParsing() {
    val almanac = Almanac.parseAlmanac(readInput("Day05_test_p1"))
    
    check(almanac.seeds == listOf(79L, 14L, 55L, 13L))
    check(almanac.sections.size == 7)
    check(almanac.sections[0].from == "seed")
    check(almanac.sections[1].from == "soil")
}
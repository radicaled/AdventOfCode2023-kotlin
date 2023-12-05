import kotlin.system.measureTimeMillis

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
        val pairs = almanac.seeds.zipWithNext()
        pairs.forEachIndexed { index, it ->
            println("Working on ${index}")
            val (start, length) = it
            
            var timing = measureTimeMillis {
                var iterations = 0
                for (seed in (start..start+length)) {
                    minLocation = minOf(minLocation, almanac.findSeedLocation(seed))
                    iterations++
                    if (iterations > 1_000_000) break
                } 
            }
            println("Timing for part 2: ${timing}ms")
//            for (seed in (start..start+length)) {
//                minLocation = minOf(minLocation, almanac.findSeedLocation(seed))
//            }
        }        

        return minLocation
    }

    testSourceDestinationMap()
    testParsing()
    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day05_test_p1")), 35)
//    testSolution(part2(readInput("Day05_test_p2")), 281)    
    
    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}

class AlmanacSection(val from: String, val to: String) {
    val maps = mutableListOf<SourceDestinationMap>()
    
    fun mapInput(input: Long): Long {
        val result = maps.firstNotNullOfOrNull { it.mapInput(input) }
        return result ?: input
    }
}

data class SourceDestinationMap(val source: Long, val destination: Long, val rangeLength: Long) {
    
    fun mapInput(input: Long): Long? {
        val isInRange = input >= source && input <= (source + rangeLength)
        
        if (!isInRange) return null
        
        val offset = input - source
        
        return destination + offset        
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
}

data class LookupResult(val type: String, val value: Long)

class Almanac(val seeds: List<Long>, val sections: List<AlmanacSection>) {
    private val sectionMap: Map<String, AlmanacSection>
    
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
                    currentSection.maps.add(sdm)
                }
            }
            
            val almanac = Almanac(seeds, sections)
            
            return almanac            
        }
    }
    
    init {
        val map = mutableMapOf<String, AlmanacSection>()
        sections.forEach {
            map[it.from] = it
        }
        
        sectionMap = map
    }

    fun findSeedLocation(seed: Long): Long {
        var lookupResult = lookup("seed", seed)

        var iterations = 0
        val maxIterations = sections.size
        while (lookupResult != null) {
            if (iterations >= maxIterations) throw IllegalStateException("Too many iterations")
            iterations++

            if (lookupResult.type == "location") return lookupResult.value

            lookupResult = lookup(lookupResult.type, lookupResult.value)
        }

        throw IllegalStateException("Did not find Location")
    }

    fun lookup(type: String, value: Long): LookupResult? {
//        val section = sections.find { it.from == type }
        val section = sectionMap[type] ?: return null
        return LookupResult(
            type = section.to,
            value = section.mapInput(value) 
        )
    }
}

fun testParsing() {
    val almanac = Almanac.parseAlmanac(readInput("Day05_test_p1"))
    
    check(almanac.seeds == listOf(79L, 14L, 55L, 13L))
    check(almanac.sections.size == 7)
    check(almanac.sections[0].from == "seed")
    check(almanac.sections[1].from == "soil")
}
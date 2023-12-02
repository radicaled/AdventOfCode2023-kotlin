fun main() {
    fun part1(input: List<String>): Int {
        val parser = GameParser()
        val gameResults = input.map {
            parser.parse(it)            
        }
//        Determine which games would have been possible if the bag had been loaded with only 12 red cubes, 13 green cubes, and 14 blue cubes.
//        What is the sum of the IDs of those games?
        val state = BagState(listOf(
            CubeSet(count = 12, color = "red"),
            CubeSet(count = 13, color = "green"),
            CubeSet(count = 14, color = "blue"),
        ))
        val possibleGames = findPossibleGames(state, gameResults)

        return possibleGames.sumOf {
            it.id
        }
    }
//    For each game, find the minimum set of cubes that must have been present. What is the sum of the power of these sets?
    fun part2(input: List<String>): Int {
        val parser = GameParser()
        val gameResults = input.map {
            parser.parse(it)            
        }

        return gameResults.sumOf { gr ->
            val minimumCubesRequired = findMinimumNumberOfCubesRequiredForGame(gr)
            // Acquire powers of cubes
            minimumCubesRequired
                    .map { it.count }
                    .reduce { a, b -> a * b }
        }
}

    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day02_test_p1")), 8)
    testSolution(part2(readInput("Day02_test_p2")), 2286)
//    testSolution(part2(listOf("seven3oneightp")), 78)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}

fun findMinimumNumberOfCubesRequiredForGame(game: GameResult): List<CubeSet> {
    val maxCubeMap = mutableMapOf<String, Int>()
    val minCubesRequired = mutableListOf<CubeSet>()
    game.cubeSets.forEach {
        val count = maxCubeMap.getOrDefault(it.color, 0)        
        maxCubeMap[it.color] = maxOf(count, it.count)
    }
    
    return maxCubeMap.entries.map {
        CubeSet(count = it.value, color = it.key)
    }
}

fun findPossibleGames(state: BagState, games: List<GameResult>): List<GameResult> {
    val possibleGames = mutableListOf<GameResult>()
    
    games.forEach { gr ->
        val isValid = gr.cubeSets.all { cs ->            
            val maxCubesInBag = state.bagContents.find { it.color == cs.color }
            if (maxCubesInBag == null) false else cs.count <= maxCubesInBag.count
        }
        
        if (isValid) {
            possibleGames.add(gr)
        }
    }
    
    return possibleGames
    
}

data class BagState(val bagContents: List<CubeSet>)

data class CubeSet(val count: Int, val color: String)
data class GameResult(val id: Int, val cubeSets: List<CubeSet>)

class GameParser {
    private val gameIdRegex = Regex("Game (\\d+):")    
    private val cubeRegex = Regex("(\\d+) (\\w+)")
    
    fun parse(input: String): GameResult {
        val cubeSets = mutableListOf<CubeSet>()
//        val gameIdMatch = gameIdRegex.find(input) ?: throw IllegalStateException()
        val gameIdMatch = gameIdRegex.find(input) ?: throw IllegalStateException()        
        val gameId = gameIdMatch.groupValues[1].toInt()

        // Trim Game ID from input string
        
        val cubeSetsString = input.removeRange(gameIdMatch.range)        
        
        cubeSetsString.split(';').forEach { it ->            
            cubeRegex.findAll(it).forEach {
                val count = it.groupValues[1].toInt()
                val color = it.groupValues[2]
                cubeSets.add(CubeSet(count, color))
            }            
        }
        
        return GameResult(gameId, cubeSets)
    }
}
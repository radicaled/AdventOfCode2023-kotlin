fun main() {
    fun part1(input: List<String>): Int {
        val map = parsePuzzle(input)
        val pathfind = map.pathfind("AAA", "ZZZ")

        return pathfind.stepSizeWithoutStart
    }

    fun part2(input: List<String>): Int {
        return 0
    }

    testPuzzleParsing()
    testPathfinding()

    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day08_test_p1")), 2)
//    testSolution(part2(readInput("Day08_test_p2")), 281)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}

private data class Map(val directions: List<Char>, val nodes: List<Node>) {
    data class Pathfinding(val steps: List<Node>) {
        val stepSizeWithoutStart: Int
            get() = steps.size - 1
    }

    fun pathfind(fromNode: String, toNode: String): Pathfinding {
        val start = nodes.first { it.name == fromNode }
        val nodeWalk = mutableListOf(start)

        var dirIndex = 0
        var node = start

        val maxIterations = 1_000_000
        var currentIterations = 0

        while (node.name != toNode) {
            if (currentIterations >= maxIterations) throw IllegalStateException("Too many iterations to find path")
            currentIterations++

            val nextNodeName = when(val dir = directions[dirIndex]) {
                'L' -> node.left
                'R' -> node.right
                else -> throw IllegalStateException("Unknown direction $dir")
            }

            dirIndex++
            if (dirIndex >= directions.size) {
                dirIndex = 0
            }

            node = nodes.first { it.name == nextNodeName }
            nodeWalk.add(node)
        }

        return Pathfinding(steps = nodeWalk)
    }
}
private data class Node(val name: String, val left: String, val right: String)

private fun parsePuzzle(input: List<String>): Map {
    val nodeExtractor = Regex("^(\\w+) = \\((\\w+), (\\w+)\\)")
    val directions = input[0].toList()
    val nodeLines = input.slice(2..<input.size)

    val nodes = nodeLines.flatMap { nodeLine ->
        nodeExtractor.findAll(nodeLine).map {
            val node = it.groupValues[1]
            val left = it.groupValues[2]
            val right = it.groupValues[3]

            Node(name = node, left = left, right = right)
        }
    }

    return Map(directions = directions, nodes = nodes)
}

private fun testPuzzleParsing() {
    val input = readInput("Day08_test_p1")
    val map = parsePuzzle(input)

    check(map.directions == listOf('R', 'L'))

    fun checkNode(node: Node, name: String, left: String, right: String) {
        check(node.name == name)
        check(node.left == left)
        check(node.right == right)
    }

    checkNode(map.nodes[0], "AAA", "BBB", "CCC")
    checkNode(map.nodes[1], "BBB", "DDD", "EEE")
    checkNode(map.nodes[2], "CCC", "ZZZ", "GGG")
    checkNode(map.nodes[3], "DDD", "DDD", "DDD")
    checkNode(map.nodes[4], "EEE", "EEE", "EEE")
    checkNode(map.nodes[5], "GGG", "GGG", "GGG")
    checkNode(map.nodes[6], "ZZZ", "ZZZ", "ZZZ")
}

private fun testPathfinding() {
    val input = readInput("Day08_test_p1")
    val map = parsePuzzle(input)

    val result = map.pathfind("AAA", "ZZZ")

    check(result.steps.map { it.name } == listOf("AAA", "CCC", "ZZZ"))
    check(result.stepSizeWithoutStart == 2)

    val map2 = Map(
        directions = listOf('L', 'L', 'R'),
        nodes = listOf(
            Node("AAA", "BBB", "BBB"),
            Node("BBB", "AAA", "ZZZ"),
            Node("ZZZ", "ZZZ", "ZZZ"),

        )
    )

    val result2 = map2.pathfind("AAA", "ZZZ")

    check(result2.steps.map { it.name } == listOf("AAA", "BBB", "AAA", "BBB", "AAA", "BBB", "ZZZ"))
    check(result2.stepSizeWithoutStart == 6)
}
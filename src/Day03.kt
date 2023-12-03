fun main() {
    fun part1(input: List<String>): Int {
        val grid = Grid.createFromInput(input)       
        
        return grid.spans.filterIsInstance<PartSpan>().filter { span ->
            var validPart = false
            
            val yRange = maxOf(0, span.y - 1)..minOf(grid.height - 1 , span.y + 1)
            val xRange = maxOf(0, span.x - 1)..minOf(grid.width - 1, span.xRange.last + 1)
            
            for (y in yRange) {
                for (x in xRange) {
                    val neighboringSpan = grid.getSpan(x, y)
                    if (neighboringSpan is SymbolSpan) {
                        validPart = true
                        break
                    }
                }
            }            
            
            validPart
        }.sumOf {
            it.value.toInt()
        }
    }

    // Funnily enough I had a feeling part2 was going to be like this, hence the investment in the Grid class...
    fun part2(input: List<String>): Int {
        val grid = Grid.createFromInput(input)      
        
        return grid.spans.filterIsInstance<SymbolSpan>()
            .filter { span -> span.symbol == '*' }
            .sumOf { span ->
                val yRange = maxOf(0, span.y - 1)..minOf(grid.height - 1, span.y + 1)
                val xRange = maxOf(0, span.x - 1)..minOf(grid.width - 1, span.xRange.last + 1)
                val neighboringPartSpans = mutableSetOf<PartSpan>()
    
                for (y in yRange) {
                    for (x in xRange) {
                        val neighboringSpan = grid.getSpan(x, y)
                        if (neighboringSpan is PartSpan) {
                            neighboringPartSpans.add(neighboringSpan)
                        }
                    }
                }    
                
                if (neighboringPartSpans.size == 2) {
                    neighboringPartSpans
                            .map { it.value.toInt() }
                            .reduce { a, b -> a * b }
                } else {
                    0
                }
        }
    }    

    // test if implementation meets criteria from the description, like:
    testSolution(part1(readInput("Day03_test_p1")), 4361)
    testSolution(part2(readInput("Day03_test_p2")), 467835)
//    testSolution(part2(listOf("seven3oneightp")), 78)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}

sealed class Cell(val character: Char)
class EmptyCell : Cell('.')
class PartCell(digit: Char) : Cell(digit)
class SymbolCell(symbol: Char) : Cell(symbol)

sealed class Span(val x: Int, val y: Int, var length: Int) {
    val xRange: IntRange
        get() = x..x + (length - 1)
    
}
class PartSpan(x: Int, y: Int, length: Int, var value: String) : Span(x = x, y = y, length = length)
class EmptySpan(x: Int, y: Int, length: Int) : Span(x = x, y = y, length = length)
class SymbolSpan(x: Int, y: Int, length: Int, val symbol: Char) : Span(x = x, y = y, length = length)


data class Row(val cells: List<Cell>)

// Maps input into a grid containing cells,
// As well as a higher level concept of spans that can consist of multiple x,y positions.
class Grid(val width: Int, val height: Int, val rows: List<List<Cell>>, val spans: List<Span>) {
    companion object {
        fun createFromInput(input: List<String>): Grid {            
            val width = input.first().length
            val height = input.size
            
            val matrix = mutableListOf<List<Cell>>()
            
            // Convert each character into a "Cell" data structure. 
            input.forEach { row ->
                val rowOfCells = mutableListOf<Cell>()
                
                row.forEachIndexed { index, c ->
                    val cell: Cell = if(c.isDigit()) {
                        PartCell(c)
                    } else if (c == '.') {
                        EmptyCell()
                    } else {
                        SymbolCell(c)
                    }
                    rowOfCells.add(cell)
                }
                matrix.add(rowOfCells)
            }            
            
            // Make another pass to map cells to spans.
            // A span is a single unit that spans multiple X cells -- IE, (0, 0) to (10, 0) can be a single span.
            // Spans only have a single Y location because this got out of control real quick and I didn't want to make it worse.
            val spans = mutableListOf<Span>()
            matrix.forEachIndexed { y, row ->                
                
                row.forEachIndexed { x, cell ->
                    
                    when(cell) {
                        is EmptyCell -> {
                            spans.add(EmptySpan(x = x, y = y, length = 1))
                        }
                        is SymbolCell -> {
                            spans.add(SymbolSpan(x = x, y = y, length = 1, symbol = cell.character))
                        }
                        is PartCell -> {                            
                            val lastSpan = if (x != 0) spans.lastOrNull() else null                         
                            val span = if (lastSpan is PartSpan) {
                                lastSpan
                            } else {
                                val newPartSpan = PartSpan(x = x, y = y, length = 0, value = "")
                                spans.add(newPartSpan)
                                newPartSpan
                            }
                            
                            span.value += cell.character
                            span.length += 1
                        }
                    }
                }
            }
            
            return Grid(width = width, height = height, rows = matrix, spans = spans)
        }
    }
    
    fun getCell(x: Int, y: Int): Cell {
        return rows[y][x]
    }
    
    fun getSpan(x: Int, y: Int): Span {
        return spans.first { span ->            
            span.y == y && span.xRange.contains(x) 
        }
    }
}
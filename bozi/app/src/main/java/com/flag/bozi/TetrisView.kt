package com.flag.bozi.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class TetrisView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val numRows = 20
    private val numCols = 10
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val grid = Array(numRows) { IntArray(numCols) { 0 } }

    private var currentPiece: Tetromino = Tetromino.random()
    private var pieceRow = 0
    private var pieceCol = numCols / 2 - 1

    private var isGameOver = false   // ⚡ добавлен флаг конца игры

    val gameHandler = Handler(Looper.getMainLooper())
    var score = 0
        private set

    var onGameOver: ((Int) -> Unit)? = null  // колбэк в Activity

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!isGameOver) {
                moveDown()
                invalidate()
                gameHandler.postDelayed(this, 500) // скорость падения
            }
        }
    }

    // клетки растягиваются по всей ширине/высоте
    private val cellWidth: Int
        get() = if (width == 0) 1 else width / numCols
    private val cellHeight: Int
        get() = if (height == 0) 1 else height / numRows

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { startGame() } // запуск после отрисовки layout
    }

    fun startGame() {
        gameHandler.removeCallbacks(gameLoop)
        isGameOver = false
        spawnPiece()
        gameHandler.post(gameLoop)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // рисуем поле
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                paint.color = if (grid[r][c] != 0) grid[r][c] else Color.DKGRAY
                canvas.drawRect(
                    (c * cellWidth).toFloat(),
                    (r * cellHeight).toFloat(),
                    ((c + 1) * cellWidth).toFloat(),
                    ((r + 1) * cellHeight).toFloat(),
                    paint
                )
            }
        }

        // рисуем текущую фигуру
        if (!isGameOver) {
            paint.color = currentPiece.color
            for (block in currentPiece.shape) {
                val r = pieceRow + block.first
                val c = pieceCol + block.second
                if (r in 0 until numRows && c in 0 until numCols) {
                    canvas.drawRect(
                        (c * cellWidth).toFloat(),
                        (r * cellHeight).toFloat(),
                        ((c + 1) * cellWidth).toFloat(),
                        ((r + 1) * cellHeight).toFloat(),
                        paint
                    )
                }
            }
        }
    }

    fun moveLeft() {
        if (!isGameOver && canMove(pieceRow, pieceCol - 1)) {
            pieceCol--
            invalidate()
        }
    }

    fun moveRight() {
        if (!isGameOver && canMove(pieceRow, pieceCol + 1)) {
            pieceCol++
            invalidate()
        }
    }

    fun rotatePiece() {
        if (isGameOver) return

        val rotatedShape = currentPiece.shape.map { Pair(it.second, -it.first) }
        val oldShape = currentPiece.shape
        currentPiece = currentPiece.copy(shape = rotatedShape)
        if (!canMove(pieceRow, pieceCol)) {
            currentPiece = currentPiece.copy(shape = oldShape) // откат если не влезает
        }
        invalidate()
    }

    private fun moveDown() {
        if (isGameOver) return

        if (canMove(pieceRow + 1, pieceCol)) {
            pieceRow++
        } else {
            placePiece()
            clearLines()
            spawnPiece()
        }
    }

    private fun canMove(newRow: Int, newCol: Int): Boolean {
        for (block in currentPiece.shape) {
            val r = newRow + block.first
            val c = newCol + block.second
            if (r >= numRows || c < 0 || c >= numCols) return false
            if (r >= 0 && grid[r][c] != 0) return false
        }
        return true
    }

    private fun placePiece() {
        currentPiece.shape.forEach {
            val r = pieceRow + it.first
            val c = pieceCol + it.second
            if (r in 0 until numRows && c in 0 until numCols) {
                grid[r][c] = currentPiece.color
            }
        }
    }

    private fun clearLines() {
        for (r in numRows - 1 downTo 0) {
            if (grid[r].all { it != 0 }) {
                for (row in r downTo 1) {
                    grid[row] = grid[row - 1].clone()
                }
                grid[0] = IntArray(numCols) { 0 }
                score += 100
            }
        }
    }

    private fun spawnPiece() {
        currentPiece = Tetromino.random()
        pieceRow = 0
        pieceCol = numCols / 2 - 1

        // ⛔ Проверка: если новая фигура не помещается — Game Over
        if (!canMove(pieceRow, pieceCol)) {
            isGameOver = true
            gameHandler.removeCallbacks(gameLoop)
            onGameOver?.invoke(score)
        }
    }

    fun restartGame() {
        grid.forEach { it.fill(0) }
        score = 0
        isGameOver = false
        startGame()
        invalidate()
    }
}

// фигуры
data class Tetromino(val shape: List<Pair<Int, Int>>, val color: Int) {
    companion object {
        fun random(): Tetromino {
            val shapes = listOf(
                listOf(Pair(0,0), Pair(0,1), Pair(1,0), Pair(1,1)), // O
                listOf(Pair(0,0), Pair(1,0), Pair(2,0), Pair(3,0)), // I
                listOf(Pair(0,0), Pair(0,1), Pair(1,1), Pair(1,2)), // Z
                listOf(Pair(0,1), Pair(0,2), Pair(1,0), Pair(1,1)), // S
                listOf(Pair(0,0), Pair(1,0), Pair(1,1), Pair(1,2)), // L
                listOf(Pair(0,2), Pair(1,0), Pair(1,1), Pair(1,2)), // J
                listOf(Pair(0,1), Pair(1,0), Pair(1,1), Pair(1,2))  // T
            )
            val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA)
            return Tetromino(shapes.random(), colors.random())
        }
    }
}

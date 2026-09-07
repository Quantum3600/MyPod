package com.trishit.mypod.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.ui.theme.ChicagoFontFamily
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.flow.SharedFlow

enum class CardSuit(val symbol: String, val isRed: Boolean) {
    HEARTS("♥", true),
    DIAMONDS("♦", true),
    CLUBS("♣", false),
    SPADES("♠", false)
}

data class SolitaireCard(
    val suit: CardSuit,
    val value: Int, // 1 = Ace, 11 = Jack, 12 = Queen, 13 = King
    var isFaceUp: Boolean = false
) {
    val displayValue: String = when (value) {
        1 -> "A"
        11 -> "J"
        12 -> "Q"
        13 -> "K"
        else -> value.toString()
    }
}

// Cursor Locations:
// 0: Stock
// 1: Waste
// 2..5: Foundations 0..3
// 6..12: Tableau 0..6
class SolitaireGameState {
    val stock = mutableStateListOf<SolitaireCard>()
    val waste = mutableStateListOf<SolitaireCard>()
    val foundations = List(4) { mutableStateListOf<SolitaireCard>() }
    val tableau = List(7) { mutableStateListOf<SolitaireCard>() }

    var cursorIndex by mutableIntStateOf(0) // 0..12
    var selectedLocation by mutableStateOf<Int?>(null) // index 0..12 of selected source
    var selectedStackCount by mutableIntStateOf(1)

    var score by mutableIntStateOf(0)
    var moves by mutableIntStateOf(0)
    var gameWon by mutableStateOf(false)

    init {
        resetGame()
    }

    fun resetGame() {
        stock.clear()
        waste.clear()
        foundations.forEach { it.clear() }
        tableau.forEach { it.clear() }

        score = 0
        moves = 0
        gameWon = false
        cursorIndex = 0
        selectedLocation = null

        val deck = mutableListOf<SolitaireCard>()
        for (suit in CardSuit.entries) {
            for (v in 1..13) {
                deck.add(SolitaireCard(suit, v, isFaceUp = false))
            }
        }
        deck.shuffle()

        // Deal tableau
        for (col in 0 until 7) {
            for (row in 0..col) {
                val card = deck.removeAt(deck.lastIndex)
                if (row == col) card.isFaceUp = true
                tableau[col].add(card)
            }
        }

        // Remaining deck goes to stock
        stock.addAll(deck)
    }

    fun moveCursor(detents: Int) {
        val total = 13
        var next = cursorIndex + detents
        while (next < 0) next += total
        cursorIndex = next % total
    }

    fun handleSelectPress() {
        val loc = cursorIndex

        // If clicking Stock (loc == 0)
        if (loc == 0) {
            if (stock.isNotEmpty()) {
                val drawn = stock.removeAt(stock.lastIndex)
                drawn.isFaceUp = true
                waste.add(drawn)
                moves++
            } else if (waste.isNotEmpty()) {
                // Recycle waste into stock
                while (waste.isNotEmpty()) {
                    val card = waste.removeAt(waste.lastIndex)
                    card.isFaceUp = false
                    stock.add(card)
                }
                moves++
            }
            selectedLocation = null
            checkWinState()
            return
        }

        // Selecting source card/stack vs target
        val src = selectedLocation
        if (src == null) {
            // Select card at cursor location
            if (canSelectLocation(loc)) {
                selectedLocation = loc
                selectedStackCount = 1
            }
        } else if (src == loc) {
            // Deselect
            selectedLocation = null
        } else {
            // Attempt move from src to loc
            if (executeMove(src, loc)) {
                moves++
                selectedLocation = null
                checkWinState()
            } else {
                // If invalid move, select target location if selectable
                if (canSelectLocation(loc)) {
                    selectedLocation = loc
                    selectedStackCount = 1
                } else {
                    selectedLocation = null
                }
            }
        }
    }

    fun cancelSelection(): Boolean {
        if (selectedLocation != null) {
            selectedLocation = null
            return true // handled cancellation
        }
        return false // let caller handle menu back
    }

    private fun canSelectLocation(loc: Int): Boolean {
        return when (loc) {
            1 -> waste.isNotEmpty()
            in 2..5 -> foundations[loc - 2].isNotEmpty()
            in 6..12 -> tableau[loc - 6].isNotEmpty()
            else -> false
        }
    }

    private fun executeMove(src: Int, dst: Int): Boolean {
        val sourceCard = getTopCardAt(src) ?: return false

        // Destination: Foundation (dst in 2..5)
        if (dst in 2..5) {
            val fIdx = dst - 2
            val fPile = foundations[fIdx]
            if (fPile.isEmpty()) {
                if (sourceCard.value == 1) { // Ace required
                    popCardFrom(src)?.let {
                        fPile.add(it)
                        score += 10
                        revealTableauTop(src)
                        return true
                    }
                }
            } else {
                val topF = fPile.last()
                if (topF.suit == sourceCard.suit && sourceCard.value == topF.value + 1) {
                    popCardFrom(src)?.let {
                        fPile.add(it)
                        score += 10
                        revealTableauTop(src)
                        return true
                    }
                }
            }
            return false
        }

        // Destination: Tableau (dst in 6..12)
        if (dst in 6..12) {
            val tIdx = dst - 6
            val tPile = tableau[tIdx]

            // Moving to empty tableau column requires King
            if (tPile.isEmpty()) {
                if (sourceCard.value == 13) {
                    val cardsToMove = popCardsFromTableau(src, sourceCard)
                    if (cardsToMove.isNotEmpty()) {
                        tPile.addAll(cardsToMove)
                        score += 5
                        revealTableauTop(src)
                        return true
                    }
                }
            } else {
                val topT = tPile.last()
                if (topT.isFaceUp &&
                    topT.suit.isRed != sourceCard.suit.isRed &&
                    sourceCard.value == topT.value - 1) {

                    val cardsToMove = popCardsFromTableau(src, sourceCard)
                    if (cardsToMove.isNotEmpty()) {
                        tPile.addAll(cardsToMove)
                        score += 5
                        revealTableauTop(src)
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun getTopCardAt(loc: Int): SolitaireCard? {
        return when (loc) {
            1 -> waste.lastOrNull()
            in 2..5 -> foundations[loc - 2].lastOrNull()
            in 6..12 -> tableau[loc - 6].lastOrNull { it.isFaceUp } ?: tableau[loc - 6].lastOrNull()
            else -> null
        }
    }

    private fun popCardFrom(loc: Int): SolitaireCard? {
        return when (loc) {
            1 -> if (waste.isNotEmpty()) waste.removeAt(waste.lastIndex) else null
            in 2..5 -> {
                val pile = foundations[loc - 2]
                if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex) else null
            }
            in 6..12 -> {
                val pile = tableau[loc - 6]
                if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex) else null
            }
            else -> null
        }
    }

    private fun popCardsFromTableau(src: Int, startCard: SolitaireCard): List<SolitaireCard> {
        if (src == 1) {
            val card = waste.removeAt(waste.lastIndex)
            return listOf(card)
        }
        if (src in 2..5) {
            val fPile = foundations[src - 2]
            if (fPile.isNotEmpty()) {
                return listOf(fPile.removeAt(fPile.lastIndex))
            }
        }
        if (src in 6..12) {
            val tPile = tableau[src - 6]
            val idx = tPile.indexOf(startCard)
            if (idx >= 0) {
                val moving = tPile.subList(idx, tPile.size).toList()
                repeat(moving.size) { tPile.removeAt(tPile.lastIndex) }
                return moving
            }
        }
        return emptyList()
    }

    private fun revealTableauTop(loc: Int) {
        if (loc in 6..12) {
            val tPile = tableau[loc - 6]
            if (tPile.isNotEmpty() && !tPile.last().isFaceUp) {
                tPile.last().isFaceUp = true
                score += 5
            }
        }
    }

    private fun checkWinState() {
        if (foundations.all { it.size == 13 }) {
            gameWon = true
        }
    }
}

@Composable
fun SolitaireScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState = remember { SolitaireGameState() }
    val textMeasurer = rememberTextMeasurer()

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> gameState.moveCursor(event.detents)
                is WheelEvent.SelectPress -> gameState.handleSelectPress()
                is WheelEvent.MenuPress -> {
                    val handled = gameState.cancelSelection()
                    if (!handled) {
                        onExitGame()
                    }
                }
                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF075E2D)) // Felt green background
    ) {
        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SOLITAIRE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "MOVES: ${gameState.moves}  SCORE: ${gameState.score}",
                fontSize = 10.sp,
                color = Color(0xFFFFD700)
            )
        }

        // Solitaire Board Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
        ) {
            val cardW = (size.width / 7.5f).coerceIn(24f, 48f)
            val cardH = cardW * 1.35f
            val spacing = (size.width - (7 * cardW)) / 8f

            // 1. Draw Stock (loc 0) & Waste (loc 1)
            val stockX = spacing
            val topRowY = spacing

            drawCardPlaceholder(stockX, topRowY, cardW, cardH, isSelected = gameState.selectedLocation == 0, isHovered = gameState.cursorIndex == 0)
            if (gameState.stock.isNotEmpty()) {
                drawCardBack(stockX, topRowY, cardW, cardH)
            } else {
                drawText(textMeasurer, "♻", Offset(stockX + cardW / 3f, topRowY + cardH / 4f), style = TextStyle(color = Color.White, fontSize = 14.sp))
            }

            val wasteX = stockX + cardW + spacing
            drawCardPlaceholder(wasteX, topRowY, cardW, cardH, isSelected = gameState.selectedLocation == 1, isHovered = gameState.cursorIndex == 1)
            gameState.waste.lastOrNull()?.let { card ->
                drawCardFace(card, wasteX, topRowY, cardW, cardH, textMeasurer)
            }

            // 2. Draw Foundations (loc 2..5)
            for (fIdx in 0 until 4) {
                val fLoc = fIdx + 2
                val fX = wasteX + (fIdx + 1.5f) * (cardW + spacing)
                drawCardPlaceholder(fX, topRowY, cardW, cardH, isSelected = gameState.selectedLocation == fLoc, isHovered = gameState.cursorIndex == fLoc)

                val fPile = gameState.foundations[fIdx]
                if (fPile.isNotEmpty()) {
                    drawCardFace(fPile.last(), fX, topRowY, cardW, cardH, textMeasurer)
                } else {
                    val suits = listOf("♥", "♦", "♣", "♠")
                    drawText(textMeasurer, suits[fIdx], Offset(fX + cardW / 3f, topRowY + cardH / 4f), style = TextStyle(color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp))
                }
            }

            // 3. Draw Tableau (loc 6..12)
            val tableauY = topRowY + cardH + (spacing * 1.5f)
            val cardVertOverlap = cardH * 0.28f

            for (tIdx in 0 until 7) {
                val tLoc = tIdx + 6
                val tX = spacing + tIdx * (cardW + spacing)
                val tPile = gameState.tableau[tIdx]

                drawCardPlaceholder(tX, tableauY, cardW, cardH, isSelected = gameState.selectedLocation == tLoc, isHovered = gameState.cursorIndex == tLoc)

                if (tPile.isNotEmpty()) {
                    for (cIdx in tPile.indices) {
                        val card = tPile[cIdx]
                        val cY = tableauY + (cIdx * cardVertOverlap)

                        if (card.isFaceUp) {
                            drawCardFace(card, tX, cY, cardW, cardH, textMeasurer)
                        } else {
                            drawCardBack(tX, cY, cardW, cardH)
                        }
                    }
                }
            }
        }

        if (gameState.gameWon) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "YOU WIN!",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Press CENTER to Play Again",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun DrawScope.drawCardPlaceholder(
    x: Float, y: Float, w: Float, h: Float,
    isSelected: Boolean, isHovered: Boolean
) {
    val color = when {
        isSelected -> Color(0xFF00E5FF)
        isHovered -> Color(0xFFFFD700)
        else -> Color.White.copy(alpha = 0.25f)
    }
    val strokeWidth = if (isSelected || isHovered) 3f else 1.5f

    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawCardBack(x: Float, y: Float, w: Float, h: Float) {
    drawRoundRect(
        color = Color(0xFF1A365D),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(w - 4f, h - 4f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawCardFace(
    card: SolitaireCard,
    x: Float, y: Float, w: Float, h: Float,
    textMeasurer: TextMeasurer
) {
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFFCCCCCC),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1f)
    )

    val textColor = if (card.suit.isRed) Color(0xFFD32F2F) else Color(0xFF212121)
    val text = "${card.displayValue}${card.suit.symbol}"

    drawText(
        textMeasurer = textMeasurer,
        text = text,
        topLeft = Offset(x + 3f, y + 2f),
        style = TextStyle(
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ChicagoFontFamily
        )
    )
}

@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.puzzle

import kotlin.random.Random

/**
 * Steps inside the adult parental gate & simulated store modal matching iOS `ParentalGateModalView`.
 */
enum class ParentalGateStep {
    CHALLENGE,
    STORE_AND_SETTINGS
}

enum class StoreStatusNotice {
    NONE,
    UNLOCKED_ALL,
    RESTORED_PURCHASE,
    NO_PRIOR_PURCHASE,
    RELOCKED,
    PROGRESS_RESET
}

/**
 * Adult-verification addition challenge matching iOS `ParentalChallenge` (`12..28 + 11..24`).
 */
data class ParentalChallenge(
    val leftOperand: Int,
    val rightOperand: Int,
    val options: List<Int>
) {
    val correctAnswer: Int
        get() = leftOperand + rightOperand

    fun isCorrect(candidate: Int): Boolean = candidate == correctAnswer

    companion object {
        fun generate(random: Random = Random.Default): ParentalChallenge {
            val left = random.nextInt(12, 29)
            val right = random.nextInt(11, 25)
            val answer = left + right
            val candidates = linkedSetOf(answer)
            val deltas = listOf(-10, -3, -2, 2, 3, 10, -4, 4)
            for (delta in deltas.shuffled(random)) {
                if (candidates.size >= 4) break
                val option = (answer + delta).coerceAtLeast(10)
                if (option != answer) {
                    candidates.add(option)
                }
            }
            while (candidates.size < 4) {
                candidates.add(answer + candidates.size + 1)
            }
            return ParentalChallenge(
                leftOperand = left,
                rightOperand = right,
                options = candidates.shuffled(random)
            )
        }
    }
}

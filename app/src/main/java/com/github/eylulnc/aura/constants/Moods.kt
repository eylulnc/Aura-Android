package com.github.eylulnc.aura.constants

import androidx.annotation.RawRes
import com.github.eylulnc.aura.R

data class MoodFace(
    val id: Int,
    val key: String,
    val label: String,
    val sub: String,
    val color: String, // hex
    @RawRes val svgRes: Int,
    @RawRes val darkSvgRes: Int
)

val MOODS: List<MoodFace> = listOf(
    MoodFace(1,  "angry",      "Angry",      "frustrated, mad",       "#C0392B", R.raw.mood_angry,      R.raw.mood_angry_dark),
    MoodFace(2,  "exhausted",  "Exhausted",  "drained, no fuel",      "#7F77DD", R.raw.mood_exhausted,  R.raw.mood_exhausted_dark),
    MoodFace(3,  "tired",      "Tired",      "sleepy, worn out",      "#9B8EC4", R.raw.mood_tired,      R.raw.mood_tired_dark),
    MoodFace(4,  "overwhelmed","Overwhelmed","too much, stressed",    "#9B2B4A", R.raw.mood_overwhelmed,R.raw.mood_overwhelmed_dark),
    MoodFace(5,  "anxious",    "Anxious",    "worried, on edge",      "#D85A30", R.raw.mood_anxious,    R.raw.mood_anxious_dark),
    MoodFace(6,  "sad",        "Sad",        "down, low",             "#378ADD", R.raw.mood_sad,        R.raw.mood_sad_dark),
    MoodFace(7,  "meh",        "Meh",        "flat, indifferent",     "#888780", R.raw.mood_meh,        R.raw.mood_meh_dark),
    MoodFace(8,  "calm",       "Calm",       "peaceful, at ease",     "#1D9E75", R.raw.mood_calm,       R.raw.mood_calm_dark),
    MoodFace(9,  "good",       "Good",       "content, doing well",   "#639922", R.raw.mood_good,       R.raw.mood_good_dark),
    MoodFace(10, "energised",  "Energised",  "motivated, driven",     "#F07D20", R.raw.mood_energised,  R.raw.mood_energised_dark),
    MoodFace(11, "happy",      "Happy",      "joyful, warm",          "#EF9F27", R.raw.mood_happy,      R.raw.mood_happy_dark),
    MoodFace(12, "excited",    "Excited",    "thrilled, buzzing",     "#E91E8C", R.raw.mood_excited,    R.raw.mood_excited_dark),
    MoodFace(13, "loved",      "Loved",      "grateful, warm inside", "#E87AAE", R.raw.mood_loved,      R.raw.mood_loved_dark),
)

fun getMoodFace(id: Int): MoodFace = MOODS.first { it.id == id }

val MOOD_NEGATIVE: List<MoodFace> = MOODS.filter { it.id in 1..6 }
val MOOD_NEUTRAL: List<MoodFace>  = MOODS.filter { it.id == 7 }
val MOOD_POSITIVE: List<MoodFace> = MOODS.filter { it.id in 8..13 }
val POSITIVE_MOOD_IDS: Set<Int>   = MOOD_POSITIVE.map { it.id }.toSet()

val MOOD_GROUPS = object {
    val negative = MOOD_NEGATIVE
    val neutral  = MOOD_NEUTRAL
    val positive = MOOD_POSITIVE
}

// Ordered from most negative → most positive for the log slider
val MOOD_SLIDER_ORDER: List<MoodFace> = listOf(1, 4, 5, 6, 2, 3, 7, 8, 9, 10, 11, 12, 13)
    .map { id -> MOODS.first { it.id == id } }

// Maps mood ID → sentiment index (0 = most negative, 12 = most positive)
val MOOD_SENTIMENT: Map<Int, Int> = MOOD_SLIDER_ORDER
    .mapIndexed { index, face -> face.id to index }
    .toMap()

// Maps mood ID → valence score for the trend chart (–2 to +2)
val MOOD_VALENCE: Map<Int, Float> = mapOf(
    1 to -2f,    // angry
    2 to -0.5f,  // exhausted
    3 to -0.5f,  // tired
    4 to -1.5f,  // overwhelmed
    5 to -1f,    // anxious
    6 to -1f,    // sad
    7 to 0f,     // meh
    8 to 1f,     // calm
    9 to 1f,     // good
    10 to 1.5f,  // energised
    11 to 2f,    // happy
    12 to 2f,    // excited
    13 to 2f,    // loved
)

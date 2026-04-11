package com.github.eylulnc.aura.constants

import androidx.annotation.RawRes
import com.github.eylulnc.aura.R

data class MoodFace(
    val id: Int,
    val key: String,
    val label: String,
    val sub: String,
    val color: String, // hex
    @RawRes val svgRes: Int
)

val MOODS: List<MoodFace> = listOf(
    MoodFace(1,  "angry",      "Angry",      "frustrated, mad",      "#C0392B", R.raw.mood_angry),
    MoodFace(2,  "exhausted",  "Exhausted",  "drained, no fuel",     "#7F77DD", R.raw.mood_exhausted),
    MoodFace(3,  "tired",      "Tired",      "sleepy, worn out",     "#9B8EC4", R.raw.mood_tired),
    MoodFace(4,  "overwhelmed","Overwhelmed","too much, stressed",   "#9B2B4A", R.raw.mood_overwhelmed),
    MoodFace(5,  "anxious",    "Anxious",    "worried, on edge",     "#D85A30", R.raw.mood_anxious),
    MoodFace(6,  "sad",        "Sad",        "down, low",            "#378ADD", R.raw.mood_sad),
    MoodFace(7,  "meh",        "Meh",        "flat, indifferent",    "#888780", R.raw.mood_meh),
    MoodFace(8,  "calm",       "Calm",       "peaceful, at ease",    "#1D9E75", R.raw.mood_calm),
    MoodFace(9,  "good",       "Good",       "content, doing well",  "#639922", R.raw.mood_good),
    MoodFace(10, "energised",  "Energised",  "motivated, driven",    "#F07D20", R.raw.mood_energised),
    MoodFace(11, "happy",      "Happy",      "joyful, warm",         "#EF9F27", R.raw.mood_happy),
    MoodFace(12, "excited",    "Excited",    "thrilled, buzzing",    "#E91E8C", R.raw.mood_excited),
    MoodFace(13, "loved",      "Loved",      "grateful, warm inside","#E87AAE", R.raw.mood_loved),
)

fun getMoodFace(id: Int): MoodFace = MOODS.first { it.id == id }

val MOOD_GROUPS = object {
    val negative = MOODS.filter { it.id in 1..6 }
    val neutral   = MOODS.filter { it.id == 7 }
    val positive  = MOODS.filter { it.id in 8..13 }
}

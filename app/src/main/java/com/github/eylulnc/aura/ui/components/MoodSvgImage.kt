package com.github.eylulnc.aura.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.caverock.androidsvg.SVG
import com.github.eylulnc.aura.constants.MoodFace

@Composable
fun MoodSvgImage(
    moodFace: MoodFace,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val svg = remember(moodFace.svgRes) {
        SVG.getFromResource(context, moodFace.svgRes)
    }

    Canvas(modifier = modifier) {
        drawIntoCanvas { canvas ->
            svg.documentWidth = size.width
            svg.documentHeight = size.height
            svg.renderToCanvas(canvas.nativeCanvas)
        }
    }
}

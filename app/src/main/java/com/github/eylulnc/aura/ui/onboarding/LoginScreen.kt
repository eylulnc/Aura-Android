package com.github.eylulnc.aura.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG
import com.github.eylulnc.aura.R
import com.github.eylulnc.aura.ui.settings.SettingsViewModel
import com.github.eylulnc.aura.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: SettingsViewModel,
    onComplete: () -> Unit
) {
    val colors = auraColors
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val signInError by viewModel.signInError.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    LaunchedEffect(currentUser, isSyncing) {
        if (currentUser != null && !isSyncing) {
            viewModel.completeOnboarding()
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(Spacing.loginIconSize)
                .shadow(
                    elevation = Spacing.loginIconShadowElevation,
                    shape = RoundedCornerShape(Spacing.loginIconRadius),
                    ambientColor = colors.accent.copy(alpha = 0.6f),
                    spotColor = colors.accent.copy(alpha = 0.8f)
                )
                .clip(RoundedCornerShape(Spacing.loginIconRadius))
                .background(colors.surface)
        ) {
            val svg = remember {
                SVG.getFromResource(context, R.raw.ic_aura_wave)
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    svg.documentWidth = size.width
                    svg.documentHeight = size.height
                    svg.renderToCanvas(canvas.nativeCanvas)
                }
            }
        }

        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.app_name).uppercase(),
            fontFamily = FontFamily.Serif,
            fontSize = FontSize.xl,
            fontWeight = FontWeight.Normal,
            color = colors.textPrimary,
            letterSpacing = TextUnit(10f, TextUnitType.Sp)
        )

        Spacer(Modifier.height(Spacing.s))

        Text(
            text = stringResource(R.string.login_subtitle),
            fontFamily = FontFamily.Serif,
            fontSize = FontSize.s,
            color = colors.accent,
            letterSpacing = TextUnit(3f, TextUnitType.Sp)
        )

        Spacer(Modifier.height(Spacing.l))

        Box(
            modifier = Modifier
                .width(Spacing.loginDividerWidth)
                .height(Spacing.borderWidth)
                .background(colors.accent.copy(alpha = 0.3f))
        )

        Spacer(Modifier.height(Spacing.l))

        Text(
            text = stringResource(R.string.login_description),
            fontSize = FontSize.s,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = FontSize.l
        )

        Spacer(Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Spacing.radiusPill))
                .background(colors.accent)
                .clickable(enabled = !isSyncing) { viewModel.signIn(context) }
                .padding(vertical = Spacing.l)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    color = colors.background,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(FontSize.m.value.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.login_sign_in),
                    fontSize = FontSize.m,
                    fontWeight = FontWeight.Medium,
                    color = colors.background
                )
            }
        }

        Spacer(Modifier.height(Spacing.m))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.completeOnboarding()
                    onComplete()
                }
                .padding(vertical = Spacing.m)
        ) {
            Text(
                text = stringResource(R.string.login_guest),
                fontSize = FontSize.m,
                color = colors.textSecondary
            )
        }

        Spacer(Modifier.height(Spacing.xxl + Spacing.xxl))
    }

    signInError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSignInError() },
            title = { Text(stringResource(R.string.settings_sign_in_error_title)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSignInError() }) {
                    Text("OK", color = colors.accent)
                }
            },
            containerColor = colors.surface
        )
    }
}

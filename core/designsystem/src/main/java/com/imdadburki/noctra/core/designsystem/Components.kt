package com.imdadburki.noctra.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Section heading used across all feature screens. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = NoctraColors.Text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 20.dp, bottom = 8.dp)
    )
}

/** Primary gold button. onClick is last so trailing-lambda calls work. */
@Composable
fun NoctraButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = NoctraColors.Gold,
            contentColor = NoctraColors.Bg,
            disabledContainerColor = NoctraColors.Elevated,
            disabledContentColor = NoctraColors.Muted
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/** Centred loading state. */
@Composable
fun LoadingState(message: String = "Loading…") {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = NoctraColors.Gold)
        Text(message, color = NoctraColors.Secondary)
    }
}

/** Centred empty state with a plain explanation. */
@Composable
fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, color = NoctraColors.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(body, color = NoctraColors.Secondary, fontSize = 14.sp)
    }
}

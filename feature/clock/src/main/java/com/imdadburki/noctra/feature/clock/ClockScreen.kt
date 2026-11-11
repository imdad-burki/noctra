package com.imdadburki.noctra.feature.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imdadburki.noctra.core.clock.AnalogClockView
import com.imdadburki.noctra.core.clock.ClockTime
import com.imdadburki.noctra.core.clock.DigitalClockView
import com.imdadburki.noctra.core.common.ClockFace
import com.imdadburki.noctra.core.common.ClockKind
import com.imdadburki.noctra.core.common.DigitalLayout
import com.imdadburki.noctra.core.common.allClockStyles
import com.imdadburki.noctra.core.datastore.SettingsStore
import com.imdadburki.noctra.core.designsystem.NoctraColors
import kotlinx.coroutines.launch

/**
 * Clocks v2 — the 150-clock catalogue as a big-preview grid.
 * Every card shows the REAL clock rendered by the engine at a big size,
 * not a tiny thumbnail. Tap a card to make it your clock (saved as the
 * default — Home and the Live Wallpaper follow it).
 */
@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val store = SettingsStore(context)
    val scope = rememberCoroutineScope()
    val selectedName by store.defaultClockName.collectAsState(initial = "Solid Standard Gold")
    val is24Hour by store.is24Hour.collectAsState(initial = false)
    val showSeconds by store.showSeconds.collectAsState(initial = true)
    val showDate by store.showDate.collectAsState(initial = true)

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") } // All | Digital | Analog
    val now = remember { ClockTime.now() }

    val all = remember { allClockStyles() }
    val shown = all.filter { face ->
        (filter == "All" || (filter == "Digital") == (face.kind == ClockKind.DIGITAL)) &&
            (query.isBlank() || face.displayName.contains(query, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoctraColors.Bg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("150 Clocks", color = NoctraColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "Each one different — tap to make it yours",
            color = NoctraColors.Secondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search — neon, flip, tall, roman…", color = NoctraColors.Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NoctraColors.Text,
                unfocusedTextColor = NoctraColors.Text,
                focusedBorderColor = NoctraColors.Gold,
                unfocusedBorderColor = NoctraColors.Border,
                cursorColor = NoctraColors.Gold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (f in listOf("All", "Digital", "Analog")) {
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NoctraColors.Gold,
                        selectedLabelColor = NoctraColors.Bg,
                        containerColor = NoctraColors.Surface,
                        labelColor = NoctraColors.Text
                    )
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Format toggles — apply to the selected clock.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleMini("24-hour", is24Hour) { scope.launch { store.set24Hour(it) } }
            ToggleMini("Seconds", showSeconds) { scope.launch { store.setShowSeconds(it) } }
            ToggleMini("Date", showDate) { scope.launch { store.setShowDate(it) } }
        }
        Spacer(Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shown, key = { it.displayName }) { face ->
                ClockCard(
                    face = face.copy(
                        is24Hour = is24Hour, showSeconds = showSeconds, showDate = showDate
                    ),
                    time = now,
                    selected = face.displayName == selectedName,
                    onClick = { scope.launch { store.setDefaultClockName(face.displayName) } }
                )
            }
        }
    }
}

@Composable
private fun ToggleMini(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = NoctraColors.Text, fontSize = 13.sp)
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NoctraColors.Bg,
                checkedTrackColor = NoctraColors.Gold,
                uncheckedThumbColor = NoctraColors.Secondary,
                uncheckedTrackColor = NoctraColors.Elevated
            )
        )
    }
}

@Composable
private fun ClockCard(face: ClockFace, time: ClockTime, selected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NoctraColors.Elevated else NoctraColors.Surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, NoctraColors.Gold)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big live preview — the real engine, not a thumbnail.
            // Aspect follows the layout: TALL clocks get a portrait card,
            // otherwise the digits drown in empty sides.
            val previewAspect = when (face.kind) {
                ClockKind.DIGITAL -> when (DigitalLayout.fromName(face.digitalLayout)) {
                    DigitalLayout.TALL -> 0.62f
                    DigitalLayout.STACKED -> 0.9f
                    else -> 1.6f
                }
                else -> 1f
            }
            when (face.kind) {
                ClockKind.DIGITAL -> DigitalClockView(
                    face, time,
                    Modifier.fillMaxWidth().aspectRatio(previewAspect)
                )
                else -> AnalogClockView(
                    face, time,
                    Modifier.fillMaxWidth().aspectRatio(1f)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                face.displayName,
                color = if (selected) NoctraColors.Gold else NoctraColors.Text,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (selected) {
                Text("✓ Your clock", color = NoctraColors.Gold, fontSize = 11.sp)
            }
        }
    }
}

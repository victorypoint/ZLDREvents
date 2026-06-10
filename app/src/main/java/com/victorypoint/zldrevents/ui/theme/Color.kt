package com.victorypoint.zldrevents.ui.theme

import androidx.compose.ui.graphics.Color

val ZldrOrange = Color(0xFFFF6B00)
val ZldrDarkOrange = Color(0xFFCC5500)
val ZldrBlue = Color(0xFF1565C0)
val ZldrSurface = Color(0xFF1A1A2E)
val ZldrBackground = Color(0xFF121212)

// Day-of-week background colors for the event list (index 0=Monday … 6=Sunday).
// Derived from vivid hues at ~60% desaturation toward luminance-grey.
val EventDayColors = listOf(
    Color(0xFF3B4867),  // Monday    — muted blue
    Color(0xFF32513B),  // Tuesday   — muted green
    Color(0xFF543F6B),  // Wednesday — muted purple
    Color(0xFF3D5C5C),  // Thursday  — muted teal
    Color(0xFF6E5D42),  // Friday    — muted amber
    Color(0xFF6B3F56),  // Saturday  — muted rose
    Color(0xFF603434),  // Sunday    — muted crimson
)

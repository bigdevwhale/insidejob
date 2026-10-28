package app.insidejob.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.insidejob.R

/** A heist: blueprint blue, the safecracker's orange, red string on the board, a velvet tray for the loot. */
@Immutable
object C {
    val navy = Color(0xFF0F2C4F)
    val navy2 = Color(0xFF163B66)
    val grid = Color(0xFF24507F)
    val line = Color(0xFFD6E7FF)
    val line2 = Color(0xFF8FB4E0)
    val orange = Color(0xFFFF5A1F)
    val red = Color(0xFFD7262E)
    val laser = Color(0xFFFF2D3A)
    val mustard = Color(0xFFF2B134)
    val cyan = Color(0xFF59D0FF)
    val violet = Color(0xFF9C7BD8)
    val paper = Color(0xFFF1E9D6)
    val paper2 = Color(0xFFE4D8BD)
    val cream = Color(0xFFFFF8EA)
    val ink = Color(0xFF141414)
    val ink2 = Color(0xFF4A4034)
    val muted = Color(0xFF7A6A4F)
    val desk = Color(0xFF2A2420)
    val deskLine = Color(0xFF4A3F35)
    val velvet = Color(0xFF3A0F1C)
    val velvet2 = Color(0xFF5A1A2C)
    val rose = Color(0xFFD7A3AE)
    val gold = Color(0xFFE8B84A)
    val steel = Color(0xFFC9CFD6)
    val bar = Color(0xFF0A1424)
    val scrim = Color(0xB3050A12)
}

/** PT Sans Narrow Bold for the big heist-movie titles. */
val Title = FontFamily(Font(R.font.pt_sans_narrow_bold, FontWeight.Bold))
/** IBM Plex Mono: the blueprint labels and body text. */
val Mono = FontFamily(Font(R.font.plex_mono, FontWeight.Normal))
/** PT Mono: the typewriter in the dossiers. */
val Type = FontFamily(Font(R.font.pt_mono, FontWeight.Normal))
/** Caveat: pencil notes on the plans. */
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Normal))

object T {
    val Big = TextStyle(fontFamily = Title, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 36.sp, color = Color.White)
    val Head = TextStyle(fontFamily = Title, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 26.sp, color = Color.White)
    val Sub = TextStyle(fontFamily = Title, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 21.sp, color = Color.White)
    val Body = TextStyle(fontFamily = Mono, fontSize = 13.5.sp, lineHeight = 20.sp, color = C.line)
    val Small = TextStyle(fontFamily = Mono, fontSize = 11.5.sp, lineHeight = 16.sp, color = C.line2)
    val Kicker = TextStyle(fontFamily = Mono, fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 2.2.sp, color = C.line2)
    val Typed = TextStyle(fontFamily = Type, fontSize = 14.sp, lineHeight = 21.sp, color = C.ink)
    val Note = TextStyle(fontFamily = Hand, fontSize = 22.sp, lineHeight = 22.sp, color = C.mustard)
    val Button = TextStyle(fontFamily = Title, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 19.sp, letterSpacing = 2.4.sp)
}

/** The same styles on paper: dossiers, sheets and the crew's board. */
object P {
    val Big = T.Big.copy(color = C.ink)
    val Head = T.Head.copy(color = C.ink)
    val Sub = T.Sub.copy(color = C.ink)
    val Body = T.Body.copy(color = C.ink)
    val Small = T.Small.copy(color = C.ink2)
    val Kicker = T.Kicker.copy(color = C.muted)
    val Note = T.Note.copy(color = C.red)
}

@Composable
fun JobTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.orange, onPrimary = C.ink, background = C.navy, surface = C.navy, onSurface = C.line,
            onBackground = C.line, outline = C.line2, error = C.red,
        ),
        content = content,
    )
}

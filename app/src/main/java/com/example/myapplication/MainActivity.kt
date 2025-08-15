package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.size // Removed size(50.dp)
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
// import androidx.compose.runtime.mutableStateOf // Already using mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.time.OffsetDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // val time = OffsetDateTime.now(TimeZone.getDefault().toZoneId()) // Not used directly here
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Clockface()
                }
            }
        }
    }
}


@Composable
fun Clockface(modifier: Modifier = Modifier.fillMaxSize()) {
    var x by remember { mutableDoubleStateOf(0.0) }
    var y by remember { mutableDoubleStateOf(0.0) }
    var minX by remember { mutableDoubleStateOf(0.0) }
    var minY by remember { mutableDoubleStateOf(0.0) }
    var hX by remember { mutableDoubleStateOf(0.0) }
    var hY by remember { mutableDoubleStateOf(0.0) }


    LaunchedEffect(true) {
        while (true) { // Added a loop to continuously update
            delay(1000)
            val now = OffsetDateTime.now()
            val timeInSeconds = now.second // More descriptive variable name
            val timeMinutes = now.minute
            val timeHours = now.hour
            // Ensure x and y correctly represent clock hand positions
            // For a second hand:
            // x = sin(angle)
            // y = -cos(angle) (because y is typically positive downwards in Compose)
            val angle = (PI * timeInSeconds) / 30.0 // 60 seconds in a circle, 2*PI radians
            x = sin(angle)
            y = -cos(angle)

            val minAngle = (PI * timeMinutes) / 30.0
            minX = sin(minAngle)
            minY = -cos(minAngle)

            // Adjust hour hand for 12-hour format and fractional movement
            val hAngle = (PI * ((timeHours % 12) / 6.0 + timeMinutes / 360.0))
            hX = sin(hAngle)
            hY = -cos(hAngle)
        }
    }
    Surface(
        shape = CircleShape, // This shape applies to the Surface, not necessarily the drawing area
        modifier = modifier // Use the passed modifier
            .padding(16.dp) // Keep padding if desired for the Surface itself
            .drawBehind {
                // The red circle is drawn behind the Canvas content
                drawCircle(
                    color = Color.Red,
                    radius = min(
                        size.width,
                        size.height
                    ) / 2 * 0.9f, // Example: 90% of available radius
                    center = Offset((size.width / 2), (size.height / 2))
                )
            }
    ) {
        // Draw dial background circle
        Background(modifier)
        // Clockface content goes here
        Dials(modifier)
        // Hour hand (shorter)
        Hand(hX, hY, modifier, strokeWidth = 15f, primary = Color.Black, secondary = Color.Black, lengthFactor = 0.5f)
        // Minute hand (medium length)
        Hand(minX, minY, modifier, strokeWidth = 10f, primary = Color.Black, secondary = Color.Black, lengthFactor = 0.8f)
        // Second hand (full length)
        Hand(x, y, modifier, strokeWidth = 5f, primary = Color.Red, secondary = Color.Red, lengthFactor = 0.9f) // Example: slightly shorter than full radius for aesthetics

    }
}

@Composable
fun Background(modifier: Modifier,
               primary: Color = Color.White,
               secondary: Color = Color.White,
               brushColors: List<Color> = listOf(
                   primary,
                   secondary,
               ),
               brush: Brush = Brush.linearGradient(brushColors)) {
    Canvas(modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = min(canvasWidth, canvasHeight) / 2
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        drawCircle(brush, radius = radius, center = Offset(centerX, centerY))
    }
}

@Composable
fun Dials(
    modifier: Modifier,
    primary: Color = MaterialTheme.colorScheme.primary, // Unused for numerals
    secondary: Color = MaterialTheme.colorScheme.secondary, // Unused for numerals
    brushColors: List<Color> = listOf( // Unused for numerals
                  primary,
                  secondary,
              ),
    brush: Brush = Brush.linearGradient(brushColors)) { // Unused for numerals
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val mainRadius = min(canvasWidth, canvasHeight) / 2f
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        val numbersRadiusFactor = 0.8f // Place numbers at 80% of the main radius
        val numbersRadius = mainRadius * numbersRadiusFactor
        val numeralFontSize = 20.sp // Reduced font size
        val numeralTextStyle = TextStyle(
            fontSize = numeralFontSize,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.None
        )

        for (i in 0..11) {
            val hour = i + 1
            val numeralText = hour.toString()

            // Angle calculation: (hour - 3.0) makes 3 o'clock angle 0.
            // Angle increases counter-clockwise. PI/6.0 for each hour.
            // cos for x, sin for y (standard Cartesian, y positive downwards in Compose)
            val angle = (hour - 3.0) * PI / 6.0

            val textLayoutResult = textMeasurer.measure(
                text = numeralText,
                style = numeralTextStyle
            )
            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height

            val textCenterX = centerX + (numbersRadius * cos(angle)).toFloat()
            val textCenterY = centerY + (numbersRadius * sin(angle)).toFloat()

            val actualTopLeftX = textCenterX - textWidth / 2f
            val actualTopLeftY = textCenterY - textHeight / 2f

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(actualTopLeftX, actualTopLeftY)
            )
        }
    }
}

@Composable
fun Hand(
    x: Double,
    y: Double,
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    secondary: Color = MaterialTheme.colorScheme.secondary,
    brushColors: List<Color> = listOf(
        primary,
        secondary,
    ),
    brush: Brush = Brush.linearGradient(brushColors),
    strokeWidth: Float = 10f,
    lengthFactor: Float = 1.0f // New parameter for length adjustment
) { // Accept modifier

    Canvas(modifier.fillMaxSize()) { // Canvas fills the Surface
        val canvasWidth = size.width
        val canvasHeight = size.height
        // Apply lengthFactor to the radius
        val radius = (min(canvasWidth, canvasHeight) / 2) * lengthFactor.coerceIn(0f, 1f)


        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        val endX = centerX + (x * radius).toFloat()
        val endY = centerY + (y * radius).toFloat()

        drawLine(
            brush = brush,
            start = Offset(centerX, centerY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Clockface()
    }
}

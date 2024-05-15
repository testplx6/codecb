package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MemoryTrainingApp()
            }
        }
    }
}

@Composable
fun MemoryTrainingApp() {
    // State variables
    var sequence by remember { mutableStateOf(generateRandomSequence()) }
    var input by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var isMemorizing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(true) }
    val bestScores = remember { mutableStateMapOf<Int, Long>() }
    var lastScore by remember { mutableStateOf(0L) }

    // Focus requester
    val focusRequester = remember { FocusRequester() }

    // Update elapsed time during memorization phase
    LaunchedEffect(isMemorizing) {
        if (isMemorizing) {
            startTime = System.currentTimeMillis()
            while (isMemorizing) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(50)
            }
        }
    }

    // Manage focus during state changes
    LaunchedEffect(isMemorizing, showResult, showInstructions) {
        if (!isMemorizing && !showResult && !showInstructions) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable {
                when {
                    showInstructions -> {
                        showInstructions = false
                        isMemorizing = true
                        startTime = System.currentTimeMillis()
                    }
                    showResult -> {
                        sequence = generateRandomSequence()
                        isMemorizing = true
                        startTime = System.currentTimeMillis()
                        input = ""
                        showResult = false
                    }
                    isMemorizing -> {
                        isMemorizing = false
                    }
                    else -> {
                        showResult = true
                        val correctDigits = input.zip(sequence).count { it.first == it.second }
                        lastScore = elapsedTime
                        if (correctDigits > 0 && (bestScores[correctDigits] == null || elapsedTime < bestScores[correctDigits]!!)) {
                            bestScores[correctDigits] = elapsedTime
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (showInstructions) {
            // Display instructions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to Memory Training!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap the screen to start memorizing a sequence of 16 digits.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "When you're ready to recall, tap the screen again and enter the sequence.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Correct digits will be shown in green, incorrect in red. Tap the screen to play again.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isMemorizing) {
                    // Memorization phase
                    Text(
                        text = sequence.chunked(4).joinToString(" "),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Elapsed Time: ${elapsedTime}ms",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (showResult) {
                    // Display results
                    Row {
                        input.padEnd(16, ' ').forEachIndexed { index, c ->
                            val color = if (index < sequence.length && c == sequence[index]) {
                                Color.Green
                            } else {
                                Color.Red
                            }
                            Text(
                                text = c.toString(),
                                color = color,
                                fontSize = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = sequence.chunked(4).joinToString(" "),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Last Score: ${lastScore}ms",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val correctDigits = input.zip(sequence).count { it.first == it.second }
                    bestScores[correctDigits]?.let { bestTime ->
                        Text(
                            text = "Best Time for $correctDigits digits: ${bestTime}ms",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Recall phase
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("Enter the sequence") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Elapsed Time: ${elapsedTime}ms",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        if (bestScores.isNotEmpty()) {
            // Display best scores
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Best Scores:",
                    style = MaterialTheme.typography.bodyLarge
                )
                bestScores.forEach { (digits, time) ->
                    Text(
                        text = "$digits digits: ${time}ms",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Function to generate a random sequence of 16 digits
fun generateRandomSequence(): String {
    return (1..16).map { Random.nextInt(0, 10) }.joinToString("")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        MemoryTrainingApp()
    }
}

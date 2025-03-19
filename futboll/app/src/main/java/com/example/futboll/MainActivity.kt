package com.example.futboll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.flow.collect
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sensorViewModel: SensorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            FutbollGameScreen(sensorViewModel)
        }
    }
}

@Composable
fun FutbollGameScreen(sensorViewModel: SensorViewModel) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    val ballRadius = 20f
    var ballX by remember { mutableStateOf(screenWidth / 2) }
    var ballY by remember { mutableStateOf(screenHeight / 2) }

    val goalWidth = 200f
    val goalHeight = 100f
    val penaltyBoxWidth = 600f
    val penaltyBoxHeight = 300f

    val topGoal = Offset((screenWidth - goalWidth) / 2, 50f)
    val bottomGoalY = screenHeight - 150f
    val bottomGoal = Offset((screenWidth - goalWidth) / 2, bottomGoalY)

    var scoreTop by remember { mutableStateOf(0) }
    var scoreBottom by remember { mutableStateOf(0) }

    LaunchedEffect(sensorViewModel.acceleration) {
        sensorViewModel.acceleration.collect { (ax, ay, _) ->
            //Definimos el nuevo movimiento y delimitando que no salga de la cancha
            val newBallX = min(max(ballX - ax * 5, ballRadius + 60f), screenWidth - ballRadius - 60f)
            val newBallY = min(max(ballY + ay * 5, ballRadius + 60f), bottomGoalY - ballRadius)

            // Se detecta si toca las porterias
            val isGoalTop = newBallY - ballRadius <= topGoal.y + goalHeight &&
                    newBallX in topGoal.x..(topGoal.x + goalWidth)
            val isGoalBottom = newBallY + ballRadius >= bottomGoal.y &&
                    newBallX in bottomGoal.x..(bottomGoal.x + goalWidth)

            //Cambia la posicion de la pelota segun el suceso
            if (isGoalTop) {
                scoreBottom++
                ballX = screenWidth / 2
                ballY = screenHeight / 2
            } else if (isGoalBottom) {
                scoreTop++
                ballX = screenWidth / 2
                ballY = screenHeight / 2
            } else {
                ballX = newBallX
                ballY = newBallY
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            color = Color(0xFF333333),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$scoreTop",
                    color = Color(0xFFFFD700),
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                //Fondo de la cancha
                for (i in 0 until 12) {
                    val color = if (i % 2 == 0) Color(0xFF4CAF50) else Color(0xFF388E3C)
                    drawRect(
                        color = color,
                        topLeft = Offset(0f, i * (screenHeight / 12)),
                        size = Size(screenWidth, screenHeight / 12)
                    )
                }

                //Definir las lineas que delimitan la cancha
                val fieldHeight = screenHeight - 130f
                val pitchOutline = Path().apply {
                    moveTo(50f, 50f)
                    lineTo(screenWidth - 50f, 50f)
                    lineTo(screenWidth - 50f, fieldHeight)
                    lineTo(50f, fieldHeight)
                    close()
                    moveTo(50f, screenHeight / 2)
                    lineTo(screenWidth - 50f, screenHeight / 2)
                }

                //Dibujo de la cancha
                drawPath(path = pitchOutline, color = Color.White, style = Stroke(3.dp.toPx()))
                drawCircle(color = Color.White, center = Offset(screenWidth / 2, screenHeight / 2), radius = 10f)
                drawCircle(color = Color.White, center = Offset(screenWidth / 2, screenHeight / 2), radius = 100f, style = Stroke(3.dp.toPx()))

                drawRect(color = Color.White, topLeft = topGoal, size = Size(goalWidth, goalHeight), style = Stroke(3.dp.toPx()))
                drawRect(color = Color.White, topLeft = Offset(screenWidth / 2 - 300f, 50f), size = Size(penaltyBoxWidth, penaltyBoxHeight), style = Stroke(3.dp.toPx()))

                drawRect(color = Color.White, topLeft = bottomGoal, size = Size(goalWidth, goalHeight), style = Stroke(3.dp.toPx()))
                drawRect(color = Color.White, topLeft = Offset(screenWidth / 2 - 300f, bottomGoalY - 200f), size = Size(penaltyBoxWidth, penaltyBoxHeight), style = Stroke(3.dp.toPx()))

                //Pelota
                drawCircle(color = Color.White, radius = ballRadius, center = Offset(ballX, ballY))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            color = Color(0xFF333333),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$scoreBottom",
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}

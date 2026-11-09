package com.example.calulu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Bg = Color(0xFF1C1C1E)       // Фон экрана
private val Dark = Color(0xFF2C2C2E)     // Фон цифр
private val Gray = Color(0xFF3A3A3C)     // Фон вспомогательных кнопок
private val Orange = Color(0xFFFF9F0A)   // Фон операторов
private val Hint = Color(0xFF8E8E93)     // Цвет текста истории

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Calc() // Вызов главного экрана калькулятора
            }
        }
    }
}

@Composable
fun Calc(vm: CalcVM = viewModel()) {

    Column(
        Modifier.fillMaxSize().background(Bg).padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // ЭКРАН
        Column(
            Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // История
            Text(
                text = vm.history, color = Hint, fontSize = 20.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            // Текущий оператор
            Text(
                text = vm.op, color = Orange, fontSize = 26.sp, maxLines = 1,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End
            )
            // Текущее число
            Text(
                text = vm.display, color = Color.White, fontSize = 60.sp,
                fontWeight = FontWeight.Light, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(12.dp))
        }

        // КЛАВИАТУРА
        val rows = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("⌫", "0", ".", "=")
        )

        // Проходимся по каждому ряду
        rows.forEach { r ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Проходимся по каждой кнопке в ряду
                r.forEach { label ->
                    Key(label, Modifier.weight(1f)) {
                        when (label) {
                            "C" -> vm.clear()
                            "⌫" -> vm.backspace()
                            "±" -> vm.sign()
                            "%" -> vm.percent()
                            "=" -> vm.equals()
                            "." -> vm.dot()
                            "+", "-", "×", "÷" -> vm.operator(label)
                            else -> vm.digit(label[0])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(label: String, mod: Modifier, onClick: () -> Unit) {
    val (bg, fg) = when (label) {
        "+", "-", "×", "÷", "=" -> Orange to Color.White
        "C", "±", "%", "⌫" -> Gray to Color.White
        else -> Dark to Color.White
    }

    // Сама кнопка
    Button(
        onClick = onClick,
        modifier = mod.aspectRatio(1f).heightIn(min = 60.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    }
}
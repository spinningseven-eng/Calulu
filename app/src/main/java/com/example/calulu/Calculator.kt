package com.example.calulu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class CalcVM : ViewModel() {

    private val tokens = mutableListOf<String>()

    // Текущее вводимое число в виде строки.
    private var num = "0"

    // Флаг, указывающий, есть ли уже десятичная точка в текущем числе
    private var hasDot = false

    // Флаг, указывающий, что последним действием был ввод оператора.
    private var afterOp = false

    // Флаг, указывающий, что последним действием было нажатие "=" (вывод результата).
    private var done = false

    // Переменные состояния Compose для отображения на экране.
    var history by mutableStateOf("")     // Верхняя строка (история)
    var op by mutableStateOf("")          // Средняя строка (текущий оператор)
    var display by mutableStateOf("0")    // Большое число (текущее число или результат)
        private set

    private fun sync() {
        history = tokens.joinToString(" ")
        op = tokens.lastOrNull()?.takeIf(::isOp) ?: ""
        display = num
    }

    // Проверка, является ли строка математическим оператором.
    private fun isOp(s: String) = s in setOf("+", "-", "×", "÷")

    fun digit(d: Char) {
        if (done) { tokens.clear(); done = false } // Если был результат, начинаем новое вычисление
        if (afterOp) {
            // Если только что нажали оператор, начинаем вводить новое число
            num = "$d"
            hasDot = false
            afterOp = false
        } else {
            // Иначе дописываем цифру к текущему числу (заменяем одиночный "0")
            num = if (num == "0") "$d" else num + d
        }
        sync()
    }


    fun dot() {
        if (done) { tokens.clear(); done = false }
        if (afterOp) {
            // Если нажали точку сразу после оператора, начинаем с "0."
            num = "0."
            hasDot = true
            afterOp = false
            sync()
            return
        }
        if (!hasDot) {
            // Добавляем точку, только если её еще нет
            num += "."
            hasDot = true
            sync()
        }
    }

    fun operator(o: String) {
        if (done) {
            // Если был результат, используем его как начало нового выражения
            tokens.clear()
            tokens += num
            done = false
        }
        if (afterOp) {
            // Если оператор нажат подряд, заменяем предыдущий оператор на новый
            if (tokens.lastOrNull()?.let(::isOp) == true) {
                tokens[tokens.size - 1] = o
            }
            sync()
            return
        }
        // Сохраняем текущее число и оператор в список токенов
        tokens += num
        tokens += o
        afterOp = true
        hasDot = false
        sync()
    }

    fun clear() {
        tokens.clear()
        num = "0"
        hasDot = false
        afterOp = false
        done = false
        sync()
    }

    fun backspace() {
        if (done) return clear() // Если показан результат, сбрасываем всё
        if (afterOp) return       // Если ввели оператор, ничего не стираем
        if (num.length <= 1) {
            num = "0"
            hasDot = false
        } else {
            if (num.last() == '.') hasDot = false
            num = num.dropLast(1)
        }
        sync()
    }

    fun sign() {
        num = if (num.startsWith("-")) num.drop(1) else "-$num"
        sync()
    }

    fun percent() {
        val value = num.toDoubleOrNull() ?: 0.0
        num = fmt(value / 100)
        hasDot = num.contains(".")
        sync()
    }

    fun equals() {
        if (tokens.isEmpty()) { done = true; return }

        val full = tokens.toMutableList().apply {
            if (afterOp) removeAt(lastIndex) else add(num)
        }

        num = try {
            fmt(calc(full))
        } catch (e: Exception) {
            "Ошибка"
        }

        tokens.clear()
        hasDot = num.contains(".")
        afterOp = false
        done = true
        sync()
    }

    private fun calc(t: List<String>): Double {
        val nums = mutableListOf(t[0].toDouble())
        val ops = mutableListOf<String>()

        var i = 1
        while (i < t.size) {
            val o = t[i]
            val v = t[i + 1].toDouble()
            when (o) {
                "×" -> nums[nums.size - 1] *= v
                "÷" -> {
                    if (v == 0.0) error("Деление на ноль")
                    nums[nums.size - 1] /= v
                }
                else -> {
                    ops += o
                    nums += v
                }
            }
            i += 2
        }

        var r = nums[0]
        ops.forEachIndexed { k, o ->
            r = if (o == "+") r + nums[k + 1] else r - nums[k + 1]
        }
        return r
    }

    private fun fmt(v: Double): String {
        return if (v == v.toLong().toDouble()) v.toLong().toString()
        else "%.10f".format(v).trimEnd('0').trimEnd('.')
    }
}
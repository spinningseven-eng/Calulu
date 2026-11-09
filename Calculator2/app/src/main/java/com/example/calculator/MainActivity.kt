package com.example.calculator // Если у тебя папка называется иначе, поменяй эту строку

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var tvDisplay: TextView? = null
    private var tvOperation: TextView? = null
    private var tvHistory: TextView? = null

    private var currentInput = "0"
    private var expression = ""
    private var lastOperation = ""
    private var isNewNumber = true
    private var justCalculated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvOperation = findViewById(R.id.tvOperation)
        tvHistory = findViewById(R.id.tvHistory)

        // Восстановление при повороте экрана
        if (savedInstanceState != null) {
            currentInput = savedInstanceState.getString("currentInput") ?: "0"
            expression = savedInstanceState.getString("expression") ?: ""
            lastOperation = savedInstanceState.getString("lastOperation") ?: ""
            isNewNumber = savedInstanceState.getBoolean("isNewNumber", true)
            justCalculated = savedInstanceState.getBoolean("justCalculated", false)
        }

        // Привязываем все кнопки к одному клику
        val buttonIds = intArrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply,
            R.id.btnDivide, R.id.btnEquals, R.id.btnC, R.id.btnBackspace,
            R.id.btnOpenParen, R.id.btnCloseParen
        )

        var i = 0
        while (i < buttonIds.size) {
            val btn = findViewById<Button>(buttonIds[i])
            btn.setOnClickListener(this)
            i++
        }

        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentInput", currentInput)
        outState.putString("expression", expression)
        outState.putString("lastOperation", lastOperation)
        outState.putBoolean("isNewNumber", isNewNumber)
        outState.putBoolean("justCalculated", justCalculated)
    }

    // Обработка нажатий
    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.btn0 -> onDigit("0")
            R.id.btn1 -> onDigit("1")
            R.id.btn2 -> onDigit("2")
            R.id.btn3 -> onDigit("3")
            R.id.btn4 -> onDigit("4")
            R.id.btn5 -> onDigit("5")
            R.id.btn6 -> onDigit("6")
            R.id.btn7 -> onDigit("7")
            R.id.btn8 -> onDigit("8")
            R.id.btn9 -> onDigit("9")
            R.id.btnDot -> onDot()
            R.id.btnPlus -> onOperator("+")
            R.id.btnMinus -> onOperator("−")
            R.id.btnMultiply -> onOperator("×")
            R.id.btnDivide -> onOperator("÷")
            R.id.btnEquals -> onEquals()
            R.id.btnC -> onClear()
            R.id.btnBackspace -> onBackspace()
            R.id.btnOpenParen -> onOpenParen()
            R.id.btnCloseParen -> onCloseParen()
        }
    }

    private fun onDigit(d: String) {
        if (isNewNumber || justCalculated) {
            currentInput = d
            isNewNumber = false
            justCalculated = false
        } else {
            if (currentInput == "0") {
                currentInput = d
            } else {
                currentInput = currentInput + d
            }
        }
        updateUI()
    }

    private fun onDot() {
        if (isNewNumber || justCalculated) {
            currentInput = "0."
            isNewNumber = false
            justCalculated = false
        } else {
            var hasDot = false
            var i = 0
            while (i < currentInput.length) {
                if (currentInput[i] == '.') hasDot = true
                i++
            }
            if (!hasDot) {
                currentInput = currentInput + "."
            }
        }
        updateUI()
    }

    private fun onOperator(op: String) {
        justCalculated = false

        if (!isNewNumber) {
            expression = expression + currentInput
            currentInput = "0"
        } else {
            if (expression.length > 0) {
                if (isLastCharOperator(expression)) {
                    expression = expression.substring(0, expression.length - 3)
                }
            }
        }

        expression = expression + " " + op + " "
        lastOperation = op
        isNewNumber = true
        updateUI()
    }

    private fun onEquals() {
        if (expression.length == 0 && lastOperation.length == 0) {
            return
        }

        var fullExpr = expression
        if (!isNewNumber) {
            fullExpr = fullExpr + currentInput
        } else {
            fullExpr = fullExpr + currentInput
        }

        var result = "Ошибка"
        try {
            val value = calculate(fullExpr)
            result = doubleToString(value)
        } catch (e: Exception) {
            result = "Ошибка"
        }

        tvHistory?.text = fullExpr + " ="
        expression = ""
        lastOperation = ""
        currentInput = result
        isNewNumber = true
        justCalculated = true
        updateUI()
    }

    private fun onClear() {
        currentInput = "0"
        expression = ""
        lastOperation = ""
        isNewNumber = true
        justCalculated = false
        updateUI()
    }

    private fun onBackspace() {
        if (justCalculated) {
            onClear()
            return
        }
        if (isNewNumber && expression.length > 0) {
            if (expression.length >= 3) {
                expression = expression.substring(0, expression.length - 3)
            } else {
                expression = ""
            }
            lastOperation = ""
        } else {
            if (currentInput.length <= 1) {
                currentInput = "0"
                isNewNumber = true
            } else {
                currentInput = currentInput.substring(0, currentInput.length - 1)
            }
        }
        updateUI()
    }

    private fun onOpenParen() {
        if (isNewNumber || justCalculated) {
            if (!justCalculated && expression.length > 0 && !isLastCharOperator(expression)) {
                expression = expression + " × "
            }
            expression = expression + "( "
            currentInput = "0"
            isNewNumber = true
            justCalculated = false
        } else {
            expression = expression + currentInput + " × ( "
            currentInput = "0"
            isNewNumber = true
        }
        updateUI()
    }

    private fun onCloseParen() {
        if (!isNewNumber) {
            expression = expression + currentInput + " ) "
            currentInput = "0"
            isNewNumber = true
        } else {
            if (expression.length > 0 && !isLastCharOperator(expression)) {
                expression = expression + ") "
            }
        }
        updateUI()
    }

    private fun isLastCharOperator(s: String): Boolean {
        if (s.isEmpty()) return false
        var ch = s[s.length - 1]
        if (ch == ' ') {
            if (s.length >= 2) ch = s[s.length - 2]
        }
        return ch == '+' || ch == '−' || ch == '×' || ch == '÷' || ch == '('
    }

    private fun updateUI() {
        tvDisplay?.text = currentInput
        tvOperation?.text = lastOperation
        tvHistory?.text = expression.trim()
    }

    private fun doubleToString(n: Double): String {
        if (n.isNaN() || n.isInfinite()) return "Ошибка"

        val asLong = n.toLong()
        if (asLong.toDouble() == n) {
            return asLong.toString()
        }
        var s = n.toString()
        while (s.endsWith("0") && s.contains(".")) {
            s = s.substring(0, s.length - 1)
        }
        if (s.endsWith(".")) {
            s = s.substring(0, s.length - 1)
        }
        return s
    }

    // --- ЛОГИКА ВЫЧИСЛЕНИЙ (без скобок в сложных функциях, только 3 шага) ---

    private fun calculate(expr: String): Double {
        var s = expr
        s = removeSpaces(s)

        while (s.indexOf("(") >= 0) {
            val open = s.lastIndexOf("(")
            val close = s.indexOf(")", open)
            if (close < 0) throw Exception("Скобки не закрыты")
            val inside = s.substring(open + 1, close)
            val valInside = calculateNoParens(inside)
            val before = s.substring(0, open)
            val after = s.substring(close + 1)
            s = before + doubleToString(valInside) + after
        }

        return calculateNoParens(s)
    }

    private fun removeSpaces(s: String): String {
        var r = ""
        var i = 0
        while (i < s.length) {
            if (s[i] != ' ') {
                r = r + s[i]
            }
            i++
        }
        return r
    }

    private fun calculateNoParens(s: String): Double {
        val parts = splitTokens(s)
        val parts2 = mutableListOf<String>()
        var i = 0
        while (i < parts.size) {
            val token = parts[i]
            if (token == "×" || token == "÷") {
                val prevStr = parts2[parts2.size - 1]
                val prev = prevStr.toDouble()
                val next = parts[i + 1].toDouble()
                var r = 0.0
                if (token == "×") r = prev * next
                if (token == "÷") {
                    if (next == 0.0) throw Exception("Деление на ноль")
                    r = prev / next
                }
                parts2[parts2.size - 1] = doubleToString(r)
                i = i + 2
            } else {
                parts2.add(token)
                i++
            }
        }

        if (parts2.isEmpty()) return 0.0
        var result = parts2[0].toDouble()
        var j = 1
        while (j < parts2.size) {
            val op = parts2[j]
            val num = parts2[j + 1].toDouble()
            if (op == "+") result = result + num
            if (op == "−") result = result - num
            j = j + 2
        }
        return result
    }

    private fun splitTokens(s: String): List<String> {
        val list = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '+' || c == '−' || c == '×' || c == '÷') {
                list.add(c.toString())
                i++
            } else if (c == '.') {
                val sb = StringBuilder()
                sb.append(c)
                i++
                while (i < s.length && s[i].isDigit()) {
                    sb.append(s[i])
                    i++
                }
                list.add(sb.toString())
            } else if (c.isDigit()) {
                val sb = StringBuilder()
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                    sb.append(s[i])
                    i++
                }
                list.add(sb.toString())
            } else {
                i++
            }
        }
        return list
    }
}
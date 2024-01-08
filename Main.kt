package converter

import kotlin.math.pow
import java.math.BigInteger
import java.math.BigDecimal
import java.math.RoundingMode

object Converter {
    enum class State { FIRST_MENU, SECOND_MENU, IDLE }
    const val FIFTY_FIVE = 55
    val zero = BigInteger.ZERO
    val one = BigInteger.ONE
    val ten = BigInteger.TEN
    var state: State = State.FIRST_MENU
    var source: BigInteger = BigInteger.TEN
    var target: BigInteger = BigInteger.TEN

    fun firstMenu() = println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")

    fun setBasesOrExit(input: String) {
        when (input) {
            "/exit" -> state = State.IDLE
            else -> {
                source = input.substringBefore(' ').toBigInteger()
                target = input.substringAfter(' ').toBigInteger()
                state = State.SECOND_MENU
            }
        }
    }

    fun secondMenu() = println("Enter number in base $source to convert to base $target (To go back type /back)")

    fun getNumberOrBack(input: String) {
         when (input) {
            "/back" -> state = State.FIRST_MENU
            else -> {
                if (source == ten) fromDecimal(input) else toDecimal(input)
            }
        }
    }

    fun fromDecimal(input: String) {
        val int: BigInteger
        var frac = BigDecimal.ZERO
        val intList = mutableListOf<String>()
        val fracList = mutableListOf<String>()

        //Get integer and fractional parts
        if ('.' in input) {
            int = input.substringBefore('.').toBigInteger()
            val possibleFrac = input.toBigDecimal() - int.toBigDecimal()
            if (possibleFrac > BigDecimal.ZERO) frac = possibleFrac
        } else {
            int = input.toBigInteger()
        }

        //Convert integer part
        var n = int
        var remainder: BigInteger
        while (n > zero) {
            remainder = n % target

            val digit = if (remainder < ten) {
                remainder.toString()
            } else {
                (remainder.toInt() + FIFTY_FIVE).toChar().toString()
            }

            intList.add(digit)
            n /= target
        }

        //Check and convert fractional part
        if ('.' in input) {
            println("frac $frac")
            fracList.add(".")
            var n = frac
            var remainder = frac
            var fracRemainder = frac
            var intRemainder: BigInteger

            while (n > BigDecimal.ZERO && fracList.size < 6) { // Out of memory
                println("n $n")
                remainder = n * target.toBigDecimal()
                println("rem $remainder")
                intRemainder = remainder.toBigInteger()
                println("intRem $intRemainder")
                fracRemainder = remainder - intRemainder.toBigDecimal()
                println("fracRem $fracRemainder \n")

                val digit = if (intRemainder < ten) {
                    intRemainder.toString()
                } else {
                    (intRemainder.toInt() + FIFTY_FIVE).toChar().toString()
                }

                fracList.add(digit)
                println("fracList $fracList")
                n = fracRemainder
            }
            //Add missing zeroes
            while (fracList.size < 6) {
                fracList.add("0")
            }
        }

        println("Conversion result: ${intList.asReversed().joinToString("")}${fracList.joinToString("")}")
    }

    fun toDecimal(input: String) {
        val int: String
        var frac = ""
        var result = BigDecimal.ZERO

        //Get integer and fractional parts
        if ('.' in input) {
            int = input.substringBefore('.')
            frac = input.substringAfter('.')
        } else {
            int = input
        }

        //Get integer part
        val reverseInt = int.reversed()
        for (i in reverseInt.indices) {
            val digit = when (reverseInt[i]) {
                in '0'..'9' -> reverseInt[i].toString().toBigInteger()
                else -> (reverseInt[i].toUpperCase().toInt() - FIFTY_FIVE).toBigInteger()
            }
            result += (digit * source.pow(i)).toBigDecimal()
        }

        //Get fractional part
        for (i in frac.indices) {
            val pow = i + 1
            val digit = when (frac[i]) {
                in '0'..'9' -> frac[i].toString().toBigDecimal()
                else -> (frac[i].toUpperCase().toInt() - FIFTY_FIVE).toBigDecimal()
            }
            val coef = BigDecimal.ONE.setScale(10, RoundingMode.HALF_UP) / source.toBigDecimal().pow(pow)
            val digitValue = digit * coef
            result += digitValue
        }

        val parameter = if (frac == "") {
            result.toBigInteger()
        } else {
            result
        }

        println("parameter ${parameter.toString()}")
        fromDecimal(parameter.toString())
    }

}

fun main() {

    var input = ""

    while (Converter.state != Converter.State.IDLE) {
        Converter.firstMenu()
        input = readln()
        Converter.setBasesOrExit(input)
        while (Converter.state == Converter.State.SECOND_MENU) {
            Converter.secondMenu()
            input = readln()
            //Goes all the way to conversion result or breaks loop by setting state = FIRST_MENU
            Converter.getNumberOrBack(input)
        }
    }

}
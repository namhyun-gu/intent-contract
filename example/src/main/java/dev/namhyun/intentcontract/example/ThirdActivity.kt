package dev.namhyun.intentcontract.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.namhyun.intentcontract.Extra
import dev.namhyun.intentcontract.IntentTarget
import dev.namhyun.intentcontract.gen.IntentContracts

@IntentTarget
class ThirdActivity : AppCompatActivity() {
    @Extra
    var extraBoolean: Boolean = false

    @Extra
    var extraByte: Byte = 0

    @Extra
    var extraChar: Char = '\u0000'

    @Extra
    var extraDouble: Double = 0.0

    @Extra
    var extraFloat: Float = 0.0f

    @Extra
    var extraInt: Int = 0

    @Extra
    var extraLong: Long = 0

    @Extra
    var extraShort: Short = 0

    @Extra
    var extraString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        IntentContracts.contact(this)
    }
}

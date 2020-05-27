package dev.namhyun.intentcontract.example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.namhyun.intentcontract.Extra
import dev.namhyun.intentcontract.IntentTarget
import dev.namhyun.intentcontract.Optional
import dev.namhyun.intentcontract.gen.IntentContracts

@IntentTarget
class SecondActivity : AppCompatActivity() {

    @Extra
    var test: String = "Hello"

    @Optional
    @Extra
    var optionalTest: String = "World"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        IntentContracts.contact(this)
        findViewById<TextView>(R.id.tv_test).text = "Receive: $test"
    }
}

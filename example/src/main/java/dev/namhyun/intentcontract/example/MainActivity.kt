package dev.namhyun.intentcontract.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.namhyun.intentcontract.gen.IntentTargets

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val secondActivity = IntentTargets.secondActivity(this, "Test", null)
        IntentTargets.thirdActivity(
            context = this,
            extraBoolean = false,
            extraByte = 0,
            extraChar = '\u0000',
            extraDouble = 0.0,
            extraFloat = 0.0f,
            extraInt = 0,
            extraLong = 0,
            extraShort = 0,
            extraString = ""
        )
        startActivity(secondActivity)
    }
}

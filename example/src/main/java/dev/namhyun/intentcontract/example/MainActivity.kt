/*
 * Copyright 2020 Namhyun, Gu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

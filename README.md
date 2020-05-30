# Intent Contract

[ ![Download](https://api.bintray.com/packages/namhyun-gu/intentcontract/intentcontract-compiler/images/download.svg) ](https://bintray.com/namhyun-gu/intentcontract/intentcontract-compiler/_latestVersion)
![Publish](https://github.com/namhyun-gu/intent-contract/workflows/Publish/badge.svg)
![GitHub](https://img.shields.io/github/license/namhyun-gu/intent-contract)

## Getting Started

Add dependency codes to your **module** level `build.gradle` file.

```groovy
apply plugin: 'kotlin-kapt'

// ...

dependencies {
  implementation 'dev.namhyun.intentcontract:intentcontract-annotations:<latest-version>'
  kapt 'dev.namhyun.intentcontract:intentcontract-compiler:<latest-version>'
}
```

## Usage

- Add `@IntentTarget` annotation to Activity.
- If you provide extra properties, Add `@Extra` annotation to field.
  - **Field must be public and writable**
  - Support types : **Boolean, Byte, Char, Double, Float, Int, Long, Short, String**
- Some extra can be optional. Add `@Optional` annotation to field.
  - Optional fields generate nullable types in IntentTargets function. If value is null, field keep initalized value.

```kotlin
@IntentTarget
class SecondActivity : AppCompatActivity() {

    @Extra
    var test: String = "Hello"

    @Optional
    @Extra
    var optionalTest: String = "World"

    // ...

}
```

- In Android Studio, Select **Build > Rebuild Project** menu for generate code.
- If no error messages, you can use this methods.

```kotlin
val intent: Intent = IntentTargets.secondActivity(this, test="Test", optionalTest=null)
```

```kotlin
// Fill value to field having @Extra annotation from Intent
IntentContracts.contact(this)
```

## Example

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val secondActivity: Intent = IntentTargets.secondActivity(this, test="Test", optionalTest=null)
        startActivity(secondActivity)
    }
}
```

```kotlin
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
      Log.d("SecondActivity", "Receive: $test") // Receive: Test
      Log.d("SecondActivity", "Receive: $optionalTest") // Receive: World
  }
}
```

## License

```xml
Copyright 2019 Namhyun, Gu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

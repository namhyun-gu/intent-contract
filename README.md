# Intent Contract

[ ![Download](https://api.bintray.com/packages/namhyun-gu/intentcontract/intentcontract-compiler/images/download.svg) ](https://bintray.com/namhyun-gu/intentcontract/intentcontract-compiler/_latestVersion)

## Getting Started

Add dependency codes to your **module** level  ```build.gradle``` file.

```groovy
apply plugin: 'kotlin-kapt'

...

dependencies {
  implementation 'dev.namhyun.intentcontract:intentcontract-annotations:0.1.0'
  kapt 'dev.namhyun.intentcontract:intentcontract-compiler:0.1.0'
}
```

## Usage

- Add ```@IntentTarget``` annotation to Activity.
- If you provide extra properties, Add ```@Extra``` annotation to field.
  - **Field must be public and writable**
  - Support types : **Boolean, Byte, Char, Double, Float, Int, Long, Short, String**
- Some extra can be optional. Add ```@Optional``` annotation to field.
  - Optional fields generate nullable types in IntentTargets function. If value is null, field keep initalized value.

```kotlin
@IntentTarget
class SecondActivity : AppCompatActivity() {

    @Extra
    var test: String = "Hello"

    @Optional
    @Extra
    var optionalTest: String = "World"

    ...

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
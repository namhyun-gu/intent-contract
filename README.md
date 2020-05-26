# Intent Contract

## Getting Started

WIP

## Usage

- Add ```@IntentTarget``` annotation to Activity.
- If you provide extra properties, Add ```@Extra``` annotation to field.
  - Support types : **Boolean, Byte, Char, Double, Float, Int, Long, Short, String**

```kotlin
@IntentTarget
class SecondActivity : AppCompatActivity() {

    @Extra
    var test: String = "Hello"

    ...

}
```

- In Android Studio, Select **Build > Rebuild Project** menu for generate code.
- If no error messages, you can use this methods.

```kotlin
val intent: Intent = IntentTargets.secondActivity(this, "Test")
```


```kotlin
IntentContracts.contact(this)
```

## Example

```kotlin
class SecondActivity : AppCompatActivity() {
  
  @Extra
  var test: String = "Hello"
  
  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_second)
      IntentContracts.contact(this)
      findViewById<TextView>(R.id.tv_test).text = "Receive: $test"
  }
}
```

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val secondActivity = IntentTargets.secondActivity(this, "Test")
        startActivity(secondActivity)
    }
}
```
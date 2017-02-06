2장. 스칼라로 함수형 프로그래밍 시작하기
=============================

- 스칼라의 언어와 그 문법의 기초를 배운다.
- 함수형 프로그래밍에 쓰이는 기본적인 기법을 배운다.
  - **꼬리 재귀 함수**를 이용해서 루프를 작성하는 방법
  - **고차 함수**(다른 함수를 인수로 받는 함수로서, 또 다른 함수를 결과로 돌려줄 수 있다)
  - **다형적** 고차 함수


2.1 스칼라 언어의 소개: 예제 하나
-------------------------

```scala
// 이것은 주석!
/* 이것도 주석 */
/** 문서화 주석 */
object MyModule {
  def abs(n: Int): Int = 
    if (n < 0) -n
    else n
  
  private def formatAbs(x: Int) = { 
    val msg = "The absolute value of %d is %d"
    msg.format(x, abs(x))
  }
  
  def main(args: Array[String]): Unit =
    println(formatAbs(-42))
}
```

위 예제는 `MyModule`이라는 이름의 객체(object; **모듈(module)**이라고도 한다)를 선언한다. 

> **object 키워드**
> 
> - singleton 객체를 생성
> - Java의 정적 멤버(static member)를 가진 클래스를 만들만한 상황에 사용
> - Companion Object를 만들 때 사용

`MyModule` 객체에는 def 키워드로 도입된 **메서드(method)**가 세 개(`abs`, `formatAbs`, `main`) 있다. 

- `def abs(n: Int): Int`: 정수(Int) 하나를 받고 결과 값으로 정수를 돌려주는 순수 함수
- `private def formatAbs(x: Int)`: 추론 가능한 반환 형식은 생략 가능 (명시적으로 지정하는 것을 권장, formatAbs는 private으로 지정되었기 때문에 생략)
- `def main(args: Array[String]): Unit`: 순수 함수적 핵심부를 호출하는 외부 계층(shell)로서 다른 말로는 **절차(produce)** 또는 **불순 함수(impure function)**

메서드 선언에서 등호(`=`)를 기준으로 좌측에 있는 것을 **좌변(left-hand side)** 또는 **서명(signature)**이라고 하고, 우측을 **우변(right-hand side)** 또는 **정의(definition)**라고 부른다. 메서드 우변의 평가 결과가 반환 값이 되기 때문에 명시적인 `return` 키워드가 없다.

`Unit`은 C나 Java에서의 `void`와 비슷한 용도로 쓰이며, 반환 형식이 `Unit`이라는 것은 그 메서드에 **부수 효과**가 존재함을 암시한다.

> Tip: [Access modifiers in Java and Scala](http://www.jesperdj.com/2016/01/08/scala-access-modifiers-and-qualifiers-in-detail)


2.2 프로그램의 실행
---------------

일반적으로는 sbt(스칼라용 빌드 도구) 또는 IDE(IntelliJ, Eclipse) 이용하며, 이번 절에서는 짧은 예제에 적합한 방법을 소개한다.

- 콘솔에서 직접 스칼라 코드를 컴파일하고 실행하는 방법
  
  ```
  > scalac MyModule.scala
  
  > scala MyModule
  The absolute value of -42 is 42.
  ```

- REPL(read-evaluate-print loop)로 실행하는 방법
  
  ```
  > scala
  Welcome to Scala 2.11.8 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_65).
  Type in expressions for evaluation. Or try :help.
  
  scala> :load MyModule.scala
  Loading MyModule.scala...
  defined object MyModule
  
  scala> MyModule.abs(-42)
  res0: Int = 42
  ```

  > 스칼라 기본 제공 REPL도 좋지만 [Ammonite-REPL](http://www.lihaoyi.com/Ammonite/#Ammonite-REPL) 사용을 추천


2.3 모듈, 객체, 이름공간
--------------------

- 스칼라의 모든 값은 **객체(object)**이다. 
- 각각의 객체는 0개 또는 하나 이상의 **멤버(member)**을 가질 수 있다. 
- 자신의 멤버들에게 **이름공간(namespace)**을 제공하는 것이 주 목적인 객체를 **모듈(module)**이라고 부른다.
- 멤버는 `def`로 선언된 메서드, `val`이나 `object`로 선언된 또 다른 객체일 수 있다.
- `2 + 1`은 `2.+(1)`의 구문적 겉치레(syntactic sugar)일 뿐이다. 인수가 하나인 메서드는 마침표과 괄호를 생략한 중위(infix) 표기법으로 호출 할 수 있다. 예를 들어, `MyModule.abs(-42)`는 `MyModule abs -42`와 같다.
  > Tip: `::`는 [right-associative](https://en.wikipedia.org/wiki/Operator_associativity) ([what-good-are-right-associative-methods-in-scala](http://stackoverflow.com/questions/1162924/what-good-are-right-associative-methods-in-scala))

- `import` 키워드로 객체의 멤버를 현재 범위로 도입(importing)하면 객체 이름을 생략하고 사용할 수 있으며, 밑줄(underscore)을 이용하여 객체의 nonprivate 멤버를 import 할 수 있다.

  ```
  scala> import MyModule.abs
  import MyModule.abs
  
  scala> abs(-42)
  res0: 42
  
  scala> import MyModule._
  import MyModule._
  ```


2.4 고차 함수: 함수를 함수에 전달
-------------------------

- **값으로서의 함수**: 함수를 변수에 배정하거나 자료구조에 저장할 수 있고 인수로서 함수에 넘겨줄 수 있다는 개념
- **고차 함수(higher-order function. HOF)**: 다른 함수를 인수로 받는 함수

### 2.4.1 잠깐 곁가지: 함수적으로 루프 작성하기

루프를 함수적으로(루프 변수의 변이 없이) 작성하는 방법은 재귀 함수를 이용하는 것이다. 스칼라는 **자기 재귀(self-recursion)**를 검출해서 재귀 호출이 **꼬리 위치(tail position)**에서 일어난다면 **꼬리 호출 제거(tail call elimination)**를 적용하여 컴파일러 최적화를 통해 `while` 루프를 사용했을 때와 같은 종류의 바이트코드로 컴파일을 한다.

> `@annotation.tailrec`을 이용하여 꼬리 재귀를 검출할 수 있다.

```scala
// 꼬리 재귀가 아닌 예: stack-overflow 위험이 있다.
def factorial(n: Int): Int = {
  if (n <= 1) 1
  else n * factorial(n - 1)
}

// 꼬리 재귀의 예: stack-overflow 위험이 없다.
def factorial(n: Int): Int = {
  @annotation.tailrec // 꼬리 재귀 검출: 꼬리 재귀가 아니라면 컴파일러 오류가 발생한다.
  def go(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else go(n - 1, n + acc)
  
  go(n, 1)
}
```

> ■ 연습문제 2.1
> 
> n번째 [피보나치 수(Fibonacci number)](https://en.wikipedia.org/wiki/Fibonacci_number)를 돌려주는 재귀 함수를 작성하라. 처음 두 피보나치 수는 0과 1이다. n번째 피보나치 수는 항상 이전 두 수의 합이다. 즉, 피보나치 수열은 0, 1, 1, 2, 3, 5로 시작한다. 반드시 지역 꼬리 재귀 함수를 사용해서 작성할 것.
> 
> `def fib(n: Int): Int`

### 2.4.2 첫 번째 고차 함수 작성

```scala
private def formabAbs(x: Int) = {
  val msg = "The absolute value of %d is %d."
  msg.format(x, abs(x))
}
  
private def formatFactorial(n: Int) = {
  val msg = "The factorial of %d is %d."
  msg.format(n, factorial(n))
}
```

`formatAbs`와 `formatFactorial`은 거의 동일하다. 두 함수에서 적용할 함수 이름과 함수 자체를 인수로 받는다면 다음과 같이 일반화할 수 있다.

```scala
def formatResult(name: String, n: Int, f: Int => Int) = {
  val msg = "The %s of %d is %d."
  msg.format(name, n, f(n))
}
```


2.5 다형적 함수: 형식에 대한 추상
-------------------------

- **단형적 함수(monomorphic function)**: 한 형식의 자료에만 작용하는 함수
- **다형적 함수(polymorphic function)**: **임의의** 형식에 대해 작동하는 함수

### 2.5.1 다형적 함수의 예

```scala
def findFirst(ss: Array[String], key: String): Int = {
  @annotation.tailrec
  def loop(n: Int): Int = 
    if (n >= ss.length) -1
    else if (ss(n) == key) n
    else loop(n + 1)
  
  loop(0)
}
```

위의 `findFirst` 함수는 `Array[String]`에서 주어진 `String`과 일치하는 첫 번째 인덱스 값을 돌려주는 **단형적** 함수이다. 이를 임의의 형식 `A`에 대해 `Array[A]`에서 `A`를 찾는 **다형적** 함수로 변경하면 다음과 같다.

```scala
def findFirst[A](as: Array[A], p: A => Boolean): Int = {
  @annotation.tailrec
  def loop(n: Int): Int = 
    if (n >= as.length) -1
    else if (p(as(n)) n
    else loop(n + 1)
  
  loop(0)
}
```

> ■ 연습문제 2.2
> 
> Array[A]가 주어진 비교 함수에 의거해서 정렬되어 있는지 점검하는 isSorted 함수를 구현하라. 서명은 다음과 같다.
> 
> `def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean`

### 2.5.2 익명 함수로 고차 함수 호출

고차 함수를 호출할 때, 기존의 이름 붙은 함수를 인수로 지정해서 호출하는 것이 아니라 **익명 함수(anonymous function)** 또는 **함수 리터럴(function literal)**을 지정해서 호출하는 것이 편리한 경우가 많다.

```
scala> findFirst(Array(7, 9, 13), (x: Int) => x == 9)
res2: Int = 1
```

`(x: Int) => x == 9`라는 구문은 **함수 리터럴** 또는 **익명 함수**이다. 함수의 인수들은 `=>`의 좌변에서 선언된다. 정수 두 개를 받아서 서로 같은지 판단하는 함수는 `(x: Int, y: Int) => x == y`로 정의한다.

> **스칼라에서 값으로서의 함수**
> 
> 함수 리터럴을 정의할 때 실제로 정의되는 것은 `apply`라는 메서드를 가진 하나의 객체이다. `apply`라는 메서드를 가진 객체는 그 자체를 메서드인 것처럼 호출할 수 있다. `(a, b) => a < b`는 사실 다음과 같은 객체 생성에 대한 syntactic sugar이다.
> 
> ```scala
> val lessThan = new Function2[Int, Int, Boolean] {
>   def apply(a: Int, b: Int) = a < b
> }
> ```
> 
> `lessThan`을 `lessThan(10, 20)` 형태로 호출하는 것은 `lessThan.apply(10, 20)`에 대한 syntactic sugar이다.
> 
> `Function2`는 표준 스칼라 라이브러리가 제공하는 보통의 **특질(trait)**로 인수 두 개를 받는 함수 객체들을 대표하며 Function0~22의 trait을 지원한다. (scala version 2.11.8)


2.6 형식에서 도출된 구현
------------------

다형적 함수를 구현할 때에는 가능한 구현들의 공간이 크게 줄어든다. 주어진 다형적 형식에 대해 단 하나의 구현만 가능해질 정도로 가능성의 공간이 축소되는 경우도 있다. 단 한 가지 방법으로만 구현할 수 있는 함수 서명의 예를 살펴본다.

##### 부분 적용(partial application)

인자 목록이 둘 이상 있는 함수의 경우, 필요한 인자 중 일부만 적용해 새로운 함수를 정의

```scala
def partial1[A, B, C](a: A, f: (A, B) => C): B => C =
  (b: B) => f(a, b)
```

##### 커링(currying)

여러 인자를 취하는 함수를 단 하나의 인자를 취하는 함수의 연속으로 변환

> ■ 연습문제 2.3
> 
> 또 다른 예로, 인수가 두 개인 함수 f를 인수 하나를 받고 그것으로 f를 부분 적용하는 함수로 변환하는 **커링(currying)**을 살펴보자. 이번에도 컴파일되는 구현은 단 한 가지이다. 그러한 구현을 작성하라.
> 
> `def curry[A, B, C](f: (A, B) => C): A => (B => C)`

> □ 연습문제 2.4
> 
> curry의 변환을 역으로 수행하는 고차 함수 uncurry를 구현하라. =>는 오른쪽으로 묶이므로, A => (B => C)를 A => B => C라고 표기할 수 있음을 주의할 것.
> 
> `def uncurry[A, B, C](f: A => B => C): (A, B) => C`

##### 함수 합성(function composition)

한 함수의 출력을 다른 함수의 입력으로 공급

> ■ 연습문제 2.5
> 
> 두 함수를 합성하는 고차 함수를 구현하라.
> 
> `def compose[A, B, C](f: B => C, g: A => B): A => C`

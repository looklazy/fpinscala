# 4장. 예외를 이용하지 않은 오류 처리

오류를 함수적으로 제기하고 처리하는 기본 원리

- 실패 상황과 예외를 보통의 값으로 표현할 수 있다.
  - 참조 투명성을 유지
- 일반적인 오류 처리/복구 패턴을 추상화한 고차 함수를 작성할 수 있다.
  - **오류 처리 논리의 통합(consolidation of error-handling logic)** 유지

## 4.1 예외의 장단점

```scala
def failingFn(i: Int): Int = {
  val y: Int = throw new Exception("fail!")
  try {
    val x = 42 + 5
    x + y
  }
  catch { case e: Exception => 43 }
}
```

##### `y`가 참조에 투명하지 않음을 증명

```scala
def failingFn2(i: Int): Int = {
  try {
    val x = 42 + 5
    x + ((throw new Exception("fail!")): Int)
  }
  catch { case e: Exception => 43 }
```

`x + y`의 `y`를 `throw new Exception("fail!")`로 치환하면 다른 결과가 나온다.

```
scala> failingFn(12)
java.lang.Exception: fail!
  at .failingFn(<console>:12)
  ... 32 elided  
  
scala> failingFn2(12)
res1: Int = 43
```

- 참조에 투명한 표현식: **문맥(context)에 의존하지 않으며 지역저으로 추론 가능**
- 참조에 투명하지 않은 표현식: **문맥에 의존적이고(context-depent)** 좀 더 전역의 추론이 필요

예외의 주된 문제 두 가지

- 예외는 참조 투명성을 위반하고 문맥 의존성을 도입한다.
- 예외는 형식에 안전하지 않다.

예외의 대안

- 예외를 던지는 대신, 예외적인 조건이 발생했음을 뜻하는 값을 돌려준다.
- 형식에 완전히 안전하며, 최소한의 구문적 잡음으로도 실수를 미리 발견할 수 있다.

###### 점검된 예외(checked exception)

> Java의 checked exception은 오류를 처리할 것인지 다시 발생시킬 것인지의 결정을 강제하나, 판에 박힌(boilerplate) 코드가 추가된다. 무엇보다, checked exception은 **고차 함수에는 통하지 않는다.**
> 
> `def map[A, B](l: List[A])(f: A => B): List[B]`
> 
> 위 함수를 `f`가 던질 수 있는 모든 checked excpetion마다 개별적인 버전으로 만들 수는 없는 일이다. 그렇게 한다 해도, 어떤 예외가 가능한지 map이 알 수 있게 할 방법이 없다. 이는 Java에서도 일반적 코드가 RuntimeException이나 공통의 checked exception 형식에 의존할 수 밖에 없는 이유이다.

## 4.2. 예외의 가능한 대안들

```scala
def mean(xs: Seq[Double]): Double =
  if (xs.isEmpty) // 빈 목록에 대해서는 평균이 정의되지 않는다.
    throw new ArithmeticException(“mean of empty list!”)
  else xs.sum / xs.length
```

`mean` 함수는 **부분 함수(partial function)**의 예이다. `mean` 함수에 대해 예외 대신 사용할 수 있는 대안 몇 가지를 살펴본다.

##### 첫 번째 대안: Double 형식의 가짜 값을 리턴

모든 경우에 `xs.sum / xs.length`를 돌려준다면 빈 목록에 대해서는 `0.0/0.0`을 돌려주게 되는데, 이는 `Double.NaN`이다. 아니면 다른 어떤 경계 값(sentinel value)이나 `null`을 돌려줄 수도 있다.

이런 접근 방식은 예외 기능이 없는 언어에서 오류를 처리하는데 흔히 사용하지만, 우리는 아래 이유로 사용하지 않는다.

- 오류가 소리 없이 전파될 수 있다. 호출자가 이런 오류 조건의 점검을 실수로 빼먹어도 컴파일러가 경고해 주지 않으며, 그러면 이후의 코드가 제대로 작동하지 않을 수 있다.
- 호출하는 쪽에서 ‘진짜’ 결과를 받았는지 점검하는 명시적 `if`문들로 구성된 boilerplate 코드가 늘어난다.
- 다형적 코드에 적용할 수 없다. 출력 형식에 따라서는 그 형식의 경계 값을 결정하는 것이 불가능할 수도 있다.
- 호출자에게 특별한 방침이나 호출 규약을 요구하여 단순히 함수를 호출하는 것 이상의 작업이 필요해진다. 이는 모든 인수를 균일한 방식으로 처리해야 하는 고차 함수로의 전달을 어렵게 만든다.

##### 또 다른 대안: 입력을 처리할 수 없는 상황에 해야할 일을 호출자에게 위임

```scala
def mean_1(xs: IndexedSeq[Double], onEmpty: Double): Double = 
  if (xs.isEmpty) onEmpty
  else xs.sum / xs.length
```

예외 상황에 대한 처리를 호출자에게 위임함으로서 `mean`은 부분 함수가 아닌 완전 함수(total function)가 된다. 그러나 예외 처리 방식을 함수의 직접적인 호출자가 알고 있어야 하고, 그런 경우에도 항상 하나의 Double 값을 결과로 돌려주어야 한다는 단점이 있다.

## 4.3 Option 자료 형식

함수가 항상 답을 내지는 못한다는 점을 반환 형식을 통해서 명시적으로 표현

```scala
sealed trait Option[+A]
case class Some[+A](get: A) extends Option[A]
case object None extends Option[Nothing]
```

`Option`은 값을 정의할 수 있는 경우 `Some`이 되고, 정의할 수 없는 경우에는 `None`이 된다.

```scala
def mean(xs: Seq[Deouble]): Option[Double] = 
  if (xs.isEmpty) None
  else Some(xs.sum / xs.length)
```

함수가 선언된 반환 형식(Option[Double])을 반환한다는 사실은 여전하므로 `mean` 함수는 이제 하나의 **완전 함수**이다. 이 함수는 입력 형식의 모든 값에 대해 정확히 하나의 출력 형식 값을 돌려준다.

### 4.3.1 Option의 사용 패턴

부분 함수는 프로그래밍에서 흔히 볼 수 있으며, FP에서는 그런 부분성을 흔히 Option 같은 자료 형식(또는 Either 자료 형식)으로 처리한다.

- [Map](http://www.scala-lang.org/api/current/index.html#scala.collection.Map)에서 주어진 키를 찾는 함수는 Option을 돌려준다.
- [목록과 기타 반복 가능 자료 형식](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List)에 정의된 headOption과 lastOption은 순차열이 비지 않은 경우 첫 요소 또는 마지막 요소를 담은 Option을 돌려준다.

Option이 편리한 이유는, 오류 처리의 공통 패턴을 고차 함수들을 이용해서 추출함으로써 예외 처리 코드에 흔히 수반되는 boilerplate 코드를 작성하지 않아도 된다는 점이다.

#### Option에 대한 기본적인 함수들

Option은 최대 하나의 원소를 담을 수 있다는 점을 제외하면 List와 비슷하다.

```scala
trait Option[+A] {
  def map[B](f: A => B): Option[B]
  def flatMap[B](f: A => Option[B]): Option[B]
  def getOrElse[B >: A](default: => B): B
  def orElse[B >: A](op: => Option[B]): Option[B]
  def filter(f: A => Boolean): Option[A]
}
```

###### 몇 가지 새로운 구문

> `default: => B`: **비엄격성(non-strictness)**을 표현하는 구문으로, 인수가 실제로 쓰일 때까지 평가되지 않는다.  
> `B >: A`: B가 반드시 A와 같거나 A의 **상위형식(supertype)**이어야 함을 뜻한다.

###### ■ 연습문제 4.1

> 목록 4.2에 나온 Option에 대한 함수들을 모두 구현하라. 각 함수를 구현할 때 그 함수가 어떤 일을 하고 어떤 상황에서 쓰일 것인지 생각해 볼 것. 이 함수들 각각의 용도를 잠시 후에 보게 될 것이다. 다음은 이 연습문제를 위한 몇 가지 힌트이다.
> 
> - 패턴 부합을 사용해도 좋으나, map과 getOrElse를 제외한 모든 함수는 패턴 부합 없이도 구현할 수 있어야 한다.
> - map과 flatMap의 형식 서명은 구현을 결정하기에 충분해야 한다.
> - getOrElse는 Option의 Some 안의 결과를 돌려준다. 단, Option이 None이면 주어진 기본값을 돌려준다.
> - orElse는 첫 Option이 정의되어 있으면 그것을 돌려주고 그렇지 않으면 둘째 Option을 돌려준다.

#### 기본적인 Option 함수들의 용례

`map` 함수는 Option 안의 결과를 변환하는 데 사용할 수 있다.

```scala
case class Employee(name: String, department: String)

def lookupByName(name: String): Option[Employee] = ...

val joeDepartment: Option[String] = 
  lookupByName("Joe").map(_.department)
```

만약 `lookupByName("Joe")`가 `None`을 돌려주었다면 **계산의 나머지 부분이 취소**되어서 `map`은 `_.department`를 호출하지 않는다.

변환을 위해 지정한 함수 자체가 실패할 수 있다는 점만 빼면 `flatMap`도 이와 비슷하다.

###### ■ 연습문제 4.2

> variance 함수를 flatMap을 이용해서 구현하라. 순차열의 평균이 m이라 할 때, 분산(variance)은 순차열의 각 요소 x에 대한 math.pow(x - m, 2)들의 평균이다. 분산의 좀 더 자세한 정의는 [위키백과](https://en.wikipedia.org/wiki/Variance#Definition)를 참고하기 바란다.
> 
> `def variance(xs: Seq[Double]): Option[Double]`

`filter`는 성공적인 값이 주어진 술어와 부합하지 않을 때 성공을 실패로 변환하는데 사용할 수 있다.

```scala
val dept: String = 
  lookupByName("Joe").map(_.dept)
                     .filter(_ != "Accounting")
                     .getOrElse("Default Dept")
```

`orElse`는 `getOrElse`와 비슷하되 첫 Option이 정의되지 않으면 다른 Option을 돌려준다.

흔한 관용구로, `o.getOrElse(throw new Exception("FAIL"))`은 Option의 None의 경우를 예외로 처리되게 만든다. _합리적인 프로그램이라면 결코 예외를 잡을 수 없을 상황에서만 예외를 사용한다._

이상에서 보듯이 오류를 보통의 값으로서 돌려주면 코드를 짜기가 편해지며, 고차 함수를 사용함으로써 예외의 주된 장점인 오류 처리 논리의 통합과 격리도 유지할 수 있다.

### 4.3.2 예외 지향적 API의 Option 합성과 승급, 감싸기

보통의 함수를 Option에 대해 작용하는 함수로 **승급(lift)** 시킬 수 있다.

```scala
def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

val abs0: Option[Double] => Option[Double] = lift(math.abs)
```

`lift`가 있으면 어떤 함수라도 Option 값의 **문맥 안에서** 작용하도록 변환할 수 있다.

##### 또 다른 예: 보험료율(insurance rate) 함수

```scala
/**
 * 두 가지 핵심 요인으로 연간 자동차 보험료를 계산하는 일급비밀 공식
 */
def insuranceRateQuote(age: Int, numberOfSpeedingTickets: Int): Double
```

`insuranceRateQuote` 함수를 사용하기 위해서 `age`와 `numberOfSpeedingTickets`을 넘겨주어야 하는데, 이 값이 문자열로 되어있을 경우 정수 값으로 파싱해야 한다.

```scala
def parseInsuranceRateQuote(
  age: String, 
  numberOfSpeedingTickets: String): Option[Double] = {
    val optAge: Option[Int] = Try(age.toInt)
    val optTickets: Option[Int] = Try(numberOfSpeedingTickets.toInt)

    insuranceRateQuote(optAge, optTickets) // 형식이 맞지 않는다.
  }

def Try[A](a: => A): Option[A] = 
  try Some(a)
  catch { case e: Exception => None }
```

`optAge`와 `optTickets`은 `Option[Int]` 형식인데, `insuranceRateQuote` 함수는 `Int` 형식을 요구한다. `insuranceRateQuote` 함수를 수정하는 대신, 그것을 Option 문맥 안에서 작동하도록 승급시키는 것이 바람직하다.

###### ■ 연습문제 4.3

> 두 Option 값을 이항 함수(binary function)을 이용해서 결합하는 일반적 함수 map2를 작성하라. 두 Option 값 중 하나라도 None이면 map2의 결과 역시 None이어야 한다. 서명은 다음과 같다.
> 
> `def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C]`

##### map2로 parseInsuranceRateQuote를 구현한 예

```scala
def parseInsuranceRateQuote(
  age: String,
  numberOfSpeedingTickets: String): Option[Double] = {
    val optAge: Option[Int] = Try { age.toInt }
    val optTickets: Option[Int] = Try { numberOfSpeedingTickets.toInt }
    
    map2(optAge, optTickets)(insuranceRateQuote)
  }
```

###### ■ 연습문제 4.4

> Option들의 목록을 받고, 그 목록에 있는 모든 Some 값으로 구성된 목록을 담은 Option을 돌려주는 함수 sequence를 작성하라. 원래의 목록에 None이 하나라도 있으면 함수의 결과도 None이어야 한다. 그렇지 않으면 원래의 목록에 있는 모든 값의 목록을 담은 Some을 돌려주어야 한다. 서명은 다음과 같다.
> 
> `def sequence[A](a: List[Option[A]]): Option[List[A]]`

실패할 수 있는 함수를 목록에 사상했을 때 만일 목록의 원소 중 하나라도 None을 돌려주면 전체 결과가 None이 되어야 하는 경우가 있다.

```scala
def parseInts(a: List[String]): Option[List[Int]] = 
  sequence(a map (i => Try(i.toint)))
```

위의 예에서 처럼 map의 결과를 sequence로 순차 결합하는 방식은 목록을 두 번 훑어야 하기 때문에 비효율적이다. 이러한 map의 결과의 순차 결합은 흔한 작업이기 때문에 다음과 같은 서명의 일반적 함수 `traverse`가 필요하다.

```scala
def traverse[A, B](a: List[A]))(f: A => Option[B]): Option[List[B]]
```

###### ■ 연습문제 4.5

> 이 함수를 구현하라. map과 sequence를 사용하면 간단하겠지만, 목록을 단 한 번만 훑는 좀 더 효율적인 구현을 시도해 볼 것. 더 나아가서, sequence를 이 traverse로 구현해 보라.

###### for-함축(for-comprehension)

> 스칼라에서 이런 승급 함수들이 흔히 쓰이기 때문에, **for-comprehension**이라고 하는 특별한 구문을 제공한다. for-comprehension은 자동으로 flatMap, map 호출들로 전개된다.
> 
> **원래의 버전**
> 
> ```scala
> def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
>   a flatMap (aa => 
>     b map (bb =>
>       f(aa, bb)))
> ```
> 
> **for-comprehension을 이용한 버전**
> 
> ```scala
> def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
>   for {
>     aa <- a
>     bb <- b
>   } yield f(aa, bb)
> ```
> 
> for-comprehension 구문은 중괄호쌍 안에 `aa <- a` 같은 binding이 있고 그 다음에 yield 표현식이 오는 형태이다. 컴파일러는 이러한 binding들을 flatMap 호출로 전개하되, 마지막 binding과 yield는 map 호출로 변환한다.

## 4.4 Either 자료 형식

- 실패의 원인을 추적할 수 있다.
- 둘 중 하나일 수 있는 값들을 대표한다.
- 두 형식의 분리합집합(disjoint union)이라 할 수 있다.
- Left, Right 값을 가지며 Right는 성공, Left는 실패에 사용한다. (scala convention; right is right)

```scala
sealed trait Either[+E, +A]
case class Left[+E](value: E) extends Either[E, Nothing]
case class Right[+A](value: A) extends Either[Nothing, A]
```

##### Exception 대신 예외 정보를 돌려주는 예제

```scala
def mean(xs: IndexedSeq[Double]): Either[String, Double] = 
  if (xs.isEmpty) 
    Left("mean of empty list!") // ArithmeticException을 직접 던지는 대신 String을 돌려준다.
  else
    Right(xs.sum / xs.length)
  
def Try[A](a: => A): Either[Exception, A] =
  try Right(a)
  catch { case e: Exception => Left(e) }
  
def safeDiv(x: Int, y: Int): Either[Exception, Int] = 
  Try(x / y)
  
def parseInsuranceRateQuote(age: String, numberOfSpeedingTickets: String): Either[Exception, Double] =
  for {
    a <- Try { age.toInt }
    tickets <- Try { numberOfSpeedingTickets.toInt }
  } yield insuranceRateQuote(a, tickets)
  
def insuranceRateQuote(age: Int, numberOfSpeedingTickets: Int): Double = ??? 
```

## 4.5 요약

예제에서는 대수적 자료 형식 Option, Either에 초점을 맞췄지만 좀 더 일반적인 착안은 예외를 보통의 값으로 표현하고 고차 함수를 이용해서 오류 처리 및 전파의 공통 패턴들을 캡슐화한다는 것이다. 
이를 더욱 일반화하면 임의의 효과를 값으로 표현한다는 착안이 된다.

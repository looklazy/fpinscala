1장. 함수형 프로그래밍이란 무엇인가?
============================

함수형 프로그래밍(Functional Programming)은 프로그램을 오직 **순수 함수(pure function)**들로만 구축하는 것을 말한다. 순수 함수는 **부수 효과(side effect)**가 없는 함수를 말하며, 부수 효과란 *결과를 돌려주는 것 이외의 어떤 일*을 말한다. 

부수 효과의 몇 가지 예는 다음과 같다.

- 변수를 수정한다.
- 자료구조를 제자리에서 수정한다.
- 객체의 필드를 설정한다.
- 예외(exception)를 던지거나 오류를 내면서 실행을 중단한다.
- 콘솔에 출력하거나 사용자의 입력을 읽어들인다.
- 파일에 기록하거나 파일에서 읽어들인다.
- 화면에 그린다.

위와 같은 일들을 수행할 수 없는 프로그래밍을 상상해보면, 다음과 같은 질문들이 떠오른다.

- 변수를 수정할 수 없다면, 루프는 어떻게 작성할까?
- 변하는 자료는 어떻게 다루어야 할까?
- 예외를 던지지 않고 오류를 처리하려면?
- 화면에 뭔가를 그리거나 파일 입출력을 수행하는 프로그램은?

FP는 프로그램을 작성하는 방식에 대한 제약이지 표현 가능한 프로그램의 종류에 대한 제약이 아니다. 

순수 함수로 프로그램을 작성하면 모듈성(modularity)이 향상되며, 이는 곧 테스트, 재사용, 병렬화, 일반화, 분석이 쉽다는 것을 의미한다. 순수 함수는 버그가 생길 여지가 훨씬 적다.

간단한 프로그램에서 부수 효과를 제거해 가면서 FP의 이점을 살펴보고 FP의 중요한 두 가지 개념, **참조 투명성(referential transparency)**과 **치환 모델(substitution model)**에 대해서 알아보자.


1.1 FP의 이점: 간단한 예제 하나
------------------------

### 1.1.1 부수 효과가 있는 프로그램

```scala
class Cafe {
  def buyCoffee(cc: CreditCard): Coffee = {
    val cup = new Coffee()
    cc.charge(cup.price)
    cup
  }
}
```

위 프로그램에서 `cc.charge(cup.price)`가 부수 효과의 예이다. `cc.charge`라는 신용카드 청구 행위에서 프로그램에서는 카드사와 연결하여 트랜잭션을 승인하고 대금을 청구하고 거래 기록을 저장하는 등의 외부와의 상호 작용이 발생한다. 히지만 이 프로그램에서는 단지 `Coffee` 객체 하나를 리턴할 뿐이고 그 외의 동작은 부수적으로(on the side) 일어난다.

위 프로그램에 대한 유닛 테스트를 작성한다고 생각해보자. 테스트를 위해 실제 신용카드 청구를 할 수는 없기 때문에 `cc.charge`는 실제로 외부와 연결되면 안되며 이는 테스트 작성을 어렵게 만든다. *(= 검사성(testability)이 부족하다)*

`CreditCard`에서 결제와 관련된 부분을 제거하고 이를 담당하는 `Payments` 객체를 `buyCoffee`에 전달한다면 코드의 모듈성과 검사성을 좀 더 높일 수 있다.

```scala
class Cafe {
  def buyCoffee(cc: CreditCard, p: Payments): Coffee = {
    val cup = new Coffee()
    p.charge(cc, cup.price)
    cup
  }
}
```

그래도 여전히 `p.charge(cc, cup.price)`를 호출할 때 부수 효과가 발생한다. `Payments` 인터페이스에 대한 Mock을 만들어 테스트 작성은 좀 더 쉬워질 수 있으나 이상적인 방법은 아니다. 예를 들어 `buyCoffee` 호출 이후에 어떤 상태 값을 검사해야 한다고 한다면, 테스트 과정에서 `p.charge` 호출에 의해 그 상태가 적절히 변경되었는지 확인해야 한다.

테스트 문제 외에도 함수를 재사용하기 어렵다는 문제가 있다. 커피 한 잔이 아니라 열두 잔을 주문한다고 했을 때, `buyCoffee`를 열두 번 호출하게 되면 카드 수수료가 12회 청구될 것이다.

### 1.1.2 함수적 해법: 부수 효과의 제거

이에 대한 함수적 해법은 부수 효과를 제거하고 `buyCoffee`가 `Coffee`뿐만 아니라 **청구서(Charge)**를 돌려주게 하는 것이다.

```scala
class Cafe {
  def buyCoffee(cc: CreditCard): (Coffee, Charge) = {
    val cup = new Coffee()
    (cup, Charge(cc, cup.price))
  }
}

case class Charge(cc: CreditCard, amount: Double) {
  def combine(other: Charge): Charge = 
    if (cc == other.cc) // scala의 if는 statement가 아니라 expression이다.
      Charge(cc, amount + other.amount)
    else
      throw new Exception("Can't combine charges to different cards")
}
```

관심사의 분리(sepration of concerns)를  통해  청구서의 **생성** 문제가 이에 대한 **처리** 또는 **연동** 문제와 분리되었다. 이제 여러 잔의 커피를 구매하기 위해 `buyCoffee`를 재사용할 수 있다.

```scala
class Cafe {
  def buyCoffee(cc: CreditCard): (Coffee, Charge) = ???
  
  def buyCoffees(cc: CreditCard, n: Int): (List[Coffee], Charge) = {
    val purchases: List[(Coffee, Charge)] = List.fill(n)(buyCoffee(cc))
    val (coffees, charges) = 
      purchases.unzip(coffees, charges.reduce((c1, c2) => c1.combine(c2)))
  }
}
```

`buyCoffee`를 직접 재사용하여 `buyCoffees` 함수를 정의하였으며, 두 함수 모두 `Payments` 인터페이스의 Mock 없이도 쉽게 테스트를 작성할 수 있게 되었다. `Charge`를 일급 함수로 만들면 이를 조합하여 같은 카드에 대한 청구서들을 하나의 `List[Charge]`로 취합하는 것도 가능하다.

```scala
def coalesce(charges: List[Charge]): List[Charge] =
  charges.groupBy(_.cc).values.map(_.reduce(_ combine _)).toList
```

FP는 많은 사람들이 좋다고 여기는 생각을 논라적인 극한으로까지 밀어붙이는, 그리고 그것을 언뜻 보기에는 적용할 수 없을 것 같은 상황에도 적용하는 규율일 뿐이다.


1.2 (순수)함수란 구체적으로 무엇인가?
----------------------------

FP는 순수 함수로 프로그래밍하는 것이며 순수 함수는 부수 효과가 없는 함수라는 것을 알아봤다. 함수적으로 프로그래밍한다는 것이 무슨 뜻인지 좀 더 구체적으로 이해하려면 효과와 순수라는 개념을 공식적으로 정의할 필요가 있다. 그러한 정의는 함수형 프로그래밍의 또 다른 이점인, 순수 함수는 추론(분석)하기가 더 쉽다는 점을 이해하는 데에도 도움이 된다.

`f: A => B`에서 b는 오직 a의 값에 의해서만 결정되며 내부의 상태 변화나 외부의 처리 과정은 `f(a)`의 결과를 결정하는데 어떠한 영향도 주지 않는다. 즉, 함수는 주어진 입력으로 결과를 계산하는 것 외에는 프로그램의 실행에 어떠한 영향도 미치지 않는다. 이를 두고 부수 효과가 없다고 말하며 좀 더 명시적으로 순수 함수라고 부르기도 한다. 

순수 함수의 이러한 개념을 **참조 투명성(referential transparency)**이라는 개념을 이용해서 공식화할 수 있다. 참조 투명성은 함수가 아니라 **표현식(expression)**의 한 속성이다. 임의의 프로그램에서 어떤 표현식을 그 평가 결과로 바꾸어도 프로그램의 의미가 변하지 않는다면 그 표현식은 참조에 투명하다. 예를 들어 프로그램에 있는 `2 + 3`을 모두 값 `5`로 바꾸어도 프로그램의 의미는 변경되지 않는다.


1.3 참조 투명성, 순수성, 그리고 치환 모델
-------------------------------

```scala
def buyCoffee(cc: CreditCard): Coffee = {
  val cup = new Coffee()
  cc.charge(cup.price)
  cup
}
```

위 예제에서 `cc.charge(cup.price)`의 반환 값은 무시되어 `buyCoffee`의 결과 값은 `new Coffee()`와 같아진다. `buyCoffee`가 순수하려면 임의의 `p`에 대해 `p(buyCoffee(cc))`가 `p(new Coffee())`와 동등해야 하지만, 그렇지 않음을 알 수 있다. 

참조 투명성은 함수가 수행하는 모든 것이 함수가 돌려주는 값으로 대표된다는 불변(invariant) 조건을 강제한다. 이러한 제약은 **치환 모형(substitution model)**이라고 불리는 프로그램 평가에 대한 추론 모형을 가능하게 한다. 표현식이 참조에 투명하다면 그 계산 과정은 대수 방정식을 풀 때와 비슷해지며 프로그램에 대한 **등식적 추론(equational reasoning)**을 가능하게 한다.

- 참조에 투명한 함수의 예.

  ```
  scala> val x = "Hello, World"
  x: java.lang.String = Hello, World
    
  scala> val r1 = x.reverse
  r1: String = dlroW ,olleH
    
  scala> val r2 = x.reverse
  r2: String = dlroW ,olleH // r1과 r2는 같다.
  
  // `x`항의 모든 출현을 `x`가 지칭하는 표현식으로 치환
    
  scala> val r1 = "Hello, World".reverse
  r1: String = dlroW ,olleH
    
  scala> val r2 = "Hello, World".reverse
  r2: String = dlroW ,olleH // r1과 r2는 여전히 같다.
  ```
	
- 참조에 투명하지 않은 함수의 예.

  ```
  scala> val x = new StringBuilder("Hello")
  x: java.lang.StringBuilder = Hello
    
  scala> val y = x.append(", World")
  y: java.lang.StringBuilder = Hello, World
    
  scala> val r1 = y.toString
  r1: java.lang.String = Hello, World
    
  scala> val r2 = y.toString
  r2: java.lang.String = Hello, World // r1과 r2는 같다.
  
  // `y`항의 모든 출현을 해당 `append` 호출로 치환
  
  scala> val x = new StringBuilder("Hello")
  x: java.lang.StringBuilder = Hello
    
  scala> val r1 = x.append(", World").toString
  r1: java.lang.String = Hello, World
    
  scala> val r2 = x.append(", World").toString
  r2: java.lang.String = Hello, World, World // 이제 r1과 r2는 같지 않다.
  ```

참조에 투명하지 않은 함수의 경우 `r1`과 `r2`가 같은 표현식처럼 보이지만 `r2`가 `x.append`를 호출하는 시점에서 `r1`으로 인해 `x`가 참조하는 객체가 변이되었다. 부수 효과로 인해 프로그램 결과에 대한 추론이 어려워지는 예이다. 반면 치환 모형은 부수 효과가 평가되는 표현식 자체에만 영향을 미치므로 **국소 추론(local reasoning)** 만으로 코드를 이해할 수 있다.

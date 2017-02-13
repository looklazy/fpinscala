3장. 함수적 자료구조
================

3.1 함수적 자료구조의 정의
--------------------

함수적 자료구조는 **불변이(immutable)**이다. 

> 예를 들어 순수 함수 `+`에서, `3 + 4`을 수행했을 때 3이나 4의 값이 변경되는 일 없이 새로운 값 7이 생성된다. 함수적 자료구조로 정의된 List에 새로운 항목을 추가해도 기존 List가 변경되지는 않는다.

```scala
package fpinscala.datastructures

sealed trait List[+A]
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x, xs) => x + sum(xs)
  }
  
  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x, xs) => x * product(xs)
  }
  
  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))
}
```

- 특질(trait)은 일부 메서드의 구현을 담을 수 있는 추상 인터페이스로, `sealed`는 이 특질의 모든 구현이 반드시 같은 파일 안에 선언되어 있어야 함을 뜻한다.
- `Nil`과 `Cons`는 `List`의 두 가지 구현, 즉 두 가지 **자료 생성자(data constructor)**이다.
- 형식 매개 변수 A에 있는 `+`는 **공변(covariant)**을 뜻한다.

> **공변과 불변에 대해**
> 
> `trait List[+A]` 선언에서 `A` 앞의 `+`는 `A`가 `List`의 **공변(covariant)** 매개 변수임을 뜻하는 **가변 지정자(variance annotation; 가변 주해)**로서, 양의(positive) 매개변수라고 하기도 한다.
> 
> 예를 들어, `Dog`가 `Animal`의 하위형식이면 `List[Dog]`가 `List[Animal]`의 하위형식으로 간주된다.
> 
> `case object Nil extends List[Nothing]`에서 `Nothing`은 모든 형식의 하위형식이고 `A`가 공변이므로 `Nil`은 `List[Int]`, `List[Double]` 등 어떤 형식으로도 간주될 수 있다.


3.2 패턴 부합
-----------

##### 동반 객체(companion object)

class나 trait와 동일한 이름을 사용하는 객체로서, 동반되는 class나 trait의 private 필드나 메서드에 접근할 수 있다.

##### 패턴 부합(pattern matching)

패턴 부합은 표현식의 구조를 따라 내려가는 `switch`문과 비슷하며 표현식(대상(target) 또는 검사자(scrutinee))으로 시작하여 키워드 `match`가 오고 일련의 `case`가 `{}`으로 감싸인 형태이다. `{}`에는 좌변에 패턴, 우변에 결과가 서술된다.

```scala
List(1, 2, 3) match { case _ => 42 } // 42
```

변수 패턴 `_`는 임의의 표현식을 뜻하며 그 결과는 무시된다.

```scala
List(1, 2, 3) match { case Cons(h, _) => h } // 1
List(1, 2, 3) match { case Cons(_, t) => t } // List(2, 3)
```

`List(1, 2, 3)`은 `Cons(1, Cons(2, Cons(3, Nil)))`과 같으며, `case Cons(h, t)`로 패턴 매칭을 하면 변수 `h`에 `1`이, `t`에 `Cons(2, Cons(3, Nil))`이 바인딩된다.

```scala
List(1, 2, 3) match { Nil => 42 } // MatchError
```

`case` 구문에서 매칭되는 표현식이 없는 경우 RuntimeException인 `MatchError`가 발생한다.

###### ■ 연습문제 3.1

> 다음 패턴 부합 표현식의 결과는 무엇인가?
> 
> ```scala
> val x = List(1, 2, 3, 4, 5) match {
>   case Cons(x, Cons(2, Cons(4, _))) => x
>   case Nil => 42
>   case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
>   case Cons(h, t) => h + sum(t)
>   case _ => 101
> }
> ```

##### 스칼라의 가변 인수 함수

```scala
def apply[A](as: A*): List[A] =
  if (as.isEmpty) Nil
  else Cons(as.head, apply(as.tail: _*))
```

**가변 인수 함수(variadic function)**는 지정된 타입의 매개변수를 0개 이상 받을 수 있음을 뜻한다. 가변 인수 함수는 요소들의 순차열을 나타내는 `Seq`를 생성하고 전달하기 위한 syntactic sugar이며 특별한 타입 애노테이션 `_*`은 Seq를 가변 인수 메서드에 전달 할 수 있게 한다.
	

3.3 함수적 자료구조의 자료 공유
------------------------

##### 자료 공유(data sharing)

기존 목록 `xs` 앞에 `1`이라는 새로운 요소를 추가하려면 `Cons(1, xs)`라는 새 목록을 만든다. 목록은 불변이므로 `xs`를 복사할 필요없이 재사용하면 된다. 마찬가지로 목록 `mylist = Cons(x, xs)`의 첫 요소를 제거하려면 xs를 돌려주면 된다.

이 과정에서 기존 목록에 대한 변경은 일어나지 않으며 여전히 사용 가능한 상태로 존재한다. 이를 두고 함수적 자료구조는 **영속적(persistent)**이라고 한다.

###### ■ 연습문제 3.2

> List의 첫 요소를 제거하는 함수 tail을 구현하라. 이 함수가 상수 시간으로 실행됨을 주의할 것. Nil인 List도 지원하도록 독자의 구현을 수정하는 여러 가지 방법들도 고려해 보라. 이에 대해서는 다음 장에서 좀 더 살펴볼 것이다.

###### ■ 연습문제 3.3

> 같은 맥락에서, List의 첫 요소를 다른 값으로 대체하는 함수 setHead를 구현하라.

### 3.3.1 자료 공유의 효율성


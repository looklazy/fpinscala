# 3장. 함수적 자료구조

## 3.1 함수적 자료구조의 정의

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

###### 공변과 불변에 대해

> `trait List[+A]` 선언에서 `A` 앞의 `+`는 `A`가 `List`의 **공변(covariant)** 매개 변수임을 뜻하는 **가변 지정자(variance annotation; 가변 주해)**로서, 양의(positive) 매개변수라고 하기도 한다.
> 
> 예를 들어, `Dog`가 `Animal`의 하위형식이면 `List[Dog]`가 `List[Animal]`의 하위형식으로 간주된다.
> 
> `case object Nil extends List[Nothing]`에서 `Nothing`은 모든 형식의 하위형식이고 `A`가 공변이므로 `Nil`은 `List[Int]`, `List[Double]` 등 어떤 형식으로도 간주될 수 있다.

## 3.2 패턴 부합

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

## 3.3 함수적 자료구조의 자료 공유

##### 자료 공유(data sharing)

기존 목록 `xs` 앞에 `1`이라는 새로운 요소를 추가하려면 `Cons(1, xs)`라는 새 목록을 만든다. 목록은 불변이므로 `xs`를 복사할 필요없이 재사용하면 된다. 마찬가지로 목록 `mylist = Cons(x, xs)`의 첫 요소를 제거하려면 xs를 돌려주면 된다.

이 과정에서 기존 목록에 대한 변경은 일어나지 않으며 여전히 사용 가능한 상태로 존재한다. 이를 두고 함수적 자료구조는 **영속적(persistent)**이라고 한다.

###### ■ 연습문제 3.2

> List의 첫 요소를 제거하는 함수 tail을 구현하라. 이 함수가 상수 시간으로 실행됨을 주의할 것. Nil인 List도 지원하도록 독자의 구현을 수정하는 여러 가지 방법들도 고려해 보라. 이에 대해서는 다음 장에서 좀 더 살펴볼 것이다.

###### ■ 연습문제 3.3

> 같은 맥락에서, List의 첫 요소를 다른 값으로 대체하는 함수 setHead를 구현하라.

### 3.3.1 자료 공유의 효율성

자료 공유를 이용하면 연산을 효율적으로 수행할 수 있다.

###### ■ 연습문제 3.4

> tail을 일반화해서, 목록에서 처음 n개의 요소를 제거하는 함수 drop을 구현하라. 이 함수의 실행 시간은 제거되는 원소의 개수에만 비례함을 주의할 것. List 전체의 복사본을 만들 필요는 없다.
> 
> `def drop[A](l: List[A], n: Int): List[A]`

###### ■ 연습문제 3.5

> 주어진 술어(predicate)와 부합하는 List의 앞 요소들(prefix)을 제거하는 함수 dropWhile을 구현하라.
> 
> `def dropWhile[A](l: List[A], f: A => Boolean): List[A]`

###### ■ 연습문제 3.6

> 그러나 모든 것이 효율적이지는 않다. 한 List의 마지막 요소를 제외한 모든 요소로 이루어진 List를 돌려주는 함수 init을 구현하라. 예를 들어 List(1, 2, 3, 4)에 대해 init은 List(1, 2, 3)을 돌려주어야 한다. 이 함수를 tail처럼 상수 시간으로 구현할 수 없는 이유는 무엇일까?
> 
> `def init[A](l: List[A]): List[A]`

##### 효율적인 예

```scala
def append[A](a1: List[A], a2: List[A]): List[A] = 
  a1 match {
    case Nil => a2
    case Cons(h, t) => Cons(h, append(t, a2))
  }
```

한 목록의 모든 요소를 다른 목록의 끝에 추가하는 함수 `append`의 실행 시간과 메모리 사용량은 `a2`를 공유하기 때문에 `a1`의 길이에만 의존한다.

##### 비효율적인 예

```scala
def init[A](l: List[A]): List[A] = 
  l match {
    case Cons(h, Nil) => Nil
    case Cons(h, t) => Cons(h, init(t))
  }
```

한 목록의 마지막 요소를 제외한 목록을 돌려주는 함수 `init`은 `Cons`의 `tail`을 치환할 때마다 반드시 이전의 모든 `Cons` 객체를 복사해야 한다.

### 3.3.2 고차 함수를 위한 형식 추론 개선

##### 명시적인 형식 선언이 필요한 예

```scala
def dropWhile[A](l: List[A], f: A => Boolean): List[A]

val xs: List[Int] = List(1, 2, 3, 4, 5)
val ex1 = dropWhile(xs, (x: Int) => x < 4)
```

위 `dropWhile` 함수의 인수 `f`에 익명 함수를 사용하려면 `(x: Int) => x < 4`와 같이 `x`의 형식을 명시적으로 표기해야 한다.

##### 고차 함수를 통해 형식 추론을 가능하게 하는 예

```scala
def dropWhile[A](as: List[A])(f: A => Boolean): List[A] =
  as match {
    case Cons(h, t) if f(h) => dropWhile(t)(f)
    case _ => as
  }

val xs: List[Int] = List(1, 2, 3, 4, 5)
val ex1 = dropWhile(xs)(x => x < 4)
```

`dropWhile(xs)`를 통해 `A`의 형식이 `Int`로 지정되었으며, 이에 따라 익명 함수 `x => x < 4`에서 `x`의 형식이 `Int`임을 컴파일러에서 추론할 수 있다.

> 이는 스칼라 컴파일러의 제약으로, Haskell이나 OCaml 같은 다른 함수형 프로그래밍 언어들은 거의 모든 상황에서 형식 주해를 생략할 수 있는 완전한 추론 기능을 제공한다.

## 3.4 목록에 대한 재귀와 고차 함수로의 일반화

`sum`과 `product`의 구현 다시 보기

```scala
def sum(ints: List[Int]): Int = ints match {
  case Nil => 0
  case Cons(x, xs) => x + sum(xs)
}

def product(ds: List[Double]): Double = ds match {
  case Nil => 1.0
  case Cons(x, xs) => x * product(xs)
}
```

두 함수의 정의가 매우 비슷하며, 이는 중복된 부분에서 부분 표현식들을 추출하여 코드를 일반화할 수 있음을 의미한다.

```scala
def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B = 
  as match {
    case Nil => z
    case Cons(x, xs) => f(x, foldRight(xs, z)(f))
  }

def sum2(ns: List[Int]) =
  foldRight(ns, 0)((x, y) => x + y)

def product2(ns: List[Double]) =
  foldRight(ns, 1.0)(_ * _)
```

###### 익명 함수를 위한 밑줄 표기법

> `x`와 `y`의 형식을 추론할 수 있다면, `(x, y) => x + y`를 `_ + _`으로 표기할 수 있다. `_`는 함수 매개변수들이 함수 본문 안에서 한 번씩만 언급될 때 유용한 단축 표기법이며 인수는 왼쪽부터 순서대로 바인딩된다.
> 
> ```scala
> _ * _    // (x, y) => x * y
> _ * 2    // x => x * 2
> _.head   // xs => xs.head
> _ drop _ // (xs, n) => xs.drop(n)
> ```
>
> 밑줄 표기법은 유용하지만 `foo(_, g(List(_ + 1), _))`과 같은 표현식은 오히려 구문을 보기 힘들게 할 수 있으므로 주의가 필요하다.

`foldRight(Cons(1, Cons(2, Cons(3, Nil))), 0)((x, y) => x + y)`의 평가 과정에 대해 추적(trace)해보면 다음과 같다.

```scala
foldRight(Cons(1, Cons(2, Cons(3, Nil))), 0)((x, y) => x + y)
1 + foldRight(Cons(2, Cons(3, Nil)), 0)((x, y) => x + y)
1 + (2 + foldRight(Cons(3, Nil), 0)((x, y) => x + y))
1 + (2 + (3 + (foldRight(Nil, 0)((x, y) => x + y))))
1 + (2 + (3 + (0)))
6
```

> `foldRight`가 하나의 값으로 축약(collapsing)되려면 반드시 목록의 끝까지 순회(traversal)해야 하며, 그 과정에서 호출 프레임을 호출 스택에 쌓게 된다.

###### ■ 연습문제 3.7

> foldRight로 구현된 product(목록3.2의 product2)가 0.0을 만났을 때 즉시 재귀를 멈추고 0.0을 돌려줄까? 왜 그럴까? 아니라면 왜 아닐까? foldRight를 긴 목록으로 호출했을 때 어떤 평가 단축이 어떤 식으로 일어나는지 고찰하라. 이는 다른 연습문제들보다 심오한 문제이며, 제5장에서 다시 살펴볼 것이다.

###### ■ 연습문제 3.8

> foldRight(List(1, 2, 3), Nil: List[Int])(Cons(_, _))처럼 Nil과 Cons 자체를 foldRight에 전달하면 어떤 일이 발생할까? 이로부터, foldRight와 List의 자료 생성자들 사이의 관계에 관해 어떤 점을 알 수 있는가?

###### ■ 연습문제 3.9

> foldRight를 이용해서 목록의 길이를 계산하라.
> 
> `def length[A](as: List[A]): Int`

###### ■ 연습문제 3.10

> 이번 절의 foldRight 구현은 꼬리 재귀가 아니므로 긴 목록에 대해서는 StackOverflowError 오류가 발생한다(이를 "**스택에 안전**(stack-safe)하지 않다"라고 말한다). 실제로 그런지 실험해 보고, 꼬리 재귀적인 또 다른 일반적 목록 재귀 함수 foldLeft를 이전 장에서 논의한 기법들을 이용해서 작성하라. 서명은 다음과 같다.
> 
> `def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B`

###### ■ 연습문제 3.11

> sum, product와 목록의 길이를 계산하는 함수를 foldLeft를 이용해서 작성하라.

###### ■ 연습문제 3.12

> 목록의 역을 돌려주는(이를테면, List(1, 2, 3)에 대해 List(3, 2, 1)을 돌려주는) 함수를 작성하라. 접기(fold) 함수를 이용해서 작성할 수 있는지 시도해 볼 것.

###### □ 연습문제 3.13

> **어려움**: foldLeft를 foldRight를 이용해서 구현할 수 있을까? 그 반대 방향은 어떨까? foldLeft를 이용하면 foldRight를 꼬리 재귀적으로 구현할 수 있으므로 긴 목록에 대해서도 스택이 넘치지 않는다는 장점이 생긴다.

###### ■ 연습문제 3.14

> append를 foldLeft나 foldRight를 이용해서 구현하라.

###### ■ 연습문제 3.15

> **어려움**: 목록들의 목록을 하나의 목록으로 연결하는 함수를 작성하라. 실행 시간은 반드시 모든 모든 목록의 전체 길이에 선형으로 비례해야 한다. 이미 정의한 함수들을 활용하도록 노력할 것.

### 3.4.1 그 외의 목록 조작 함수들

###### ■ 연습문제 3.16

> 정수 목록의 각 요소에 1을 더해서 목록을 변환하는 함수를 작성하라. (주의: 새 List를 돌려주는 순수 함수이어야 한다.)

###### ■ 연습문제 3.17

> List[Double]의 각 값을 String으로 변환하는 함수를 작성하라. d: Double을 String으로 변환할 때에는 d.toString이라는 표현식을 사용하면 된다.

###### ■ 연습문제 3.18

> 목록의 구조를 유지하면서 목록의 각 요소를 수정하는 작업을 일반화한 함수 map을 작성하라. 서명은 다음과 같다. 
> 
> `def map[A, B](as: List[A])(f: A => B): List[B]`

###### ■ 연습문제 3.19

> 목록에서 주어진 술어를 만족하지 않는 요소들을 제거하는 함수 filter를 작성하라. 그리고 그 함수를 이용해서 List[Int]에서 모든 홀수를 제거하라.
> 
> `def filter[A](as: List[A])(f: A -> Boolean): List[A]`

###### ■ 연습문제 3.20

> map과 비슷하되 하나의 요소가 아니라 목록을 최종 결과 목록에 삽입하는 함수 flatMap을 작성하라. 서명은 다음과 같다.
> 
> `def flatMap[A, B](as: List[A])(f: A => List[B]): List[B]`
> 
> 예를 들어 flatMap(List(1, 2, 3))(i => List(i, i))는 List(1, 1, 2, 2, 3, 3)이 되어야 한다.

###### ■ 연습문제 3.21

> flatMap을 이용해서 filter를 구현하라.

###### ■ 연습문제 3.22

> 목록 두 개를 받아서 대응되는 요소들을 더한 값들로 이루어진 새 목록을 구축하는 함수를 작성하라. 예를 들어 List(1, 2, 3)과 List(4, 5, 6)은 List(5, 7, 9)가 되어야 한다.

###### ■ 연습문제 3.23

> 연습문제 3.22의 함수를 정수나 덧셈에 국한되지 않도록 일반화하라. 함수의 이름은 zipWith로 할 것.

#### 표준 라이브러리의 목록들

스칼라 표준 라이브러리의 [`List`](http://www.scala-lang.org/api/current/scala/collection/immutable/List.html)에서는 `Cons`를 `::`으로 부르며 `::`는 오른쪽으로 연관(right-associate)된다.

```scala
1 :: 2 :: Nil == 1 :: (2 :: Nil) == List(1, 2)
```

표준 라이브러리 `List`의 유용한 메서드 목록

- `def take(n: Int): List[A]`: this의 처음 n개의 요소들로 이루어진 목록을 돌려준다.
- `def takeWhile(f: A => Boolean): List[A]`: 주어진 술어 f를 만족하는, this의 가장 긴 선행 요소들로 이루어진 목록을 돌려준다.
- `def forall(f: A => Boolean): Boolean`: this의 모든 요소가 술어 f를 만족할 때에만 true를 돌려준다.
- `def exists(f: A => Boolean): Boolean`: this의 요소들 중 하나라도 f를 만족하는 것이 있으면 true를 돌려준다.
- `scanLeft`와 `scanRight`: foldLeft 및 foldRight와 비슷하되 최종적으로 누적된 값만 돌려주는 것이 아니라 부분 결과들의 List를 돌려준다.

### 3.4.2 단순 구성요소들로 목록 함수를 조립할 때의 효율성 손실

`List`는 연산이나 알고리즘을 범용적인 함수들로 표현할 수 있지만, 그 구현이 항상 효율적이지는 않다. 

- 같은 입력을 여러번 순회하는 구현이 만들어질 수 있다.
- 이른 종료를 위해 명시적인 재귀 루프를 작성해야 할 수 있다.

###### ■ 연습문제 3.24

> **어려움**: 효율성 손실의 한 예로, List가 또 다른 List를 부분 순차열로서 담고 있는지 점검하는 hasSubsequence 함수를 구현하라. 예를 들어 List(1, 2)나 List(2, 3), List(4)는 List(1, 2, 3, 4)의 부분 순차열이다. 간결하고 순수 함수로만 이루어진, 그러면서도 효율적인 구현을 고안하기가 어려울 수 있겠지만, 그래도 개의치 말고 일단은 가장 자연스러운 방식으로 함수를 구현할 것. 나중에 제5장에서 이 함수를 좀 더 개선해 볼 것이다. 참고: 스칼라에서 임의의 두 값 x와 y의 상동(equality)을 비교하는 표현식은 x == y이다.
> 
> `def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean`

## 3.5 트리

**대수적 자료 형식(algebraic data type, ADT)**

- 하나 이상의 자료 생성자로 이루어진 자료 형식
- 자료 생성자는 0개 이상의 인수를 받을 수 있다.
- 자료 형식: 자료 생성자들의 합(sum) 또는 합집합(union)
- 자료 생성자: 인수들의 곱(product)

대수적 자료 형식의 예로, `List`나 `Tree` 등이 있다.

```scala
sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
```

###### ■ 연습문제 3.25

> 트리의 노드, 즉 잎(leaf)과 가지(brach)의 개수를 세는 함수 size를 작성하라.

###### ■ 연습문제 3.26

> Tree[Int]에서 가장 큰 요소를 돌려주는 함수 maximum을 작성하라. (스칼라에서 두 정수 x와 y의 최댓값은 x.max(y)나 x max y로 계산할 수 있다.)

###### ■ 연습문제 3.27

> 트리의 뿌리(root)에서 임의의 잎으로의 가장 긴 경로의 길이를 돌려주는 함수 depth를 작성하라.

###### ■ 연습문제 3.28

> List에 대한 함수 map과 비슷하게 트리의 각 요소를 주어진 함수로 수정하는 함수 map을 작성하라.

###### ■ 연습문제 3.29

> size와 maximum, depth, map의 유사성을 요약해서 일반화한 새 함수 fold를 작성하라. 그런 다음 그 함수들을 새 fold를 이용해서 다시 구현하라. 이 fold 함수와 List에 대한 왼쪽, 오른쪽 fold 사이의 유사성을 찾아낼 수 있는가?

###### ADT와 캡슐화

> 대수적 자료 형식이 형식의 내부 표현을 공용(public)으로 노출하므로 캡슐화(encapsulation)를 위반한다고 생각할 수 있으나 FP에서는 캡슐화 문제를 다르게 취급한다.
> 
> FP 자료 형식에서 그 내부를 노출한다고 해도 버그로 이어질만한 예민한 mutable 상태가 별로 없기 때문에 형식의 자료 생성자를 노출해도 문제가 없는 경우가 많으며 자료 생성자의 노출 여부는 자료 형식의 API 중 어떤 것을 공용으로 노출할 것인가에 대한 결정과 비슷하게 이루어진다.

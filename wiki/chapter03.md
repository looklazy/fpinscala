3장. 함수적 자료구조
================

- 함수적 자료구조(functional data structure)
- 패턴 부합(pattern matching)


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

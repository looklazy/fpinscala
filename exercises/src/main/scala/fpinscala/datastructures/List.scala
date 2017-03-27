package fpinscala.datastructures

sealed trait List[+A] // `List` data type, parameterized on a type, `A`
case object Nil extends List[Nothing] // A `List` data constructor representing the empty list
/* Another data constructor, representing nonempty lists. Note that `tail` is another `List[A]`,
which may be `Nil` or another `Cons`.
 */
case class Cons[+A](head: A, tail: List[A]) extends List[A]

object List { // `List` companion object. Contains functions for creating and working with lists.
  def sum(ints: List[Int]): Int = ints match { // A function that uses pattern matching to add up a list of integers
    case Nil => 0 // The sum of the empty list is 0.
    case Cons(x,xs) => x + sum(xs) // The sum of a list starting with `x` is `x` plus the sum of the rest of the list.
  }

  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x,xs) => x * product(xs)
  }

  def apply[A](as: A*): List[A] = // Variadic function syntax
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  val x = List(1,2,3,4,5) match {
    case Cons(x, Cons(2, Cons(4, _))) => x
    case Nil => 42
    case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
    case Cons(h, t) => h + sum(t)
    case _ => 101
  }

  def append[A](a1: List[A], a2: List[A]): List[A] =
    a1 match {
      case Nil => a2
      case Cons(h,t) => Cons(h, append(t, a2))
    }

  def foldRight[A,B](as: List[A], z: B)(f: (A, B) => B): B = // Utility functions
    as match {
      case Nil => z
      case Cons(x, xs) => f(x, foldRight(xs, z)(f))
    }

  def sum2(ns: List[Int]) =
    foldRight(ns, 0)((x,y) => x + y)

  def product2(ns: List[Double]) =
    foldRight(ns, 1.0)(_ * _) // `_ * _` is more concise notation for `(x,y) => x * y`; see sidebar

  // Exercise 2: List의 첫 요소를 제거하는 함수 tail을 구현하라. 이 함수가 상수 시간으로 실행됨을 주의할 것. Nil인 List도 지원하도록 독자의 구현을 수정하는 여러 가지 방법들도 고려해 보라. 이에 대해서는 다음 장에서 좀 더 살펴볼 것이다.
  def tail[A](l: List[A]): List[A] = l match {
    case Nil => Nil
    case Cons(_, t) => t
  }

  // Exercise 3: 같은 맥락에서, List의 첫 요소를 다른 값으로 대체하는 함수 setHead를 구현하라.
  def setHead[A](l: List[A], h: A): List[A] = l match {
    case Nil => Nil
    case Cons(_, t) => Cons(h, t)
  }

  // Exercise 4: tail을 일반화해서, 목록에서 처음 n개의 요소를 제거하는 함수 drop을 구현하라. 이 함수의 실행 시간은 제거되는 원소의 개수에만 비례함을 주의할 것. List 전체의 복사본을 만들 필요는 없다.
  def drop[A](l: List[A], n: Int): List[A] =
    if (n <= 0) l
    else l match {
      case Nil => Nil
      case Cons(_, t) => drop(t, n - 1)
    }

  // Exercise 5: 주어진 술어(predicate)와 부합하는 List의 앞 요소들(prefix)을 제거하는 함수 dropWhile을 구현하라.
  def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
    case Cons(h, t) if f(h) => dropWhile(t, f)
    case _ => l
  }

  // Exercise 6: 그러나 모든 것이 효율적이지는 않다. 한 List의 마지막 요소를 제외한 모든 요소로 이루어진 List를 돌려주는 함수 init을 구현하라. 예를 들어 List(1, 2, 3, 4)에 대해 init은 List(1, 2, 3)을 돌려주어야 한다. 이 함수를 tail처럼 상수 시간으로 구현할 수 없는 이유는 무엇일까?
  def init[A](l: List[A]): List[A] = l match {
    case Nil => Nil
    case Cons(_, Nil) => Nil
    case Cons(h, t) => Cons(h, init(t))
  }

  // Exercise 9: foldRight를 이용해서 목록의 길이를 계산하라.
  def length[A](l: List[A]): Int =
    foldRight(l, 0)((_, acc) => acc + 1)

  // Exercise 10: 이번 절의 foldRight 구현은 꼬리 재귀가 아니므로 긴 목록에 대해서는 StackOverflowError 오류가 발생한다(이를 "스택에 안전(stack-safe)하지 않다"라고 말한다). 실제로 그런지 실험해 보고, 꼬리 재귀적인 또 다른 일반적 목록 재귀 함수 foldLeft를 이전 장에서 논의한 기법들을 이용해서 작성하라. 서명은 다음과 같다.
  def foldLeft[A,B](l: List[A], z: B)(f: (B, A) => B): B =
    l match {
      case Nil => z
      case Cons(h, t) => foldLeft(t, f(z, h))(f)
    }

  // Exercise 11: sum, product와 목록의 길이를 계산하는 함수를 foldLeft를 이용해서 작성하라.
  def sumViaFoldLeft(ns: List[Int]): Int =
    foldLeft(ns, 0)(_ + _)

  def productViaFoldLeft(ns: List[Double]): Double =
    foldLeft(ns, 1.0)(_ * _)

  def lengthViaFoldLeft[A](as: List[A]): Int =
    foldLeft(as, 0)((acc, _) => acc + 1)

  // Exercise 12: 목록의 역을 돌려주는(이를테면, List(1, 2, 3)에 대해 List(3, 2, 1)을 돌려주는) 함수를 작성하라. 접기(fold) 함수를 이용해서 작성할 수 있는지 시도해 볼 것.
  def reverse[A](as: List[A]): List[A] =
    foldLeft(as, List[A]())((acc, h) => Cons(h, acc))

  // Exercise 13: 어려움: foldLeft를 foldRight를 이용해서 구현할 수 있을까? 그 반대 방향은 어떨까? foldLeft를 이용하면 foldRight를 꼬리 재귀적으로 구현할 수 있으므로 긴 목록에 대해서도 스택이 넘치지 않는다는 장점이 생긴다.
  def foldLeftViaFoldRight[A, B](as: List[A], z: B)(f: (B, A) => B): B =
    foldRight(as, (b: B) => b)((a, g) => b => g(f(b, a)))(z)

  def foldRightViaFoldLeft[A, B](as: List[A], z: B)(f: (A, B) => B): B =
    foldLeft(as, (b: B) => b)((g, a) => b => g(f(a, b)))(z)

  // Exercise 14: append를 foldLeft나 foldRight를 이용해서 구현하라.
  def appendViaFoldRight[A](l: List[A], r: List[A]): List[A] =
    foldRight(l, r)(Cons(_, _))

  def appendViaFoldLeft[A](l: List[A], r: List[A]): List[A] =
    foldLeft(reverse(l), r)((a, b) => Cons(b, a))

  // Exercise 15: 어려움: 목록들의 목록을 하나의 목록으로 연결하는 함수를 작성하라. 실행 시간은 반드시 모든 모든 목록의 전체 길이에 선형으로 비례해야 한다. 이미 정의한 함수들을 활용하도록 노력할 것.
  def concat[A](l: List[List[A]]): List[A] =
    foldRight(l, Nil: List[A])(List.append)

  // Exercise 16: 정수 목록의 각 요소에 1을 더해서 목록을 변환하는 함수를 작성하라. (주의: 새 List를 돌려주는 순수 함수이어야 한다.)
  def add1(l: List[Int]): List[Int] =
    foldRight(l, Nil: List[Int])((h, t) => Cons(h + 1, t))

  // Exercise 17: List[Double]의 각 값을 String으로 변환하는 함수를 작성하라. d: Double을 String으로 변환할 때에는 d.toString이라는 표현식을 사용하면 된다.
  def doubleToString(l: List[Double]): List[String] =
    foldRight(l, Nil: List[String])((h, t) => Cons(h.toString, t))

  // Exercise 18: 목록의 구조를 유지하면서 목록의 각 요소를 수정하는 작업을 일반화한 함수 map을 작성하라. 서명은 다음과 같다.
  def map[A,B](l: List[A])(f: A => B): List[B] =
    foldRightViaFoldLeft(l, Nil: List[B])((h, t) => Cons(f(h), t))

  // Exercise 19: 목록에서 주어진 술어를 만족하지 않는 요소들을 제거하는 함수 filter를 작성하라. 그리고 그 함수를 이용해서 List[Int]에서 모든 홀수를 제거하라.
  def filter[A](l: List[A])(f: A => Boolean): List[A] =
    foldRightViaFoldLeft(l, Nil: List[A])((h, t) => if (f(h)) Cons(h, t) else t)

  // Exercise 20: map과 비슷하되 하나의 요소가 아니라 목록을 최종 결과 목록에 삽입하는 함수 flatMap을 작성하라. 서명은 다음과 같다.
  def flatMap[A, B](l: List[A])(f: A => List[B]): List[B] =
    concat(map(l)(f))

  // Exercise 21: flatMap을 이용해서 filter를 구현하라.
  def filterViaFlatMap[A](l: List[A])(f: A => Boolean): List[A] =
    flatMap(l)(a => if (f(a)) List(a) else Nil)

  // Exercise 22: 목록 두 개를 받아서 대응되는 요소들을 더한 값들로 이루어진 새 목록을 구축하는 함수를 작성하라. 예를 들어 List(1, 2, 3)과 List(4, 5, 6)은 List(5, 7, 9)가 되어야 한다.
  def addPairwise(a: List[Int], b: List[Int]): List[Int] = (a, b) match {
    case (Nil, _) => Nil
    case (_, Nil) => Nil
    case (Cons(h1, t1), Cons(h2, t2)) => Cons(h1 + h2, addPairwise(t1, t2))
  }

  // Exercise 23: 연습문제 3.22의 함수를 정수나 덧셈에 국한되지 않도록 일반화하라. 함수의 이름은 zipWith로 할 것.
  def zipWith[A, B, C](a: List[A], b: List[B])(f: (A, B) => C): List[C] =
    (a, b) match {
      case (Nil, _) => Nil
      case (_, Nil) => Nil
      case (Cons(h1, t1), Cons(h2, t2)) => Cons(f(h1, h2), zipWith(t1, t2)(f))
    }

  // Exercise 24: 어려움: 효율성 손실의 한 예로, List가 또 다른 List를 부분 순차열로서 담고 있는지 점검하는 hasSubsequence 함수를 구현하라. 예를 들어 List(1, 2)나 List(2, 3), List(4)는 List(1, 2, 3, 4)의 부분 순차열이다. 간결하고 순수 함수로만 이루어진, 그러면서도 효율적인 구현을 고안하기가 어려울 수 있겠지만, 그래도 개의치 말고 일단은 가장 자연스러운 방식으로 함수를 구현할 것. 나중에 제5장에서 이 함수를 좀 더 개선해 볼 것이다. 참고: 스칼라에서 임의의 두 값 x와 y의 상동(equality)을 비교하는 표현식은 x == y이다.
  @annotation.tailrec
  def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean = sup match {
    case Nil => sub == Nil
    case _ if startsWith(sup, sub) => true
    case Cons(h, t) => hasSubsequence(t, sub)
  }

  @annotation.tailrec
  def startsWith[A](l: List[A], prefix: List[A]): Boolean = (l, prefix) match {
    case (_, Nil) => true
    case (Cons(h, t), Cons(h2, t2)) if h == h2 => startsWith(t, t2)
    case _ => false
  }

}

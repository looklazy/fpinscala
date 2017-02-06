/* =========================================================================
 * Exercise 2.1
 * -------------------------------------------------------------------------
 * n번째 피보나치 수를 돌려주는 재귀 함수를 작성하라. 처음 두 피보나치 수는 0과 1이다.
 * n번째 피보나치 수는 항상 이전 두 수의 합이다. 즉, 피보나치 수열은 0, 1, 1, 2, 3, 5로 시작한다.
 * 반드시 지역 꼬리 재귀 함수를 사용해서 작성할 것.
 * ------------------------------------------------------------------------- */
def fib(n: Int): Int = {
  @annotation.tailrec
  def loop(n: Int, prev: Int, cur: Int): Int =
    if (n == 0) prev
    else loop(n - 1, cur, prev + cur)

  loop(n, 0, 1)
}


/* =========================================================================
 * Exercise 2.2
 * -------------------------------------------------------------------------
 * Array[A]가 주어진 비교 함수에 의거해서 정렬되어 있는지 점검하는 isSorted 함수를 구현하라.
 * ------------------------------------------------------------------------- */
def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean = {
  @annotation.tailrec
  def loop(n: Int): Boolean =
    if (n >= as.length - 1) true
    else if (ordered(as(n), as(n + 1))) false
    else loop(n + 1)

  loop(0)
}


/* =========================================================================
 * Exercise 2.3
 * -------------------------------------------------------------------------
 * 인수가 두 개인 함수 f를 인수 하나를 받고 그것으로 f를 부분 적용하는 함수로 변환하는
 * 커링(currying)을 살펴보자. 이번에도 컴파일되는 구현은 단 한 가지이다. 그러한 구현을 작성하라.
 * ------------------------------------------------------------------------- */
def curry[A, B, C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)


/* =========================================================================
 * Exercise 2.4
 * -------------------------------------------------------------------------
 * curry의 변환을 역으로 수행하는 고차 함수 uncurry를 구현하라.
 * =>는 오른쪽으로 묶이므로, A => (B => C)를 A => B => C라고 표기할 수 있음을 주의할 것.
 * ------------------------------------------------------------------------- */
def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)


/* =========================================================================
 * Exercise 2.5
 * -------------------------------------------------------------------------
 * 두 함수를 합성하는 고차 함수를 구현하라.
 * ------------------------------------------------------------------------- */
def compose[A, B, C](f: B => C, g: A => B): A => C =
  a => f(g(a))
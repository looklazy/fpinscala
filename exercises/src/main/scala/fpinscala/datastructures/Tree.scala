package fpinscala.datastructures

sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]


object Tree {

  // Exercise 25: 트리의 노드, 즉 잎(leaf)과 가지(brach)의 개수를 세는 함수 size를 작성하라.
  def size[A](t: Tree[A]): Int = t match {
    case Leaf(_) => 1
    case Branch(l, r) => 1 + size(l) + size(r)
  }

  // Exercise 26: Tree[Int]에서 가장 큰 요소를 돌려주는 함수 maximum을 작성하라. (스칼라에서 두 정수 x와 y의 최댓값은 x.max(y)나 x max y로 계산할 수 있다.)
  def maximum(t: Tree[Int]): Int = t match {
    case Leaf(n) => n
    case Branch(l, r) => maximum(l) max maximum(r)
  }

  // Exercise 27: 트리의 뿌리(root)에서 임의의 잎으로의 가장 긴 경로의 길이를 돌려주는 함수 depth를 작성하라.
  def depth[A](t: Tree[A]): Int = t match {
    case Leaf(_) => 0
    case Branch(l, r) => 1 + (depth(l) max depth(r))
  }

  // Exercise 28: List에 대한 함수 map과 비슷하게 트리의 각 요소를 주어진 함수로 수정하는 함수 map을 작성하라.
  def map[A, B](t: Tree[A])(f: A => B): Tree[B] = t match {
    case Leaf(a) => Leaf(f(a))
    case Branch(l, r) => Branch(map(l)(f), map(r)(f))
  }

  // Exercise 29: size와 maximum, depth, map의 유사성을 요약해서 일반화한 새 함수 fold를 작성하라. 그런 다음 그 함수들을 새 fold를 이용해서 다시 구현하라. 이 fold 함수와 List에 대한 왼쪽, 오른쪽 fold 사이의 유사성을 찾아낼 수 있는가?
  def fold[A, B](t: Tree[A])(f: A => B)(g: (B, B) => B): B = t match {
    case Leaf(a) => f(a)
    case Branch(l, r) => g(fold(l)(f)(g), fold(r)(f)(g))
  }

  def sizeViaFold[A](t: Tree[A]): Int =
    fold(t)(_ => 1)(1 + _ + _)

  def maximumViaFold(t: Tree[Int]): Int =
    fold(t)(a => a)(_ max _)

  def depthViaFold[A](t: Tree[A]): Int =
    fold(t)(_ => 0)((d1, d2) => 1 + (d1 max d2))

  def mapViaFold[A, B](t: Tree[A])(f: A => B): Tree[B] =
    fold(t)(a => Leaf(f(a)): Tree[B])(Branch(_, _))

}
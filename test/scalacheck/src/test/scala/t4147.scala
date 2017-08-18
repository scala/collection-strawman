import org.scalacheck.Prop.{forAll, throws}
import org.scalacheck.Properties
import org.scalacheck.Gen


import strawman.collection.{mutable, Set, toOldSeq, toNewSeq}
import strawman.collection.immutable.List


object SI4147Test extends Properties("Mutable TreeSet") {

  val generator = Gen.listOfN(1000, Gen.chooseNum(0, 1000)).map(l => List.fromIterable(l.toStrawman))

  val denseGenerator = Gen.listOfN(1000, Gen.chooseNum(0, 200)).map(l => List.fromIterable(l.toStrawman))

  property("Insertion doesn't allow duplicates values.") = forAll(generator) { (s: List[Int]) =>
    {
      val t = mutable.TreeSet[Int](s.toClassic: _*)
      t == s.to(Set)
    }
  }

  property("Verification of size method validity") = forAll(generator) { (s: List[Int]) =>
    {
      val t = mutable.TreeSet[Int](s.toClassic: _*)
      for (a <- s) {
        t -= a
      }
      t.size == 0
    }
  }

  property("All inserted elements are removed") = forAll(generator) { (s: List[Int]) =>
    {
      val t = mutable.TreeSet[Int](s.toClassic: _*)
      for (a <- s) {
        t -= a
      }
      t == Set()
    }
  }

  property("Elements are sorted.") = forAll(generator) { (s: List[Int]) =>
    {
      val t = mutable.TreeSet[Int](s.toClassic: _*)
      t.to(List) == s.distinct.sorted
    }
  }

  property("A view doesn't expose off bounds elements") = forAll(denseGenerator) { (s: List[Int]) =>
    {
      val t = mutable.TreeSet[Int](s.toClassic: _*)
      val view = t.rangeImpl(Some(50), Some(150))
      view.filter(_ < 50) == Set[Int]() && view.filter(_ >= 150) == Set[Int]()
    }
  }

  property("ordering must not be null") =
    throws(classOf[NullPointerException])(mutable.TreeSet.empty[Int](null))
}

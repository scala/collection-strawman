package strawman.collection

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import scala.{Int, Long, Unit}
import scala.Predef.{intWrapper}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class ListGroupByBuilder {
  import immutable.List

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: List[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = List((1 to size).map(_.toLong): _*)
  }


  @Benchmark
  def listGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByBuilder[Long, Long, List[Long]](xs)(() => List.newBuilder())(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class ListGroupByImmutable {
  import immutable.{List, Nil}

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: List[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = List((1 to size).map(_.toLong): _*)
  }

  @Benchmark
  def listGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByImmutable[Long, Long, List[Long]](xs)(Nil, _ :: _)(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class HashSetGroupByBuilder {
  import immutable.HashSet

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: HashSet[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = HashSet((1 to size).map(_.toLong): _*)
  }


  @Benchmark
  def hashsetGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByBuilder[Long, Long, HashSet[Long]](xs)(() => HashSet.newBuilder())(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class HashSetGroupByImmutable {
  import immutable.HashSet

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: HashSet[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = HashSet((1 to size).map(_.toLong): _*)
  }

  @Benchmark
  def hashsetGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByImmutable[Long, Long, HashSet[Long]](xs)(HashSet.empty, (v, vs) => vs + v)(_ % 5)
    bh.consume(result)
  }

}


@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class ImmutableArrayGroupByBuilder {
  import immutable.ImmutableArray

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: ImmutableArray[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = ImmutableArray((1 to size).map(_.toLong): _*)
  }


  @Benchmark
  def immutableArrayGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByBuilder[Long, Long, ImmutableArray[Long]](xs)(() => ImmutableArray.newBuilder())(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class ImmutableArrayGroupByImmutable {
  import immutable.ImmutableArray

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: ImmutableArray[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = ImmutableArray((1 to size).map(_.toLong): _*)
  }

  @Benchmark
  def immutableArrayGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByImmutable[Long, Long, ImmutableArray[Long]](xs)(ImmutableArray.empty, (v, vs) => vs :+ v)(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class ArrayBufferGroupByBuilder {
  import mutable.ArrayBuffer

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: ArrayBuffer[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = ArrayBuffer((1 to size).map(_.toLong): _*)
  }


  @Benchmark
  def arrayBufferGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByBuilder[Long, Long, ArrayBuffer[Long]](xs)(() => ArrayBuffer.newBuilder())(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class ArrayBufferGroupByGrowable {
  import mutable.ArrayBuffer

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: ArrayBuffer[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = ArrayBuffer((1 to size).map(_.toLong): _*)
  }

  @Benchmark
  def arrayBufferGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByGrowable[Long, Long, ArrayBuffer[Long]](xs)(() => ArrayBuffer.empty)(_ % 5)
    bh.consume(result)
  }

}


@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class MutableHashSetGroupByBuilder {
  import mutable.HashSet

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: HashSet[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = HashSet((1 to size).map(_.toLong): _*)
  }


  @Benchmark
  def mutableHashsetGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByBuilder[Long, Long, HashSet[Long]](xs)(() => HashSet.newBuilder())(_ % 5)
    bh.consume(result)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 12)
@Measurement(iterations = 12)
@State(Scope.Benchmark)
class MutableHashSetGroupByGrowable {
  import mutable.HashSet

  @Param(scala.Array(/*"0",*/ "1", "2", "3", "4", "7", "8", "15", "16", "17", "39", "282", "73121", "7312102"))
  var size: Int = _

  var xs: HashSet[Long] = _

  @Setup(Level.Trial)
  def initData(): Unit = {
    xs = HashSet((1 to size).map(_.toLong): _*)
  }

  @Benchmark
  def mutableHashsetGroupBy(bh: Blackhole): Unit = {
    val result = GroupBys.groupByGrowable[Long, Long, HashSet[Long]](xs)(() => HashSet.empty)(_ % 5)
    bh.consume(result)
  }

}

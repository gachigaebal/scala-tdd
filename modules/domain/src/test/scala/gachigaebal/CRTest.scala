package gachigaebal

import minitest.SimpleTestSuite
import hedgehog.minitest.HedgehogSupport
import hedgehog.*
import hedgehog.state.*
import java.io.{File, FileWriter}

opaque type CRId = String
object CRId:
  def apply(render: String): CRId = render
extension (id: CRId)
  def render: String = id

trait CR:
  def create(v: String): CRId
  def read(id: CRId): Option[String]
object CR:
  def uuidId: () => String = () => java.util.UUID.randomUUID().toString

  def monotonicIdsAtomic: () => String =
    val count = new java.util.concurrent.atomic.AtomicInteger(0)
    () => count.addAndGet(1).toString

  def monotonicIdsVar: () => String =
    var count: Int = 0
    () =>
      val previous = count
      // Let's force the bug
      Thread.sleep(1)
      count += 1
      previous.toString
  

  def fileWithId(root: File, newId: () => String): CR = new CR:
    override def create(v: String): CRId =
      val id = newId()
      val f = new File(root, id)
      f.getParentFile.mkdirs()
      val w = new FileWriter(f)
      w.write(v)
      w.close()
      CRId(id)
    override def read(id: CRId): Option[String] =
      val f = new File(root, id.render)
      if !f.exists() then None else Some(scala.io.Source.fromFile(f).mkString)

object CRTest extends SimpleTestSuite with HedgehogSupport:

  def deleteRecursively(file: File): Unit =
    if file.isDirectory then file.listFiles.foreach(deleteRecursively)
    file.delete
    ()

  case class State(map: Map[Var[CRId], String])
  case class Create(value: String)
  case class Read(key: Var[CRId])

  def commands(CR: CR): List[CommandIO[State]] = List(commandCreate(CR), commandRead(CR))

  def commandCreate(CR: CR): CommandIO[State] = new Command[State, Create, CRId]:
    override def gen(s: State): Option[Gen[Create]] = Some(
      for
        v <- Gen.string(Gen.lower, Range.linear(1, 10))
      yield Create(v)
    )
    override def execute(env: Environment, i: Create): Either[String, CRId] = Right(CR.create(i.value))
    override def update(s: State, i: Create, o: Var[CRId]): State = s.copy(map = s.map + (o -> i.value))
    override def ensure(env: Environment, s0: State, s: State, i: Create, o: CRId): Result = Result.success

  def commandRead(CR: CR): CommandIO[State] = new Command[State, Read, Option[String]]:
    override def gen(s: State): Option[Gen[Read]] = s.map.keys.toList match
      case Nil => None
      case h :: t => Some(Gen.element(h, t).map(Read(_)))
    override def vars(i: Read): List[Var[?]] = List(i.key)
    override def execute(env: Environment, i: Read): Either[String, Option[String]] =
      Right(CR.read(i.key.get(env)))
    override def update(s: State, i: Read, o: Var[Option[String]]): State = s
    override def ensure(env: Environment, s0: State, s: State, i: Read, o: Option[String]): Result =
      s.map.get(i.key) ==== o

  def testSequential(newCR: File => CR): Property =
    val root = new File("tmpdir")
    val CR = newCR(root)
    sequential(
      range = Range.linear(1, 100),
      initial = State(Map()),
      commands = commands(CR),
      cleanup = () => deleteRecursively(root),
    )
  
  def testParallel(newCR: File => CR): Property =
    import scala.concurrent.ExecutionContext.Implicits.global
    val root = new File("tmpdir")
    val CR = newCR(root)
    parallel(
      prefixN = Range.linear(1, 10),
      parallelN = Range.linear(1, 10),
      initial = State(Map()),
      commands = commands(CR),
      cleanup = () => deleteRecursively(root),
    )

  property("test file CR (sequential) - uuid") {
    testSequential(CR.fileWithId(_, CR.uuidId))
  }

  property("test file CR (sequential) - monotonic IDs var") {
    testSequential(CR.fileWithId(_, CR.monotonicIdsVar))
  }

  property("test file CR (parallel) - uuid") {
    testParallel(CR.fileWithId(_, CR.uuidId))
  }
  
  property("test file CR (parallel) - monotonic IDs atomic") {
    testParallel(CR.fileWithId(_, CR.monotonicIdsAtomic))
  }

//  property("test file CR (parallel) - monotonic IDs var (broken)") {
//    testParallel(CR.fileWithId(_, CR.monotonicIdsVar))
//  }

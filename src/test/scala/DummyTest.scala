import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestConsole

object KanbanAppTest extends DefaultRunnableSpec {
    def spec: ZSpec[Environment,Failure] = suite("kanban app")(
        testM("show welcome prompt") {
            val result = for {
                _ <- KanbanApp.launch.catchSome {
                    case e: java.io.EOFException => ZIO.unit
                }
                lines <- TestConsole.output
            } yield lines

            def expected = 
                Vector(
                    "Welcome to Kanban App!!\n",
                    "> "
                )

            assertM(result)(equalTo(expected))
        },
        testM("if enter, then new prompt") {
            val result = for {
                _ <- TestConsole.feedLines("", "exit")
                _ <- KanbanApp.launch
                lines <- TestConsole.output
            } yield lines

            def expected =
                Vector(
                    "Welcome to Kanban App!!\n",
                    "> ",
                    "> ",
                    "Good bye~\n"
                )

            assertM(result)(equalTo(expected))
        },
        testM("exit kanban app") {
            val result = for {
                _ <- TestConsole.feedLines("exit")
                _ <- KanbanApp.launch
                lines <- TestConsole.output
            } yield lines

            def expected =
                Vector(
                    "Welcome to Kanban App!!\n",
                    "> ",
                    "Good bye~\n"
                )

            assertM(result)(equalTo(expected))
        },
        testM("enter empty commands then exit") {
            checkM(Gen.int(1, 100)) { n => 
                val lines = Vector.fill(n)("") :+ "exit"

                val result =
                        (
                            for {
                                _ <- TestConsole.feedLines(lines: _*)
                                _ <- KanbanApp.launch
                                lines <- TestConsole.output
                            } yield lines
                        ).provideLayer(TestConsole.silent)

                def expected =
                    "Welcome to Kanban App!!\n" +:
                        Vector.fill(n + 1)("> ") :+
                        "Good bye~\n"

                assertM(result)(equalTo(expected))
            }
        },
    )
}
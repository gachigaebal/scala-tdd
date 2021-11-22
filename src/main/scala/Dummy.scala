import zio._
import zio.console.Console

object KanbanApp {
    import zio.console._

    def launch: RIO[Console, Unit] =
        for {
            _ <- putStrLn(s"Welcome to Kanban App!!")
            _ <- loop
        } yield ()

    def loop: RIO[Console, Unit] =
        for {
            _ <- putStr(s"> ")
            cmd <- getStrLn
            cont <- execute(cmd)
            _ <- loop.when(cont)
        } yield ()

    def execute(command: String): RIO[Console, Boolean] =
        if (command == "exit")
            putStrLn(s"Good bye~").as(false)
        else
            RIO.succeed(true)
}

object ConsoleKanbanApp extends App {
    def run(args: List[String]): URIO[ZEnv,ExitCode] =
        KanbanApp.launch.exitCode
}
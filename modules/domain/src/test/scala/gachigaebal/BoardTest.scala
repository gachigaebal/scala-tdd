package gachigaebal

import minitest.SimpleTestSuite
import hedgehog.minitest.HedgehogSupport
import hedgehog.*

object BoardTest extends SimpleTestSuite with HedgehogSupport:

  // 보드를 만들면 보드가 생긴다

  example("create empty board") {
    val board: Board = Board.empty

    Result.all(List( 
      board.todo ==== Column.empty,
      board.inProgress ==== Column.empty,
      board.done ==== Column.empty,
    ))
  }

  // 카드를 만든다
  example("create a card") {
    val card: Card = Card(
      id = "1",
      title = "title",
      description = "카드입니다."
    )
    Result.all(List( 
      card.id ==== "1",
      card.title ==== "title",
      card.description ==== "카드입니다."
    ))
  }

  // 보드에 카드를 추가한다

  example("add a card to the board") {
    val board: Board = Board.empty
    val card1: Card = Card(
      id = "1",
      title = "title 1",
      description = "카드1 입니다."
    )
    val card2: Card = Card(
      id = "2",
      title = "title 2",
      description = "카드2 입니다."
    )

    val newBoard: Board = board.add(card1).flatMap(_.add(card2)).toOption.get
    Result.all(List( 
      newBoard.todo ==== Column.from(card1, card2),
      newBoard.inProgress.isEmpty ==== true,
      newBoard.done.isEmpty ==== true
    ))
  }


  example("Card id expects to be unique in a board") {
    val board: Board = Board.empty
    val card1: Card = Card(
      id = "1",
      title = "title 1",
      description = "카드1 입니다."
    )

    // error modes
    //
    // 1. ~~the same board~~
    // 2. ~~throw DuplicatedCardIdException()~~
    // 3. ~~None~~
    // 4. Either[DuplicatedCardIdException, Board]
    // 5. Either[String, Board]

    val result = board.add(card1).flatMap(_.add(card1))
    result ==== Left(s"Duplicated card id: ${card1.id}")
  }

  example("move card to inProgress column") {
    val board: Board = Board.empty
    val card1: Card = Card(
      id = "1",
      title = "title 1",
      description = "카드1 입니다."
    )

    val newBoard: Board = board.add(card1).toOption.get
    val movedBoard: Board = newBoard.moveToInprogress(card1.id)

    Result.all(List(
      movedBoard.todo.isEmpty ==== true,
      movedBoard.inProgress.isEmpty ==== false,
      movedBoard.done.isEmpty ==== true
    ))
  }

  example("move non-existing card to inProgress column") {
    val board: Board = Board.empty
    val card1: Card = Card(
      id = "1",
      title = "title 1",
      description = "카드1 입니다."
    )

    val newBoard: Board = board.add(card1).toOption.get
    val movedBoard: Board = newBoard.moveToInprogress("2")

    newBoard ==== movedBoard
  }

end BoardTest
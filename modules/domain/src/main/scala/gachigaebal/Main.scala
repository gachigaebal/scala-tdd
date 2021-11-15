package gachigaebal

case class Board(
  inProgress: Column,
  done: Column,
  todo: Column,
)
object Board:
  def empty: Board = Board(
    inProgress = Column.empty,
    done = Column.empty,
    todo = Column.empty,
  )

extension (b: Board)
  def add(c: Card): Either[String, Board] = 
    (b.inProgress.cards ++ b.todo.cards ++ b.done.cards).find(_.id == c.id).match 
      case Some(_) =>
        Left(s"Duplicated card id: ${c.id}")
      case None => 
        Right(b.copy(
          todo = b.todo.add(c)
        ))

  def moveToInprogress(cardID: String): Board =
    b.todo.cards.find(_.id == cardID).fold(b){ card =>
      b.copy(
        inProgress = b.inProgress.add(card),
        todo = Column(b.todo.cards.filterNot(_.id == cardID)),
      )
    }

case class Column(
  cards: Seq[Card],
)

object Column:
  def empty: Column = Column(Seq.empty)

  def from(card: Card, cards: Card*): Column = Column(card +: cards.toVector)

extension (column: Column)
  def add(c: Card): Column = column.copy(cards = column.cards :+ c)

  def isEmpty: Boolean = column.cards.isEmpty

case class Card(
  id: String,
  title: String,
  description: String,
)

@main def hello() = println("Hello, world!")


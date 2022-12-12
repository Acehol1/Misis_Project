package main.repository

import main.db.CartDb._
import main.model.{Account, CreateAcc, Transaction, Transfercash}
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class CartRepositoryDb(implicit val ec :ExecutionContext, db : Database) extends CartRepository {

  override def list(): Future[Seq[Account]] = {
    db.run(itemTable.result)
  }

  override def get(id: UUID): Future[Account] = {
    db.run(itemTable.filter(_.id === id).result.head)
  }

  def find(id: UUID): Future[Option[Account]] = {
    db.run(itemTable.filter(_.id === id).result.headOption)
  }

  override def create(createacc: CreateAcc): Future[Account] = {
    val acc = Account(firstname = createacc.firstname, surname = createacc.surname)
    for {
      _ <- db.run(itemTable += acc)
      res <-get(acc.id)
    } yield res
  }

  override def transfer(carts: Transfercash): Future[Either[String,Account]] = {
    takes(Transaction(carts.id_1, carts.amount))
    deposit(Transaction(carts.id_2, carts.amount))
  }

  override def deposit(carts: Transaction): Future[Either[String,Account]] = {

  val query = itemTable
    .filter(_.id === carts.id)
    .map(_.cash)
  for {
    oldcash <- db.run(query.result.headOption)
    cash = carts.amount
    updateCash = oldcash.map { oldc =>
       Right(oldc + cash)
    }.getOrElse(Left("Не найдено значение"))
    future = updateCash.map(price => db.run {
      query.update(price)
    }) match {
      case Right(future) => future.map(Right(_))
      case Left(s) => Future.successful(Left(s))
    }
    updated <- future
    res <- find(carts.id)
  } yield updated.map(_ => res.get)
}

  override def takes(carts: Transaction): Future[Either[String,Account]] = {
    val query = itemTable
        .filter(_.id === carts.id)
        .map(_.cash)
    for {
      oldcash <- db.run(query.result.headOption)
      cash = carts.amount
      updateCash = oldcash.map { oldc =>
        if (oldc - cash < 0)
          Left("Недостаточно стредств")
        else Right (oldc - cash)
      }.getOrElse(Left("Не найдено значение"))
      future = updateCash.map(price => db.run {  query.update(price)})match {
        case Right(future) => future.map(Right(_))
        case Left(s) => Future.successful(Left(s))
      }
      updated <- future
      res <- find(carts.id)
      }yield updated.map(_ => res.get)
  }

  override def delete(id: UUID): Future[Unit] = {
    db.run(itemTable.filter(_.id ===id).delete).map(_ => ())
  }
}

class  BiggerException extends  Error
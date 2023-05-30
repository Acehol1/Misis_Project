package misis.repository

import io.circe.generic.auto._
import akka.actor.ActorSystem
import misis.TopicName
import misis.kafka.Fee_cb_streams
import misis.model.{AccountUpdate, CashBack}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class CashBackRepository()(implicit
    val system: ActorSystem,
    executionContext: ExecutionContext
) {

    private val cashbacks: ListBuffer[CashBack] = ListBuffer.empty[CashBack]

    def addAccount(accountId: Int): Future[CashBack] = {
        val account = CashBack(accountId, 0)
        cashbacks += account
        Future.successful(account)
    }

    def getCbBalance(accountId: Int): Int = {
        cashbacks.find(_.id == accountId).map(_.amount).getOrElse(0)
    }

    def containsAccount(accountId: Int): Boolean = {
        cashbacks.exists(_.id == accountId)
    }

    def updateAccount(accountId: Int, value: Int): Future[CashBack] = {
        val accountOption = cashbacks.find(_.id == accountId)
        accountOption match {
            case Some(account) =>
                val updatedAccount = account.update(value)
                cashbacks -= account
                cashbacks += updatedAccount
                Future.successful(updatedAccount)
            case None =>
                throw new IllegalArgumentException(s"Аккаунт с таким ID $accountId не найден")
        }
    }

}

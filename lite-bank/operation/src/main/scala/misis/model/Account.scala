package misis.model

import java.util.UUID

case class Account(id: Int, amount: Int) {
    def update(value: Int) = this.copy(amount = amount + value)
}

trait Command
case class AccountUpdate(accountId: Int, value: Int, transaction: Int = 0, directId: Int = 0, category: String = "Non")

case class AccountCreate(accountId: Int)

trait Event
case class AccountUpdated(
                             accountId: Int,
                             value: Int,
                             transaction: Int,
                             directId: Int,
                             category: String
                         )
case class AccountCreated(accountId: Int)


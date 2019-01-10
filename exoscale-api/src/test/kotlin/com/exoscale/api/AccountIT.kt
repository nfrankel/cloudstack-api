package com.exoscale.api

import org.testng.annotations.Test
import strikt.api.expect
import strikt.assertions.isEqualTo

class AccountIT {

    @Test
    fun `should find the correct number of accounts`() {
        val result = withAccount(ACCOUNT)(ListAccounts())
        expect {
            that(result.listaccountsresponse) {
                get { count }.isEqualTo(1)
            }
            that(result.listaccountsresponse.Account.size).isEqualTo(1)
            that(result.listaccountsresponse.Account[0]) {
                get { name }.isEqualTo(ACCOUNT)
            }
        }
    }
}
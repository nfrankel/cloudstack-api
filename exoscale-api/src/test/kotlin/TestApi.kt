import com.exoscale.api.*

fun main(args: Array<String>) {
    val command = ListAccounts()
    val execute = withDefaultAccount()
    val result = execute(command)
    println(result.listaccountsresponse.Account[0].cpuavailable)
}
import com.exoscale.api.*
import java.util.*

fun main(args: Array<String>) {
    val command = ListApis()
    val execute = Execute("https://api.exoscale.ch/compute", System.getProperty("exo.api.key"), System.getProperty("exo.api.secret"))
    val (request, response, result) = execute(command)
    println(request)
    println(response)
    println(result)
}
import com.exoscale.api.DeployVirtualMachine
import java.util.*

fun main(args: Array<String>) {
    DeployVirtualMachine(
        UUID.fromString("foo"),
        UUID.fromString("bar"),
        UUID.fromString("zone")
    )
}
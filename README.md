# Cloudstack API for the Kotlin language

"[Apache CloudStack](https://cloudstack.apache.org/) is open source software designed to deploy and manage large networks of virtual machines".   

Managing CloudStack is done through a REST API.

## How-to

```$kotlin
val client = Client("https://api.exoscale.ch/compute",
        apiKey,
        apiSecret)

fun main(args: Array<String>) {
    ListVolumes().run(Output.XML)
    ListFirewallRules().run()
    ListResourceLimits().run()
}

fun Command.run(output: Output = Output.JSON) = client.execute(this, output).third.fold(
        { println(it) },
        { throw it })
```

## Design principles

* Every command is designed as a class
* Required arguments are passed as constructor arguments
* Optional arguments are passed inside a single `Map<String,String>` argument

```$kotlin
class ListVolumes(parameters: Map<String, String> = mapOf()) : Command("listVolumes", parameters)

class ListVolumesOnFiler(val poolname: String, 
                         parameters: Map<String, String> = mapOf()) : Command("listVolumesOnFiler", parameters)
```

## Compatibility

The library is compatible with version 4.11.

To generate the library for other versions:
 
 * clone the project
 * change the `cloudstack-api` POM configuration:  

```$xml
<plugin>
    <groupId>${project.groupId}</groupId>
    <artifactId>cloudstack-maven-plugin</artifactId>
    <version>${project.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <phase>generate-sources</phase>
        </execution>
    </executions>
    <configuration>
        <version>4.11</version>
    </configuration>
</plugin>
```
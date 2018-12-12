# Exoscale API for the Java language

The HTTP API is a first-class citizen to manage Exoscale.

This project is a Java library wrapper around the HTTP API.
It can also be used in Kotlin.

## How-to

```java
public class PrintListAccounts {

    public static void main(String[] args) {
        ExoscaleClient client = ExoscaleClientBuilderKt.withDefaultAccount();
        ListAccounts command = new ListAccounts();
        ListAccountsResult result = client.invoke(command);
        List<Account> accounts = result.listaccountsresponse.getAccount();
        for (Account account: accounts) {
            System.out.println("account = " + account);
        }
    }
}
```

## Design principles

* Every command is designed as a class
* Every result is designed as a class
* As much typing as possible

## Compatibility

The library is compatible with version 4.4 of the Cloudstack API

## Updating the API

To generate the library:
 
 * Store the result of `exo api api list` as `exoscale-generate-plugin/src/main/resources/api.json`  
 * Run `./mvnw install` in the project's root folder
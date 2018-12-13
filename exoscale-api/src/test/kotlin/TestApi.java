import com.exoscale.api.*;

import java.util.List;

public class TestApi {

    public static void main(String[] args) {
        ExoscaleClient client = ExoscaleClientBuilder.withDefaultAccount();
        ListAccounts command = new ListAccounts();
        ListAccountsResult result = client.invoke(command);
        List<Account> accounts = result.listaccountsresponse.getAccount();
        for (Account account: accounts) {
            System.out.println(account.getName() + ": " + account.getCpuavailable());
        }
    }
}

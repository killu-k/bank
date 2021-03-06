package com.bcs.bank.restbank;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    // TODO: loo teenus createExampleAccount() mis loob uue AccountDto objekti:
    //  account number = random account number
    //  firstName "John"
    //  lastName "Smith"
    //  balance 0
    //  locked false

    public AccountDto createExampleAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setAccountNumber(createRandomAccountNumber());
        accountDto.setFirstName("Juss");
        accountDto.setLastName("Sepp");
        accountDto.setBalance(0);
        accountDto.setLocked(false);
        return accountDto;
    }

    private String createRandomAccountNumber() {
        //  Creates random account number between EE1000 -  EE9999
        Random random = new Random();
        return "EE" + (random.nextInt(9999) + 1000);
    }

    public boolean accountIdExist(List<AccountDto> accounts, int accountId) {
        for (AccountDto account : accounts) {
            if (account.getId() == accountId) {
                return true;
            }
        }
        return false;
    }

    public boolean accountLocked(List<AccountDto> accounts, int accountId) {
        for (AccountDto account : accounts) {
            if (!account.getLocked()) {
                return true;
            }
        }
        return false;
    }

    public AccountDto getAccountById(List<AccountDto> accounts, int accountId) {

        //todo: käime läbi kõik koontod accounts listis ja
        //paneme iga konto muutujasse 'account'

        // for tüüp väärtus : kontrollid vastu väärtust, kuni kõik on kontrollitud, kas on samaväärsed


        for (AccountDto account : accounts) {
            //kui leiame konto, mille id on võrdne, siis paneme accountID-ga
            if (account.getId() == accountId) {
                //siis tagastame selle konto
                return account;

            }

        }
        return null;
    }

    public boolean accountNumberExist(List<AccountDto> accounts, String receiverAccountNumber) {
        // käime kõik kontod läbi ja vaatame kas meil on sellise numbriga kontot
        for (AccountDto account : accounts) {
            if (account.getAccountNumber().equals(receiverAccountNumber)) {
                return true;
            }
        }
        return false;
    }

    public AccountDto getAccountByNumber(List<AccountDto> accounts, String receiverAccountNumber) {
        for (AccountDto account : accounts) {
            if (account.getAccountNumber().equals(receiverAccountNumber)) {
                return account;
            }
        }
        return null;
    }

    public RequestResult updateOwnerDetails(List<AccountDto> accounts, AccountDto accountDto) {
        RequestResult requestResult = new RequestResult();

        int accountId = accountDto.getId();
        if (!accountIdExist(accounts, accountId)){
            requestResult.setError("Account ID " + accountId + "does not exist!");
            requestResult.setAccountId(accountId);
            return requestResult;
        }

        AccountDto account = getAccountById(accounts, accountId);
        account.setFirstName(accountDto.getFirstName());
        accountDto.setLastName(accountDto.getLastName());

        requestResult.setAccountId(accountId);
        requestResult.setMessage("Successfully updated account");

        return requestResult;
    }

    public RequestResult lockAccount(List<AccountDto> accounts, AccountDto accountDto) {
        RequestResult requestResult = new RequestResult();

//        kõigepealt kontrollin , kas see konto on olemas

        int accountId = accountDto.getId();
        if (!accountIdExist(accounts, accountId)){
            requestResult.setError("Account ID " + accountId + "does not exist!");
            requestResult.setAccountId(accountId);
            return requestResult;
        }

//        kui konto on olemas, siis nüüd pean kontrollima
        AccountDto account = getAccountById(accounts, accountId);
        if (account.getLocked()) {
            account.setLocked(false);
            requestResult.setMessage("Account is unlocked");
        } else {
            account.setLocked(true);
            requestResult.setMessage("Account is locked");
        }

        requestResult.setAccountId(accountId);

        return requestResult;

    }

//    public boolean getAccountIsLocked(List<AccountDto> accounts, boolean isAccountLocked) {
//        accountService.accountIsLocked(accounts, isAccountLocked);
//    }

    public RequestResult deleteAccount(List<AccountDto> accounts, int accountId) {
        RequestResult requestResult = new RequestResult();

        if (!accountIdExist(accounts, accountId)){
            requestResult.setError("Account ID " + accountId + "does not exist!");
            requestResult.setAccountId(accountId);
            return requestResult;
        }
//      küsime kindla konto ja siis eemaldame selle
        AccountDto account = getAccountById(accounts, accountId);
        accounts.remove(account);

        requestResult.setMessage("Account deleted!");
        requestResult.setAccountId(accountId);
        return requestResult;
    }
}

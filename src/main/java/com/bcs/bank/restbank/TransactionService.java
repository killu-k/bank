package com.bcs.bank.restbank;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    public static final String ATM = "ATM";
    public static final char NEW_ACCOUNT = 'n';
    public static final char DEPOSIT = 'd';
    public static final char WITHDRAWAL = 'w';
    public static final char SEND_MONEY = 's';
    public static final char RECEIVE_MONEY = 'r';

    @Resource
    private AccountService accountService;

    @Resource
    private BalanceService balanceService;

//    transactionType
//    n - new account
//    d - deposit
//    w - withdrawal
//    s - send money
//    r - receive money


    // TODO:    createExampleTransaction()
    //  account id 123
    //  balance 1000
    //  amount 100
    //  transactionType 's'
    //  receiver EE123
    //  sender EE456

    public TransactionDto createExampleTransaction() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountId(123);
        transactionDto.setBalance(1000);
        transactionDto.setAmount(100);
        transactionDto.setTransactionType(SEND_MONEY);
        transactionDto.setReceiverAccountNumber("EE123");
        transactionDto.setSenderAccountNumber("EE456");
        transactionDto.setLocalDateTime(LocalDateTime.now());
        return transactionDto;
    }

    public RequestResult addNewTransaction(Bank bank, TransactionDto transactionDto) {
        // loon vajalikud tühjad objektid
        RequestResult requestResult = new RequestResult();


        // vajalike andmete lisamine muutujasse
        List<AccountDto> accounts = bank.getAccounts(); //saame accountsi listi
        int accountId = transactionDto.getAccountId();

        // kontollime kas konto eksisteerib
        if (!accountService.accountIdExist(accounts, accountId)) {
            requestResult.setAccountId(accountId);
            requestResult.setError("Account ID " + accountId + " does not exist!");
            return requestResult;
        }

        // veel vajalike andmete lisamine muutujasse
        Character transactionType = transactionDto.getTransactionType();
        Integer amount = transactionDto.getAmount();
        int transactionId = bank.getTransactionIdCount();


        // pärin välja accountId abiga õige konto ja balance'i
        AccountDto account = accountService.getAccountById(accounts, accountId);
        Integer balance = account.getBalance();

        // käime läbi erinevad olukkorrad
        int newBalance; //muutuja tüübi ja nime anname siin aga case sees anname väärtuse
        String receiverAccountNumber;

        switch (transactionType) {
            case NEW_ACCOUNT:

                // täidame ära transactionDto
                transactionDto.setSenderAccountNumber(null);
                transactionDto.setReceiverAccountNumber(null);
                transactionDto.setBalance(0);
                transactionDto.setAmount(0);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);

                //lisame tehingu transactinite alla ja incremeteerime
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionId();

                //meisterdame valmis result objekti
                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully added 'new account' transaction");
                return requestResult;


            case DEPOSIT:

                //arvutame välja uue balance
                newBalance = balance + amount;

                // täidame ära transactionDto
                transactionDto.setSenderAccountNumber(ATM);
                transactionDto.setReceiverAccountNumber(account.getAccountNumber());
                transactionDto.setBalance(newBalance);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);

                //lisame tehingu transactinite alla ja inkremeteerime
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionId();

                //uuendame konto balance'it
                account.setBalance(newBalance);

                //meisterdame valmis result objekti
                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully made deposit transaction");
                return requestResult;


            case WITHDRAWAL:

                if (!balanceService.enoughMoneyOnAccount(balance, amount)) {
                    requestResult.setAccountId(accountId);
                    requestResult.setError("Not enough money" + amount);
                    return requestResult;

                }

                //arvutame välja uue balance
                newBalance = balance - amount;


                // täidame ära transactionDto
                transactionDto.setSenderAccountNumber(account.getAccountNumber());
                transactionDto.setReceiverAccountNumber(ATM);
                transactionDto.setBalance(newBalance);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);

                //lisame tehingu transactinite alla ja inkremeteerime
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionId();

                //uuendame konto balance'it
                account.setBalance(newBalance);

                //meisterdame valmis result objekti
                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully made withdrawal transaction");
                return requestResult;


            case SEND_MONEY:

                //kontrollime kas saatjal on piisavalt raha
                if (!balanceService.enoughMoneyOnAccount(balance, amount)) {
                    requestResult.setAccountId(accountId);
                    requestResult.setError("Not enough money" + amount);
                    return requestResult;

                }

                //arvutame välja uue balance, saatjalt võtame raha ära
                newBalance = balance - amount;


                // saatja transakstsioon täidame ära transactionDto
                transactionDto.setSenderAccountNumber(account.getAccountNumber()); //senderi võtame id küljest aga receiveri saame jsonist
                transactionDto.setBalance(newBalance);
                transactionDto.setLocalDateTime(LocalDateTime.now());
                transactionDto.setId(transactionId);

                //lisame tehingu transactinite alla ja inkremeteerime
                bank.addTransactionToTransactions(transactionDto);
                bank.incrementTransactionId();

                //uuendame konto balance'it
                account.setBalance(newBalance);

                //meisterdame valmis result objekti - enne kui saadame
                requestResult.setTransactionId(transactionId);
                requestResult.setAccountId(accountId);
                requestResult.setMessage("Successfully sent money");

                //teeme Saaja (receiver) transactioni
                // teeme muutuja, sest kasutame seda hiljem veel
                receiverAccountNumber = transactionDto.getReceiverAccountNumber();

                // kontrollime, kas saaja konto nr eksisteerib meie andmebaasis (bank) ja kui eksisteerib, siis peame tegema saaja tegevuse
                if (accountService.accountNumberExist(accounts, receiverAccountNumber)) {
                    AccountDto receiverAccount = accountService.getAccountByNumber(accounts, receiverAccountNumber);
                    int receiverNewBalance = receiverAccount.getBalance() + amount;

                    //loome tühja objekti, et saaksime sinna infot hakata panema
                    TransactionDto receiverTransactionDto = new TransactionDto();

                    // täidame ära receiverTransactionDto
                    receiverTransactionDto.setSenderAccountNumber(account.getAccountNumber()); //saatja konto nr on konto küljes olemas
                    receiverTransactionDto.setReceiverAccountNumber(account.getAccountNumber()); //receiver nr tuli sõnumist
                    receiverTransactionDto.setBalance(newBalance);
                    receiverTransactionDto.setLocalDateTime(LocalDateTime.now());
                    receiverTransactionDto.setId(bank.getTransactionIdCount());
                    receiverTransactionDto.setAmount(amount);
                    receiverTransactionDto.setTransactionType(RECEIVE_MONEY);

                    //lisame tehingu transactionite alla (pluss inkrementeerime)
                    bank.addTransactionToTransactions(receiverTransactionDto);
                    bank.incrementTransactionId();
                    receiverAccount.setBalance(receiverNewBalance);
                }
                return requestResult;

            default:
                requestResult.setError("Unknown transaction type: " + transactionType);
                return requestResult;
        }

    }
// loome uue klassi, mis tagastab request..., selle nimi on receive ....( sellesse annan kaasa/ on objektid nimedega...)
    public RequestResult receiveNewTransaction(Bank bank, TransactionDto transactionDto) {
        //loome muutujad if-st välja, sest plaanime neid veel kasutatda.
//        loome need muutujad, sest kasutame neid juba mitmel korral.

        RequestResult requestResult = new RequestResult();
        String receiverAccountNumber = transactionDto.getReceiverAccountNumber();
        List<AccountDto> accounts = bank.getAccounts();

//        kontollime, kas seda kontot meie pangas üldse on

        if (!accountService.accountNumberExist(accounts, receiverAccountNumber)) {
            requestResult.setError("No such account in our bank!" + receiverAccountNumber);
            return requestResult;
        }

//        meil on vaja nüüd seda kontot ( sest ta eksisteerib)

        AccountDto receiverAccount = accountService.getAccountByNumber(accounts, receiverAccountNumber);
        int transactionId = bank.getTransactionIdCount();

//        võtame receiveri balance ja lisame juurde, mis sõnumist saime ja paneme selle muutujasse
//        hoiame seda järjekorda, mis varem tegime
        int receiverNewBalance = receiverAccount.getBalance() + transactionDto.getAmount();

        transactionDto.setTransactionType(RECEIVE_MONEY);
        transactionDto.setBalance(receiverNewBalance);
        transactionDto.setId(transactionId);
        transactionDto.setAccountId(receiverAccount.getId());
        transactionDto.setLocalDateTime(LocalDateTime.now());

//      nüüd lisame selle tehingu banka
        bank.addTransactionToTransactions(transactionDto);
        bank.incrementTransactionId();

//      muudame balacei ära
        receiverAccount.setBalance(receiverNewBalance);

        requestResult.setTransactionId(transactionId);
        requestResult.setMessage("Transaction received");

        return requestResult;
    }


    // TODO:    createTransactionForNewAccount()
    //  account number
    //  balance 0
    //  amount 0
    //  transactionType 'n'
    //  receiver jääb null
    //  sender jääb null


}

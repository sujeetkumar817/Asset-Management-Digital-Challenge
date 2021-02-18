package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountValidationException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  @Synchronized
  public Account accountFundTransfer (String debitAccountId, String creditAccountId, BigDecimal amount)
          throws AccountValidationException {
    Account debitAccount = null;
    Account creditAccount = null;

    // Validate Accounts
    debitAccount = this.accountsRepository.getAccount(debitAccountId);
    if (Objects.isNull(debitAccount)) {
      throw new AccountValidationException("Account Not available for " + debitAccountId);
    }
    creditAccount = this.accountsRepository.getAccount(creditAccountId);
    if (Objects.isNull(creditAccount)) {
      throw new AccountValidationException("Account Not available for " + creditAccountId);
    }

    // Validate Account Balance
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new AccountValidationException("Transfer amount must be positive");
    } else if (debitAccount.getBalance().compareTo(amount) < 0) {
      throw new AccountValidationException("Insufficient Fund in Debit account");
    }

    debitAccount.getBalance().subtract(amount);
    creditAccount.getBalance().add(amount);
    notificationService.notifyAboutTransfer(debitAccount, "Your account is debit with amount " + amount.toString() + " and creditted to account " + creditAccountId);
    notificationService.notifyAboutTransfer(creditAccount, "Your account is credit with amount " + amount.toString() + " from the account " + debitAccountId);
    return debitAccount;
  }
}

package fr.dev.sensei.guild.keeper.finance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuildFinanceServiceTest {

    private static final String GUILD_ID = "guilde-du-crepuscule";

    @Mock
    GuildAccountRepository accountRepository;

    @InjectMocks
    GuildFinanceService service;

    @Test
    void should_increase_balance_and_persist_account_when_deposit_is_valid() {
        // Arrange : un GuildAccount de solde connu
        GuildAccount account = new GuildAccount(GUILD_ID, 100);

        // Act : service.deposit(account, montant) depot d'un montant
        service.deposit(account, 50);

        // Assert : nouveau solde attendu + verify(accountRepository).save(...)
        assertThat(account.balance()).isEqualTo(150);
        verify(accountRepository).save(account);
    }

    @Test
    void should_accumulate_balance_when_several_deposits_are_made() {
        // Arrange
        GuildAccount account = new GuildAccount(GUILD_ID, 100);

        // Act : deux depots successifs
        service.deposit(account, 30);
        service.deposit(account, 30);

        // Assert : les montants s'additionnent et chaque depot declenche sa propre sauvegarde
        assertThat(account.balance()).isEqualTo(160);
        verify(accountRepository, times(2)).save(account);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -250})
    void should_throw_InvalidAmountException_when_deposit_amount_is_not_positive(int amount) {
        // Arrange
        GuildAccount account = new GuildAccount(GUILD_ID, 100);

        // Act & Assert
        assertThatThrownBy(() -> service.deposit(account, amount))
            .isInstanceOf(InvalidAmountException.class);
        assertThat(account.balance()).isEqualTo(100);
    }

    @Test
    void should_decrease_balance_and_persist_account_when_loot_is_distributed() {
        // Arrange
        GuildAccount account = new GuildAccount(GUILD_ID, 100);

        // Act : distribution d'un butin couvert par le solde
        service.distributeLoot(account, 40);

        // Assert : le solde est debite ET le compte est persiste une seule fois
        assertThat(account.balance()).isEqualTo(60);
        verify(accountRepository).save(account);
    }

    @Test
    void should_empty_balance_and_persist_account_when_loot_equals_balance() {
        // Arrange
        GuildAccount account = new GuildAccount(GUILD_ID, 100);

        // Act : on vide integralement le compte
        service.distributeLoot(account, 100);

        // Assert : solde a zero et sauvegarde effectuee
        assertThat(account.balance()).isZero();
        verify(accountRepository).save(account);
    }

    @Test
    void should_reject_loot_and_keep_balance_unchanged_when_funds_are_insufficient() {
        // Arrange
        GuildAccount account = new GuildAccount(GUILD_ID, 100);

        // Act & Assert
        assertThatThrownBy(() -> service.distributeLoot(account, 150))
            .isInstanceOf(InsufficientFundsException.class);
        // rien n'est debite ni persiste
        assertThat(account.balance()).isEqualTo(100);
        verify(accountRepository, never()).save(account);
    }

    @Test
    void should_reject_loot_when_account_is_empty() {
        // Arrange : compte vide
        GuildAccount account = new GuildAccount(GUILD_ID, 0);

        // Act & Assert
        assertThatThrownBy(() -> service.distributeLoot(account, 20))
            .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.balance()).isZero();
        verify(accountRepository, never()).save(account);
    }

    @ParameterizedTest
    @CsvSource({
        "100, 50, true",
        "100, 100, true",
        "100, 101, false",
        "0, 0, true",
        "0, 1, false"
    })
    void should_tell_whether_balance_covers_the_requested_amount(int balance, int amount, boolean expected) {
        // Arrange
        GuildAccount account = new GuildAccount(GUILD_ID, balance);

        // Act : lecture de checkSolvency
        boolean solvent = service.checkSolvency(account, amount);

        // Assert : verification de chaque ligne
        assertThat(solvent).isEqualTo(expected);
    }
}

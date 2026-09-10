package fr.dev.sensei.guild.keeper.cucumber;

import fr.dev.sensei.guild.keeper.finance.*;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

public class FinanceSteps {

    private GuildAccountRepository guildAccountRepository;
    private GuildFinanceService guildFinanceService;
    private GuildAccount account;
    private String guildId;
    private Throwable caughtException;

    @Soit("une guilde {string}")
    public void une_guilde(String name) {
        guildId = name;
        guildAccountRepository = new InMemoryGuildAccountRepository();
        guildFinanceService = new GuildFinanceService(guildAccountRepository);
        caughtException = null;
    }

    @Soit("le compte de la guilde a {int} pieces d'or")
    public void le_compte_de_la_guilde_a(int balance) {
        account = new GuildAccount(guildId, balance);
    }

    @Quand("je distribue {int} pieces d'or de butin")
    public void je_distribue_de_butin(int amount) {
        caughtException = catchThrowable(() -> guildFinanceService.distributeLoot(account, amount));
    }

    @Quand("je depose {int} pieces d'or dans le compte de la guilde")
    public void je_depose_dans_le_compte_de_la_guilde(int amount) {
        caughtException = catchThrowable(() -> guildFinanceService.deposit(account, amount));
    }

    @Alors("la guilde a {int} pieces d'or")
    public void la_guilde_a_pieces_d_or(int expectedBalance) {
        assertThat(account.balance()).isEqualTo(expectedBalance);
    }

    @Alors("la distribution est refusee pour fonds insuffisants")
    public void la_distribution_est_refusee_pour_fonds_insuffisants() {
        assertThat(caughtException).isInstanceOf(InsufficientFundsException.class);
    }

    @Et("le compte de la guilde est enregistre")
    public void le_compte_de_la_guilde_est_enregistre() {
        assertThat(guildAccountRepository.findByGuildId(guildId)).contains(account);
    }

    @Et("le compte de la guilde n'est pas enregistre")
    public void le_compte_de_la_guilde_n_est_pas_enregistre() {
        assertThat(guildAccountRepository.findByGuildId(guildId)).isEmpty();
    }
}

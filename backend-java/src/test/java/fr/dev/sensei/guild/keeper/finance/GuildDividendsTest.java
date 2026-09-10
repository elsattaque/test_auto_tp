package fr.dev.sensei.guild.keeper.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GuildDividendsTest {

    @Mock
    GuildAccountRepository accountRepository;

    @InjectMocks
    GuildFinanceService service;

    @Test
    void should_distribute_nothing_when_guild_has_no_members() {
        GuildAccount account = new GuildAccount("g-1", 1000);

        Map<Member, Integer> shares = service.distributeDividends(account, List.of(), 10);

        assertThat(shares).isEmpty();
        assertThat(account.balance()).isEqualTo(1000);
    }

    @Test
    void should_give_the_whole_enveloppe_to_the_only_member() {
        GuildAccount account = new GuildAccount("g-1", 1000);
        Member member = new Member("m-1", "Dragan", MemberRank.NOVICE, 0, 1);

        Map<Member, Integer> shares = service.distributeDividends(account, List.of(member), 10);

        assertThat(shares).containsExactly(entry(member, 100));
        assertThat(account.balance()).isEqualTo(900);
        verify(accountRepository).save(account);
    }

    @Test
    void should_split_the_envelope_according_to_rank_weights() {
        GuildAccount account = new GuildAccount("g-1", 1000);
        Member novice = new Member("m-1", "Dragan", MemberRank.NOVICE, 0, 1);
        Member veteran = new Member("m-2", "Sylve", MemberRank.VETERAN, 0, 1);

        Map<Member, Integer> shares = service.distributeDividends(account, List.of(novice, veteran), 10);

        assertThat(shares).containsExactly(entry(novice, 25), entry(veteran, 75));
        assertThat(account.balance()).isEqualTo(900);
        verify(accountRepository).save(account);
    }

    @Test
    void should_leave_the_rest_on_the_account_when_shares_do_not_divide_evenly() {
        GuildAccount account = new GuildAccount("g-1", 1000);
        Member novice = new Member("m-1", "Dragan", MemberRank.NOVICE, 0, 1);
        Member apprentice = new Member("m-2", "Sylve", MemberRank.APPRENTICE, 0, 1);

        Map<Member, Integer> shares = service.distributeDividends(account, List.of(novice, apprentice), 10);

        assertThat(shares).containsExactly(entry(novice, 33), entry(apprentice, 66));
        assertThat(account.balance()).isEqualTo(901);
        verify(accountRepository).save(account);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void should_reject_dividends_when_percentage_is_not_positive(int percentage) {
        GuildAccount account = new GuildAccount("g-1", 1000);
        Member member = new Member("m-1", "Dragan", MemberRank.NOVICE, 0, 1);

        assertThatThrownBy(() -> service.distributeDividends(account, List.of(member), percentage))
            .isInstanceOf(InvalidAmountException.class);
        assertThat(account.balance()).isEqualTo(1000);
        verify(accountRepository, never()).save(account);
    }

    @Test
    void should_reject_dividends_when_account_cannot_cover_the_shares() {
        GuildAccount account = new GuildAccount("g-1", 100);
        Member member = new Member("m-1", "Dragan", MemberRank.NOVICE, 0, 1);

        assertThatThrownBy(() -> service.distributeDividends(account, List.of(member), 200))
            .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.balance()).isEqualTo(100);
        verify(accountRepository, never()).save(account);
    }

    @Test
    void should_never_let_the_balance_become_negative() {
        GuildAccount account = new GuildAccount("g-1", 100);
        Member novice = new Member("m-1", "Dragan", MemberRank.NOVICE, 0, 1);
        Member master = new Member("m-2", "Sylve", MemberRank.GUILD_MASTER, 0, 1);

        service.distributeDividends(account, List.of(novice, master), 100);

        assertThat(account.balance()).isNotNegative();
    }
}

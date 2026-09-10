package fr.dev.sensei.guild.keeper.finance;

import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operations financieres sur le compte d'une guilde.
 *
 * <p>Perimetre volontairement limite a ce qui est specifie : depot, distribution
 * de butin, verification de solvabilite. La distribution de dividendes par rang
 * n'est PAS implementee ici : elle est a developper en TDD par les etudiants.
 * Les instructions a suivre sont donnees avec le projet final.
 */
public class GuildFinanceService {

    private static final Map<MemberRank, Integer> WEIGHTS_BY_RANK = Map.of(
        MemberRank.NOVICE, 1,
        MemberRank.APPRENTICE, 2,
        MemberRank.VETERAN, 3,
        MemberRank.ELITE, 4,
        MemberRank.GUILD_MASTER, 5);

    private final GuildAccountRepository guildAccountRepository;

    public GuildFinanceService(GuildAccountRepository guildAccountRepository) {
        this.guildAccountRepository = guildAccountRepository;
    }

    /**
     * Credite le compte de la guilde.
     *
     * @throws InvalidAmountException si {@code amount <= 0}
     */
    public void deposit(GuildAccount account, int amount) {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        account.increaseBy(amount);
        guildAccountRepository.save(account);
    }

    /**
     * Debite le compte du montant de butin distribue a un membre.
     *
     * @throws InsufficientFundsException si le compte n'est pas solvable pour ce montant
     */
    public void distributeLoot(GuildAccount account, int amount) {
        if (!checkSolvency(account, amount)) {
            throw new InsufficientFundsException(account.balance(), amount);
        }
        account.decreaseBy(amount);
        guildAccountRepository.save(account);
    }

    /** @return {@code true} si le solde couvre {@code amount}. */
    public boolean checkSolvency(GuildAccount account, int amount) {
        return account.balance() >= amount;
    }

    /**
     * Distribue une part du solde de la guilde à ses membres, proportionnellement à leur rang.
     *
     * @return la repartition membre -> part ; donne vide si la guilde vide
     */
    public Map<Member, Integer> distributeDividends(GuildAccount account, List<Member> members, int percentage) {
        if (percentage <= 0) {
            throw new InvalidAmountException(percentage);
        }
        if (members.isEmpty()) {
            return Map.of();
        }

        int envelope = account.balance() * percentage / 100;
        int totalWeight = members.stream()
            .mapToInt(member -> WEIGHTS_BY_RANK.get(member.rank()))
            .sum();

        Map<Member, Integer> shares = new LinkedHashMap<>();
        int distributed = 0;
        for (Member member : members) {
            int share = envelope * WEIGHTS_BY_RANK.get(member.rank()) / totalWeight;
            shares.put(member, share);
            distributed += share;
        }

        if (!checkSolvency(account, distributed)) {
            throw new InsufficientFundsException(account.balance(), distributed);
        }

        account.decreaseBy(distributed);
        guildAccountRepository.save(account);

        return shares;
    }
}

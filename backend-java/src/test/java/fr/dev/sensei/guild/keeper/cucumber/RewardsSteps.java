package fr.dev.sensei.guild.keeper.cucumber;

import fr.dev.sensei.guild.keeper.experience.ExperienceCalculator;
import fr.dev.sensei.guild.keeper.missions.LootCalculator;
import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.missions.QuestDifficulty;
import fr.dev.sensei.guild.keeper.recruitment.InMemoryMemberRepository;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.rewards.FakeNotificationPort;
import fr.dev.sensei.guild.keeper.rewards.RewardsDistributionService;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Squelette des steps de {@code features/rewards.feature} — ATELIER CHAPITRE 6.
 *
 * <p>Modèle : {@link RecruitmentSteps}. Câbler {@code RewardsDistributionService}
 * (avec {@code fr.dev.sensei.guild.keeper.rewards.FakeNotificationPort},
 * {@code ExperienceCalculator}, {@code LootCalculator}, un {@code InMemoryMemberRepository})
 * puis implémenter chaque méthode. Enfin, élargir {@code @SelectClasspathResource}
 * de {@link fr.dev.sensei.guild.keeper.RunCucumberTest} au dossier {@code "features"}.
 *
 * <p>Tant que ce n'est pas fait, {@code rewards.feature} n'est pas exécuté et ces
 * méthodes ne sont jamais appelées.
 */
public class RewardsSteps {

    /**
     * La chance n'apparaît pas dans le scénario ; on la fixe pour rendre le
     * butin déterministe. loot = base + trunc(base * 5 / 20) => 100 -> 125.
     */
    private static final int CHANCE_PAR_DEFAUT = 5;

    private final InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
    private final FakeNotificationPort notificationPort = new FakeNotificationPort();
    private final RewardsDistributionService rewardsDistributionService = new RewardsDistributionService(
        memberRepository, notificationPort, new ExperienceCalculator(), new LootCalculator());

    private final Map<String, Quest> questsParTitre = new HashMap<>();
    private RewardsDistributionService.RewardsResult dernierResultat;

    @Soit("un aventurier {string} de rang {string}")
    public void un_aventurier_de_rang(String nom, String rang) {
        memberRepository.save(new Member("m-" + nom, nom, MemberRank.valueOf(rang), 0, CHANCE_PAR_DEFAUT));
    }

    @Et("une quête {string} de difficulté {string} rapportant {int} d'expérience et {int} d'or")
    public void une_quete_de_difficulte(String titre, String difficulte, int experienceBase, int orBase) {
        questsParTitre.put(titre, Quest.standalone(
            "q-" + titre, titre, QuestDifficulty.valueOf(difficulte), experienceBase, orBase));
    }

    @Quand("{string} termine la quête {string}")
    public void termine_la_quete(String nom, String titre) {
        Member membre = memberRepository.findByName(nom).orElseThrow();
        QuestAssignment assignment = new QuestAssignment(
            membre, questsParTitre.get(titre), QuestAssignmentStatus.COMPLETED);
        dernierResultat = rewardsDistributionService.distributeRewards(assignment);
    }

    @Alors("{string} gagne {int} points d'expérience")
    public void gagne_points_d_experience(String nom, int experienceAttendue) {
        assertThat(dernierResultat.experienceGained()).isEqualTo(experienceAttendue);
        assertThat(memberRepository.findByName(nom)).get()
            .extracting(Member::experiencePoints)
            .isEqualTo(experienceAttendue);
    }

    @Et("{string} reçoit {int} pièces d'or de butin")
    public void recoit_pieces_d_or(String nom, int butinAttendu) {
        assertThat(dernierResultat.lootValue()).isEqualTo(butinAttendu);
    }

    @Et("{string} est notifié")
    public void est_notifie(String nom) {
        Member membre = memberRepository.findByName(nom).orElseThrow();
        assertThat(notificationPort.hasNotified(membre)).isTrue();
    }
}

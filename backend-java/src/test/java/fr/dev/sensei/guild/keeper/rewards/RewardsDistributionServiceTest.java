package fr.dev.sensei.guild.keeper.rewards;

import fr.dev.sensei.guild.keeper.experience.ExperienceCalculator;
import fr.dev.sensei.guild.keeper.missions.LootCalculator;
import fr.dev.sensei.guild.keeper.missions.Quest;
import fr.dev.sensei.guild.keeper.missions.QuestAssignment;
import fr.dev.sensei.guild.keeper.missions.QuestAssignmentStatus;
import fr.dev.sensei.guild.keeper.missions.QuestDifficulty;
import fr.dev.sensei.guild.keeper.recruitment.Member;
import fr.dev.sensei.guild.keeper.recruitment.MemberRank;
import fr.dev.sensei.guild.keeper.recruitment.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardsDistributionServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private final FakeNotificationPort notificationPort = spy(new FakeNotificationPort());
    private final ExperienceCalculator experienceCalculator = new ExperienceCalculator();
    private final LootCalculator lootCalculator = new LootCalculator();
    private RewardsDistributionService rewardsDistributionService;

    @BeforeEach
    void setUp() {
        rewardsDistributionService = new RewardsDistributionService(
            memberRepository, notificationPort, experienceCalculator, lootCalculator
        );
    }

    @Test
    void should_credit_experience_and_loot_when_quest_is_completed() {
        // Arrange
        Member veteran = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(veteran, quest, QuestAssignmentStatus.COMPLETED);
        RewardsDistributionService service = new RewardsDistributionService(
                memberRepository, notificationPort, experienceCalculator, lootCalculator);

        // Act
        RewardsDistributionService.RewardsResult result = service.distributeRewards(assignment);

        // Assert
        assertThat(result.experienceGained()).isEqualTo(120);
        assertThat(result.lootValue()).isEqualTo(125);
        assertThat(veteran.experiencePoints()).isEqualTo(120);
        verify(memberRepository).save(veteran);
    }

    // Chapitre 4 — live coding « Isoler la distribution de récompenses, en Java puis en TypeScript »
    @Test
    void should_notify_member_after_distributing_rewards() {
        // Arrange
        Member veteran = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(veteran, quest, QuestAssignmentStatus.COMPLETED);

        // Act
        RewardsDistributionService.RewardsResult result = rewardsDistributionService.distributeRewards(assignment);

        //assert
        assertThat(notificationPort.count()).isEqualTo(1);
        assertThat(notificationPort.lastNotification())
            .extracting(FakeNotificationPort.SentNotification::member)
            .isEqualTo(veteran);
        assertThat(notificationPort.lastNotification())
            .extracting(FakeNotificationPort.SentNotification::message)
            .asString()
            .contains(quest.title())
            .contains(result.experienceGained() + " XP")
            .contains(result.lootValue() + " pieces d'or");

        verify(notificationPort, times(1)).notifyMember(any(), any());
    }

    // Chapitre 4 — « Atelier pratique - Isoler les dépendances externes de GuildKeeper »
    //       (test d'isolation : quête non COMPLETED -> exception levée et personne n'est notifié, verify(..., never()))
    @Test
    void should_not_notify_anyone_when_quest_is_not_completed() {
        // arrange
        Member veteran = new Member("m-1", "Dante", MemberRank.VETERAN, 0, 5);
        Quest quest = Quest.standalone("q-1", "Purger le donjon", QuestDifficulty.HARD, 100, 100);
        QuestAssignment assignment = new QuestAssignment(veteran, quest, QuestAssignmentStatus.ABANDONED);

        // act & assert
        assertThatThrownBy(() -> rewardsDistributionService.distributeRewards(assignment))
            .isInstanceOf(IllegalStateException.class);

        verify(memberRepository, never()).save(any());
        verify(notificationPort, never()).notifyMember(any(), any()); // attention à bien mettre spy() devant l'instance de notificationPort
        assertThat(veteran.experiencePoints()).isZero();
    }
}

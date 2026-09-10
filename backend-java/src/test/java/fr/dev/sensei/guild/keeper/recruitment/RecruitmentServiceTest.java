package fr.dev.sensei.guild.keeper.recruitment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RecruitmentService recruitmentService;

    @Test
    void should_recruit_candidate_when_name_is_valid_and_not_taken() {
        // Arrange
        when(memberRepository.findByName("Dorian")).thenReturn(Optional.empty());

        // Act
        Member recruit = recruitmentService.recruit("Dorian");

        // Assert
        assertThat(recruit.name()).isEqualTo("Dorian");
        assertThat(recruit.rank()).isEqualTo(MemberRank.NOVICE);
        assertThat(recruit.experiencePoints()).isZero();

        ArgumentCaptor<Member> savedMember = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(savedMember.capture());
        assertThat(savedMember.getValue().name()).isEqualTo("Dorian");
    }

    // Chapitre 4 — « TP guidé - Isoler le service de recrutement »
    //       (Given/When/Then posés en Chapitre 1 — « Atelier pratique - Premiers pas sur GuildKeeper »)
    @Test
    void should_throw_DuplicateMemberException_when_name_already_exists() {
        // given : un membre Dorian qui existe dans la bdd
        // when : on recrute Dorian
        // then : on a un DuplicateMemberException qui contient "Dorian"

        // Arrange
        Member existingMember = Member.novice("id-1", "Dorian", 3);
        when(memberRepository.findByName("Dorian")).thenReturn(Optional.of(existingMember));

        // Act & Assert
        assertThatThrownBy(() -> recruitmentService.recruit("Dorian"))
            .isInstanceOf(DuplicateMemberException.class)
            .hasMessageContaining("Dorian");

        verify(memberRepository, never()).save(any());
    }

    // Chapitre 4 — « TP guidé - Isoler le service de recrutement »
    //       (Given/When/Then posés en Chapitre 1 — « Atelier pratique - Premiers pas sur GuildKeeper »)
    @Test
    void should_reject_candidate_when_name_is_blank() {
        // act : on recrute un membre sans nom
        // assert : on a une IllegalArgumentException

        // Act & Assert
        assertThatThrownBy(() -> recruitmentService.recruit(" "))
            .isInstanceOf(IllegalArgumentException.class);

        // verifier qu'on fait même pas appel a la base de données - on le passe direct
        verify(memberRepository, never()).findByName(any());
        verify(memberRepository, never()).save(any());
    }
}

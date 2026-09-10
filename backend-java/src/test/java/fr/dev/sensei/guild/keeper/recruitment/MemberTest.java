package fr.dev.sensei.guild.keeper.recruitment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Atelier du chapitre 2 — test d'effet de bord.
 *
 * <p>{@link Member#addExperience(int)} ne retourne rien : elle agit par effet de bord en
 * modifiant {@code experiencePoints}. Le test interroge donc l'état de l'objet après l'appel.
 *
 * <p>À compléter :
 * <ul>
 *   <li>{@code addExperience(60)} sur un membre à 0 XP -> {@code experiencePoints()} vaut 60 ;</li>
 *   <li>{@code addExperience(0)} ou une valeur négative -> {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * <p>Une fois écrit, retirer {@code @Tag("todo")} et le {@code fail(...)}.
 */
class MemberTest {

    // Chapitre 2 — « Atelier pratique - Consolider la suite de tests GuildKeeper » (effet de bord)
    @Test
    void should_increase_experience_points_when_experience_is_added() {
        // arrange
        Member member = Member.novice("id-1", "Ektor", 1);

        // act
        member.addExperience(100);

        // assert
        assertThat(member.experiencePoints()).isEqualTo(100);
    }

    // Chapitre 2 — « Atelier pratique - Consolider la suite de tests GuildKeeper » (effet de bord)
    @Test
    void should_reject_a_non_positive_experience_gain() {
        // arrange
        Member member = Member.novice("id-1", "Dante", 1);

        // act & assert
        assertThatThrownBy(() -> member.addExperience(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> member.addExperience(-10)).isInstanceOf(IllegalArgumentException.class);
        assertThat(member.experiencePoints()).isZero();
    }
}

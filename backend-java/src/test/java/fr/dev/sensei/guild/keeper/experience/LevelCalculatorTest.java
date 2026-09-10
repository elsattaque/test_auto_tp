package fr.dev.sensei.guild.keeper.experience;

import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Chapitre 5 — live coding « TDD sur le calcul de niveau » — <b>à développer entièrement en TDD</b>.
 *
 * <p>La classe de production {@code LevelCalculator} n'existe pas encore : c'est le seul
 * exercice vraiment "from scratch" du cours. On la fait naitre d'un test, en suivant le
 * cycle red / green / refactor.
 *
 * <p>Règle visée : {@code levelFor(int experiencePoints)} renvoie le niveau d'affichage
 * d'un membre, dérivé de ses points d'expérience.
 * <ul>
 *   <li>0 XP -> niveau 1 ;</li>
 *   <li>+1 niveau tous les 100 XP (100 -> 2, 250 -> 3, ...) ;</li>
 *   <li>expérience négative -> {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * <p>Classe à créer dans ce package ({@code experience}), non câblée au reste du domaine.
 * Une fois un test écrit, retirer {@code @Tag("todo")} et le {@code fail(...)}.
 */
class LevelCalculatorTest {

    private final LevelCalculator levelCalculator = new LevelCalculator();

    // red -> écrire ce test, créer LevelCalculator, le faire passer au vert
    @Test
    void should_return_level_1_for_zero_experience() {
        int level = levelCalculator.calculateLevel(0);
        assertThat(level).isEqualTo(1);
    }

    // refactor -> ce deuxième palier force à généraliser la formule
    @Test
    void should_return_level_2_from_100_experience_points() {
        int level = levelCalculator.calculateLevel(100);
        assertThat(level).isEqualTo(2);
    }

    // cas limite
    @Test
    void should_reject_negative_experience() {
        assertThatThrownBy(() -> levelCalculator.calculateLevel(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("L'expérience ne peut pas être négative");
    }
}

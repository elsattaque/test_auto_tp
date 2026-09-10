package fr.dev.sensei.guild.keeper.missions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LootCalculatorTest {

    private final LootCalculator lootCalculator = new LootCalculator();

    @Test
    void should_calculate_loot_with_low_luck() {
        // Act
        int loot = lootCalculator.calculateLoot(100, 2);

        // Assert
        assertThat(loot).isEqualTo(110);
    }

    @Test
    void should_calculate_loot_with_medium_luck() {
        // Act
        int loot = lootCalculator.calculateLoot(100, 5);

        // Assert
        assertThat(loot).isEqualTo(125);
    }

    @Test
    void should_calculate_loot_with_high_luck() {
        // Act
        int loot = lootCalculator.calculateLoot(100, 10);

        // Assert
        assertThat(loot).isEqualTo(150);
    }

    // Chapitre 2 — « Convertir des tests dupliqués »
    //       (repris en Chapitre 5 — « TP guidé - Calculer le butin d'une quête », en kata TDD)
    @ParameterizedTest(name = "baseLootValue={0}, luck={1} -> {2}")
    @MethodSource("lootScenarios")
    void should_calculate_loot_for_various_luck_and_base_values(int baseLootValue, int luck, int expectedLoot) {
        // Act
        int loot = lootCalculator.calculateLoot(baseLootValue, luck);

        // Assert
        assertThat(loot).isEqualTo(expectedLoot);
    }

    static Stream<Arguments> lootScenarios() {
        // Chapitre 2 — compléter la source avec plusieurs couples (baseLootValue, luck, expectedLoot)
        return Stream.of(
                Arguments.of(100, 2, 110),
                Arguments.of(100, 5, 125),
                Arguments.of(100, 10, 150)
        );
    }
}

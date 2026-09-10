package fr.dev.sensei.guild.keeper.experience;

public class LevelCalculator {

    public int calculateLevel(int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("L'expérience ne peut pas être négative");
        }
        return exp/100 +1;
    }
}

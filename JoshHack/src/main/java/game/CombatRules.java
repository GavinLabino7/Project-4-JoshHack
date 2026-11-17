package game;

public class CombatRules {

    public static int calculateNewHp(int currentHp, int maxHp, int amount) {
        int newHp = currentHp + amount;

        if (newHp > maxHp)
            return maxHp;

        if (newHp < 0)
            return 0;

        return newHp;
    }

    public static boolean isDead(int hp) {
        return hp < 1;
    }
}

package game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import java.awt.Color;

public class CreatureTest {

private World world;
private Creature creature;

@BeforeEach
void setUp() {
    Tile[][][] tiles = new Tile[3][3][1];

    for (int x = 0; x < 3; x++) {
        for (int y = 0; y < 3; y++) {
            tiles[x][y][0] = Tile.FLOOR;
        }
    }

    world = new World(tiles);

    creature = new Creature(world, '@', Color.GRAY, "josh", 100, 10, 5);

    creature.x = 1;
    creature.y = 1;
    creature.z = 0;
}

@Test
void testCalculateNewHpIsDeadTrue(){
    int hp = CombatRules.calculateNewHp(1, 100, -1);
    assertEquals(0, hp);
    assertTrue(CombatRules.isDead(hp));
}

@ParameterizedTest 
@CsvSource({
    "100, 100, 1",
    "1, 1, 0",
    "2, 40, -38"
}) 
void testCalculateNewHpIsDeadFalse(int expected, int current, int amount){
    int hp = CombatRules.calculateNewHp(current, 100, amount);
    assertEquals(expected, hp);
    assertFalse(CombatRules.isDead(hp));
}

}
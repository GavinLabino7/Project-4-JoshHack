package game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.awt.Color;
import java.util.List;

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
    creature.setCreatureAi(new CreatureAi(creature));

    creature.x = 1;
    creature.y = 1;
    creature.z = 0;
    world.add(creature);
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

@ParameterizedTest
@CsvSource({
    "50, 100, 50, 100",
    "80, 100, 50, 100",
    "100, 100, 0, 100",
    "100, 100, -20, 80",
    "0, 100, 0, 0"
})
void testCalculateNewHpBoundaries(int current, int max, int amount, int expected){
    assertEquals(expected, CombatRules.calculateNewHp(current, max, amount));
}

@Test
void testCalculateNewHpDoesNotReturnNegativeHp(){
    assertEquals(0, CombatRules.calculateNewHp(10, 100, -50));
}

@Test
void testIsDeadAtExactZero(){
    assertTrue(CombatRules.isDead(0));
    assertTrue(CombatRules.isDead(-1));
    assertFalse(CombatRules.isDead(1));
}

@Test
void testModifyHpAlive(){
    creature.modifyHp(-94, "");
    assertEquals(6, creature.hp());
    assertNull(world.item(1, 1, 0));
}

@Test
void testModifyHpDead(){
    creature.modifyHp(-100, "combat");
    assertEquals(0, creature.hp());
    assertNotNull(world.item(1, 1, 0));
    assertEquals("combat", creature.causeOfDeath());
    assertNull(world.creature(1, 1, 0));
}

@Test
void testModifyHpOverkillClampsAtZero(){
    creature.modifyHp(-500, "overkill");
    assertEquals(0, creature.hp());
    assertEquals("overkill", creature.causeOfDeath());
}

@Test
void testZeroDamageAttackDoesNotDamageOrAwardXp(){
    Creature defender = new Creature(world, 'g', Color.GREEN, "goblin", 20, 5, 10);
    defender.setCreatureAi(new CreatureAi(defender));
    defender.x = 2;
    defender.y = 1;
    defender.z = 0;
    world.add(defender);

    creature.meleeAttack(defender);

    assertEquals(20, defender.hp());
    assertEquals(0, creature.xp());
    assertFalse(CombatRules.isDead(defender.hp()));
}

@Test
void testGainXpRejectsZeroAndNegativeRewards(){
    creature.gainXp(new Creature(world, 'g', Color.GREEN, "weak", 1, 0, -1));
    assertEquals(0, creature.xp());

    creature.gainXp(new Creature(world, 'g', Color.GREEN, "weak", 1, 0, 0));
    assertEquals(0, creature.xp());
}

@Test
void testModifyXpIncreasesLevelAfterThreshold(){
    RecordingCreatureAi ai = new RecordingCreatureAi(creature);
    creature.setCreatureAi(ai);

    creature.modifyXp(26);

    assertEquals(26, creature.xp());
    assertEquals(2, creature.level());
    assertEquals(1, ai.levelUps);
}

@Test
void testEffectsCannotBeMutatedThroughGetter(){
    creature.effects();
    List<Effect> snapshot = creature.effects();

    assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new Effect(1)));
}

@Test
void testLinePointsCannotBeMutatedThroughGetter(){
    Line line = new Line(0, 0, 2, 2);
    List<Point> snapshot = line.getPoints();
    snapshot.get(0).x = 99;

    assertEquals(0, line.getPoints().get(0).x);
    assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
}

@Test
void testWrittenSpellsCannotBeMutatedThroughGetter(){
    Item item = new Item('?', Color.BLUE, "Spellbook", null);
    item.addWrittenSpell("Fireball", 5, null);

    List<Spell> snapshot = item.writtenSpells();

    assertEquals(1, snapshot.size());
    assertThrows(UnsupportedOperationException.class, snapshot::clear);
}

@Test
void testPathPointsCannotBeMutatedThroughGetter(){
    Path path = new Path(creature, 2, 2);
    List<Point> snapshot = path.points();

    assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    if (!snapshot.isEmpty()) {
        snapshot.get(0).x = 99;
        assertNotEquals(99, path.points().get(0).x);
    }
}

}
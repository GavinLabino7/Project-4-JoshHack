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

@Test
void testCreatureStatsAndModifiers(){
    creature.modifyMaxHp(5);
    creature.modifyAttackValue(2);
    creature.modifyDefenseValue(3);
    creature.modifyVisionRadius(2);
    creature.modifyMaxMana(3);
    creature.modifyMana(-20);
    creature.modifyMana(4);
    creature.modifyRegenHpPer1000(5);
    creature.modifyRegenManaPer1000(5);
    creature.modifyDetectCreatures(1);

    assertEquals(105, creature.maxHp());
    assertEquals(12, creature.attackValue());
    assertEquals(8, creature.defenseValue());
    assertEquals(11, creature.visionRadius());
    assertEquals(8, creature.maxMana());
    assertEquals(4, creature.mana());
    assertEquals(666, creature.food());
    assertTrue(creature.isPlayer());
    assertEquals("josh", creature.name());
    assertEquals(Color.GRAY, creature.color());
    assertEquals('@', creature.glyph());
}

@Test
void testModifyManaDoesNotExceedMaximum(){
    creature.modifyMana(10);

    assertEquals(creature.maxMana(), creature.mana());
}

@Test
void testMovementStairsAndDigging(){
    Tile[][][] tiles = floorTiles(3, 3, 2);
    tiles[1][1][0] = Tile.STAIRS_DOWN;
    tiles[1][1][1] = Tile.STAIRS_UP;
    World stairWorld = new World(tiles);
    Creature climber = new Creature(stairWorld, '@', Color.GRAY, "climber", 20, 5, 1);
    new CreatureAi(climber);
    climber.x = 1;
    climber.y = 1;
    stairWorld.add(climber);

    climber.moveBy(0, 0, 0);
    climber.moveBy(0, 0, 1);
    assertEquals(1, climber.z);
    climber.moveBy(0, 0, -1);
    assertEquals(0, climber.z);
    tiles[1][1][0] = Tile.FLOOR;
    climber.moveBy(0, 0, -1);
    assertEquals(0, climber.z);

    creature.moveBy(1, 0, 0);
    assertEquals(2, creature.x);
    creature.dig(0, 0, 0);
}

@Test
void testAttacksAwardXpAndUseEquipment(){
    Creature defender = new Creature(world, 'g', Color.GREEN, "goblin", 20, 1, 0);
    new CreatureAi(defender);
    defender.x = 2;
    defender.y = 1;
    defender.z = 0;
    world.add(defender);

    Item weapon = new Item('/', Color.WHITE, "bow", null);
    weapon.modifyAttackValue(4);
    weapon.modifyRangedAttackValue(6);
    creature.equip(weapon);
    creature.rangedWeaponAttack(defender);
    assertTrue(defender.hp() < 20);
    defender.modifyHp(-100, "reset");

    Creature victim = new Creature(world, 'g', Color.GREEN, "victim", 2, 1, 0);
    new CreatureAi(victim);
    victim.x = 2;
    victim.y = 2;
    world.add(victim);
    creature.meleeAttack(victim);
    assertEquals(0, victim.hp());
    assertTrue(creature.xp() > 0);
}

@Test
void testPickupDropEquipAndConsume(){
    Item item = new Item('!', Color.WHITE, "potion", null);
    world.addAtEmptySpace(item, 1, 1, 0);
    creature.pickup();
    assertTrue(creature.inventory().contains(item));

    creature.equip(item);
    creature.unequip(item);
    creature.drop(item);
    assertFalse(creature.inventory().contains(item));

    Item food = new Item('%', Color.WHITE, "food", null);
    food.modifyFoodValue(20);
    creature.inventory().add(food);
    creature.eat(food);
    assertFalse(creature.inventory().contains(food));

    Item gross = new Item('%', Color.WHITE, "gross", null);
    gross.modifyFoodValue(-20);
    creature.inventory().add(gross);
    creature.quaff(gross);
    creature.modifyFood(-10000);
    assertEquals(0, creature.hp());
}

@Test
void testHealthManaEffectsAndSpells(){
    creature.modifyHp(-10, "damage");
    creature.modifyMana(-3);
    creature.modifyRegenHpPer1000(1000);
    creature.modifyRegenManaPer1000(1000);
    creature.update();
    assertEquals(91, creature.hp());
    assertEquals(3, creature.mana());

    Creature target = new Creature(world, 'g', Color.GREEN, "target", 50, 1, 0);
    new CreatureAi(target);
    target.x = 2;
    target.y = 1;
    target.z = 0;
    world.add(target);
    creature.castSpell(new Spell("zap", 2, new Effect(2)), 2, 1);
    assertEquals(1, creature.mana());
    creature.castSpell(new Spell("expensive", 20, new Effect(1)), 2, 1);
    creature.castSpell(new Spell("zap", 2, new Effect(1)), 0, 0);
}

@Test
void testVisibilityNamingActionsAndDetails(){
    Creature observer = new Creature(world, 'o', Color.WHITE, "observer", 20, 1, 1);
    new CreatureAi(observer);
    observer.x = 0;
    observer.y = 1;
    observer.z = 0;
    world.add(observer);

    assertTrue(creature.canSee(0, 0, 0));
    assertEquals(Tile.FLOOR, creature.tile(0, 0, 0));
    assertTrue(creature.canEnter(2, 2, 0));
    assertFalse(creature.canEnter(0, 1, 0));
    creature.doAction("wave");
    creature.doAction(new Item('?', Color.WHITE, "thing", null), "use");

    Item unknown = new Item('?', Color.WHITE, "potion", "red liquid");
    assertEquals("red liquid", creature.nameOf(unknown));
    creature.learnName(unknown);
    assertEquals("potion", creature.nameOf(unknown));
    assertTrue(creature.details().contains("level:"));
    creature.modifyDetectCreatures(-1);
    assertNull(creature.item(99, 99, 0));
    assertNull(creature.creature(99, 99, 0));
}

private static Tile[][][] floorTiles(int width, int height, int depth) {
    Tile[][][] tiles = new Tile[width][height][depth];
    for (int x = 0; x < width; x++)
        for (int y = 0; y < height; y++)
            for (int z = 0; z < depth; z++)
                tiles[x][y][z] = Tile.FLOOR;
    return tiles;
}

}
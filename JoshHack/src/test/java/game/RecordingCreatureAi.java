package game;

class RecordingCreatureAi extends CreatureAi {
    int levelUps;

    RecordingCreatureAi(Creature creature) {
        super(creature);
    }

    @Override
    public void onGainLevel() {
        levelUps++;
    }
}

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.vfx.GameSavedEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpirePatch(
        clz = AbstractRoom.class,
        method = "update"
)
class PennyMachine
{
    private static final Logger logger = LogManager.getLogger(PennyMachine.class.getName());
    @SpireInsertPatch(
            rloc=244-225
    )
    public static void giveMoney(AbstractRoom ar)
    {
        if(shouldEnter(ar))
        {
            logger.info("Generating a special reward");
            ar.addGoldToRewards(1);

            ar.dropReward();

            if(ar.rewardAllowed)
            {
                AbstractDungeon.combatRewardScreen.open();
                AbstractDungeon.combatRewardScreen.rewards.removeIf(card -> (card.type == RewardItem.RewardType.CARD));
            }
            //Everything below here seems to be about writing to a save file.
            if(!CardCrawlGame.loadingSave && !AbstractDungeon.loading_post_combat)
            {
                SaveFile saveFile = new SaveFile(SaveFile.SaveType.POST_COMBAT);
                if(ar.combatEvent)
                {
                    --saveFile.event_seed_count;
                }
                SaveAndContinue.save(saveFile);
                AbstractDungeon.effectList.add(new GameSavedEffect());
            }
            else
            {
                CardCrawlGame.loadingSave = false;
            }
            AbstractDungeon.loading_post_combat = false;
        }
    }

    //This method checks a bunch of different conditions to see if the player should get this reward
    public static boolean shouldEnter(AbstractRoom ar)
    {
        boolean okToSpawn = false;
        //on ascension 20 you don't get a reward until both act 3 bosses are defeated
        if(AbstractDungeon.ascensionLevel == 20 && AbstractDungeon.bossList.size() == 1)
        {
            okToSpawn = true;
        }
        else if(AbstractDungeon.ascensionLevel < 20)
        {
            okToSpawn = true;
        }
        return  ar.isBattleOver && AbstractDungeon.actionManager.actions.isEmpty() &&
                (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss) &&
                (CardCrawlGame.dungeon instanceof TheBeyond) &&
                !AbstractDungeon.loading_post_combat &&
                !CardCrawlGame.loadingSave &&
                okToSpawn;
    }
}
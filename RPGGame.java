package rpggame;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class RPGGame {
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}

// 게임 상태 열거형
enum GameState {
    MAIN_MENU, EXPLORATION, BATTLE, SHOP, QUEST, INVENTORY, GAME_OVER, SAVE_LOAD
}

// 플레이어 직업 열거형
enum PlayerClass {
    WARRIOR("전사"), ARCHER("궁수"), MAGE("마법사");
    
    private String title;
    
    PlayerClass(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
}

// 몬스터 타입 열거형
enum MonsterType {
    NORMAL, BEAST, PLANT, UNDEAD, GHOST, GIANT, FLYING, 
    BOSS, ELEMENTAL, DEMON, DRAGON, CONSTRUCT;
    
    public String getKoreanName() {
        switch(this) {
            case BEAST: return "야수";
            case PLANT: return "식물";
            case UNDEAD: return "언데드";
            case GHOST: return "유령";
            case GIANT: return "거인";
            case FLYING: return "비행";
            case BOSS: return "보스";
            case ELEMENTAL: return "정령";
            case DEMON: return "악마";
            case DRAGON: return "드래곤";
            case CONSTRUCT: return "구조물";
            default: return "일반";
        }
    }
}

// 지역 타입 열거형
enum LocationType {
    TOWN("마을"), FOREST("숲"), MOUNTAIN("산"), 
    GRAVEYARD("묘지"), DUNGEON("던전"), LAKE("호수");
    
    private String koreanName;
    
    LocationType(String koreanName) {
        this.koreanName = koreanName;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
}

// 상점 타입 열거형
enum ShopType {
    WEAPON("무기 상점"), ARMOR("방어구 상점"), 
    POTION("물약 상점"), SPECIAL("특수 상점");
    
    private String name;
    
    ShopType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}

// 상태 이상 열거형
enum StatusEffectType {
    POISON("중독", "매 턴 체력 5% 감소", true),
    BURN("화상", "매 턴 체력 3% 감소", true),
    FREEZE("빙결", "행동 불가", false),
    BLESS("축복", "공격력 10% 증가", false);
    
    private String name;
    private String description;
    private boolean isDamageOverTime;
    
    StatusEffectType(String name, String description, boolean isDamageOverTime) {
        this.name = name;
        this.description = description;
        this.isDamageOverTime = isDamageOverTime;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isDamageOverTime() { return isDamageOverTime; }
}

// 지역 클래스
class Location {
    private String name;
    private String description;
    private LocationType type;
    private int minLevel;
    private int maxLevel;
    
    public Location(String name, String description, LocationType type, int minLevel, int maxLevel) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocationType getType() { return type; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
    
    public boolean isSuitableFor(int playerLevel) {
        return playerLevel >= minLevel && playerLevel <= maxLevel;
    }
}

// NPC 클래스
class NPC {
    private String name;
    private String dialogue;
    private ShopType shopType;
    private int friendshipLevel;
    
    public NPC(String name, String dialogue) {
        this(name, dialogue, null);
    }
    
    public NPC(String name, String dialogue, ShopType shopType) {
        this.name = name;
        this.dialogue = dialogue;
        this.shopType = shopType;
        this.friendshipLevel = 0;
    }
    
    public String getName() { return name; }
    public String getDialogue() { return dialogue; }
    public ShopType getShopType() { return shopType; }
    public int getFriendshipLevel() { return friendshipLevel; }
    
    public void increaseFriendship(int amount) {
        friendshipLevel += amount;
    }
    
    public String getFriendshipTitle() {
        if (friendshipLevel >= 100) return "친구";
        else if (friendshipLevel >= 50) return "아는 사이";
        else return "낯선 사람";
    }
}

// 아이템 인터페이스
interface Item extends Serializable {
    String getName();
    int getPrice();
    String getDescription();
    int getLevelRequirement();
    default int getSellPrice() {
        return (int)(getPrice() * 0.7);
    }
}

// 장비 추상 클래스
abstract class Equipment implements Item {
    protected String name;
    protected int price;
    protected int attack;
    protected int defense;
    protected int levelRequirement;
    protected PlayerClass requiredClass;
    protected int durability;
    protected int maxDurability;
    protected String description;
    protected int enhanceLevel;
    
    public Equipment(String name, int price, int attack, int defense, 
                    int levelRequirement, PlayerClass requiredClass, 
                    int durability, String description) {
        this.name = name;
        this.price = price;
        this.attack = attack;
        this.defense = defense;
        this.levelRequirement = levelRequirement;
        this.requiredClass = requiredClass;
        this.durability = durability;
        this.maxDurability = durability;
        this.description = description;
        this.enhanceLevel = 0;
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
    public PlayerClass getRequiredClass() { return requiredClass; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public int getEnhanceLevel() { return enhanceLevel; }
    
    @Override
    public int getSellPrice() {
        return (int)(price * 0.7 * (durability / (double)maxDurability));
    }
    
    public void reduceDurability(int amount) {
        durability = Math.max(0, durability - amount);
    }
    
    public void repair() {
        durability = maxDurability;
    }
    
    public boolean isBroken() {
        return durability <= 0;
    }
    
    public void enhance() {
        enhanceLevel++;
        if (this instanceof Weapon) {
            attack += (int)(attack * 0.1);
        } else if (this instanceof Armor) {
            defense += (int)(defense * 0.1);
        }
    }
}

// 무기 클래스
class Weapon extends Equipment implements Serializable {
    private static final long serialVersionUID = 1L;
    private double criticalChance;
    private double accuracy;
    
    public Weapon(String name, int price, int attack, int levelRequirement, 
                 PlayerClass requiredClass, int durability, String description,
                 double criticalChance, double accuracy) {
        super(name, price, attack, 0, levelRequirement, requiredClass, durability, description);
        this.criticalChance = criticalChance;
        this.accuracy = accuracy;
    }
    
    public Weapon(String name, int price, int attack, int levelRequirement, PlayerClass requiredClass) {
        this(name, price, attack, levelRequirement, requiredClass, 100, "기본 무기", 0.1, 0.9);
    }
    
    public double getCriticalChance() { return criticalChance; }
    public double getAccuracy() { return accuracy; }
    
    public boolean isCriticalHit() {
        return Math.random() < criticalChance;
    }
    
    public boolean isAttackHit() {
        return Math.random() < accuracy;
    }
}

// 방어구 클래스
class Armor extends Equipment {
    private double evasion;
    private double damageReduction;
    
    public Armor(String name, int price, int defense, int levelRequirement,
                PlayerClass requiredClass, int durability, String description,
                double evasion, double damageReduction) {
        super(name, price, 0, defense, levelRequirement, requiredClass, durability, description);
        this.evasion = evasion;
        this.damageReduction = damageReduction;
    }
    
    public Armor(String name, int price, int defense, int levelRequirement, PlayerClass requiredClass) {
        this(name, price, defense, levelRequirement, requiredClass, 100, "기본 방어구", 0.05, 0.1);
    }
    
    public double getEvasion() { return evasion; }
    public double getDamageReduction() { return damageReduction; }
    
    public boolean isEvaded() {
        return Math.random() < evasion;
    }
    
    public int calculateReducedDamage(int damage) {
        return (int)(damage * (1.0 - damageReduction));
    }
}

// 물약 인터페이스
interface Potion extends Item {
    void use(Player player);
    int getAmount();
    int getRemainingUses();
    void setRemainingUses(int remainingUses);
}

// 체력 물약
class HealthPotion implements Potion, Serializable {
    private String name;
    private int price;
    private int amount;
    private int remainingUses;
    private int levelRequirement;
    private String description;
    
    public HealthPotion(String name, int price, int amount, int uses, int levelReq, String desc) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.remainingUses = uses;
        this.levelRequirement = levelReq;
        this.description = desc;
    }
    
    public HealthPotion(String name, int price, int amount) {
        this(name, price, amount, 1, 1, "체력을 회복하는 물약");
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getAmount() { return amount; }
    public int getRemainingUses() { return remainingUses; }
    public void setRemainingUses(int remainingUses) { this.remainingUses = remainingUses; }
    
    @Override
    public void use(Player player) {
        if (remainingUses > 0) {
            player.setHp(player.getHp() + amount);
            remainingUses--;
        }
    }
    
    @Override
    public int getSellPrice() {
        return (int)(price * 0.5 * (remainingUses / (double)(remainingUses + 1)));
    }
}

// 마나 물약
class ManaPotion implements Potion, Serializable {
    private String name;
    private int price;
    private int amount;
    private int remainingUses;
    private int levelRequirement;
    private String description;
    
    public ManaPotion(String name, int price, int amount, int uses, int levelReq, String desc) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.remainingUses = uses;
        this.levelRequirement = levelReq;
        this.description = desc;
    }
    
    public ManaPotion(String name, int price, int amount) {
        this(name, price, amount, 1, 1, "마나를 회복하는 물약");
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getAmount() { return amount; }
    public int getRemainingUses() { return remainingUses; }
    public void setRemainingUses(int remainingUses) { this.remainingUses = remainingUses; }
    
    @Override
    public void use(Player player) {
        if (remainingUses > 0) {
            player.setMana(player.getMana() + amount);
            remainingUses--;
        }
    }
    
    @Override
    public int getSellPrice() {
        return (int)(price * 0.5 * (remainingUses / (double)(remainingUses + 1)));
    }
}

// 스태미나 물약
class StaminaPotion implements Potion, Serializable {
    private String name;
    private int price;
    private int amount;
    private int remainingUses;
    private int levelRequirement;
    private String description;
    
    public StaminaPotion(String name, int price, int amount, int uses, int levelReq, String desc) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.remainingUses = uses;
        this.levelRequirement = levelReq;
        this.description = desc;
    }
    
    public StaminaPotion(String name, int price, int amount) {
        this(name, price, amount, 1, 1, "스태미나를 회복하는 물약");
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getAmount() { return amount; }
    public int getRemainingUses() { return remainingUses; }
    public void setRemainingUses(int remainingUses) { this.remainingUses = remainingUses; }
    
    @Override
    public void use(Player player) {
        if (remainingUses > 0) {
            player.setStamina(player.getStamina() + amount);
            remainingUses--;
        }
    }
    
    @Override
    public int getSellPrice() {
        return (int)(price * 0.5 * (remainingUses / (double)(remainingUses + 1)));
    }
}

// 만능 물약
class UniversalPotion implements Potion, Serializable {
    private String name;
    private int price;
    private int healthAmount;
    private int manaAmount;
    private int staminaAmount;
    private int remainingUses;
    private int levelRequirement;
    private String description;
    
    public UniversalPotion(String name, int price, int healthAmount, int manaAmount, int staminaAmount) {
        this(name, price, healthAmount, manaAmount, staminaAmount, 1, 5, "체력, 마나, 스태미나를 모두 회복하는 만능 물약");
    }
    
    public UniversalPotion(String name, int price, int healthAmount, int manaAmount, int staminaAmount, 
                         int uses, int levelReq, String desc) {
        this.name = name;
        this.price = price;
        this.healthAmount = healthAmount;
        this.manaAmount = manaAmount;
        this.staminaAmount = staminaAmount;
        this.remainingUses = uses;
        this.levelRequirement = levelReq;
        this.description = desc;
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getAmount() { return healthAmount + manaAmount + staminaAmount; }
    public int getRemainingUses() { return remainingUses; }
    public void setRemainingUses(int remainingUses) { this.remainingUses = remainingUses; }
    
    @Override
    public void use(Player player) {
        if (remainingUses > 0) {
            player.setHp(player.getHp() + healthAmount);
            player.setMana(player.getMana() + manaAmount);
            player.setStamina(player.getStamina() + staminaAmount);
            remainingUses--;
        }
    }
    
    @Override
    public int getSellPrice() {
        return (int)(price * 0.5 * (remainingUses / (double)(remainingUses + 1)));
    }
}

// 두루마리
class Scroll implements Item, Serializable {
    private String name;
    private int price;
    private String description;
    private int levelRequirement;
    
    public Scroll(String name, int price) {
        this(name, price, "마법이 담긴 두루마리", 1);
    }
    
    public Scroll(String name, int price, String description, int levelReq) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.levelRequirement = levelReq;
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
    
    public void use(Player player) {
        if (name.contains("귀환")) {
            player.setCurrentLocation("마을");
            System.out.println("마을로 귀환했습니다!");
        } else if (name.contains("정화")) {
            System.out.println("모든 상태 이상이 치료되었습니다!");
        }
    }
}

// 상태 이상 클래스
class StatusEffect implements Serializable {
    private StatusEffectType type;
    private int duration;
    private int remainingTurns;
    
    public StatusEffect(StatusEffectType type, int duration) {
        this.type = type;
        this.duration = duration;
        this.remainingTurns = duration;
    }
    
    public StatusEffectType getType() { return type; }
    public int getDuration() { return duration; }
    public int getRemainingTurns() { return remainingTurns; }
    
    public void applyEffect(Player player) {
        remainingTurns--;
        if (type.isDamageOverTime()) {
            int damage = (int)(player.getMaxHp() * (type == StatusEffectType.POISON ? 0.05 : 0.03));
            player.takeDamage(damage);
            System.out.printf("[%s] %s으로 인해 체력 %d 감소\n", 
                type.getName(), type.getDescription(), damage);
        }
    }
    
    public boolean isExpired() {
        return remainingTurns <= 0;
    }

	public void applyEffect(Monster monster) {
		// TODO Auto-generated method stub
		
	}
}

// 스킬 클래스
class Skill implements Serializable {
    private String name;
    private int manaCost;
    private String description;
    private BiConsumer<Player, Monster> effect;
    private int levelRequirement;
    
    public Skill(String name, int manaCost, String description, BiConsumer<Player, Monster> effect, int levelReq) {
        this.name = name;
        this.manaCost = manaCost;
        this.description = description;
        this.effect = effect;
        this.levelRequirement = levelReq;
    }
    
    public void use(Player player, Monster monster) {
        effect.accept(player, monster);
    }
    
    public String getName() { return name; }
    public int getManaCost() { return manaCost; }
    public String getDescription() { return description; }
    public int getLevelRequirement() { return levelRequirement; }
}

// 퀘스트 클래스
class Quest implements Serializable {
    private String title;
    private String description;
    private Predicate<Monster> condition;
    private int requiredProgress;
    private int currentProgress;
    private int expReward;
    private int goldReward;
    private Item rewardItem;
    private boolean isCompleted;
    private int levelRequirement;
    
    public Quest(String title, String description, Predicate<Monster> condition, 
                int requiredProgress, int expReward, int goldReward) {
        this(title, description, condition, requiredProgress, expReward, goldReward, null, 1);
    }
    
    public Quest(String title, String description, Predicate<Monster> condition, 
                int requiredProgress, int expReward, int goldReward, Item rewardItem, int levelReq) {
        this.title = title;
        this.description = description;
        this.condition = condition;
        this.requiredProgress = requiredProgress;
        this.currentProgress = 0;
        this.expReward = expReward;
        this.goldReward = goldReward;
        this.rewardItem = rewardItem;
        this.isCompleted = false;
        this.levelRequirement = levelReq;
    }
    
    public void updateProgress(Monster monster) {
        if (!isCompleted && condition.test(monster)) {
            currentProgress++;
            if (currentProgress >= requiredProgress) {
                isCompleted = true;
            }
        }
    }
    
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getRequiredProgress() { return requiredProgress; }
    public int getCurrentProgress() { return currentProgress; }
    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }
    public Item getRewardItem() { return rewardItem; }
    public boolean isCompleted() { return isCompleted; }
    public int getLevelRequirement() { return levelRequirement; }
}

// 상점 클래스
class Shop implements Serializable {
    private String name;
    private ShopType type;
    private List<Item> items;
    
    public Shop(String name, ShopType type) {
        this.name = name;
        this.type = type;
        this.items = new ArrayList<>();
    }
    
    public void addItem(Item item) {
        items.add(item);
    }
    
    public void restock() {
        items.clear();
        Random random = new Random();
        
        switch (type) {
            case WEAPON:
                // 전사용 무기
                items.add(new Weapon("강철 검", 400, 15, 3, PlayerClass.WARRIOR, 100, "튼튼한 강철로 만든 검", 0.1, 0.9));
                items.add(new Weapon("전투 도끼", 500, 18, 4, PlayerClass.WARRIOR, 100, "무거운 전투용 도끼", 0.15, 0.8));
                // 궁수용 무기
                items.add(new Weapon("강력한 활", 450, 16, 3, PlayerClass.ARCHER, 100, "강력한 장궁", 0.2, 0.85));
                items.add(new Weapon("정밀 석궁", 550, 20, 4, PlayerClass.ARCHER, 100, "정밀하게 제작된 석궁", 0.25, 0.75));
                // 마법사용 무기
                items.add(new Weapon("에너지 스태프", 480, 8, 3, PlayerClass.MAGE, 100, "마력이 담긴 지팡이", 0.05, 0.95));
                items.add(new Weapon("신비의 봉", 600, 12, 5, PlayerClass.MAGE, 100, "고대의 마법이 깃든 봉", 0.1, 0.9));
                break;
                
            case ARMOR:
                // 전사용 방어구
                items.add(new Armor("강철 갑옷", 500, 12, 3, PlayerClass.WARRIOR, 100, "강철로 만든 튼튼한 갑옷", 0.05, 0.2));
                items.add(new Armor("전사용 흉갑", 700, 16, 5, PlayerClass.WARRIOR, 100, "전문 전사용 흉갑", 0.03, 0.25));
                // 궁수용 방어구
                items.add(new Armor("가죽 갑옷", 400, 8, 3, PlayerClass.ARCHER, 100, "가벼운 가죽 갑옷", 0.15, 0.1));
                items.add(new Armor("숙련자 복장", 650, 12, 6, PlayerClass.ARCHER, 100, "숙련된 궁수를 위한 복장", 0.2, 0.15));
                // 마법사용 방어구
                items.add(new Armor("마법사 로브", 450, 5, 4, PlayerClass.MAGE, 100, "마법 보호가 깃든 로브", 0.1, 0.05));
                items.add(new Armor("현자의 가운", 680, 8, 8, PlayerClass.MAGE, 100, "현자들이 입던 가운", 0.15, 0.1));
                break;
                
            case POTION:
                items.add(new HealthPotion("하급 체력 물약", 50, 30, 1, 1, "체력을 30 회복하는 물약"));
                items.add(new HealthPotion("중급 체력 물약", 120, 70, 1, 3, "체력을 70 회복하는 물약"));
                items.add(new ManaPotion("하급 마나 물약", 60, 30, 1, 1, "마나를 30 회복하는 물약"));
                items.add(new ManaPotion("중급 마나 물약", 150, 70, 1, 3, "마나를 70 회복하는 물약"));
                items.add(new StaminaPotion("활력 물약", 80, 40, 1, 1, "스태미나를 40 회복하는 물약"));
                break;
                
            case SPECIAL:
                items.add(new Scroll("귀환 두루마리", 200, "마을로 순간이동하는 두루마리", 1));
                items.add(new UniversalPotion("만능 물약", 500, 50, 50, 50, 1, 5, "체력, 마나, 스태미나를 모두 회복"));
                if (random.nextDouble() < 0.3) {
                    items.add(new Armor("행운의 반지", 1000, 0, 0, null, 100, "행운을 가져다주는 반지", 0.1, 0.05));
                }
                break;
        }
    }
    
    public String getName() { return name; }
    public ShopType getType() { return type; }
    public List<Item> getItems() { return items; }
}

// 몬스터 클래스
class Monster implements Serializable {
    private String name;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int expReward;
    private int goldReward;
    private int level;
    private MonsterType type;
    private List<StatusEffect> statusEffects;
    private double criticalChance;
    private double evasion;
    private List<Item> lootTable;
    private int lootChance;
    
    public Monster(String name, int maxHp, int attack, int defense, 
                  int expReward, int level, MonsterType type) {
        this(name, maxHp, attack, defense, expReward, level, type, 0.1, 0.1, 30);
    }
    
    public Monster(String name, int maxHp, int attack, int defense, 
                  int expReward, int level, MonsterType type,
                  double criticalChance, double evasion, int lootChance) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.expReward = expReward;
        this.goldReward = expReward / 2;
        this.level = level;
        this.type = type;
        this.statusEffects = new ArrayList<>();
        this.criticalChance = criticalChance;
        this.evasion = evasion;
        this.lootTable = new ArrayList<>();
        this.lootChance = lootChance;
        
        // 기본 루트 테이블 설정
        if (random.nextDouble() < 0.5) {
            lootTable.add(new HealthPotion("하급 체력 물약", 0, 30));
        }
        if (random.nextDouble() < 0.3) {
            lootTable.add(new ManaPotion("하급 마나 물약", 0, 30));
        }
    }
    
    private static final Random random = new Random();
    
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public void addToLootTable(Item item, int weight) {
        for (int i = 0; i < weight; i++) {
            lootTable.add(item);
        }
    }
    
    public Item generateLoot() {
        if (lootTable.isEmpty() || Math.random() * 100 > lootChance) {
            return null;
        }
        return lootTable.get((int)(Math.random() * lootTable.size()));
    }
    
    public void applyStatusEffect(StatusEffect effect) {
        statusEffects.add(effect);
    }
    
    public void processStatusEffects() {
        Iterator<StatusEffect> it = statusEffects.iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next();
            effect.applyEffect(this);
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }
    
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }
    public int getLevel() { return level; }
    public MonsterType getType() { return type; }
    public double getCriticalChance() { return criticalChance; }
    public double getEvasion() { return evasion; }
    public List<StatusEffect> getStatusEffects() { return statusEffects; }
    
    public void setHp(int hp) { this.hp = Math.min(hp, maxHp); }
}

// 플레이어 클래스
class Player implements Serializable {
    private String name;
    private PlayerClass playerClass;
    private int level;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private int stamina;
    private int maxStamina;
    private int baseAttack;
    private int baseDefense;
    private int agility;
    private int intelligence;
    private int exp;
    private int maxExp;
    private int gold;
    private List<Item> inventory;
    private List<Quest> activeQuests;
    private List<Quest> completedQuests;
    private Weapon equippedWeapon;
    private Armor equippedArmor;
    private String currentLocation;
    private List<StatusEffect> statusEffects;
    private int statPoints;
    private Map<String, Integer> skillLevels;
    private int fame;
    private List<String> unlockedLocations;
    private int consecutiveBattles;
    
    public Player(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass;
        this.level = 1;
        this.exp = 0;
        this.maxExp = 100;
        this.gold = 100;
        this.inventory = new ArrayList<>();
        this.activeQuests = new ArrayList<>();
        this.completedQuests = new ArrayList<>();
        this.currentLocation = "마을";
        this.statusEffects = new ArrayList<>();
        this.statPoints = 0;
        this.skillLevels = new HashMap<>();
        this.fame = 0;
        this.unlockedLocations = new ArrayList<>(Arrays.asList("마을", "서쪽 숲"));
        this.consecutiveBattles = 0;
        
        // 기본 스탯 설정
        switch (playerClass) {
            case WARRIOR:
                this.maxHp = 120;
                this.maxMana = 30;
                this.maxStamina = 80;
                this.baseAttack = 12;
                this.baseDefense = 10;
                this.agility = 5;
                this.intelligence = 3;
                break;
            case ARCHER:
                this.maxHp = 80;
                this.maxMana = 50;
                this.maxStamina = 100;
                this.baseAttack = 10;
                this.baseDefense = 6;
                this.agility = 12;
                this.intelligence = 6;
                break;
            case MAGE:
                this.maxHp = 70;
                this.maxMana = 120;
                this.maxStamina = 60;
                this.baseAttack = 6;
                this.baseDefense = 4;
                this.agility = 4;
                this.intelligence = 15;
                break;
        }
        
        this.hp = maxHp;
        this.mana = maxMana;
        this.stamina = maxStamina;
        
        // 초기 아이템 지급
        addItem(new HealthPotion("하급 체력 물약", 0, 30));
        addItem(new ManaPotion("마나 물약", 0, 30));
        
        if (playerClass == PlayerClass.WARRIOR) {
            equip(new Weapon("초보자 검", 0, 5, 1, PlayerClass.WARRIOR, 100, "초보자용 검", 0.05, 0.85));
            equip(new Armor("초보자 갑옷", 0, 3, 1, PlayerClass.WARRIOR, 100, "초보자용 갑옷", 0.03, 0.1));
        } else if (playerClass == PlayerClass.ARCHER) {
            equip(new Weapon("초보자 활", 0, 4, 1, PlayerClass.ARCHER, 100, "초보자용 활", 0.1, 0.8));
            equip(new Armor("초보자 가죽 갑옷", 0, 2, 1, PlayerClass.ARCHER, 100, "초보자용 가죽 갑옷", 0.1, 0.05));
        } else {
            equip(new Weapon("초보자 지팡이", 0, 3, 1, PlayerClass.MAGE, 100, "초보자용 지팡이", 0.03, 0.9));
            equip(new Armor("초보자 로브", 0, 1, 1, PlayerClass.MAGE, 100, "초보자용 로브", 0.05, 0.03));
        }
    }
    
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public void gainExp(int amount) {
        // 연속 전투 보너스
        if (consecutiveBattles >= 5) {
            amount = (int)(amount * 1.1);
        }
        
        exp += amount;
        while (exp >= maxExp) {
            levelUp();
        }
    }
    
    public void levelUp() {
        level++;
        exp -= maxExp;
        maxExp = (int)(maxExp * 1.5);
        statPoints += 3;
        
        // 기본 스탯 증가
        maxHp += 10 + (playerClass == PlayerClass.WARRIOR ? 5 : 0);
        maxMana += 5 + (playerClass == PlayerClass.MAGE ? 10 : 0);
        maxStamina += 8 + (playerClass == PlayerClass.ARCHER ? 5 : 0);
        
        // 직업별 추가 보너스
        switch(playerClass) {
            case WARRIOR:
                baseAttack += 2;
                baseDefense += 1;
                break;
            case ARCHER:
                agility += 2;
                break;
            case MAGE:
                intelligence += 3;
                break;
        }
        
        hp = maxHp;
        mana = maxMana;
        stamina = maxStamina;
        
        System.out.println("\n=========================");
        System.out.println("  레벨 업! " + level + " 레벨이 되었습니다!");
        System.out.println("=========================");
        System.out.println("스탯 포인트 3점을 획득했습니다!");
    }
    
    public void distributeStatPoint(String stat) {
        if (statPoints <= 0) return;
        
        switch(stat.toLowerCase()) {
            case "공격력":
                baseAttack++;
                break;
            case "방어력":
                baseDefense++;
                break;
            case "민첩성":
                agility++;
                break;
            case "지능":
                intelligence++;
                break;
            case "체력":
                maxHp += 5;
                hp += 5;
                break;
        }
        statPoints--;
    }
    
    public void gainGold(int amount) {
        gold += amount;
    }
    
    public void spendGold(int amount) {
        gold = Math.max(0, gold - amount);
    }
    
    public void addItem(Item item) {
        inventory.add(item);
    }
    
    public void removeItem(Item item) {
        inventory.remove(item);
    }
    
    public void equip(Equipment equipment) {
        if (equipment instanceof Weapon) {
            if (equippedWeapon != null) {
                unequipWeapon();
            }
            equippedWeapon = (Weapon) equipment;
        } else if (equipment instanceof Armor) {
            if (equippedArmor != null) {
                unequipArmor();
            }
            equippedArmor = (Armor) equipment;
        }
        inventory.remove(equipment);
    }
    
    public Weapon unequipWeapon() {
        Weapon weapon = equippedWeapon;
        if (weapon != null) {
            inventory.add(weapon);
            equippedWeapon = null;
        }
        return weapon;
    }
    
    public Armor unequipArmor() {
        Armor armor = equippedArmor;
        if (armor != null) {
            inventory.add(armor);
            equippedArmor = null;
        }
        return armor;
    }
    
    public void useStamina(int amount) {
        stamina = Math.max(0, stamina - amount);
    }
    
    public void acceptQuest(Quest quest) {
        activeQuests.add(quest);
    }
    
    public void completeQuest(Quest quest) {
        activeQuests.remove(quest);
        completedQuests.add(quest);
        gainExp(quest.getExpReward());
        gainGold(quest.getGoldReward());
        if (quest.getRewardItem() != null) {
            addItem(quest.getRewardItem());
        }
        fame += quest.getLevelRequirement() * 10;
    }
    
    public boolean hasActiveQuest(Quest quest) {
        return activeQuests.contains(quest);
    }
    
    public boolean hasCompletedQuest(Quest quest) {
        return completedQuests.contains(quest);
    }
    
    public void applyStatusEffect(StatusEffect effect) {
        statusEffects.add(effect);
    }
    
    public void processStatusEffects() {
        Iterator<StatusEffect> it = statusEffects.iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next();
            effect.applyEffect(this);
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }
    
    public void increaseConsecutiveBattles() {
        consecutiveBattles++;
        if (consecutiveBattles % 5 == 0) {
            System.out.println("연속 전투 보너스! 획득 경험치 10% 증가!");
        }
    }
    
    public void resetConsecutiveBattles() {
        consecutiveBattles = 0;
    }
    
    public void unlockLocation(String location) {
        if (!unlockedLocations.contains(location)) {
            unlockedLocations.add(location);
            System.out.println("새로운 지역 " + location + "이(가) 열렸습니다!");
        }
    }
    
    // Getter 메소드들
    public String getName() { return name; }
    public PlayerClass getPlayerClass() { return playerClass; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
    public int getStamina() { return stamina; }
    public int getMaxStamina() { return maxStamina; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getAgility() { return agility; }
    public int getIntelligence() { return intelligence; }
    public int getExp() { return exp; }
    public int getMaxExp() { return maxExp; }
    public int getGold() { return gold; }
    public List<Item> getInventory() { return inventory; }
    public List<Quest> getActiveQuests() { return activeQuests; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor getEquippedArmor() { return equippedArmor; }
    public String getCurrentLocation() { return currentLocation; }
    public List<StatusEffect> getStatusEffects() { return statusEffects; }
    public int getStatPoints() { return statPoints; }
    public int getFame() { return fame; }
    public List<String> getUnlockedLocations() { return unlockedLocations; }
    public int getConsecutiveBattles() { return consecutiveBattles; }
    
    // Setter 메소드들
    public void setHp(int hp) { this.hp = Math.min(hp, maxHp); }
    public void setMana(int mana) { this.mana = Math.min(mana, maxMana); }
    public void setStamina(int stamina) { this.stamina = Math.min(stamina, maxStamina); }
    public void setCurrentLocation(String location) { this.currentLocation = location; }
    
    public int getAttack() {
        int attack = baseAttack;
        if (equippedWeapon != null) {
            attack += equippedWeapon.getAttack();
        }
        
        // 상태 이상 효과 적용
        for (StatusEffect effect : statusEffects) {
            if (effect.getType() == StatusEffectType.BLESS) {
                attack = (int)(attack * 1.1);
            }
        }
        
        return attack;
    }
    
    public int getDefense() {
        int defense = baseDefense;
        if (equippedArmor != null) {
            defense += equippedArmor.getDefense();
        }
        return defense;
    }
    
    public double getEvasion() {
        double evasion = 0.0;
        if (equippedArmor != null) {
            evasion += equippedArmor.getEvasion();
        }
        evasion += agility * 0.01;
        return Math.min(evasion, 0.5); // 최대 50% 회피율
    }
}

// 전투 클래스
class Battle {
    private Player player;
    private Monster monster;
    private Game game;
    private Scanner scanner;
    private Random random;
    private List<Consumer<Player>> postBattleActions;
    
    public Battle(Player player, Monster monster, Game game) {
        this.player = player;
        this.monster = monster;
        this.game = game;
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        this.postBattleActions = new ArrayList<>();
    }
    
    public void start() {
        System.out.println("\n===== 전투 시작! =====");
        
        boolean playerFirst = isPlayerFirst();
        
        while (player.isAlive() && monster.isAlive()) {
            showStatus();
            
            if (playerFirst) {
                playerTurn();
                if (!monster.isAlive()) break;
                
                monsterTurn();
                if (!player.isAlive()) break;
            } else {
                monsterTurn();
                if (!player.isAlive()) break;
                
                playerTurn();
                if (!monster.isAlive()) break;
            }
            
            // 상태 이상 처리
            player.processStatusEffects();
            monster.processStatusEffects();
            
            player.setStamina(Math.min(player.getMaxStamina(), player.getStamina() + 5));
        }
        
        if (player.isAlive()) {
            playerWin();
            executePostBattleActions();
        } else {
            game.setGameState(GameState.GAME_OVER);
        }
    }
    
    private void executePostBattleActions() {
        for (Consumer<Player> action : postBattleActions) {
            action.accept(player);
        }
        postBattleActions.clear();
    }
    
    public void addPostBattleAction(Consumer<Player> action) {
        postBattleActions.add(action);
    }
    
    private boolean isPlayerFirst() {
        int playerAgility = player.getAgility();
        int monsterAgility = monster.getLevel() * 5;
        return random.nextInt(playerAgility + monsterAgility) > monsterAgility;
    }
    
    private void showStatus() {
        System.out.println("\n-----------------------");
        System.out.println(player.getName() + " (Lv." + player.getLevel() + ")");
        System.out.printf("HP: %d/%d | 마나: %d/%d | 스태미나: %d/%d\n", 
            player.getHp(), player.getMaxHp(), 
            player.getMana(), player.getMaxMana(),
            player.getStamina(), player.getMaxStamina());
        
        // 플레이어 상태 이상 표시
        if (!player.getStatusEffects().isEmpty()) {
            System.out.print("상태 이상: ");
            for (StatusEffect effect : player.getStatusEffects()) {
                System.out.print(effect.getType().getName() + " ");
            }
            System.out.println();
        }
        
        System.out.println("\nVS");
        
        System.out.println("\n" + monster.getName() + " (Lv." + monster.getLevel() + ")");
        System.out.printf("HP: %d/%d\n", monster.getHp(), monster.getMaxHp());
        
        // 몬스터 상태 이상 표시
        if (!monster.getStatusEffects().isEmpty()) {
            System.out.print("상태 이상: ");
            for (StatusEffect effect : monster.getStatusEffects()) {
                System.out.print(effect.getType().getName() + " ");
            }
            System.out.println();
        }
        
        System.out.println("-----------------------");
    }
    
    private void playerTurn() {
        System.out.println("\n[당신의 턴]");
        System.out.println("1. 기본 공격");
        System.out.println("2. 스킬 사용");
        System.out.println("3. 아이템 사용");
        System.out.print("선택: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                basicAttack();
                break;
            case 2:
                useSkill();
                break;
            case 3:
                useItem();
                break;
            default:
                System.out.println("잘못된 선택입니다. 기본 공격을 합니다.");
                basicAttack();
        }
    }
    
    private void basicAttack() {
        if (player.getEquippedWeapon() == null) {
            System.out.println("무기가 없어 맨손으로 공격합니다!");
        }
        
        int damage = calculateDamage(player.getAttack(), monster.getDefense());
        
        // 크리티컬 여부 확인
        boolean isCritical = player.getEquippedWeapon() != null && 
                            player.getEquippedWeapon().isCriticalHit();
        
        if (isCritical) {
            damage *= 2;
            System.out.println("크리티컬 히트!");
        }
        
        monster.takeDamage(damage);
        System.out.printf("\n%s이(가) %s에게 %d의 데미지를 입혔습니다!\n", 
            player.getName(), monster.getName(), damage);
        
        player.useStamina(5);
    }
    
    private void useSkill() {
        System.out.println("\n사용할 스킬을 선택하세요:");
        
        List<Skill> availableSkills = getAvailableSkills();
        if (availableSkills.isEmpty()) {
            System.out.println("사용할 수 있는 스킬이 없습니다. 기본 공격을 합니다.");
            basicAttack();
            return;
        }
        
        for (int i = 0; i < availableSkills.size(); i++) {
            Skill skill = availableSkills.get(i);
            System.out.printf("%d. %s (마나: %d) - %s\n", 
                i + 1, skill.getName(), skill.getManaCost(), skill.getDescription());
        }
        
        System.out.print("선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= availableSkills.size()) {
            Skill selectedSkill = availableSkills.get(choice - 1);
            
            if (player.getMana() < selectedSkill.getManaCost()) {
                System.out.println("마나가 부족합니다! 기본 공격을 합니다.");
                basicAttack();
                return;
            }
            
            player.setMana(player.getMana() - selectedSkill.getManaCost());
            selectedSkill.use(player, monster);
        } else {
            System.out.println("잘못된 선택입니다. 기본 공격을 합니다.");
            basicAttack();
        }
    }
    
    private List<Skill> getAvailableSkills() {
        List<Skill> skills = new ArrayList<>();
        
        switch (player.getPlayerClass()) {
            case WARRIOR:
                if (player.getLevel() >= 1) {
                    skills.add(new Skill("강타", 10, "강력한 한 방을 날립니다.", 
                        (p, m) -> {
                            int damage = calculateDamage(p.getAttack() * 2, m.getDefense());
                            m.takeDamage(damage);
                            System.out.printf("\n%s이(가) 강타로 %s에게 %d의 데미지를 입혔습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }, 1));
                }
                if (player.getLevel() >= 3) {
                    skills.add(new Skill("방어 태세", 15, "방어력을 증가시킵니다.", 
                        (p, m) -> {
                            p.applyStatusEffect(new StatusEffect(StatusEffectType.BLESS, 3));
                            System.out.printf("\n%s이(가) 방어 태세를 취해 방어력이 증가했습니다!\n", p.getName());
                        }, 3));
                }
                if (player.getLevel() >= 5) {
                    skills.add(new Skill("분노의 일격", 25, "분노를 담아 강력한 공격을 합니다.", 
                        (p, m) -> {
                            int damage = calculateDamage(p.getAttack() * 3, m.getDefense() / 2);
                            m.takeDamage(damage);
                            System.out.printf("\n%s이(가) 분노의 일격으로 %s에게 %d의 데미지를 입혔습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }, 5));
                }
                break;
            case ARCHER:
                if (player.getLevel() >= 1) {
                    skills.add(new Skill("연속 사격", 12, "빠르게 두 번 공격합니다.", 
                        (p, m) -> {
                            int damage1 = calculateDamage(p.getAttack(), m.getDefense());
                            int damage2 = calculateDamage(p.getAttack(), m.getDefense());
                            m.takeDamage(damage1);
                            m.takeDamage(damage2);
                            System.out.printf("\n%s이(가) 연속 사격으로 %s에게 %d와 %d의 데미지를 입혔습니다!\n", 
                                p.getName(), m.getName(), damage1, damage2);
                        }, 1));
                }
                if (player.getLevel() >= 3) {
                    skills.add(new Skill("저격", 20, "강력한 한 방을 날립니다.", 
                        (p, m) -> {
                            int damage = calculateDamage(p.getAttack() * 3, m.getDefense() / 2);
                            m.takeDamage(damage);
                            System.out.printf("\n%s이(가) 저격으로 %s에게 %d의 데미지를 입혔습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }, 3));
                }
                if (player.getLevel() >= 5) {
                    skills.add(new Skill("독화살", 18, "적을 중독시킵니다.", 
                        (p, m) -> {
                            int damage = calculateDamage(p.getAttack(), m.getDefense());
                            m.takeDamage(damage);
                            m.applyStatusEffect(new StatusEffect(StatusEffectType.POISON, 3));
                            System.out.printf("\n%s이(가) 독화살로 %s에게 %d의 데미지를 입히고 중독시켰습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }, 5));
                }
                break;
            case MAGE:
                if (player.getLevel() >= 1) {
                    skills.add(new Skill("파이어볼", 15, "불덩이를 날립니다.", 
                        (p, m) -> {
                            int damage = calculateMagicDamage(p.getIntelligence() * 2, m.getDefense());
                            m.takeDamage(damage);
                            System.out.printf("\n%s이(가) 파이어볼로 %s에게 %d의 데미지를 입혔습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }, 1));
                }
                if (player.getLevel() >= 3) {
                    skills.add(new Skill("치유", 20, "체력을 회복합니다.", 
                        (p, m) -> {
                            int healAmount = p.getIntelligence() * 3;
                            p.setHp(p.getHp() + healAmount);
                            System.out.printf("\n%s이(가) 치유 마법으로 %d 체력을 회복했습니다!\n", 
                                p.getName(), healAmount);
                        }, 3));
                }
                if (player.getLevel() >= 5) {
                    skills.add(new Skill("빙결", 30, "적을 얼려 행동 불가로 만듭니다.", 
                        (p, m) -> {
                            int damage = calculateMagicDamage(p.getIntelligence(), m.getDefense());
                            m.takeDamage(damage);
                            m.applyStatusEffect(new StatusEffect(StatusEffectType.FREEZE, 1));
                            System.out.printf("\n%s이(가) 빙결 마법으로 %s에게 %d의 데미지를 입히고 얼렸습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }, 5));
                }
                break;
        }
        
        return skills;
    }
    
    private void useItem() {
        List<Potion> potions = player.getInventory().stream()
            .filter(item -> item instanceof Potion)
            .map(item -> (Potion) item)
            .collect(Collectors.toList());
        
        if (potions.isEmpty()) {
            System.out.println("사용할 수 있는 아이템이 없습니다. 기본 공격을 합니다.");
            basicAttack();
            return;
        }
        
        System.out.println("\n사용할 아이템을 선택하세요:");
        for (int i = 0; i < potions.size(); i++) {
            Potion potion = potions.get(i);
            String effect = potion instanceof HealthPotion ? "체력 +" + potion.getAmount() :
                          potion instanceof ManaPotion ? "마나 +" + potion.getAmount() :
                          "스태미나 +" + potion.getAmount();
            System.out.printf("%d. %s - %s\n", i + 1, potion.getName(), effect);
        }
        
        System.out.print("선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= potions.size()) {
            Potion selectedPotion = potions.get(choice - 1);
            selectedPotion.use(player);
            player.removeItem(selectedPotion);
            System.out.printf("\n%s이(가) %s을(를) 사용했습니다!\n", 
                player.getName(), selectedPotion.getName());
        } else {
            System.out.println("잘못된 선택입니다. 기본 공격을 합니다.");
            basicAttack();
        }
    }
    
    private void monsterTurn() {
        System.out.printf("\n[%s의 턴]\n", monster.getName());
        
        // 회피 여부 확인
        if (Math.random() < player.getEvasion()) {
            System.out.printf("%s의 공격을 회피했습니다!\n", monster.getName());
            return;
        }
        
        if (random.nextDouble() < 0.8 || monster.getLevel() < 3) {
            int damage = calculateDamage(monster.getAttack(), player.getDefense());
            
            // 방어구 데미지 감소 적용
            if (player.getEquippedArmor() != null) {
                damage = player.getEquippedArmor().calculateReducedDamage(damage);
            }
            
            player.takeDamage(damage);
            System.out.printf("%s이(가) %s에게 %d의 데미지를 입혔습니다!\n", 
                monster.getName(), player.getName(), damage);
        } else {
            int damage = calculateDamage(monster.getAttack() * 2, player.getDefense());
            
            // 방어구 데미지 감소 적용
            if (player.getEquippedArmor() != null) {
                damage = player.getEquippedArmor().calculateReducedDamage(damage);
            }
            
            player.takeDamage(damage);
            System.out.printf("%s이(가) 강력한 공격으로 %s에게 %d의 데미지를 입혔습니다!\n", 
                monster.getName(), player.getName(), damage);
        }
        
        // 무기 내구도 감소
        if (player.getEquippedWeapon() != null) {
            player.getEquippedWeapon().reduceDurability(1);
            if (player.getEquippedWeapon().isBroken()) {
                System.out.println(player.getEquippedWeapon().getName() + "이(가) 부서졌습니다!");
                player.unequipWeapon();
            }
        }
        
        // 방어구 내구도 감소
        if (player.getEquippedArmor() != null) {
            player.getEquippedArmor().reduceDurability(1);
            if (player.getEquippedArmor().isBroken()) {
                System.out.println(player.getEquippedArmor().getName() + "이(가) 부서졌습니다!");
                player.unequipArmor();
            }
        }
    }
    
    private int calculateDamage(int attack, int defense) {
        int baseDamage = Math.max(1, attack - defense);
        int randomFactor = random.nextInt(baseDamage / 2 + 1);
        return baseDamage + randomFactor;
    }
    
    private int calculateMagicDamage(int magicPower, int defense) {
        int baseDamage = Math.max(1, magicPower - defense / 2);
        int randomFactor = random.nextInt(baseDamage / 2 + 1);
        return baseDamage + randomFactor;
    }
    
    private void playerWin() {
        int exp = monster.getExpReward();
        int gold = monster.getGoldReward();
        
        System.out.println("\n" + monster.getName() + "을(를) 처치했습니다!");
        System.out.println(exp + " 경험치를 얻었습니다!");
        System.out.println(gold + " 골드를 얻었습니다!");
        
        player.gainExp(exp);
        player.gainGold(gold);
        player.increaseConsecutiveBattles();
        
        game.updateQuestProgress(monster);
        
        // 드롭 아이템 확인
        Item droppedItem = monster.generateLoot();
        if (droppedItem != null) {
            player.addItem(droppedItem);
            System.out.println(droppedItem.getName() + "을(를) 획득했습니다!");
        }
        
        // 레벨에 따라 새로운 지역 해금
        if (player.getLevel() >= 3 && !player.getUnlockedLocations().contains("동쪽 산")) {
            player.unlockLocation("동쪽 산");
        }
        if (player.getLevel() >= 5 && !player.getUnlockedLocations().contains("북쪽 묘지")) {
            player.unlockLocation("북쪽 묘지");
        }
    }
}

// 게임 메인 클래스
class Game {
    private Player player;
    private List<Monster> monsters;
    private List<Quest> quests;
    private Scanner scanner;
    private Random random;
    private boolean isRunning;
    private GameState gameState;
    private Map<String, Location> worldMap;
    private List<Shop> shops;
    private List<NPC> npcs;
    private int gameDay;
    private boolean autoSave;
    private int autoSaveInterval;
    private int battleCount;
    
    public Game() {
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        this.monsters = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.shops = new ArrayList<>();
        this.npcs = new ArrayList<>();
        this.gameState = GameState.MAIN_MENU;
        this.gameDay = 1;
        this.autoSave = true;
        this.autoSaveInterval = 5;
        this.battleCount = 0;
        
        initializeGameWorld();
    }

    private void initializeGameWorld() {
        initializeMonsters();
        initializeQuests();
        initializeShops();
        initializeNPCs();
        initializeWorldMap();
    }

    private void initializeMonsters() {
        // 일반 몬스터들
        monsters.add(new Monster("고블린 졸병", 40, 8, 4, 15, 1, MonsterType.NORMAL));
        monsters.add(new Monster("고블린 투사", 60, 12, 6, 25, 2, MonsterType.NORMAL));
        monsters.add(new Monster("고블린 샤먼", 50, 15, 3, 30, 2, MonsterType.NORMAL));
        
        // 숲 지역 몬스터들
        monsters.add(new Monster("독침 늑대", 70, 14, 5, 30, 2, MonsterType.BEAST));
        monsters.add(new Monster("거대 거미", 55, 10, 8, 25, 2, MonsterType.BEAST));
        monsters.add(new Monster("식인 식물", 80, 12, 10, 35, 3, MonsterType.PLANT));
        
        // 산 지역 몬스터들
        monsters.add(new Monster("오크 전사", 100, 18, 8, 40, 3, MonsterType.NORMAL));
        monsters.add(new Monster("오크 샤먼", 70, 22, 5, 45, 3, MonsterType.NORMAL));
        monsters.add(new Monster("트롤", 150, 20, 12, 60, 4, MonsterType.GIANT));
        monsters.add(new Monster("하피", 65, 16, 7, 40, 3, MonsterType.FLYING));
        
        // 묘지 지역 몬스터들
        monsters.add(new Monster("해골 전사", 60, 14, 6, 30, 2, MonsterType.UNDEAD));
        monsters.add(new Monster("망령", 45, 18, 3, 35, 3, MonsterType.GHOST));
        monsters.add(new Monster("좀비", 90, 12, 5, 25, 2, MonsterType.UNDEAD));
        monsters.add(new Monster("리치", 80, 25, 10, 70, 5, MonsterType.UNDEAD));
        
        // 던전 지역 몬스터들
        monsters.add(new Monster("미노타우르스", 180, 25, 15, 100, 6, MonsterType.BOSS));
        monsters.add(new Monster("화염 정령", 70, 30, 5, 60, 5, MonsterType.ELEMENTAL));
        monsters.add(new Monster("어둠의 기사", 120, 22, 18, 80, 6, MonsterType.DEMON));
        
        // 특수 몬스터들
        monsters.add(new Monster("드래곤", 250, 35, 20, 150, 8, MonsterType.DRAGON));
        monsters.add(new Monster("골렘", 200, 28, 25, 90, 7, MonsterType.CONSTRUCT));
        
        // 몬스터별 드롭 아이템 설정
        for (Monster monster : monsters) {
            if (monster.getType() == MonsterType.BOSS || monster.getType() == MonsterType.DRAGON) {
                monster.addToLootTable(new UniversalPotion("고급 만능 물약", 0, 100, 100, 100), 1);
                monster.addToLootTable(new Weapon("전설의 무기", 0, 30, 10, null, 200, "전설의 무기", 0.2, 0.9), 1);
            } else if (monster.getLevel() >= 5) {
                monster.addToLootTable(new HealthPotion("고급 체력 물약", 0, 100), 1);
                monster.addToLootTable(new ManaPotion("고급 마나 물약", 0, 100), 1);
            }
        }
    }

    private void initializeQuests() {
        quests.add(new Quest("초보자의 첫 걸음", "고블린 3마리 처치", 
            monster -> monster.getName().contains("고블린"), 3, 50, 100));
        quests.add(new Quest("오크 사냥꾼", "오크 2마리 처치", 
            monster -> monster.getName().contains("오크"), 2, 80, 150));
        quests.add(new Quest("언데드 퇴치", "언데드 타입 몬스터 5마리 처치", 
            monster -> monster.getType() == MonsterType.UNDEAD, 5, 150, 300,
            new Armor("성스러운 갑옷", 0, 10, 5, null, 150, "언데드에 강한 갑옷", 0.05, 0.2), 3));
        quests.add(new Quest("드래곤 슬레이어", "드래곤 1마리 처치", 
            monster -> monster.getType() == MonsterType.DRAGON, 1, 500, 1000,
            new Weapon("드래곤 슬레이어", 0, 30, 10, null, 200, "드래곤을 잡은 자의 무기", 0.25, 0.95), 5));
        quests.add(new Quest("숲의 정화", "숲의 몬스터 10마리 처치", 
            monster -> worldMap.get(player.getCurrentLocation()).getType() == LocationType.FOREST, 10, 200, 300));
    }

    private void initializeShops() {
        // 무기 상점
        Shop weaponShop = new Shop("무기 상점", ShopType.WEAPON);
        
        // 전사용 무기
        weaponShop.addItem(new Weapon("단검", 100, 5, 1, PlayerClass.WARRIOR, 100, "기본적인 단검", 0.05, 0.85));
        weaponShop.addItem(new Weapon("양손검", 300, 12, 3, PlayerClass.WARRIOR, 120, "양손으로 사용하는 큰 검", 0.1, 0.8));
        weaponShop.addItem(new Weapon("도끼", 250, 10, 2, PlayerClass.WARRIOR, 110, "무거운 전투 도끼", 0.15, 0.75));
        weaponShop.addItem(new Weapon("철퇴", 350, 8, 4, PlayerClass.WARRIOR, 130, "강력한 타격을 주는 철퇴", 0.2, 0.7));
        
        // 궁수용 무기
        weaponShop.addItem(new Weapon("숏보우", 120, 6, 1, PlayerClass.ARCHER, 100, "짧은 활", 0.1, 0.85));
        weaponShop.addItem(new Weapon("롱보우", 320, 14, 3, PlayerClass.ARCHER, 110, "긴 사정거리의 활", 0.15, 0.8));
        weaponShop.addItem(new Weapon("석궁", 400, 16, 4, PlayerClass.ARCHER, 90, "강력한 석궁", 0.2, 0.75));
        weaponShop.addItem(new Weapon("듀얼 대거", 280, 8, 2, PlayerClass.ARCHER, 100, "한 쌍의 단검", 0.25, 0.85));
        
        // 마법사용 무기
        weaponShop.addItem(new Weapon("오크 지팡이", 150, 3, 1, PlayerClass.MAGE, 100, "오크 나무로 만든 지팡이", 0.05, 0.9));
        weaponShop.addItem(new Weapon("마법봉", 350, 5, 3, PlayerClass.MAGE, 120, "마력이 담긴 봉", 0.1, 0.95));
        weaponShop.addItem(new Weapon("주문서", 400, 8, 4, PlayerClass.MAGE, 80, "마법 주문이 적힌 두루마리", 0.15, 0.9));
        weaponShop.addItem(new Weapon("마력의 구슬", 500, 10, 5, PlayerClass.MAGE, 150, "순수한 마력이 담긴 구슬", 0.2, 0.95));
        
        shops.add(weaponShop);
        
        // 방어구 상점
        Shop armorShop = new Shop("방어구 상점", ShopType.ARMOR);
        
        // 전사용 방어구
        armorShop.addItem(new Armor("가죽 갑옷", 80, 3, 1, PlayerClass.WARRIOR, 100, "기본적인 가죽 갑옷", 0.05, 0.1));
        armorShop.addItem(new Armor("사슬 갑옷", 250, 8, 3, PlayerClass.WARRIOR, 120, "사슬로 만든 갑옷", 0.03, 0.15));
        armorShop.addItem(new Armor("판금 갑옷", 600, 15, 5, PlayerClass.WARRIOR, 150, "강철 판금 갑옷", 0.01, 0.25));
        armorShop.addItem(new Armor("용사의 갑옷", 1200, 20, 8, PlayerClass.WARRIOR, 200, "용사만이 착용할 수 있는 갑옷", 0.05, 0.3));
        
        // 궁수용 방어구
        armorShop.addItem(new Armor("가죽 튜닉", 70, 2, 1, PlayerClass.ARCHER, 100, "가벼운 가죽 튜닉", 0.1, 0.05));
        armorShop.addItem(new Armor("엘븐 메일", 300, 5, 4, PlayerClass.ARCHER, 110, "엘프의 기술로 만든 갑옷", 0.15, 0.1));
        armorShop.addItem(new Armor("레인저 코트", 500, 8, 6, PlayerClass.ARCHER, 120, "레인저용 코트", 0.2, 0.15));
        armorShop.addItem(new Armor("그림자 복장", 1000, 12, 10, PlayerClass.ARCHER, 150, "그림자처럼 움직일 수 있는 복장", 0.25, 0.2));
        
        // 마법사용 방어구
        armorShop.addItem(new Armor("마법사 로브", 60, 1, 1, PlayerClass.MAGE, 100, "기본적인 마법사 로브", 0.1, 0.03));
        armorShop.addItem(new Armor("룬 메일", 280, 3, 5, PlayerClass.MAGE, 120, "룬 문양이 새겨진 로브", 0.15, 0.05));
        armorShop.addItem(new Armor("신비의 가운", 450, 5, 8, PlayerClass.MAGE, 130, "신비한 힘이 깃든 가운", 0.2, 0.08));
        armorShop.addItem(new Armor("대마법사의 의복", 900, 8, 12, PlayerClass.MAGE, 180, "대마법사만이 착용할 수 있는 의복", 0.25, 0.1));
        
        shops.add(armorShop);
        
        // 물약 상점
        Shop potionShop = new Shop("물약 상점", ShopType.POTION);
        potionShop.addItem(new HealthPotion("하급 체력 물약", 50, 30, 1, 1, "체력을 30 회복하는 물약"));
        potionShop.addItem(new HealthPotion("중급 체력 물약", 120, 70, 1, 3, "체력을 70 회복하는 물약"));
        potionShop.addItem(new HealthPotion("상급 체력 물약", 250, 150, 1, 5, "체력을 150 회복하는 물약"));
        potionShop.addItem(new ManaPotion("하급 마나 물약", 60, 30, 1, 1, "마나를 30 회복하는 물약"));
        potionShop.addItem(new ManaPotion("중급 마나 물약", 150, 70, 1, 3, "마나를 70 회복하는 물약"));
        potionShop.addItem(new ManaPotion("상급 마나 물약", 300, 150, 1, 5, "마나를 150 회복하는 물약"));
        potionShop.addItem(new StaminaPotion("활력 물약", 80, 40, 1, 1, "스태미나를 40 회복하는 물약"));
        potionShop.addItem(new StaminaPotion("정신력 물약", 180, 80, 1, 3, "스태미나를 80 회복하는 물약"));
        
        shops.add(potionShop);
        
        // 특수 상점
        Shop specialShop = new Shop("특수 아이템 상점", ShopType.SPECIAL);
        specialShop.addItem(new Scroll("귀환 두루마리", 200, "마을로 순간이동하는 두루마리", 1));
        specialShop.addItem(new Scroll("정화 두루마리", 300, "모든 상태 이상을 치료하는 두루마리", 3));
        specialShop.addItem(new UniversalPotion("만능 물약", 500, 50, 50, 50, 1, 5, "체력, 마나, 스태미나를 모두 회복"));
        specialShop.addItem(new Armor("행운의 반지", 1000, 0, 0, null, 100, "행운을 가져다주는 반지", 0.1, 0.05));
        
        shops.add(specialShop);
    }

    private void initializeNPCs() {
        npcs.add(new NPC("무기 상인", "좋은 무기들이 많이 있습니다. 직업에 맞는 무기를 선택하세요!", ShopType.WEAPON));
        npcs.add(new NPC("방어구 상인", "튼튼한 방어구들이 준비되어 있습니다.", ShopType.ARMOR));
        npcs.add(new NPC("물약 상인", "모험에 필요한 각종 물약을 판매합니다.", ShopType.POTION));
        npcs.add(new NPC("특수 아이템 상인", "특별한 아이템들을 구경해 보세요!", ShopType.SPECIAL));
        npcs.add(new NPC("경비병", "마을을 지켜주세요. 서쪽 숲에서 고블린들이 출몰하고 있습니다."));
        npcs.add(new NPC("대장장이", "특별 주문도 받습니다. 원하는 무기가 있으면 말씀하세요."));
        npcs.add(new NPC("여관 주인", "휴식하시겠습니까? 하루 숙박에 50골드입니다."));
    }

    private void initializeWorldMap() {
        worldMap = new HashMap<>();
        worldMap.put("마을", new Location("마을", "평화로운 시작의 마을", LocationType.TOWN, 1, 10));
        worldMap.put("서쪽 숲", new Location("서쪽 숲", "고블린과 늑대가 서식하는 위험한 숲", LocationType.FOREST, 1, 5));
        worldMap.put("동쪽 산", new Location("동쪽 산", "오크와 트롤이 살고 있는 험준한 산", LocationType.MOUNTAIN, 3, 7));
        worldMap.put("북쪽 묘지", new Location("북쪽 묘지", "언데드가 돌아다니는 음침한 묘지", LocationType.GRAVEYARD, 5, 8));
        worldMap.put("용의 둥지", new Location("용의 둥지", "강력한 드래곤이 서식하는 위험지역", LocationType.DUNGEON, 7, 10));
        worldMap.put("남쪽 호수", new Location("남쪽 호수", "아름답지만 위험한 생물들이 서식하는 호수", LocationType.LAKE, 2, 6));
    }

    public void start() {
        System.out.println("====================================");
        System.out.println("          RPG 게임에 오신 것을 환영합니다!");
        System.out.println("====================================");
        
        createPlayer();
        isRunning = true;
        gameState = GameState.MAIN_MENU;
        
        while (isRunning) {
            switch (gameState) {
                case MAIN_MENU:
                    showMainMenu();
                    break;
                case EXPLORATION:
                    showExplorationMenu();
                    break;
                case BATTLE:
                    // 전투는 Battle 클래스에서 처리
                    break;
                case SHOP:
                    showShopMenu();
                    break;
                case QUEST:
                    showQuestMenu();
                    break;
                case INVENTORY:
                    showInventoryMenu();
                    break;
                case GAME_OVER:
                    handleGameOver();
                    break;
                case SAVE_LOAD:
                    showSaveLoadMenu();
                    break;
            }
        }
    }

    private void createPlayer() {
        System.out.print("\n플레이어 이름을 입력하세요: ");
        String name = scanner.nextLine();
        
        System.out.println("\n직업을 선택하세요:");
        System.out.println("1. 전사 - 높은 체력과 공격력");
        System.out.println("2. 궁수 - 빠른 공격과 회피");
        System.out.println("3. 마법사 - 강력한 마법 공격");
        System.out.print("선택: ");
        
        int classChoice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        
        PlayerClass playerClass;
        switch (classChoice) {
            case 1:
                playerClass = PlayerClass.WARRIOR;
                break;
            case 2:
                playerClass = PlayerClass.ARCHER;
                break;
            case 3:
                playerClass = PlayerClass.MAGE;
                break;
            default:
                System.out.println("잘못된 선택입니다. 전사로 설정됩니다.");
                playerClass = PlayerClass.WARRIOR;
        }
        
        this.player = new Player(name, playerClass);
        System.out.printf("\n%s %s 캐릭터가 생성되었습니다!\n", playerClass.getTitle(), name);
    }

    private void showMainMenu() {
        System.out.println("\n====================================");
        System.out.printf(" Day %d | %s | 위치: %s\n", gameDay, getTimeOfDay(), player.getCurrentLocation());
        System.out.println("====================================");
        System.out.println("1. 상태 보기");
        System.out.println("2. 탐험하기");
        System.out.println("3. 상점 가기");
        System.out.println("4. 퀘스트");
        System.out.println("5. 인벤토리");
        System.out.println("6. NPC와 대화하기");
        System.out.println("7. 휴식하기 (하루가 지납니다)");
        System.out.println("8. 저장/불러오기");
        System.out.println("9. 게임 종료");
        System.out.print("선택: ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기
            
            switch (choice) {
                case 1:
                    showPlayerStatus();
                    break;
                case 2:
                    gameState = GameState.EXPLORATION;
                    break;
                case 3:
                    gameState = GameState.SHOP;
                    break;
                case 4:
                    gameState = GameState.QUEST;
                    break;
                case 5:
                    gameState = GameState.INVENTORY;
                    break;
                case 6:
                    talkToNPC();
                    break;
                case 7:
                    rest();
                    break;
                case 8:
                    gameState = GameState.SAVE_LOAD;
                    break;
                case 9:
                    isRunning = false;
                    System.out.println("게임을 종료합니다.");
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        } catch (InputMismatchException e) {
            System.out.println("숫자를 입력해주세요.");
            scanner.nextLine(); // 잘못된 입력 비우기
        }
    }

    private String getTimeOfDay() {
        int hour = (gameDay % 3);
        if (hour == 0) return "아침";
        else if (hour == 1) return "낮";
        else return "밤";
    }

    private void showPlayerStatus() {
        System.out.println("\n===== 캐릭터 상태 =====");
        System.out.printf("이름: %s (%s)\n", player.getName(), player.getPlayerClass().getTitle());
        System.out.printf("레벨: %d (경험치: %d/%d)\n", 
            player.getLevel(), player.getExp(), player.getMaxExp());
        System.out.printf("체력: %d/%d\n", player.getHp(), player.getMaxHp());
        System.out.printf("마나: %d/%d\n", player.getMana(), player.getMaxMana());
        System.out.printf("스태미나: %d/%d\n", player.getStamina(), player.getMaxStamina());
        System.out.println("--- 능력치 ---");
        System.out.printf("공격력: %d (+%d)\n", 
            player.getBaseAttack(), player.getEquippedWeapon() != null ? player.getEquippedWeapon().getAttack() : 0);
        System.out.printf("방어력: %d (+%d)\n", 
            player.getBaseDefense(), player.getEquippedArmor() != null ? player.getEquippedArmor().getDefense() : 0);
        System.out.printf("민첩성: %d\n", player.getAgility());
        System.out.printf("지능: %d\n", player.getIntelligence());
        System.out.println("--- 장비 ---");
        System.out.printf("무기: %s\n", 
            player.getEquippedWeapon() != null ? player.getEquippedWeapon().getName() + " (공격력 +" + player.getEquippedWeapon().getAttack() + ")" : "없음");
        System.out.printf("방어구: %s\n", 
            player.getEquippedArmor() != null ? player.getEquippedArmor().getName() + " (방어력 +" + player.getEquippedArmor().getDefense() + ")" : "없음");
        System.out.printf("골드: %d G\n", player.getGold());
        
        // 상태 이상 표시
        if (!player.getStatusEffects().isEmpty()) {
            System.out.println("\n--- 상태 이상 ---");
            for (StatusEffect effect : player.getStatusEffects()) {
                System.out.printf("%s: %s (%d턴 남음)\n", 
                    effect.getType().getName(), effect.getType().getDescription(), effect.getRemainingTurns());
            }
        }
        
        System.out.println("================");
        
        System.out.println("\n계속하려면 엔터를 누르세요...");
        scanner.nextLine();
    }

    private void showExplorationMenu() {
        System.out.println("\n===== 탐험 =====");
        System.out.println("현재 위치: " + player.getCurrentLocation());
        System.out.println("1. 마을로 돌아가기");
        
        // 해금된 지역만 표시
        List<String> unlockedLocations = player.getUnlockedLocations();
        int option = 2;
        for (String location : unlockedLocations) {
            if (!location.equals("마을") && !location.equals(player.getCurrentLocation())) {
                System.out.printf("%d. %s로 이동\n", option++, location);
            }
        }
        
        System.out.printf("%d. 주변 탐색하기\n", option++);
        System.out.print("선택: ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기
            
            if (choice == 1) {
                player.setCurrentLocation("마을");
                gameState = GameState.MAIN_MENU;
                System.out.println("마을로 돌아왔습니다.");
            } 
            else if (choice > 1 && choice < option) {
                String[] locations = unlockedLocations.stream()
                    .filter(l -> !l.equals("마을") && !l.equals(player.getCurrentLocation()))
                    .toArray(String[]::new);
                
                if (choice - 2 < locations.length) {
                    moveToLocation(locations[choice - 2]);
                } else {
                    exploreArea();
                }
            } 
            else if (choice == option - 1) {
                exploreArea();
            } 
            else {
                System.out.println("잘못된 선택입니다.");
            }
        } catch (InputMismatchException e) {
            System.out.println("숫자를 입력해주세요.");
            scanner.nextLine(); // 잘못된 입력 비우기
        }
    }

    private void moveToLocation(String locationName) {
        if (player.getStamina() < 15) {
            System.out.println("스태미나가 부족하여 이동할 수 없습니다.");
            return;
        }

        Location destination = worldMap.get(locationName);
        if (destination == null) {
            System.out.println("존재하지 않는 지역입니다.");
            return;
        }
        
        if (!destination.isSuitableFor(player.getLevel())) {
            System.out.println("이 지역은 레벨 " + destination.getMinLevel() + " 이상 " + 
                              destination.getMaxLevel() + " 이하의 플레이어에게 적합합니다.");
            return;
        }

        player.useStamina(15);
        player.setCurrentLocation(locationName);
        System.out.println(locationName + "에 도착했습니다.");

        if (random.nextDouble() < 0.5) {
            triggerTravelEvent();
        }
    }

    private void triggerTravelEvent() {
        double eventRoll = random.nextDouble();
        
        if (eventRoll < 0.4) {
            Monster monster = getRandomMonsterForLocation(worldMap.get(player.getCurrentLocation()));
            System.out.println("\n이동 중 " + monster.getName() + "을(를) 만났습니다!");
            
            Battle battle = new Battle(player, monster, this);
            gameState = GameState.BATTLE;
            battle.start();
        } 
        else if (eventRoll < 0.7) {
            Item item = generateRandomItem();
            System.out.println("\n이동 중 " + item.getName() + "을(를) 발견했습니다!");
            player.addItem(item);
        } 
        else if (eventRoll < 0.85) {
            triggerSpecialEvent();
        } 
        else {
            System.out.println("\n조용히 이동했습니다...");
        }
    }

    private void exploreArea() {
        Location currentLoc = worldMap.get(player.getCurrentLocation());
        System.out.println("\n" + currentLoc.getName() + "을(를) 탐색합니다...");
        
        if (player.getStamina() < 20) {
            System.out.println("스태미나가 부족하여 탐색할 수 없습니다.");
            return;
        }
        player.useStamina(20);
        player.increaseConsecutiveBattles();
        
        double eventRoll = random.nextDouble();
        
        if (eventRoll < 0.6) {
            Monster monster = getRandomMonsterForLocation(currentLoc);
            System.out.println("\n" + monster.getName() + "을(를) 만났습니다!");
            
            Battle battle = new Battle(player, monster, this);
            gameState = GameState.BATTLE;
            battle.start();
        } 
        else if (eventRoll < 0.85) {
            Item item = generateRandomItem();
            System.out.println("\n" + item.getName() + "을(를) 발견했습니다!");
            player.addItem(item);
            
            if (random.nextDouble() < 0.1) {
                Item extraItem = generateRandomItem();
                System.out.println("추가로 " + extraItem.getName() + "을(를) 발견했습니다!");
                player.addItem(extraItem);
            }
        } 
        else if (eventRoll < 0.95) {
            triggerSpecialEvent();
        } 
        else {
            System.out.println("\n아무것도 발견하지 못했습니다...");
        }
        
        // 자동 저장 체크
        battleCount++;
        if (autoSave && battleCount % autoSaveInterval == 0) {
            autoSave();
        }
    }

    private Monster getRandomMonsterForLocation(Location location) {
        List<Monster> locationMonsters = monsters.stream()
            .filter(m -> m.getLevel() <= player.getLevel() + 2)
            .filter(m -> {
                switch (location.getType()) {
                    case FOREST:
                        return m.getName().contains("고블린") || m.getType() == MonsterType.BEAST || m.getType() == MonsterType.PLANT;
                    case MOUNTAIN:
                        return m.getName().contains("오크") || m.getName().contains("트롤") || m.getName().contains("하피");
                    case GRAVEYARD:
                        return m.getType() == MonsterType.UNDEAD || m.getType() == MonsterType.GHOST;
                    case DUNGEON:
                        return m.getType() == MonsterType.BOSS || m.getType() == MonsterType.DEMON;
                    case LAKE:
                        return m.getType() == MonsterType.ELEMENTAL || m.getName().contains("하피");
                    default:
                        return false;
                }
            })
            .collect(Collectors.toList());
        
        if (locationMonsters.isEmpty()) {
            return monsters.get(random.nextInt(monsters.size()));
        }
        
        return locationMonsters.get(random.nextInt(locationMonsters.size()));
    }

    public Item generateRandomItem() {
        int roll = random.nextInt(100);
        
        if (roll < 40) {
            return new HealthPotion("체력 물약", 0, 30 + random.nextInt(20));
        } else if (roll < 70) {
            return new ManaPotion("마나 물약", 0, 20 + random.nextInt(15));
        } else if (roll < 85) {
            return new StaminaPotion("스태미나 물약", 0, 25 + random.nextInt(15));
        } else if (roll < 95) {
            if (random.nextBoolean()) {
                return new Weapon("발견한 " + getRandomWeaponName(), 0, 
                    3 + random.nextInt(5), 1 + random.nextInt(3), null, 50 + random.nextInt(50),
                    "탐험 중 발견한 무기", 0.05 + random.nextDouble() * 0.1, 0.8 + random.nextDouble() * 0.15);
            } else {
                return new Armor("발견한 " + getRandomArmorName(), 0, 
                    2 + random.nextInt(4), 1 + random.nextInt(2), null, 60 + random.nextInt(60),
                    "탐험 중 발견한 방어구", 0.03 + random.nextDouble() * 0.07, 0.05 + random.nextDouble() * 0.1);
            }
        } else {
            if (random.nextBoolean()) {
                return new Weapon("희귀한 " + getRandomWeaponName(), 0, 
                    8 + random.nextInt(7), 3 + random.nextInt(4), null, 80 + random.nextInt(70),
                    "희귀한 무기", 0.15 + random.nextDouble() * 0.1, 0.85 + random.nextDouble() * 0.1);
            } else {
                return new Armor("희귀한 " + getRandomArmorName(), 0, 
                    6 + random.nextInt(5), 3 + random.nextInt(3), null, 90 + random.nextInt(80),
                    "희귀한 방어구", 0.1 + random.nextDouble() * 0.1, 0.15 + random.nextDouble() * 0.1);
            }
        }
    }

    private String getRandomWeaponName() {
        String[] types = {"검", "도끼", "둔기", "창", "활", "석궁", "지팡이", "단검"};
        String[] prefixes = {"날카로운", "강력한", "빛나는", "고대의", "마법의", "정교한"};
        return prefixes[random.nextInt(prefixes.length)] + " " + types[random.nextInt(types.length)];
    }

    private String getRandomArmorName() {
        String[] types = {"갑옷", "투구", "장갑", "부츠", "망토", "방패"};
        String[] prefixes = {"튼튼한", "가벼운", "빛나는", "고대의", "마법의", "정교한"};
        return prefixes[random.nextInt(prefixes.length)] + " " + types[random.nextInt(types.length)];
    }

    private void triggerSpecialEvent() {
        int eventType = random.nextInt(5);
        
        switch (eventType) {
            case 0:
                System.out.println("\n신비한 분수가 발견되었습니다! 체력과 마나가 모두 회복됩니다.");
                player.setHp(player.getMaxHp());
                player.setMana(player.getMaxMana());
                break;
            case 1:
                System.out.println("\n보물 상자를 발견했습니다!");
                int goldFound = 50 + random.nextInt(100);
                player.gainGold(goldFound);
                System.out.println(goldFound + " 골드를 얻었습니다!");
                break;
            case 2:
                System.out.println("\n떠돌이 상인을 만났습니다. 특별한 아이템을 판매하고 있습니다.");
                Shop travelingMerchant = new Shop("떠돌이 상인", ShopType.SPECIAL);
                travelingMerchant.addItem(new HealthPotion("신비한 체력 물약", 200, 100));
                travelingMerchant.addItem(new ManaPotion("신비한 마나 물약", 180, 80));
                travelingMerchant.addItem(new Weapon("전설의 검", 1000, 25, 10, null, 200, "전설로 전해지는 검", 0.2, 0.95));
                showShopMenu(travelingMerchant);
                break;
            case 3:
                System.out.println("\n함정에 걸렸습니다! 피해를 입습니다.");
                int damage = 10 + random.nextInt(20);
                player.takeDamage(damage);
                System.out.println(damage + " 피해를 입었습니다!");
                break;
            case 4:
                System.out.println("\n고대의 유적을 발견했습니다. 경험치를 얻습니다!");
                int expGain = 30 + random.nextInt(50);
                player.gainExp(expGain);
                System.out.println(expGain + " 경험치를 얻었습니다!");
                break;
        }
    }

    private void showShopMenu() {
        System.out.println("\n===== 상점 =====");
        System.out.println("1. 무기 상점");
        System.out.println("2. 방어구 상점");
        System.out.println("3. 물약 상점");
        System.out.println("4. 특수 아이템 상점");
        System.out.println("5. 상점 나가기");
        System.out.print("선택: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        
        if (choice >= 1 && choice <= 4) {
            showShopMenu(shops.get(choice - 1));
        } else if (choice == 5) {
            gameState = GameState.MAIN_MENU;
        } else {
            System.out.println("잘못된 선택입니다.");
        }
    }

    private void showShopMenu(Shop shop) {
        boolean inShop = true;
        
        while (inShop) {
            System.out.println("\n===== " + shop.getName() + " =====");
            System.out.println("보유 골드: " + player.getGold() + " G");
            System.out.println("1. 아이템 구매");
            System.out.println("2. 아이템 판매");
            System.out.println("3. 장비 수리");
            System.out.println("4. 상점 나가기");
            System.out.print("선택: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기
            
            switch (choice) {
                case 1:
                    buyItems(shop);
                    break;
                case 2:
                    sellItems();
                    break;
                case 3:
                    repairEquipment();
                    break;
                case 4:
                    inShop = false;
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }

    private void buyItems(Shop shop) {
        List<Item> availableItems = shop.getItems().stream()
            .filter(item -> {
                if (item instanceof Equipment) {
                    Equipment equip = (Equipment) item;
                    return equip.getRequiredClass() == null || 
                           equip.getRequiredClass() == player.getPlayerClass();
                }
                return true;
            })
            .collect(Collectors.toList());
        
        if (availableItems.isEmpty()) {
            System.out.println("\n현재 구매 가능한 아이템이 없습니다.");
            return;
        }
        
        System.out.println("\n===== 구매 가능한 아이템 =====");
        for (int i = 0; i < availableItems.size(); i++) {
            Item item = availableItems.get(i);
            System.out.printf("%d. %s - %d G", i + 1, item.getName(), item.getPrice());
            
            if (item instanceof Equipment) {
                Equipment equip = (Equipment) item;
                System.out.printf(" (공격력: +%d, 방어력: +%d, 레벨 제한: %d)", 
                    equip.getAttack(), equip.getDefense(), equip.getLevelRequirement());
            } else if (item instanceof Potion) {
                Potion potion = (Potion) item;
                System.out.printf(" (효과: %s +%d)", 
                    potion instanceof HealthPotion ? "체력" : 
                    potion instanceof ManaPotion ? "마나" : "스태미나", 
                    potion.getAmount());
            }
            
            System.out.println();
        }
        
        System.out.print("구매할 아이템 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        
        if (choice > 0 && choice <= availableItems.size()) {
            Item selectedItem = availableItems.get(choice - 1);
            
            if (selectedItem.getPrice() > player.getGold()) {
                System.out.println("골드가 부족합니다!");
                return;
            }
            
            if (selectedItem instanceof Equipment) {
                Equipment equip = (Equipment) selectedItem;
                if (player.getLevel() < equip.getLevelRequirement()) {
                    System.out.println("레벨이 부족하여 구매할 수 없습니다!");
                    return;
                }
            }
            
            player.addItem(selectedItem);
            player.spendGold(selectedItem.getPrice());
            System.out.println(selectedItem.getName() + "을(를) 구매했습니다!");
        }
    }

    private void sellItems() {
        List<Item> sellableItems = player.getInventory().stream()
            .filter(item -> item.getPrice() > 0)
            .collect(Collectors.toList());
        
        if (sellableItems.isEmpty()) {
            System.out.println("판매할 수 있는 아이템이 없습니다.");
            return;
        }
        
        System.out.println("\n===== 판매 가능한 아이템 =====");
        for (int i = 0; i < sellableItems.size(); i++) {
            Item item = sellableItems.get(i);
            int sellPrice = item.getSellPrice();
            System.out.printf("%d. %s - %d G\n", i + 1, item.getName(), sellPrice);
        }
        
        System.out.print("판매할 아이템 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= sellableItems.size()) {
            Item selectedItem = sellableItems.get(choice - 1);
            int sellPrice = selectedItem.getSellPrice();
            
            player.removeItem(selectedItem);
            player.gainGold(sellPrice);
            System.out.println(selectedItem.getName() + "을(를) " + sellPrice + " G에 판매했습니다!");
        }
    }
    
    private void repairEquipment() {
        List<Equipment> equipments = new ArrayList<>();
        if (player.getEquippedWeapon() != null) equipments.add(player.getEquippedWeapon());
        if (player.getEquippedArmor() != null) equipments.add(player.getEquippedArmor());
        
        equipments.addAll(player.getInventory().stream()
            .filter(item -> item instanceof Equipment)
            .map(item -> (Equipment) item)
            .collect(Collectors.toList()));
        
        if (equipments.isEmpty()) {
            System.out.println("수리할 장비가 없습니다.");
            return;
        }
        
        System.out.println("\n===== 수리 가능한 장비 =====");
        for (int i = 0; i < equipments.size(); i++) {
            Equipment equip = equipments.get(i);
            int repairCost = (int)(equip.getPrice() * 0.1 * 
            	    (1.0 - (equip.getDurability() / (double)equip.getMaxDurability())));
            System.out.printf("%d. %s - 내구도 %d/%d (수리 비용: %d G)\n", 
                i + 1, equip.getName(), equip.getDurability(), equip.getMaxDurability(), repairCost);
        }
        
        System.out.print("수리할 장비 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= equipments.size()) {
            Equipment selectedEquip = equipments.get(choice - 1);
            int repairCost = (int)(selectedEquip.getPrice() * 0.1 * 
                (1.0 - (selectedEquip.getDurability() / (double)selectedEquip.getMaxDurability())));
            
            if (player.getGold() < repairCost) {
                System.out.println("골드가 부족합니다!");
                return;
            }
            
            selectedEquip.repair();
            player.spendGold(repairCost);
            System.out.println(selectedEquip.getName() + "을(를) 수리했습니다! (" + repairCost + " G 사용)");
        }
    }

    private void showQuestMenu() {
        System.out.println("\n===== 퀘스트 =====");
        System.out.println("1. 수락한 퀘스트 보기");
        System.out.println("2. 새로운 퀘스트 보기");
        System.out.println("3. 퀘스트 보상 받기");
        System.out.println("4. 퀘스트 메뉴 나가기");
        System.out.print("선택: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                showActiveQuests();
                break;
            case 2:
                showAvailableQuests();
                break;
            case 3:
                claimQuestRewards();
                break;
            case 4:
                gameState = GameState.MAIN_MENU;
                break;
            default:
                System.out.println("잘못된 선택입니다.");
        }
    }

    private void showActiveQuests() {
        List<Quest> activeQuests = player.getActiveQuests();
        
        if (activeQuests.isEmpty()) {
            System.out.println("\n수락한 퀘스트가 없습니다.");
            return;
        }
        
        System.out.println("\n===== 수락한 퀘스트 =====");
        for (int i = 0; i < activeQuests.size(); i++) {
            Quest quest = activeQuests.get(i);
            System.out.printf("%d. %s - %s\n", i + 1, quest.getTitle(), quest.getDescription());
            System.out.printf("   진행 상황: %d/%d\n", quest.getCurrentProgress(), quest.getRequiredProgress());
        }
        
        System.out.println("\n계속하려면 엔터를 누르세요...");
        scanner.nextLine();
    }

    private void showAvailableQuests() {
        List<Quest> availableQuests = quests.stream()
            .filter(q -> !player.hasActiveQuest(q) && 
                !player.hasCompletedQuest(q) &&
                q.getLevelRequirement() <= player.getLevel())
            .collect(Collectors.toList());
        
        if (availableQuests.isEmpty()) {
            System.out.println("\n새로운 퀘스트가 없습니다.");
            return;
        }
        
        System.out.println("\n===== 새로운 퀘스트 =====");
        for (int i = 0; i < availableQuests.size(); i++) {
            Quest quest = availableQuests.get(i);
            System.out.printf("%d. %s - %s\n", i + 1, quest.getTitle(), quest.getDescription());
            System.out.printf("   보상: %d 경험치, %d 골드\n", quest.getExpReward(), quest.getGoldReward());
            System.out.printf("   권장 레벨: %d\n", quest.getLevelRequirement());
        }
        
        System.out.print("\n수락할 퀘스트 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= availableQuests.size()) {
            Quest selectedQuest = availableQuests.get(choice - 1);
            player.acceptQuest(selectedQuest);
            System.out.println("\n퀘스트 '" + selectedQuest.getTitle() + "'를 수락했습니다!");
        }
    }

    private void claimQuestRewards() {
        List<Quest> completableQuests = player.getActiveQuests().stream()
            .filter(Quest::isCompleted)
            .collect(Collectors.toList());
        
        if (completableQuests.isEmpty()) {
            System.out.println("\n완료한 퀘스트가 없습니다.");
            return;
        }
        
        System.out.println("\n===== 완료한 퀘스트 =====");
        for (int i = 0; i < completableQuests.size(); i++) {
            Quest quest = completableQuests.get(i);
            System.out.printf("%d. %s - %s\n", i + 1, quest.getTitle(), quest.getDescription());
            System.out.printf("   보상: %d 경험치, %d 골드\n", quest.getExpReward(), quest.getGoldReward());
        }
        
        System.out.print("\n보상을 받을 퀘스트 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= completableQuests.size()) {
            Quest completedQuest = completableQuests.get(choice - 1);
            player.completeQuest(completedQuest);
            
            System.out.println("\n퀘스트 '" + completedQuest.getTitle() + "' 완료!");
            System.out.printf("%d 경험치와 %d 골드를 얻었습니다!\n", 
                completedQuest.getExpReward(), completedQuest.getGoldReward());
            
            if (completedQuest.getRewardItem() != null) {
                System.out.println("추가 보상: " + completedQuest.getRewardItem().getName());
            }
        }
    }

    private void showInventoryMenu() {
        boolean inInventory = true;
        
        while (inInventory) {
            System.out.println("\n===== 인벤토리 =====");
            System.out.println("보유 골드: " + player.getGold() + " G");
            System.out.println("1. 아이템 사용");
            System.out.println("2. 장비 착용");
            System.out.println("3. 장비 해제");
            System.out.println("4. 스탯 포인트 분배");
            System.out.println("5. 인벤토리 나가기");
            System.out.print("선택: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        useItem();
                        break;
                    case 2:
                        equipItem();
                        break;
                    case 3:
                        unequipItem();
                        break;
                    case 4:
                        distributeStatPoints();
                        break;
                    case 5:
                        inInventory = false;
                        gameState = GameState.MAIN_MENU;
                        return;
                    default:
                        System.out.println("잘못된 선택입니다.");
                }
            } catch (InputMismatchException e) {
                System.out.println("숫자를 입력해주세요.");
                scanner.nextLine();
            }
        }
    }

    private void useItem() {
        List<Potion> potions = player.getInventory().stream()
            .filter(item -> item instanceof Potion)
            .map(item -> (Potion) item)
            .collect(Collectors.toList());
        
        if (potions.isEmpty()) {
            System.out.println("\n사용할 수 있는 아이템이 없습니다.");
            return;
        }
        
        System.out.println("\n===== 사용 가능한 아이템 =====");
        for (int i = 0; i < potions.size(); i++) {
            Potion potion = potions.get(i);
            String effect = potion instanceof HealthPotion ? "체력 +" + potion.getAmount() :
                          potion instanceof ManaPotion ? "마나 +" + potion.getAmount() :
                          "스태미나 +" + potion.getAmount();
            System.out.printf("%d. %s - %s\n", i + 1, potion.getName(), effect);
        }
        
        System.out.print("\n사용할 아이템 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= potions.size()) {
            Potion selectedPotion = potions.get(choice - 1);
            selectedPotion.use(player);
            player.removeItem(selectedPotion);
            System.out.println("\n" + selectedPotion.getName() + "을(를) 사용했습니다!");
        }
    }

    private void equipItem() {
        List<Equipment> equipments = player.getInventory().stream()
            .filter(item -> item instanceof Equipment)
            .map(item -> (Equipment) item)
            .filter(equip -> player.getLevel() >= equip.getLevelRequirement())
            .collect(Collectors.toList());
        
        if (equipments.isEmpty()) {
            System.out.println("\n착용할 수 있는 장비가 없습니다.");
            return;
        }
        
        System.out.println("\n===== 착용 가능한 장비 =====");
        for (int i = 0; i < equipments.size(); i++) {
            Equipment equip = equipments.get(i);
            String type = equip instanceof Weapon ? "무기" : "방어구";
            System.out.printf("%d. %s (%s) - 공격력: +%d, 방어력: +%d, 레벨 제한: %d\n", 
                i + 1, equip.getName(), type, equip.getAttack(), equip.getDefense(), 
                equip.getLevelRequirement());
        }
        
        System.out.print("\n착용할 장비 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= equipments.size()) {
            Equipment selectedEquip = equipments.get(choice - 1);
            player.equip(selectedEquip);
            System.out.println("\n" + selectedEquip.getName() + "을(를) 착용했습니다!");
        }
    }

    private void unequipItem() {
        System.out.println("\n===== 현재 착용 중인 장비 =====");
        System.out.println("1. 무기: " + 
            (player.getEquippedWeapon() != null ? player.getEquippedWeapon().getName() : "없음"));
        System.out.println("2. 방어구: " + 
            (player.getEquippedArmor() != null ? player.getEquippedArmor().getName() : "없음"));
        System.out.print("\n해제할 장비 번호를 선택하세요 (0: 취소): ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            if (player.getEquippedWeapon() != null) {
                Equipment unequipped = player.unequipWeapon();
                System.out.println("\n" + unequipped.getName() + "을(를) 해제했습니다!");
            } else {
                System.out.println("\n착용 중인 무기가 없습니다.");
            }
        } else if (choice == 2) {
            if (player.getEquippedArmor() != null) {
                Equipment unequipped = player.unequipArmor();
                System.out.println("\n" + unequipped.getName() + "을(를) 해제했습니다!");
            } else {
                System.out.println("\n착용 중인 방어구가 없습니다.");
            }
        }
    }
    
    private void distributeStatPoints() {
        if (player.getStatPoints() <= 0) {
            System.out.println("분배할 스탯 포인트가 없습니다.");
            return;
        }
        
        boolean distributing = true;
        
        while (distributing && player.getStatPoints() > 0) {
            System.out.println("\n===== 스탯 포인트 분배 =====");
            System.out.println("보유 포인트: " + player.getStatPoints());
            System.out.println("1. 공격력 (" + player.getBaseAttack() + ")");
            System.out.println("2. 방어력 (" + player.getBaseDefense() + ")");
            System.out.println("3. 민첩성 (" + player.getAgility() + ")");
            System.out.println("4. 지능 (" + player.getIntelligence() + ")");
            System.out.println("5. 체력 (" + player.getMaxHp() + ")");
            System.out.println("6. 분배 종료");
            System.out.print("선택: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    player.distributeStatPoint("공격력");
                    System.out.println("공격력이 증가했습니다! (" + player.getBaseAttack() + ")");
                    break;
                case 2:
                    player.distributeStatPoint("방어력");
                    System.out.println("방어력이 증가했습니다! (" + player.getBaseDefense() + ")");
                    break;
                case 3:
                    player.distributeStatPoint("민첩성");
                    System.out.println("민첩성이 증가했습니다! (" + player.getAgility() + ")");
                    break;
                case 4:
                    player.distributeStatPoint("지능");
                    System.out.println("지능이 증가했습니다! (" + player.getIntelligence() + ")");
                    break;
                case 5:
                    player.distributeStatPoint("체력");
                    System.out.println("최대 체력이 증가했습니다! (" + player.getMaxHp() + ")");
                    break;
                case 6:
                    distributing = false;
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }

    private void talkToNPC() {
        System.out.println("\n===== NPC 목록 =====");
        for (int i = 0; i < npcs.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, npcs.get(i).getName());
        }
        
        System.out.print("\n대화할 NPC 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice > 0 && choice <= npcs.size()) {
            NPC npc = npcs.get(choice - 1);
            System.out.println("\n[" + npc.getName() + "] " + npc.getDialogue());
            
            if (npc.getShopType() != null) {
                System.out.print("상점을 이용하시겠습니까? (1: 예, 2: 아니오): ");
                int shopChoice = scanner.nextInt();
                scanner.nextLine();
                
                if (shopChoice == 1) {
                    Shop npcShop = shops.stream()
                        .filter(shop -> shop.getType() == npc.getShopType())
                        .findFirst()
                        .orElse(null);
                    
                    if (npcShop != null) {
                        gameState = GameState.SHOP;
                        showShopMenu(npcShop);
                    }
                }
            } else if (npc.getName().equals("경비병")) {
                System.out.println("\n경비병: 서쪽 숲에서 고블린들이 마을을 위협하고 있습니다. 처치해 주시겠습니까?");
                System.out.println("1. 퀘스트 수락하기");
                System.out.println("2. 거절하기");
                System.out.print("선택: ");
                
                int questChoice = scanner.nextInt();
                scanner.nextLine();
                
                if (questChoice == 1) {
                    Quest goblinQuest = quests.get(0);
                    if (!player.hasActiveQuest(goblinQuest) && !player.hasCompletedQuest(goblinQuest)) {
                        player.acceptQuest(goblinQuest);
                        System.out.println("\n퀘스트 '" + goblinQuest.getTitle() + "'를 수락했습니다!");
                    } else {
                        System.out.println("\n이미 해당 퀘스트를 수락했거나 완료했습니다.");
                    }
                }
            } else if (npc.getName().equals("여관 주인")) {
                System.out.print("\n하루 숙박에 50골드입니다. 휴식하시겠습니까? (1: 예, 2: 아니오): ");
                int restChoice = scanner.nextInt();
                scanner.nextLine();
                
                if (restChoice == 1) {
                    if (player.getGold() >= 50) {
                        player.spendGold(50);
                        rest();
                    } else {
                        System.out.println("골드가 부족합니다!");
                    }
                }
            }
        }
        
        System.out.println("\n계속하려면 엔터를 누르세요...");
        scanner.nextLine();
    }

    private void rest() {
        System.out.println("\n하루를 쉬며 체력을 회복합니다...");
        player.setHp(player.getMaxHp());
        player.setMana(player.getMaxMana());
        player.setStamina(player.getMaxStamina());
        player.resetConsecutiveBattles();
        
        // 상태 이상 치료
        player.getStatusEffects().clear();
        
        gameDay++;
        System.out.println("새로운 날이 밝았습니다. (Day " + gameDay + ")");
        
        // 상점 물품 갱신
        shops.forEach(shop -> {
            shop.restock();
            System.out.println(shop.getName() + "의 물품이 갱신되었습니다!");
        });
        
        // 몬스터 리젠
        if (gameDay % 3 == 0) {
            System.out.println("몬스터들이 다시 생성되었습니다.");
        }
    }

    private void handleGameOver() {
        System.out.println("\n===== GAME OVER =====");
        System.out.println(player.getName() + "은(는) 패배했습니다...");
        System.out.println("1. 마을에서 재시작 (체력 50% 회복)");
        System.out.println("2. 게임 종료");
        System.out.print("선택: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            player.setHp(player.getMaxHp() / 2);
            player.setCurrentLocation("마을");
            gameState = GameState.MAIN_MENU;
        } else {
            isRunning = false;
        }
    }
    
    private void showSaveLoadMenu() {
        boolean inMenu = true;
        
        while (inMenu) {
            System.out.println("\n===== 저장/불러오기 =====");
            System.out.println("1. 게임 저장");
            System.out.println("2. 게임 불러오기");
            System.out.println("3. 메인 메뉴로 돌아가기");
            System.out.print("선택: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    saveGame();
                    break;
                case 2:
                    loadGame();
                    break;
                case 3:
                    inMenu = false;
                    gameState = GameState.MAIN_MENU;
                    break;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }
    
    private void saveGame() {
        System.out.print("\n저장할 파일 이름을 입력하세요 (예: save1.sav): ");
        String filename = scanner.nextLine();
        
        try {
            saveGame(filename);
            System.out.println("게임이 성공적으로 저장되었습니다!");
        } catch (IOException e) {
            System.out.println("게임 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private void loadGame() {
        System.out.print("\n불러올 파일 이름을 입력하세요 (예: save1.sav): ");
        String filename = scanner.nextLine();
        
        try {
            loadGame(filename);
            System.out.println("게임이 성공적으로 불러와졌습니다!");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("게임 불러오기 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    public void saveGame(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(filename))) {
            oos.writeObject(player);
            oos.writeObject(gameDay);
            oos.writeObject(player.getCurrentLocation());
            System.out.println("게임이 저장되었습니다!");
        }
    }
    
    public void loadGame(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(filename))) {
            player = (Player) ois.readObject();
            gameDay = (int) ois.readObject();
            String location = (String) ois.readObject();
            player.setCurrentLocation(location);
            System.out.println("게임을 불러왔습니다!");
        }
    }
    
    private void autoSave() {
        try {
            saveGame("autosave.sav");
            System.out.println("\n게임이 자동 저장되었습니다.");
        } catch (IOException e) {
            System.out.println("\n자동 저장에 실패했습니다.");
        }
    }

    public void updateQuestProgress(Monster monster) {
        player.getActiveQuests().forEach(q -> q.updateProgress(monster));
    }
    
    public void setGameState(GameState state) {
        this.gameState = state;
    }
}

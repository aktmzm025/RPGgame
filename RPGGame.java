package rpggame;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class RPGGame {
    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}

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

    public Game() {
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        this.monsters = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.shops = new ArrayList<>();
        this.npcs = new ArrayList<>();
        this.gameState = GameState.MAIN_MENU;
        this.gameDay = 1;
        
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
    }

    private void initializeQuests() {
        quests.add(new Quest("초보자의 첫 걸음", "고블린 3마리 처치", 
            monster -> monster.getName().contains("고블린"), 3, 50, 100));
        quests.add(new Quest("오크 사냥꾼", "오크 2마리 처치", 
            monster -> monster.getName().contains("오크"), 2, 80, 150));
        quests.add(new Quest("언데드 퇴치", "언데드 타입 몬스터 5마리 처치", 
            monster -> monster.getType() == MonsterType.UNDEAD, 5, 150, 300));
        quests.add(new Quest("드래곤 슬레이어", "드래곤 1마리 처치", 
            monster -> monster.getType() == MonsterType.DRAGON, 1, 500, 1000));
        quests.add(new Quest("숲의 정화", "숲의 몬스터 10마리 처치", 
            monster -> worldMap.get(player.getCurrentLocation()).getType() == LocationType.FOREST, 10, 200, 300));
    }

    private void initializeShops() {
        // 무기 상점
        Shop weaponShop = new Shop("무기 상점", ShopType.WEAPON);
        
        // 전사용 무기
        weaponShop.addItem(new Weapon("단검", 100, 5, 1, PlayerClass.WARRIOR));
        weaponShop.addItem(new Weapon("양손검", 300, 12, 3, PlayerClass.WARRIOR));
        weaponShop.addItem(new Weapon("도끼", 250, 10, 2, PlayerClass.WARRIOR));
        weaponShop.addItem(new Weapon("철퇴", 350, 8, 4, PlayerClass.WARRIOR));
        
        // 궁수용 무기
        weaponShop.addItem(new Weapon("숏보우", 120, 6, 1, PlayerClass.ARCHER));
        weaponShop.addItem(new Weapon("롱보우", 320, 14, 3, PlayerClass.ARCHER));
        weaponShop.addItem(new Weapon("석궁", 400, 16, 4, PlayerClass.ARCHER));
        weaponShop.addItem(new Weapon("듀얼 대거", 280, 8, 2, PlayerClass.ARCHER));
        
        // 마법사용 무기
        weaponShop.addItem(new Weapon("오크 지팡이", 150, 3, 1, PlayerClass.MAGE));
        weaponShop.addItem(new Weapon("마법봉", 350, 5, 3, PlayerClass.MAGE));
        weaponShop.addItem(new Weapon("주문서", 400, 8, 4, PlayerClass.MAGE));
        weaponShop.addItem(new Weapon("마력의 구슬", 500, 10, 5, PlayerClass.MAGE));
        
        shops.add(weaponShop);
        
        // 방어구 상점
        Shop armorShop = new Shop("방어구 상점", ShopType.ARMOR);
        
        // 전사용 방어구
        armorShop.addItem(new Armor("가죽 갑옷", 80, 3, 1, PlayerClass.WARRIOR));
        armorShop.addItem(new Armor("사슬 갑옷", 250, 8, 3, PlayerClass.WARRIOR));
        armorShop.addItem(new Armor("판금 갑옷", 600, 15, 5, PlayerClass.WARRIOR));
        armorShop.addItem(new Armor("용사의 갑옷", 1200, 20, 8, PlayerClass.WARRIOR));
        
        // 궁수용 방어구
        armorShop.addItem(new Armor("가죽 튜닉", 70, 2, 1, PlayerClass.ARCHER));
        armorShop.addItem(new Armor("엘븐 메일", 300, 5, 4, PlayerClass.ARCHER));
        armorShop.addItem(new Armor("레인저 코트", 500, 8, 6, PlayerClass.ARCHER));
        armorShop.addItem(new Armor("그림자 복장", 1000, 12, 10, PlayerClass.ARCHER));
        
        // 마법사용 방어구
        armorShop.addItem(new Armor("마법사 로브", 60, 1, 1, PlayerClass.MAGE));
        armorShop.addItem(new Armor("룬 메일", 280, 3, 5, PlayerClass.MAGE));
        armorShop.addItem(new Armor("신비의 가운", 450, 5, 8, PlayerClass.MAGE));
        armorShop.addItem(new Armor("대마법사의 의복", 900, 8, 12, PlayerClass.MAGE));
        
        shops.add(armorShop);
        
        // 물약 상점
        Shop potionShop = new Shop("물약 상점", ShopType.POTION);
        potionShop.addItem(new HealthPotion("하급 체력 물약", 50, 30));
        potionShop.addItem(new HealthPotion("중급 체력 물약", 120, 70));
        potionShop.addItem(new HealthPotion("상급 체력 물약", 250, 150));
        potionShop.addItem(new ManaPotion("하급 마나 물약", 60, 30));
        potionShop.addItem(new ManaPotion("중급 마나 물약", 150, 70));
        potionShop.addItem(new ManaPotion("상급 마나 물약", 300, 150));
        potionShop.addItem(new StaminaPotion("활력 물약", 80, 40));
        potionShop.addItem(new StaminaPotion("정신력 물약", 180, 80));
        
        shops.add(potionShop);
        
        // 특수 상점
        Shop specialShop = new Shop("특수 아이템 상점", ShopType.SPECIAL);
        specialShop.addItem(new Scroll("귀환 두루마리", 200));
        specialShop.addItem(new Scroll("정화 두루마리", 300));
        specialShop.addItem(new Potion("만능 물약", 500, 50, 50, 50));
        specialShop.addItem(new Equipment("행운의 반지", 1000, 0, 0, 0, 0));
        
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
        worldMap.put("마을", new Location("마을", "평화로운 시작의 마을", LocationType.TOWN));
        worldMap.put("서쪽 숲", new Location("서쪽 숲", "고블린과 늑대가 서식하는 위험한 숲", LocationType.FOREST));
        worldMap.put("동쪽 산", new Location("동쪽 산", "오크와 트롤이 살고 있는 험준한 산", LocationType.MOUNTAIN));
        worldMap.put("북쪽 묘지", new Location("북쪽 묘지", "언데드가 돌아다니는 음침한 묘지", LocationType.GRAVEYARD));
        worldMap.put("용의 둥지", new Location("용의 둥지", "강력한 드래곤이 서식하는 위험지역", LocationType.DUNGEON));
        worldMap.put("남쪽 호수", new Location("남쪽 호수", "아름답지만 위험한 생물들이 서식하는 호수", LocationType.LAKE));
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
        
        // 초기 아이템 지급
        player.addItem(new HealthPotion("하급 체력 물약", 0, 30));
        player.addItem(new ManaPotion("마나 물약", 0, 30));
        
        if (playerClass == PlayerClass.WARRIOR) {
            player.equip(new Weapon("초보자 검", 0, 5, 1, PlayerClass.WARRIOR));
            player.equip(new Armor("초보자 갑옷", 0, 3, 1, PlayerClass.WARRIOR));
        } else if (playerClass == PlayerClass.ARCHER) {
            player.equip(new Weapon("초보자 활", 0, 4, 1, PlayerClass.ARCHER));
            player.equip(new Armor("초보자 가죽 갑옷", 0, 2, 1, PlayerClass.ARCHER));
        } else {
            player.equip(new Weapon("초보자 지팡이", 0, 3, 1, PlayerClass.MAGE));
            player.equip(new Armor("초보자 로브", 0, 1, 1, PlayerClass.MAGE));
        }
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
        System.out.println("8. 게임 종료");
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
            player.getEquippedWeapon() != null ? player.getEquippedWeapon().getName() : "없음");
        System.out.printf("방어구: %s\n", 
            player.getEquippedArmor() != null ? player.getEquippedArmor().getName() : "없음");
        System.out.printf("골드: %d G\n", player.getGold());
        System.out.println("================");
        
        System.out.println("\n계속하려면 엔터를 누르세요...");
        scanner.nextLine();
    }

    private void showExplorationMenu() {
        System.out.println("\n===== 탐험 =====");
        System.out.println("현재 위치: " + player.getCurrentLocation());
        System.out.println("1. 마을로 돌아가기");
        System.out.println("2. 서쪽 숲으로 이동");
        System.out.println("3. 동쪽 산으로 이동");
        System.out.println("4. 북쪽 묘지로 이동");
        System.out.println("5. 용의 둥지로 이동");
        System.out.println("6. 남쪽 호수로 이동");
        System.out.println("7. 주변 탐색하기");
        System.out.print("선택: ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기
            
            switch (choice) {
                case 1:
                    player.setCurrentLocation("마을");
                    gameState = GameState.MAIN_MENU;
                    System.out.println("마을로 돌아왔습니다.");
                    break;
                case 2:
                    moveToLocation("서쪽 숲");
                    break;
                case 3:
                    moveToLocation("동쪽 산");
                    break;
                case 4:
                    moveToLocation("북쪽 묘지");
                    break;
                case 5:
                    if (player.getLevel() < 5) {
                        System.out.println("용의 둥지는 너무 위험합니다. 레벨 5 이상이 되어야 합니다.");
                    } else {
                        moveToLocation("용의 둥지");
                    }
                    break;
                case 6:
                    moveToLocation("남쪽 호수");
                    break;
                case 7:
                    exploreArea();
                    break;
                default:
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

    private Item generateRandomItem() {
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
                    3 + random.nextInt(5), 1 + random.nextInt(3), null);
            } else {
                return new Armor("발견한 " + getRandomArmorName(), 0, 
                    2 + random.nextInt(4), 1 + random.nextInt(2), null);
            }
        } else {
            if (random.nextBoolean()) {
                return new Weapon("희귀한 " + getRandomWeaponName(), 0, 
                    8 + random.nextInt(7), 3 + random.nextInt(4), null);
            } else {
                return new Armor("희귀한 " + getRandomArmorName(), 0, 
                    6 + random.nextInt(5), 3 + random.nextInt(3), null);
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
                travelingMerchant.addItem(new Weapon("전설의 검", 1000, 25, 10, null));
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
            System.out.println("3. 상점 나가기");
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
            int sellPrice = (int) (item.getPrice() * 0.7);
            System.out.printf("%d. %s - %d G\n", i + 1, item.getName(), sellPrice);
        }
        
        System.out.print("판매할 아이템 번호를 선택하세요 (0: 취소): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기
        
        if (choice > 0 && choice <= sellableItems.size()) {
            Item selectedItem = sellableItems.get(choice - 1);
            int sellPrice = (int) (selectedItem.getPrice() * 0.7);
            
            player.removeItem(selectedItem);
            player.gainGold(sellPrice);
            System.out.println(selectedItem.getName() + "을(를) " + sellPrice + " G에 판매했습니다!");
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
        scanner.nextLine(); // 버퍼 비우기
        
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
        scanner.nextLine(); // 버퍼 비우기
        
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
        scanner.nextLine(); // 버퍼 비우기
        
        if (choice > 0 && choice <= completableQuests.size()) {
            Quest completedQuest = completableQuests.get(choice - 1);
            player.completeQuest(completedQuest);
            
            System.out.println("\n퀘스트 '" + completedQuest.getTitle() + "' 완료!");
            System.out.printf("%d 경험치와 %d 골드를 얻었습니다!\n", 
                completedQuest.getExpReward(), completedQuest.getGoldReward());
            
            if (completedQuest.getRewardItem() != null) {
                player.addItem(completedQuest.getRewardItem());
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
            System.out.println("4. 인벤토리 나가기");
            System.out.print("선택: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // 버퍼 비우기
                
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

    public void updateQuestProgress(Monster monster) {
        player.getActiveQuests().forEach(q -> q.updateProgress(monster));
    }
    
    public void setGameState(GameState state) {
        this.gameState = state;
    }
}

enum GameState {
    MAIN_MENU, EXPLORATION, BATTLE, SHOP, QUEST, INVENTORY, GAME_OVER
}

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

enum MonsterType {
    NORMAL, BEAST, PLANT, UNDEAD, GHOST, GIANT, FLYING, BOSS, ELEMENTAL, DEMON, DRAGON, CONSTRUCT
}

enum LocationType {
    TOWN, FOREST, MOUNTAIN, GRAVEYARD, DUNGEON, LAKE
}

enum ShopType {
    WEAPON, ARMOR, POTION, SPECIAL
}

class Location {
    private String name;
    private String description;
    private LocationType type;
    
    public Location(String name, String description, LocationType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocationType getType() { return type; }
}

class NPC {
    private String name;
    private String dialogue;
    private ShopType shopType;
    
    public NPC(String name, String dialogue) {
        this(name, dialogue, null);
    }
    
    public NPC(String name, String dialogue, ShopType shopType) {
        this.name = name;
        this.dialogue = dialogue;
        this.shopType = shopType;
    }
    
    public String getName() { return name; }
    public String getDialogue() { return dialogue; }
    public ShopType getShopType() { return shopType; }
}

class Shop {
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
                items.add(new Weapon("강철 검", 400, 15, 3, PlayerClass.WARRIOR));
                items.add(new Weapon("전투 도끼", 500, 18, 4, PlayerClass.WARRIOR));
                // 궁수용 무기
                items.add(new Weapon("강력한 활", 450, 16, 3, PlayerClass.ARCHER));
                items.add(new Weapon("정밀 석궁", 550, 20, 4, PlayerClass.ARCHER));
                // 마법사용 무기
                items.add(new Weapon("에너지 스태프", 480, 8, 3, PlayerClass.MAGE));
                items.add(new Weapon("신비의 봉", 600, 12, 5, PlayerClass.MAGE));
                break;
                
            case ARMOR:
                // 전사용 방어구
                items.add(new Armor("강철 갑옷", 500, 12, 3, PlayerClass.WARRIOR));
                items.add(new Armor("전사용 흉갑", 700, 16, 5, PlayerClass.WARRIOR));
                // 궁수용 방어구
                items.add(new Armor("가죽 갑옷", 400, 8, 3, PlayerClass.ARCHER));
                items.add(new Armor("숙련자 복장", 650, 12, 6, PlayerClass.ARCHER));
                // 마법사용 방어구
                items.add(new Armor("마법사 로브", 450, 5, 4, PlayerClass.MAGE));
                items.add(new Armor("현자의 가운", 680, 8, 8, PlayerClass.MAGE));
                break;
                
            case POTION:
                items.add(new HealthPotion("하급 체력 물약", 50, 30));
                items.add(new HealthPotion("중급 체력 물약", 120, 70));
                items.add(new ManaPotion("하급 마나 물약", 60, 30));
                items.add(new ManaPotion("중급 마나 물약", 150, 70));
                items.add(new StaminaPotion("활력 물약", 80, 40));
                break;
                
            case SPECIAL:
                items.add(new Scroll("귀환 두루마리", 200));
                items.add(new Potion("만능 물약", 500, 50, 50, 50));
                if (random.nextDouble() < 0.3) {
                    items.add(new Equipment("행운의 반지", 1000, 0, 0, 0, 0));
                }
                break;
        }
    }
    
    public String getName() { return name; }
    public ShopType getType() { return type; }
    public List<Item> getItems() { return items; }
}

class Quest {
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
        this.title = title;
        this.description = description;
        this.condition = condition;
        this.requiredProgress = requiredProgress;
        this.currentProgress = 0;
        this.expReward = expReward;
        this.goldReward = goldReward;
        this.isCompleted = false;
        this.levelRequirement = title.equals("드래곤 슬레이어") ? 5 : 1;
        
        if (title.equals("드래곤 슬레이어")) {
            this.rewardItem = new Weapon("드래곤 슬레이어", 0, 30, 10, null);
        } else if (title.equals("언데드 퇴치")) {
            this.rewardItem = new Armor("성스러운 갑옷", 0, 10, 15, null);
        }
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

class Player {
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
    }
    
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public void gainExp(int amount) {
        exp += amount;
        while (exp >= maxExp) {
            levelUp();
        }
    }
    
    private void levelUp() {
        level++;
        exp -= maxExp;
        maxExp = (int) (maxExp * 1.5);
        
        maxHp += 10 + (playerClass == PlayerClass.WARRIOR ? 5 : 0);
        maxMana += 5 + (playerClass == PlayerClass.MAGE ? 10 : 0);
        maxStamina += 8 + (playerClass == PlayerClass.ARCHER ? 5 : 0);
        baseAttack += 2 + (playerClass == PlayerClass.WARRIOR ? 1 : 0);
        baseDefense += 1 + (playerClass == PlayerClass.WARRIOR ? 1 : 0);
        agility += 1 + (playerClass == PlayerClass.ARCHER ? 2 : 0);
        intelligence += 1 + (playerClass == PlayerClass.MAGE ? 3 : 0);
        
        hp = maxHp;
        mana = maxMana;
        stamina = maxStamina;
        
        System.out.println("\n=========================");
        System.out.println("  레벨 업! " + level + " 레벨이 되었습니다!");
        System.out.println("=========================");
        System.out.println("HP: " + (maxHp - 10) + " -> " + maxHp);
        System.out.println("마나: " + (maxMana - 5) + " -> " + maxMana);
        System.out.println("스태미나: " + (maxStamina - 8) + " -> " + maxStamina);
        System.out.println("공격력: " + (baseAttack - 2) + " -> " + baseAttack);
        System.out.println("방어력: " + (baseDefense - 1) + " -> " + baseDefense);
        System.out.println("민첩성: " + (agility - 1) + " -> " + agility);
        System.out.println("지능: " + (intelligence - 1) + " -> " + intelligence);
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
    }
    
    public boolean hasActiveQuest(Quest quest) {
        return activeQuests.contains(quest);
    }
    
    public boolean hasCompletedQuest(Quest quest) {
        return completedQuests.contains(quest);
    }
    
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
    
    public void setHp(int hp) { this.hp = Math.min(hp, maxHp); }
    public void setMana(int mana) { this.mana = Math.min(mana, maxMana); }
    public void setStamina(int stamina) { this.stamina = Math.min(stamina, maxStamina); }
    public void setCurrentLocation(String location) { this.currentLocation = location; }
    
    public int getAttack() {
        return baseAttack + (equippedWeapon != null ? equippedWeapon.getAttack() : 0);
    }
    
    public int getDefense() {
        return baseDefense + (equippedArmor != null ? equippedArmor.getDefense() : 0);
    }
}

class Monster {
    private String name;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int expReward;
    private int goldReward;
    private int level;
    private MonsterType type;
    
    public Monster(String name, int maxHp, int attack, int defense, 
                  int expReward, int level, MonsterType type) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.expReward = expReward;
        this.goldReward = expReward / 2;
        this.level = level;
        this.type = type;
    }
    
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }
    
    public boolean isAlive() {
        return hp > 0;
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
}

interface Item {
    String getName();
    int getPrice();
}

abstract class Equipment implements Item {
    protected int attack;
    protected int defense;
    protected int levelRequirement;
    protected PlayerClass requiredClass;
    
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getLevelRequirement() { return levelRequirement; }
    public PlayerClass getRequiredClass() { return requiredClass; }
}

class Weapon extends Equipment {
    private String name;
    private int price;
    
    public Weapon(String name, int price, int attack, int levelRequirement, PlayerClass requiredClass) {
        this.name = name;
        this.price = price;
        this.attack = attack;
        this.defense = 0;
        this.levelRequirement = levelRequirement;
        this.requiredClass = requiredClass;
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
}

class Armor extends Equipment {
    private String name;
    private int price;
    
    public Armor(String name, int price, int defense, int levelRequirement, PlayerClass requiredClass) {
        this.name = name;
        this.price = price;
        this.attack = 0;
        this.defense = defense;
        this.levelRequirement = levelRequirement;
        this.requiredClass = requiredClass;
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
}

interface Potion extends Item {
    void use(Player player);
    int getAmount();
}

class HealthPotion implements Potion {
    private String name;
    private int price;
    private int amount;
    
    public HealthPotion(String name, int price, int amount) {
        this.name = name;
        this.price = price;
        this.amount = amount;
    }
    
    @Override
    public void use(Player player) {
        player.setHp(player.getHp() + amount);
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getAmount() { return amount; }
}

class ManaPotion implements Potion {
    private String name;
    private int price;
    private int amount;
    
    public ManaPotion(String name, int price, int amount) {
        this.name = name;
        this.price = price;
        this.amount = amount;
    }
    
    @Override
    public void use(Player player) {
        player.setMana(player.getMana() + amount);
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getAmount() { return amount; }
}

class StaminaPotion implements Potion {
    private String name;
    private int price;
    private int amount;
    
    public StaminaPotion(String name, int price, int amount) {
        this.name = name;
        this.price = price;
        this.amount = amount;
    }
    
    @Override
    public void use(Player player) {
        player.setStamina(player.getStamina() + amount);
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getAmount() { return amount; }
}

class Potion implements Item {
    private String name;
    private int price;
    private int healthAmount;
    private int manaAmount;
    private int staminaAmount;
    
    public Potion(String name, int price, int healthAmount, int manaAmount, int staminaAmount) {
        this.name = name;
        this.price = price;
        this.healthAmount = healthAmount;
        this.manaAmount = manaAmount;
        this.staminaAmount = staminaAmount;
    }
    
    public void use(Player player) {
        player.setHp(player.getHp() + healthAmount);
        player.setMana(player.getMana() + manaAmount);
        player.setStamina(player.getStamina() + staminaAmount);
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
}

class Scroll implements Item {
    private String name;
    private int price;
    
    public Scroll(String name, int price) {
        this.name = name;
        this.price = price;
    }
    
    public void use(Player player) {
        if (name.contains("귀환")) {
            player.setCurrentLocation("마을");
            System.out.println("마을로 귀환했습니다!");
        } else if (name.contains("정화")) {
            System.out.println("모든 상태 이상이 치료되었습니다!");
        }
    }
    
    public String getName() { return name; }
    public int getPrice() { return price; }
}

class Skill {
    private String name;
    private int manaCost;
    private String description;
    private BiConsumer<Player, Monster> effect;
    
    public Skill(String name, int manaCost, String description, BiConsumer<Player, Monster> effect) {
        this.name = name;
        this.manaCost = manaCost;
        this.description = description;
        this.effect = effect;
    }
    
    public void use(Player player, Monster monster) {
        effect.accept(player, monster);
    }
    
    public String getName() { return name; }
    public int getManaCost() { return manaCost; }
    public String getDescription() { return description; }
}

class Battle {
    private Player player;
    private Monster monster;
    private Game game;
    private Scanner scanner;
    private Random random;
    
    public Battle(Player player, Monster monster, Game game) {
        this.player = player;
        this.monster = monster;
        this.game = game;
        this.scanner = new Scanner(System.in);
        this.random = new Random();
    }
    
    public void start() {
        System.out.println("\n===== 전투 시작! =====");
        
        boolean playerFirst = random.nextInt(player.getAgility() + monster.getLevel() * 5) > monster.getLevel() * 5;
        
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
            
            player.setStamina(Math.min(player.getMaxStamina(), player.getStamina() + 5));
        }
        
        if (player.isAlive()) {
            playerWin();
        } else {
            game.setGameState(GameState.GAME_OVER);
        }
    }
    
    private void showStatus() {
        System.out.println("\n-----------------------");
        System.out.println(player.getName() + " (Lv." + player.getLevel() + ")");
        System.out.printf("HP: %d/%d | 마나: %d/%d | 스태미나: %d/%d\n", 
            player.getHp(), player.getMaxHp(), 
            player.getMana(), player.getMaxMana(),
            player.getStamina(), player.getMaxStamina());
        
        System.out.println("\nVS");
        
        System.out.println("\n" + monster.getName() + " (Lv." + monster.getLevel() + ")");
        System.out.printf("HP: %d/%d\n", monster.getHp(), monster.getMaxHp());
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
        int damage = calculateDamage(player.getAttack(), monster.getDefense());
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
                        }));
                }
                if (player.getLevel() >= 3) {
                    skills.add(new Skill("방어 태세", 15, "방어력을 증가시킵니다.", 
                        (p, m) -> {
                            System.out.printf("\n%s이(가) 방어 태세를 취해 방어력이 증가했습니다!\n", p.getName());
                        }));
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
                        }));
                }
                if (player.getLevel() >= 3) {
                    skills.add(new Skill("저격", 20, "강력한 한 방을 날립니다.", 
                        (p, m) -> {
                            int damage = calculateDamage(p.getAttack() * 3, m.getDefense() / 2);
                            m.takeDamage(damage);
                            System.out.printf("\n%s이(가) 저격으로 %s에게 %d의 데미지를 입혔습니다!\n", 
                                p.getName(), m.getName(), damage);
                        }));
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
                        }));
                }
                if (player.getLevel() >= 3) {
                    skills.add(new Skill("치유", 20, "체력을 회복합니다.", 
                        (p, m) -> {
                            int healAmount = p.getIntelligence() * 3;
                            p.setHp(p.getHp() + healAmount);
                            System.out.printf("\n%s이(가) 치유 마법으로 %d 체력을 회복했습니다!\n", 
                                p.getName(), healAmount);
                        }));
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
        
        if (random.nextDouble() < 0.8 || monster.getLevel() < 3) {
            int damage = calculateDamage(monster.getAttack(), player.getDefense());
            player.takeDamage(damage);
            System.out.printf("%s이(가) %s에게 %d의 데미지를 입혔습니다!\n", 
                monster.getName(), player.getName(), damage);
        } else {
            int damage = calculateDamage(monster.getAttack() * 2, player.getDefense());
            player.takeDamage(damage);
            System.out.printf("%s이(가) 강력한 공격으로 %s에게 %d의 데미지를 입혔습니다!\n", 
                monster.getName(), player.getName(), damage);
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
        
        game.updateQuestProgress(monster);
        
        if (Math.random() < getDropRate()) {
            Item item = game.generateRandomItem();
            player.addItem(item);
            System.out.println(item.getName() + "을(를) 획득했습니다!");
        }
        
        game.setGameState(GameState.MAIN_MENU);
    }
    
    private double getDropRate() {
        int levelDiff = player.getLevel() - monster.getLevel();
        double baseRate = 0.3;
        
        if (levelDiff > 3) {
            return baseRate * 0.5;
        } else if (levelDiff < -3) {
            return baseRate * 1.5;
        }
        return baseRate;
    }
 // ManaPotion 클래스 추가
    class ManaPotion implements Potion {
        private String name;
        private int price;
        private int amount;
        
        public ManaPotion(String name, int price, int amount) {
            this.name = name;
            this.price = price;
            this.amount = amount;
        }
        
        @Override
        public void use(Player player) {
            player.setMana(player.getMana() + amount);
        }
        
        public String getName() { return name; }
        public int getPrice() { return price; }
        public int getAmount() { return amount; }
    }

    // Armor 클래스 추가
    class Armor extends Equipment {
        private String name;
        private int price;
        
        public Armor(String name, int price, int defense, int levelRequirement, PlayerClass requiredClass) {
            this.name = name;
            this.price = price;
            this.attack = 0;
            this.defense = defense;
            this.levelRequirement = levelRequirement;
            this.requiredClass = requiredClass;
        }
        
        public String getName() { return name; }
        public int getPrice() { return price; }
    }
}
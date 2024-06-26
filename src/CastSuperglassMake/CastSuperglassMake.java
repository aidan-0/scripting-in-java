package CastSuperglassMake;

import Antiban.AntiBan;
import MasterSelector.MasterScript;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Lunar;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.awt.*;



@ScriptManifest(name = "Cast Superglass Make", description = "Cast Superglass Make on Lunar Isle", author = "Luten",
        version = 1.0, category = Category.CONSTRUCTION, image = "")
public class CastSuperglassMake extends AbstractScript {

    //    private final MasterScript masterScript;
    State state;
    Timer timer = new Timer();
    private int planksMade = 0;
    private int previousXP = 0;
    Area nearBabaYagaHouse = new Area(2094, 3928, 2084, 3932, 0);
    Area inBabaYagaHouse = new Area(2449, 4645, 2453, 4649, 0);
    Area bankArea = new Area(2095, 3916, 2101, 3919,0);
    int numOfAstralRunesToPurchase = 3000; //change this later to become whatever the user inputs on startup
    AntiBan antiBan = new AntiBan();


    @Override
    public void onStart() {
        log("Script Started");
        SkillTracker.start();
        SkillTracker.start(Skill.MAGIC);
        SkillTracker.start(Skill.CRAFTING);
//        setZoomAndYaw();

        previousXP = Skills.getExperience(Skill.MAGIC);
    }


    @Override
    public int onLoop() {
        switch (getState()) {
            case WITHDRAW_ITEMS:
                if(!bankArea.contains(Players.getLocal().getTile())) {
                    Walking.walk(bankArea);
                    log("Moving to bank area");
                    sleepUntil(() -> bankArea.contains(Players.getLocal().getTile()), 5000, 50);
                    sleep(600, 1200);
                } else {
                    sleep(antiBan.randomDelayMedium(3));
//                    sleep(antiBan.randomDelayLong(1));
                    if (!Bank.isOpen() && NPCs.closest(6126) != null) {
                        NPCs.closest(6126).interact("Bank");
                        Sleep.sleepUntil(Bank::isOpen, 15000);
                        sleep(600, 1400);
                    }

                    if (!Inventory.onlyContains("Astral rune", "Giant Seaweed", "Bucket of sand", "Coins", "Molten glass")) {
                        log("Inventory contained unwanted item(s). Banking all and re-gearing");
                        Bank.depositAllItems();
                        Sleep.sleepUntil(Inventory::isEmpty, 2000);
                    }

                    if (Inventory.contains("Molten glass")) {
                        Bank.depositAll("Molten glass");
                        Sleep.sleepUntil(Inventory::isEmpty, 2000);
                        sleep(200, 600);
                    }

                    if (!Inventory.contains("Astral rune") && Bank.contains("Astral rune")) {
                        Bank.withdrawAll("Astral rune");
                        log("Withdrawing Astral runes");
                        sleepUntil(() -> Inventory.contains("Astral rune"), 10000, 50);
                        sleep(1600, 2100);
                    } else if (!Inventory.contains("Astral rune") && !Bank.contains("Astral rune")) {
                        log("Failed to withdraw Astral runes");
                        if (numOfAstralRunesToPurchase > 1) {
                            break;
                        } else {
                            stop();
                            //                    masterScript.signalStop();
                        }
                    }

                    if (!Inventory.contains("Giant seaweed") && Bank.contains("Giant seaweed")) {
                        Bank.withdraw("Giant seaweed", 2);
                        log("Withdrawing Giant seaweed");
                        sleepUntil(() -> Inventory.contains("Giant seaweed"), 10000, 50);

                        sleep(1600, 2100);
                    } else if (!Inventory.contains("Giant seaweed") && !Bank.contains("Giant seaweed")) {
                        log("Failed to withdraw Giant seaweed");
                        stop();
                        //                    masterScript.signalStop();
                    }

                    if (!Inventory.contains("Bucket of sand") && Bank.contains("Bucket of sand")) {
                        Bank.withdraw("Bucket of sand", 12);
                        log("Withdrawing 10 Buckets of sand");
                        sleepUntil(() -> Inventory.contains("Bucket of sand"), 10000, 50);
                        sleep(300,600);
                    } else if (!Inventory.contains("Bucket of sand") && !Bank.contains("Bucket of sand")) {
                        log("Failed to withdraw Bucket of sand");
                        stop();
                        //                    masterScript.signalStop();
                    }
                    Bank.close();
                }
                sleep(600, 1200);
                break;


            case CAST_SUPERGLASS_MAKE:
                if(Bank.isOpen()) {
                    Bank.close();
                }
                if (!Players.getLocal().isAnimating()) {
                    Magic.castSpell(Lunar.SUPERGLASS_MAKE);
                    log("casting SUPERGLASS MAKE");
                    Mouse.click();
                    sleep(600, 1200);
                    checkXPGains();
//                    Mouse.move(new Point(800, Calculations.random(0,502))); //Move mouse off the screen to the right
                    sleep(600);
                }
                checkXPGains();
                sleep(400,600);
                break;

            case PREP_TO_RESTOCK_ASTRALS:
                sleep(antiBan.randomDelayMedium(18));
                sleep(antiBan.randomDelayLong(1));
                if (!Inventory.contains("Coins") && !Bank.isOpen() && NPCs.closest(6126) != null) {
                    NPCs.closest(6126).interact("Bank");
                    Sleep.sleepUntil(Bank::isOpen, 15000);
                    sleep(2600, 3400);
                }

                if(Bank.isOpen() && Bank.contains("Coins")) {
                    Bank.depositAllItems();
                    Sleep.sleepUntil(Inventory::isEmpty, 5000);
                    Bank.withdrawAll("Coins");
                    log("withdrawing coins");
                    sleepUntil(() -> Inventory.contains("Coins"), 10000, 50);
                }

                if (Inventory.contains("Coins") && !nearBabaYagaHouse.contains(Players.getLocal().getTile()) || !inBabaYagaHouse.contains(Players.getLocal().getTile())) {
                    Walking.walk(nearBabaYagaHouse);
                    log("Moving near Baba Yaga's House");
                    sleepUntil(() -> nearBabaYagaHouse.contains(Players.getLocal().getTile()), 10000, 50);
                    sleep(600, 1200);
                }

                if (NPCs.closest(3836) != null) { //the house
                    NPCs.closest(3836).interact("Go-inside");
                    sleepUntil(() -> inBabaYagaHouse.contains(Players.getLocal().getTile()), 10000, 50);
                }

                sleep(600, 1200);
                break;


            case RESTOCK_ASTRALS:
                if (!Shop.isOpen() && NPCs.closest(3837) != null) {
                    NPCs.closest(3837).interact("Trade");
                    sleep(2000, 3500);
                }

                if (Shop.isOpen() && Shop.count("Astral rune") > 10) {
                    Shop.purchaseFifty("Astral rune");
                    log("Total astrals in inventory is " + Inventory.count("Astral rune"));
                    log("need to purchase " + (numOfAstralRunesToPurchase - Inventory.count("Astral rune")) + " more Astral runes.");
                    sleep(100, 1100);
                } else if (Shop.isOpen() && Shop.count("Astral rune") <= 10) {
                    antiBan.hopWorlds();
                }
                sleep(1600, 2200);
                break;



            case RETURN_TO_BANK:
                GameObject door = GameObjects.closest("Door");
                door.interact("Open");
                sleep(2500,4000);

                if(!bankArea.contains(Players.getLocal().getTile())) {
                    Walking.walk(bankArea);
                    log("Moving to bank area");
                    sleepUntil(() -> bankArea.contains(Players.getLocal().getTile()), 5000, 50);
                    sleep(600, 1200);
                } else {
                    sleep(antiBan.randomDelayMedium(18));
                    sleep(antiBan.randomDelayLong(1));
                    if (!Bank.isOpen() && NPCs.closest(6126) != null) {
                        NPCs.closest(6126).interact("Bank");
                        Sleep.sleepUntil(Bank::isOpen, 15000);
                        sleep(600, 1400);
                    }

                    if (Inventory.contains("Teak plank")) {
                        Bank.depositAll("Teak plank");
                        Sleep.sleepUntil(Inventory::isEmpty, 2000);
                        sleep(200, 600);
                    }
                }

                break;
        }
        return 1;
    }

    private void setZoomAndYaw() {
        int zoom = Calculations.random(275, 450);
        log("Setting zoom level");
        Camera.setZoom(zoom);

        int yaw = Calculations.random(1, 360);
        log("Setting Yaw");
        Camera.rotateToYaw(yaw);
    }

    private void checkXPGains() {
        int currentXP = Skills.getExperience(Skill.MAGIC);
        if (currentXP > previousXP) {
            planksMade++;
            previousXP = currentXP;
        }
    }


    private State getState() {
        if (Inventory.count("Astral rune") < 10 && !Bank.contains("Astral rune") && !inBabaYagaHouse.contains(Players.getLocal().getTile())) {
            log("State is now PREP_TO_RESTOCK_ASTRALS");
            return State.PREP_TO_RESTOCK_ASTRALS;
//            If restock astrals checkbox is ticked (true) then when astrals run out it will purchase the amount of astrals input by the user from babas shop, before continuing the script
            //this will only work if you have started on lunar isle

        } else if (inBabaYagaHouse.contains(Players.getLocal().getTile()) && Inventory.count("Astral rune") < numOfAstralRunesToPurchase) {
            log("State is now RESTOCK_ASTRALS");
            return State.RESTOCK_ASTRALS;
//            If restock astrals checkbox is ticked (true) then when astrals run out it will purchase the amount of astrals input by the user from babas shop, before continuing the script
            //this will only work if you have started on lunar isle

        } else if (inBabaYagaHouse.contains(Players.getLocal().getTile()) && Inventory.count("Astral rune") >= numOfAstralRunesToPurchase) {
            log("State is now RETURN_TO_BANK");
            return State.RETURN_TO_BANK;

        } else if (!Inventory.contains("Astral rune") || !Inventory.contains("Giant seaweed") || !Inventory.contains("Bucket of sand")) {
            log("State is now WITHDRAW_ITEMS");
            return State.WITHDRAW_ITEMS;

        } else if (Inventory.contains("Astral rune") && Inventory.contains("Giant Seaweed") && Inventory.contains("Bucket of sand") && bankArea.contains(Players.getLocal().getTile()))  {
            //and air staff equipped
//            log("State is now CAST_PLANK_MAKE");
            return State.CAST_SUPERGLASS_MAKE;
        }
        log("returning state");
        return state;
    }

    @Override
    public void onPaint(Graphics g) {
        super.onPaint(g);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(30, 35, 150, 100);
        g.setFont(new Font("Arial", Font.PLAIN, 12));


        g.setColor(Color.white);
        g.drawString("Magic XP/HR: " + (SkillTracker.getGainedExperiencePerHour(Skill.MAGIC)), 35, 50);
        g.drawString("Crafting XP/HR: " + (SkillTracker.getGainedExperiencePerHour(Skill.CRAFTING)), 35, 70);
        g.drawString("Timer: " + timer.formatTime(), 35, 90);

        long milliseconds = SkillTracker.getTimeToLevel(Skill.MAGIC);
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        g.drawString("Magic TTL: " + hours + " hr, " + minutes + " min.", 35, 110);

        long craftingMilliseconds = SkillTracker.getTimeToLevel(Skill.CRAFTING);
        long craftingSeconds = craftingMilliseconds / 1000;
        long craftingHours = craftingSeconds / 3600;
        long craftingMinutes = (craftingSeconds % 3600) / 60;
        g.drawString("Crafting TTL: " + craftingHours + " hr, " + craftingMinutes + " min.", 35, 130);
    }

    @Override
    public void onExit() {
        super.onExit();
    }
}
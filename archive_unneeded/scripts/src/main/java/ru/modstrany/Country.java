package ru.modstrany;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public class Country {
    private final String id;
    private String name;
    private final int color;
    private final Set<UUID> members = new HashSet<>();
    private final Set<String> allies = new HashSet<>();
    private final Set<String> enemies = new HashSet<>();
    private final Set<BlockPos> borderPositions = new HashSet<>();
    private long balance;

    public Country(String id, String name, int color, long startingBalance) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.balance = startingBalance;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public String getColorHex() {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    public Set<UUID> getMembers() {
        return Set.copyOf(members);
    }

    public Set<String> getAllies() {
        return Set.copyOf(allies);
    }

    public Set<String> getEnemies() {
        return Set.copyOf(enemies);
    }

    public boolean addMember(UUID playerId) {
        return members.add(playerId);
    }

    public boolean addAlly(String otherCountryId) {
        return allies.add(otherCountryId.toLowerCase());
    }

    public boolean addEnemy(String otherCountryId) {
        return enemies.add(otherCountryId.toLowerCase());
    }

    public boolean deposit(long amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    public boolean withdraw(long amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public long getBalance() {
        return balance;
    }

    public boolean addBorderPosition(BlockPos pos) {
        return borderPositions.add(pos.immutable());
    }

    public Set<BlockPos> getBorderPositions() {
        return Set.copyOf(borderPositions);
    }

    public boolean isAlliedWith(String countryId) {
        return allies.contains(countryId.toLowerCase());
    }

    public boolean isAtWarWith(String countryId) {
        return enemies.contains(countryId.toLowerCase());
    }
}

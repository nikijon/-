package ru.modstrany;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CountryManager {
    public static final CountryManager INSTANCE = new CountryManager();

    private final Map<String, Country> countries = new HashMap<>();
    private final Map<String, Alliance> alliances = new HashMap<>();

    private CountryManager() {
    }

    public Country createCountry(String id, String name) {
        String key = normalizeId(id);
        if (countries.containsKey(key)) {
            return null;
        }

        Country country = new Country(key, name, createRandomColor(), 5_000_000L);
        countries.put(key, country);
        return country;
    }

    public boolean transferMoney(String fromId, String toId, long amount) {
        String fromKey = normalizeId(fromId);
        String toKey = normalizeId(toId);
        if (fromKey.equals(toKey) || amount <= 0) {
            return false;
        }
        Country from = countries.get(fromKey);
        Country to = countries.get(toKey);
        if (from == null || to == null) {
            return false;
        }
        if (!from.withdraw(amount)) {
            return false;
        }
        return to.deposit(amount);
    }

    public Optional<Alliance> getAlliance(String id) {
        return Optional.ofNullable(alliances.get(normalizeId(id)));
    }

    public boolean createAlliance(String allianceId, String displayName, String countryA, String countryB) {
        String key = normalizeId(allianceId);
        if (alliances.containsKey(key)) return false;
        Optional<Country> a = getCountry(countryA);
        Optional<Country> b = getCountry(countryB);
        if (a.isEmpty() || b.isEmpty()) return false;
        Alliance alliance = new Alliance(allianceId, displayName);
        alliance.addMember(a.get().getId());
        alliance.addMember(b.get().getId());
        alliances.put(key, alliance);
        return true;
    }

    public boolean addCountryToAlliance(String allianceId, String countryId) {
        Alliance alliance = alliances.get(normalizeId(allianceId));
        if (alliance == null) return false;
        if (!countries.containsKey(normalizeId(countryId))) return false;
        return alliance.addMember(countryId);
    }

    public boolean renameAlliance(String allianceId, String newDisplayName) {
        Alliance alliance = alliances.get(normalizeId(allianceId));
        if (alliance == null) return false;
        alliance.setDisplayName(newDisplayName);
        return true;
    }

    public boolean addMoneyToCountry(String id, long amount) {
        if (amount <= 0) {
            return false;
        }
        return getCountry(id).map(country -> country.deposit(amount)).orElse(false);
    }

    public boolean withdrawMoneyFromCountry(String id, long amount) {
        if (amount <= 0) {
            return false;
        }
        return getCountry(id).map(country -> country.withdraw(amount)).orElse(false);
    }

    public boolean addMoneyToPlayerCountry(UUID playerId, long amount) {
        if (amount <= 0) {
            return false;
        }
        return getCountryByPlayer(playerId).map(country -> country.deposit(amount)).orElse(false);
    }

    public boolean withdrawMoneyFromPlayerCountry(UUID playerId, long amount) {
        if (amount <= 0) {
            return false;
        }
        return getCountryByPlayer(playerId).map(country -> country.withdraw(amount)).orElse(false);
    }

    public Optional<Country> getCountry(String id) {
        return Optional.ofNullable(countries.get(normalizeId(id)));
    }

    public Optional<Country> getCountryByPlayer(UUID playerId) {
        return countries.values().stream()
                .filter(country -> country.getMembers().contains(playerId))
                .findFirst();
    }

    public boolean makeAlliance(String firstId, String secondId) {
        String firstKey = normalizeId(firstId);
        String secondKey = normalizeId(secondId);
        if (firstKey.equals(secondKey)) {
            return false;
        }

        Country first = countries.get(firstKey);
        Country second = countries.get(secondKey);
        if (first == null || second == null) {
            return false;
        }

        boolean changed = first.addAlly(secondKey);
        changed |= second.addAlly(firstKey);
        return changed;
    }

    public boolean declareWar(String attackerId, String defenderId) {
        String attackerKey = normalizeId(attackerId);
        String defenderKey = normalizeId(defenderId);
        if (attackerKey.equals(defenderKey)) {
            return false;
        }

        Country attacker = countries.get(attackerKey);
        Country defender = countries.get(defenderKey);
        if (attacker == null || defender == null) {
            return false;
        }

        boolean changed = attacker.addEnemy(defenderKey);
        changed |= defender.addEnemy(attackerKey);
        return changed;
    }

    public Collection<Country> getCountries() {
        return countries.values();
    }

    private int createRandomColor() {
        float hue = ThreadLocalRandom.current().nextFloat();
        float saturation = 0.6f + ThreadLocalRandom.current().nextFloat() * 0.4f;
        float brightness = 0.7f + ThreadLocalRandom.current().nextFloat() * 0.3f;
        return hsbToRgb(hue, saturation, brightness);
    }

    private int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0.0f) {
            r = g = b = Math.round(brightness * 255.0f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - saturation * (1.0f - f));
            switch ((int) h) {
                case 0 -> {
                    r = Math.round(brightness * 255.0f);
                    g = Math.round(t * 255.0f);
                    b = Math.round(p * 255.0f);
                }
                case 1 -> {
                    r = Math.round(q * 255.0f);
                    g = Math.round(brightness * 255.0f);
                    b = Math.round(p * 255.0f);
                }
                case 2 -> {
                    r = Math.round(p * 255.0f);
                    g = Math.round(brightness * 255.0f);
                    b = Math.round(t * 255.0f);
                }
                case 3 -> {
                    r = Math.round(p * 255.0f);
                    g = Math.round(q * 255.0f);
                    b = Math.round(brightness * 255.0f);
                }
                case 4 -> {
                    r = Math.round(t * 255.0f);
                    g = Math.round(p * 255.0f);
                    b = Math.round(brightness * 255.0f);
                }
                case 5, 6 -> {
                    r = Math.round(brightness * 255.0f);
                    g = Math.round(p * 255.0f);
                    b = Math.round(q * 255.0f);
                }
            }
        }
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private String normalizeId(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }
}

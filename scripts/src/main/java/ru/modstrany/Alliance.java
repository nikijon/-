package ru.modstrany;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Alliance {
    private final String id;
    private String displayName;
    private final Set<String> members = new HashSet<>();
    public static final int MAX_MEMBERS = 10;

    public Alliance(String id, String displayName) {
        this.id = normalizeId(id);
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String newName) {
        this.displayName = newName;
    }

    public Set<String> getMembers() {
        return Set.copyOf(members);
    }

    public boolean addMember(String countryId) {
        if (members.size() >= MAX_MEMBERS) return false;
        return members.add(normalizeId(countryId));
    }

    private String normalizeId(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }
}

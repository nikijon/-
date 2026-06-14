package ru.modstrany;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("country")
                .then(Commands.literal("create")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> createCountry(context)))))
                .then(Commands.literal("ally")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("create")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                .then(Commands.argument("first", StringArgumentType.word())
                                    .then(Commands.argument("second", StringArgumentType.word())
                                        .executes(context -> createNamedAlliance(context)))))))
                    .then(Commands.literal("add")
                        .then(Commands.argument("allianceId", StringArgumentType.word())
                            .then(Commands.argument("country", StringArgumentType.word())
                                .executes(context -> addToAlliance(context)))))
                    .then(Commands.literal("rename")
                        .then(Commands.argument("allianceId", StringArgumentType.word())
                            .then(Commands.argument("newName", StringArgumentType.greedyString())
                                .executes(context -> renameAlliance(context)))))
                    .then(Commands.argument("first", StringArgumentType.word())
                        .then(Commands.argument("second", StringArgumentType.word())
                            .executes(context -> createAlliance(context)))))
                .then(Commands.literal("join")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> joinCountry(context))))
                .then(Commands.literal("rename")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("newName", StringArgumentType.greedyString())
                            .executes(context -> renameCountry(context)))))
                .then(Commands.literal("war")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("attacker", StringArgumentType.word())
                                .then(Commands.argument("defender", StringArgumentType.word())
                                        .executes(context -> declareWar(context)))))
                .then(Commands.literal("transfer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("from", StringArgumentType.word())
                                .then(Commands.argument("to", StringArgumentType.word())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> transferMoney(context))))))
                .then(Commands.literal("money")
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> addMoney(context))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> removeMoney(context)))))
                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(context -> showCountryInfo(context))));
    }

    private static int createCountry(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        String name = StringArgumentType.getString(context, "name");
        Country country = CountryManager.INSTANCE.createCountry(id, name);

        if (country == null) {
            source.sendFailure(Component.literal("Страна с таким ID уже существует."));
            return 0;
        }

        source.sendSuccess(Component.literal("Создана страна '" + country.getName() + "' с цветом " + country.getColorHex() + " и балансом 5 000 000."), true);
        return 1;
    }

    private static int createAlliance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String first = StringArgumentType.getString(context, "first");
        String second = StringArgumentType.getString(context, "second");

        if (!CountryManager.INSTANCE.getCountry(first).isPresent() || !CountryManager.INSTANCE.getCountry(second).isPresent()) {
            source.sendFailure(Component.literal("Обе страны должны существовать для создания союза."));
            return 0;
        }

        boolean result = CountryManager.INSTANCE.makeAlliance(first, second);
        if (!result) {
            source.sendFailure(Component.literal("Не удалось создать союз. Возможно, страны уже союзники или указаны одинаковые страны."));
            return 0;
        }

        Component message = Component.literal("Союз создан: '" + first + "' + '" + second + "'!");
        source.sendSuccess(Component.literal("Союз между '" + first + "' и '" + second + "' создан."), true);
        broadcastChat(source.getServer(), message);
        return 1;
    }

    private static int declareWar(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String attacker = StringArgumentType.getString(context, "attacker");
        String defender = StringArgumentType.getString(context, "defender");

        if (!CountryManager.INSTANCE.getCountry(attacker).isPresent() || !CountryManager.INSTANCE.getCountry(defender).isPresent()) {
            source.sendFailure(Component.literal("Обе страны должны существовать для объявления войны."));
            return 0;
        }

        // запрет на атаку страны без реальных (онлайн) игроков
        var optDef = CountryManager.INSTANCE.getCountry(defender);
        if (optDef.isPresent()) {
            var server = source.getServer();
            boolean hasOnline = false;
            if (server != null) {
                var players = server.getPlayerList().getPlayers();
                for (var p : players) {
                    if (optDef.get().getMembers().contains(p.getUUID())) {
                        hasOnline = true;
                        break;
                    }
                }
            }
            if (!hasOnline) {
                source.sendFailure(Component.literal("Нельзя нападать на страну, в которой нет реальных игроков онлайн."));
                return 0;
            }
        }

        boolean result = CountryManager.INSTANCE.declareWar(attacker, defender);
        if (!result) {
            source.sendFailure(Component.literal("Не удалось объявить войну. Возможно, вы указали одну и ту же страну."));
            return 0;
        }

        Component message = Component.literal("Война объявлена: '" + attacker + "' против '" + defender + "'!");
        source.sendSuccess(Component.literal("Страна '" + attacker + "' объявила войну стране '" + defender + "'."), true);
        broadcastChat(source.getServer(), message);
        return 1;
    }

    private static int createNamedAlliance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        String display = StringArgumentType.getString(context, "displayName");
        String first = StringArgumentType.getString(context, "first");
        String second = StringArgumentType.getString(context, "second");

        if (!CountryManager.INSTANCE.createAlliance(id, display, first, second)) {
            source.sendFailure(Component.literal("Не удалось создать альянс. Проверьте уникальность ID и существование стран."));
            return 0;
        }

        Component message = Component.literal("Создан альянс '" + display + "' с ID '" + id + "'.");
        source.sendSuccess(Component.literal("Альянс '" + display + "' создан."), true);
        broadcastChat(source.getServer(), message);
        return 1;
    }

    private static int addToAlliance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String allianceId = StringArgumentType.getString(context, "allianceId");
        String country = StringArgumentType.getString(context, "country");

        if (!CountryManager.INSTANCE.addCountryToAlliance(allianceId, country)) {
            source.sendFailure(Component.literal("Не удалось добавить страну в альянс. Проверьте ID альянса, страны и лимит членов."));
            return 0;
        }

        Component message = Component.literal("Страна '" + country + "' добавлена в альянс '" + allianceId + "'.");
        source.sendSuccess(Component.literal("Страна добавлена в альянс."), true);
        broadcastChat(source.getServer(), message);
        return 1;
    }

    private static int renameAlliance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String allianceId = StringArgumentType.getString(context, "allianceId");
        String newName = StringArgumentType.getString(context, "newName");

        if (!CountryManager.INSTANCE.renameAlliance(allianceId, newName)) {
            source.sendFailure(Component.literal("Не удалось переименовать альянс. Проверьте ID альянса."));
            return 0;
        }

        Component message = Component.literal("Альянс '" + allianceId + "' переименован в '" + newName + "'.");
        source.sendSuccess(Component.literal("Альянс переименован."), true);
        broadcastChat(source.getServer(), message);
        return 1;
    }

    private static int renameCountry(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        String newName = StringArgumentType.getString(context, "newName");

        return CountryManager.INSTANCE.getCountry(id)
                .map(country -> {
                    country.setName(newName);
                    Component message = Component.literal("Страна '" + id + "' переименована в '" + newName + "'.");
                    source.sendSuccess(Component.literal("Страна переименована."), true);
                    broadcastChat(source.getServer(), message);
                    return 1;
                })
                .orElseGet(() -> {
                    source.sendFailure(Component.literal("Страна с ID '" + id + "' не найдена."));
                    return 0;
                });
    }

    private static int joinCountry(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        try {
            Player player = source.getPlayerOrException();
            return CountryManager.INSTANCE.getCountry(id)
                    .map(country -> {
                        boolean added = country.addMember(player.getUUID());
                        if (added) {
                            source.sendSuccess(Component.literal("Ты вступил в страну '" + country.getName() + "'."), true);
                            return 1;
                        }
                        source.sendFailure(Component.literal("Ты уже член этой страны."));
                        return 0;
                    })
                    .orElseGet(() -> {
                        source.sendFailure(Component.literal("Страна с ID '" + id + "' не найдена."));
                        return 0;
                    });
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("Эта команда должна выполняться игроком."));
            return 0;
        }
    }

    private static int transferMoney(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String from = StringArgumentType.getString(context, "from");
        String to = StringArgumentType.getString(context, "to");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        if (!CountryManager.INSTANCE.transferMoney(from, to, amount)) {
            source.sendFailure(Component.literal("Не удалось перевести деньги. Проверьте страны и баланс."));
            return 0;
        }

        Component message = Component.literal("Перевод средств: '" + from + "' -> '" + to + "' : " + amount + "$");
        source.sendSuccess(Component.literal("Переведено " + amount + " из '" + from + "' в '" + to + "'."), true);
        broadcastChat(source.getServer(), message);
        return 1;
    }

    private static int addMoney(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        try {
            Player player = source.getPlayerOrException();
            if (!CountryManager.INSTANCE.addMoneyToPlayerCountry(player.getUUID(), amount)) {
                source.sendFailure(Component.literal("Не удалось добавить деньги. Ты должен быть в стране и сумма должна быть положительной."));
                return 0;
            }
            source.sendSuccess(Component.literal("К твоей стране добавлено " + amount + " денег."), true);
            return 1;
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("Эта команда должна выполняться игроком."));
            return 0;
        }
    }

    private static int removeMoney(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int amount = IntegerArgumentType.getInteger(context, "amount");
        try {
            Player player = source.getPlayerOrException();
            if (!CountryManager.INSTANCE.withdrawMoneyFromPlayerCountry(player.getUUID(), amount)) {
                source.sendFailure(Component.literal("Не удалось снять деньги. Ты должен быть в стране и сумма должна быть положительной и не превышать баланс."));
                return 0;
            }
            Component message = Component.literal("Страна '" + CountryManager.INSTANCE.getCountryByPlayer(player.getUUID()).map(Country::getName).orElse("неизвестная") + "' потратила " + amount + ".");
            source.sendSuccess(Component.literal("Из бюджета твоей страны списано " + amount + " денег."), true);
            broadcastChat(source.getServer(), message);
            return 1;
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("Эта команда должна выполняться игроком."));
            return 0;
        }
    }

    private static void broadcastChat(MinecraftServer server, Component message) {
        if (server == null) {
            return;
        }
        server.getPlayerList().broadcastMessage(message, ChatType.SYSTEM, Util.NIL_UUID);
    }

    private static int showCountryInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        return CountryManager.INSTANCE.getCountry(id)
                .map(country -> {
                    source.sendSuccess(Component.literal("Страна: " + country.getName()), false);
                    source.sendSuccess(Component.literal("ID: " + country.getId()), false);
                    source.sendSuccess(Component.literal("Цвет: " + country.getColorHex()), false);
                    source.sendSuccess(Component.literal("Баланс: " + country.getBalance()), false);
                    source.sendSuccess(Component.literal("Союзы: " + String.join(", ", country.getAllies())), false);
                    source.sendSuccess(Component.literal("Войны: " + String.join(", ", country.getEnemies())), false);
                    source.sendSuccess(Component.literal("Игроков: " + country.getMembers().size()), false);
                    return 1;
                })
                .orElseGet(() -> {
                    source.sendFailure(Component.literal("Страна с ID '" + id + "' не найдена."));
                    return 0;
                });
    }
}

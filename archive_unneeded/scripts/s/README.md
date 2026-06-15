# Папка спрайтов

Всю графику и спрайты помещайте сюда в стандартной Minecraft-структуре ресурсов.

Пример структуры:

- `s/assets/modstrany/textures/...`
- `s/assets/modstrany/models/...`
- `s/assets/modstrany/font/...`
- `s/assets/modstrany/lang/...`

Файлы из папки `s` автоматически попадут в ресурсы мода при сборке.

## Команды мода

- `/country create <id> <name>` — создать страну с уникальным ID, случайным цветом и стартовым балансом 5 000 000
- `/country join <id>` — вступить в страну
- `/country ally <id1> <id2>` — создать союз между двумя странами
- `/country war <attacker> <defender>` — объявить войну между странами
- `/country transfer <from> <to> <amount>` — перевести деньги между странами (требуются права администратора)
- `/country money add <amount>` — добавить деньги своей стране через команду
- `/country money remove <amount>` — потратить деньги из бюджета своей страны
- `/country info <id>` — показать информацию о стране, включая баланс

> Сообщения о переводах, объявлениях войны и расходах автоматически отправляются в общий чат сервера.

## Предмет для расширения границ

- Предмет: `border_expander`
- Модель: `s/assets/modstrany/models/item/border_expander.json`
- Текстура предмета: `s/assets/modstrany/textures/item/Sprite-0002.png`
- Текстура шлема: `s/assets/modstrany/textures/models/armor/border_layer_1.png`

Теперь этот предмет можно надеть на голову как шлем, чтобы видеть границы.
Пока шлем надет, внизу экрана будет отображаться строка с названием страны, количеством союзников и балансом.
Положи файл `Sprite-0002.png` с текстурой предмета в `s/assets/modstrany/textures/item/`.
Если у тебя есть `Sprite-0002.gif`, сконвертируй его в PNG и используй то же имя.

Если нужно, создай `s/assets/modstrany/textures/models/armor/border_layer_1.png` для внешнего вида надетого шлема.

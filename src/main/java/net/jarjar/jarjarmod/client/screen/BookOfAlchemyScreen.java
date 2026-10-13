
package net.jarjar.jarjarmod.client.screen;

import net.jarjar.jarjarmod.client.AlchemyArchiveClientCache;
import net.jarjar.jarjarmod.client.AlchemyClientPackets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BookOfAlchemyScreen extends Screen {
    private static final Identifier BOOK_TEXTURE =
            new Identifier("jarjarmod", "textures/gui/book_of_alchemy.png");
    private static final Identifier PROGRESS_TEXTURE =
            new Identifier("jarjarmod", "textures/gui/progress_bar.png");

    private static final int SCALE = 6;
    private static final int TEXTURE_SIZE = 256 * SCALE / 4;
    private static final int BOOK_WIDTH = 256 * SCALE / 4;
    private static final int BOOK_HEIGHT = 151 * SCALE / 4;

    private static final int ARROW_U = 83 * SCALE / 4;
    private static final int ARROW_V = 48 * SCALE / 4;
    private static final int ARROW_WIDTH = 9 * SCALE / 4;
    private static final int ARROW_HEIGHT = 30 * SCALE / 4;

    private int bookX;
    private int bookY;

    private TextFieldWidget searchField;
    private String selectedItemId;
    private List<String> visibleItems = new ArrayList<>();

    public BookOfAlchemyScreen() {
        super(Text.translatable("screen.jarjarmod.alchemy.title"));
    }

    @Override
    protected void init() {
        bookX = (width - BOOK_WIDTH) / 2;
        bookY = (height - BOOK_HEIGHT) / 2;

        searchField = new TextFieldWidget(
                textRenderer,
                bookX + 169,
                bookY + 30,
                122,
                13,
                Text.translatable("screen.jarjarmod.alchemy.search")
        );
        searchField.setMaxLength(64);
        searchField.setChangedListener(value -> refreshVisibleItems());
        addSelectableChild(searchField);

        addDrawableChild(ButtonWidget.builder(
                Text.literal("+1"),
                button -> sendDeposit(1)
        ).dimensions(bookX + 169, bookY + 151, 28, 14).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("+16"),
                button -> sendDeposit(16)
        ).dimensions(bookX + 199, bookY + 151, 32, 14).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("-1"),
                button -> sendWithdraw(1)
        ).dimensions(bookX + 234, bookY + 151, 28, 14).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("-16"),
                button -> sendWithdraw(16)
        ).dimensions(bookX + 264, bookY + 151, 32, 14).build());

        refreshVisibleItems();
    }

    private void refreshVisibleItems() {
        String query = searchField == null
                ? ""
                : searchField.getText().toLowerCase(Locale.ROOT);

        visibleItems = AlchemyArchiveClientCache.getAllowedItems().stream()
                .filter(id -> {
                    Item item = getItem(id);
                    if (item == null) return false;

                    String itemName = item.getName().getString()
                            .toLowerCase(Locale.ROOT);
                    return id.toLowerCase(Locale.ROOT).contains(query)
                            || itemName.contains(query);
                })
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private Item getItem(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) return null;
        return Registries.ITEM.get(id);
    }

    private void sendDeposit(int amount) {
        if (selectedItemId != null) {
            AlchemyClientPackets.requestDeposit(selectedItemId, amount);
        }
    }

    private void sendWithdraw(int amount) {
        if (selectedItemId != null) {
            AlchemyClientPackets.requestWithdraw(selectedItemId, amount);
        }
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderBackground(context);

        context.drawTexture(
                BOOK_TEXTURE,
                bookX,
                bookY,
                0.0F,
                0.0F,
                BOOK_WIDTH,
                BOOK_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        // Static preview only; real brewing progress comes in a later stage.
        context.drawTexture(
                PROGRESS_TEXTURE,
                bookX + ARROW_U,
                bookY + ARROW_V,
                (float) ARROW_U,
                (float) ARROW_V,
                ARROW_WIDTH,
                ARROW_HEIGHT,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        searchField.render(context, mouseX, mouseY, delta);

        int columns = 4;
        int slotStep = 25;
        int gridX = bookX + 169;
        int gridY = bookY + 49;
        int slotsToRender = Math.min(visibleItems.size(), 16);

        for (int i = 0; i < slotsToRender; i++) {
            int x = gridX + (i % columns) * slotStep;
            int y = gridY + (i / columns) * slotStep;
            String itemId = visibleItems.get(i);
            Item item = getItem(itemId);
            if (item == null) continue;

            boolean selected = itemId.equals(selectedItemId);
            int slotColor = selected ? 0xFFFFD45A : 0xFF72583A;
            context.fill(x, y, x + 19, y + 19, slotColor);
            context.fill(x + 1, y + 1, x + 18, y + 18, 0xFFDED0AA);
            context.drawItem(new ItemStack(item), x + 2, y + 2);

            int stored = AlchemyArchiveClientCache.getStored(itemId);
            if (stored > 0) {
                context.drawItemInSlot(
                        textRenderer,
                        new ItemStack(item, Math.min(stored, 64)),
                        x + 2,
                        y + 2,
                        stored > 64 ? Integer.toString(stored) : null
                );
            }
        }

        String storedText = selectedItemId == null
                ? "Select an ingredient"
                : getSelectedName() + ": "
                + AlchemyArchiveClientCache.getStored(selectedItemId)
                + "/1024";

        context.drawText(
                textRenderer,
                Text.literal(storedText),
                bookX + 169,
                bookY + 132,
                0xFF4A2E1B,
                false
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private String getSelectedName() {
        Item item = selectedItemId == null ? null : getItem(selectedItemId);
        return item == null ? "" : item.getName().getString();
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int gridX = bookX + 169;
        int gridY = bookY + 49;
        int slotStep = 25;

        for (int i = 0; i < Math.min(visibleItems.size(), 16); i++) {
            int x = gridX + (i % 4) * slotStep;
            int y = gridY + (i / 4) * slotStep;

            if (mouseX >= x && mouseX < x + 19
                    && mouseY >= y && mouseY < y + 19) {
                selectedItemId = visibleItems.get(i);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

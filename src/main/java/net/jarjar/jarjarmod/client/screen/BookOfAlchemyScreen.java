
package net.jarjar.jarjarmod.client.screen;

import net.jarjar.jarjarmod.client.AlchemyArchiveClientCache;
import net.jarjar.jarjarmod.client.AlchemyClientPackets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

public class BookOfAlchemyScreen extends Screen {
    private static final Identifier BOOK_TEXTURE =
            new Identifier("jarjarmod", "textures/gui/book_of_alchemy.png");

    private static final Identifier PROGRESS_TEXTURE =
            new Identifier("jarjarmod", "textures/gui/progress_bar.png");

    private static final int SCALE = 6;
    private static final int TEXTURE_SIZE = 256 * SCALE / 4;
    private static final int BOOK_WIDTH = 243 * SCALE / 4;
    private static final int BOOK_HEIGHT = 151 * SCALE / 4;

    private static final int ARROW_U = 83;
    private static final int ARROW_V = 48;
    private static final int ARROW_WIDTH = 9 * SCALE / 4;
    private static final int ARROW_HEIGHT = 30 * SCALE / 4;

    private static final int ROW_HEIGHT = 27;
    private static final int ICON_SIZE = 16;

    private int bookX;
    private int bookY;

    private int listX;
    private int listY;
    private int listWidth;
    private int listHeight;

    private TextFieldWidget searchField;
    private final List<String> filteredItems = new ArrayList<>();
    private String selectedItemId;
    private int scrollOffset;

    public BookOfAlchemyScreen() {
        super(Text.translatable("screen.jarjarmod.alchemy.title"));
    }

    @Override
    protected void init() {
        bookX = (width - BOOK_WIDTH) / 2;
        bookY = (height - BOOK_HEIGHT) / 2;

        int rightPageX = bookX + 169;
        int rightPageWidth = 128;

        // Search field sits beneath the title printed on the texture.
        searchField = new TextFieldWidget(
                textRenderer,
                rightPageX + 35,
                bookY + 55,
                rightPageWidth - 4,
                13,
                Text.translatable("screen.jarjarmod.alchemy.search")
        );
        searchField.setMaxLength(64);
        searchField.setChangedListener(value -> {
            scrollOffset = 0;
            refreshFilteredItems();
        });
        addSelectableChild(searchField);

        listX = rightPageX + 36;
        listY = bookY + 73;
        listWidth = rightPageWidth - 6;

        // Leave space at the bottom for the selection summary and buttons.
        listHeight = BOOK_HEIGHT - (listY - bookY) - 62;

        int buttonY = bookY + BOOK_HEIGHT - 67;
        int buttonWidth = 34;
        int buttonGap = 4;
        int totalWidth = buttonWidth * 3 + buttonGap * 2;
        int buttonX = rightPageX + 40;

        addDrawableChild(new DualClickButton(
                buttonX, buttonY, buttonWidth, 18,
                "1x",
                ignored -> deposit(1),
                ignored -> withdraw(1)
        ));

        addDrawableChild(new DualClickButton(
                buttonX + buttonWidth + buttonGap,
                buttonY, buttonWidth, 18,
                "16x",
                ignored -> deposit(16),
                ignored -> withdraw(16)
        ));

        addDrawableChild(new DualClickButton(
                buttonX + (buttonWidth + buttonGap) * 2,
                buttonY, buttonWidth, 18,
                "All",
                ignored -> depositAll(),
                ignored -> { }
        ));

        refreshFilteredItems();
    }

    private void refreshFilteredItems() {
        String query = searchField == null
                ? ""
                : searchField.getText().toLowerCase(Locale.ROOT);

        filteredItems.clear();

        AlchemyArchiveClientCache.getAllowedItems().stream()
                .filter(id -> {
                    Item item = getItem(id);
                    if (item == null) {
                        return false;
                    }

                    String name = item.getName().getString()
                            .toLowerCase(Locale.ROOT);

                    return name.contains(query)
                            || id.toLowerCase(Locale.ROOT).contains(query);
                })
                .sorted(Comparator.comparing(
                        id -> getItem(id).getName().getString(),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .forEach(filteredItems::add);

        int visibleRows = Math.max(1, listHeight / ROW_HEIGHT);
        int maxOffset = Math.max(0, filteredItems.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));

        if (selectedItemId != null
                && !AlchemyArchiveClientCache.getAllowedItems()
                .contains(selectedItemId)) {
            selectedItemId = null;
        }
    }

    private Item getItem(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            return null;
        }

        Item item = Registries.ITEM.get(id);
        return item == net.minecraft.item.Items.AIR ? null : item;
    }

    private void deposit(int amount) {
        if (selectedItemId != null) {
            AlchemyClientPackets.requestDeposit(selectedItemId, amount);
        }
    }

    private void withdraw(int amount) {
        if (selectedItemId != null) {
            AlchemyClientPackets.requestWithdraw(selectedItemId, amount);
        }
    }

    private void depositAll() {
        if (selectedItemId != null) {
            AlchemyClientPackets.requestDepositAll(selectedItemId);
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
                bookX, bookY,
                0, 0,
                BOOK_WIDTH, BOOK_HEIGHT,
                TEXTURE_SIZE, TEXTURE_SIZE
        );

        // Temporary static preview; brewing progress will replace this later.
        context.drawTexture(
                PROGRESS_TEXTURE,
                bookX + (int) (ARROW_U * SCALE / 4.0),
                bookY + (int) (ARROW_V * SCALE / 4.0),
                ARROW_U, ARROW_V,
                ARROW_WIDTH, ARROW_HEIGHT,
                TEXTURE_SIZE, TEXTURE_SIZE
        );

        searchField.render(context, mouseX, mouseY, delta);

        int visibleRows = Math.max(1, listHeight / ROW_HEIGHT);
        int end = Math.min(filteredItems.size(), scrollOffset + visibleRows);

        context.enableScissor(
                listX, listY,
                listX + listWidth, listY + listHeight
        );

        for (int index = scrollOffset; index < end; index++) {
            int row = index - scrollOffset;
            int x = listX;
            int y = listY + row * ROW_HEIGHT;

            String itemId = filteredItems.get(index);
            Item item = getItem(itemId);
            if (item == null) {
                continue;
            }

            boolean selected = itemId.equals(selectedItemId);
            boolean hovered = mouseX >= x && mouseX < x + listWidth
                    && mouseY >= y && mouseY < y + ROW_HEIGHT;

            if (selected) {
                context.fill(
                        x, y, x + listWidth, y + ROW_HEIGHT - 1,
                        0xAAE5C875
                );
            } else if (hovered) {
                context.fill(
                        x, y, x + listWidth, y + ROW_HEIGHT - 1,
                        0x557A5934
                );
            }

            // Item icon on the left.
            context.drawItem(new ItemStack(item), x + 1, y + 4);

            // Name beside the icon.
            String itemName = item.getName().getString();
            int textX = x + ICON_SIZE + 5;
            int maxTextWidth = listWidth - (textX - x) - 2;

            context.drawText(
                    textRenderer,
                    Text.literal(textRenderer.trimToWidth(itemName, maxTextWidth)),
                    textX,
                    y + 2,
                    0xFF382313,
                    false
            );

            // Stored quantity underneath the name.
            int stored = AlchemyArchiveClientCache.getStored(itemId);
            context.drawText(
                    textRenderer,
                    Text.literal("Stored: " + stored + "/1024"),
                    textX,
                    y + 13,
                    0xFF705335,
                    false
            );

            if (hovered) {
                context.drawTooltip(
                        textRenderer,
                        List.of(item.getName(), Text.literal(itemId)),
                        mouseX, mouseY
                );
            }
        }

        context.disableScissor();

        if (filteredItems.isEmpty()) {
            context.drawText(
                    textRenderer,
                    Text.literal("No ingredients found"),
                    listX,
                    listY + 3,
                    0xFF705335,
                    false
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private String getSelectedName() {
        Item item = selectedItemId == null ? null : getItem(selectedItemId);
        return item == null ? "" : item.getName().getString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (mouseX < listX || mouseX >= listX + listWidth
                || mouseY < listY || mouseY >= listY + listHeight) {
            return false;
        }

        int row = (int) ((mouseY - listY) / ROW_HEIGHT);
        int index = scrollOffset + row;

        if (index >= 0 && index < filteredItems.size()) {
            selectedItemId = filteredItems.get(index);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX < listX || mouseX >= listX + listWidth
                || mouseY < listY || mouseY >= listY + listHeight) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        int visibleRows = Math.max(1, listHeight / ROW_HEIGHT);
        int maxOffset = Math.max(0, filteredItems.size() - visibleRows);

        scrollOffset -= (int) Math.signum(amount);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class DualClickButton extends ButtonWidget {
        private final IntConsumer leftAction;
        private final IntConsumer rightAction;

        protected DualClickButton(
                int x,
                int y,
                int width,
                int height,
                String label,
                IntConsumer leftAction,
                IntConsumer rightAction
        ) {
            super(
                    x, y, width, height,
                    Text.literal(label),
                    button -> leftAction.accept(0),
                    ButtonWidget.DEFAULT_NARRATION_SUPPLIER
            );
            this.leftAction = leftAction;
            this.rightAction = rightAction;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (active && visible && isHovered()
                    && button == 1) {
                rightAction.accept(1);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void renderButton(
                DrawContext context,
                int mouseX,
                int mouseY,
                float delta
        ) {
            super.renderButton(context, mouseX, mouseY, delta);
        }
    }
}

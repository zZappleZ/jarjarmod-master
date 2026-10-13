
package net.jarjar.jarjarmod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BookOfAlchemyScreen extends Screen {

    private static final Identifier BOOK_TEXTURE =
            new Identifier(
                    "jarjarmod",
                    "textures/gui/book_of_alchemy.png"
            );

    private static final Identifier PROGRESS_TEXTURE =
            new Identifier(
                    "jarjarmod",
                    "textures/gui/progress_bar.png"
            );

    private static final double scale = 1.25;

    private static final int TEXTURE_SIZE = (int) Math.round(256*scale);
    private static final int BOOK_WIDTH = (int) Math.round(256*scale);
    private static final int BOOK_HEIGHT = (int) Math.round(151*scale);

    // The visible arrow occupies this source rectangle in progress_bar.png.
    private static final int ARROW_U = (int) Math.round(83*scale);
    private static final int ARROW_V = (int) Math.round(48*scale);
    private static final int ARROW_WIDTH = (int) Math.round(9*scale);
    private static final int ARROW_HEIGHT = (int) Math.round(30*scale);

    private int bookX;
    private int bookY;

    public BookOfAlchemyScreen() {
        super(Text.translatable("screen.jarjarmod.alchemy.title"));
    }

    @Override
    protected void init() {
        bookX = (width - BOOK_WIDTH) / 2;
        bookY = (height - BOOK_HEIGHT) / 2;
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderBackground(context);

        // Draw the original artwork without scaling or recoloring it.
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

        // Render the arrow at the coordinates used in its source canvas.
        // This is a static preview until the brewing engine is connected.
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

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

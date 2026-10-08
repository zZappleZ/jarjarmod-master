package net.jarjar.jarjarmod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ArchivistMenuScreen extends Screen {

    public ArchivistMenuScreen() {
        super(Text.literal("The Archivist"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 150;
        int buttonHeight = 20;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // First button
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Button 1"),
                        button -> {
                            System.out.println("Button 1 clicked!");
                        }
                ).dimensions(
                        centerX - buttonWidth / 2,
                        centerY - 30,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        // Second button
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Button 2"),
                        button -> {
                            System.out.println("Button 2 clicked!");
                        }
                ).dimensions(
                        centerX - buttonWidth / 2,
                        centerY,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        // Close button
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Close"),
                        button -> {
                            this.close();
                        }
                ).dimensions(
                        centerX - buttonWidth / 2,
                        centerY + 30,
                        buttonWidth,
                        buttonHeight
                ).build()
        );
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Draw the default darkened background.
        this.renderBackground(context);

        // Draw the screen title.
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                40,
                0xFFFFFF
        );

        // Draw all buttons and other children.
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

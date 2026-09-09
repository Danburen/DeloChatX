package org.nitrowater.velochatx.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.time.Duration;

public class TitleShow implements Title {
    private static String mainTitleText;
    private static String subTitleText;
    private static int[] time;
    public TitleShow(String main, String sub, int[] time){
        mainTitleText = main;
        subTitleText = sub;
        TitleShow.time = time;
    }
    @Override
    public @NotNull Component title() {
        return Component.text(mainTitleText);
    }

    @Override
    public @NotNull Component subtitle() {
        return Component.text(subTitleText);
    }

    @Override
    public @Nullable Times times() {
        return Times.times(Duration.ofSeconds(time[0]),Duration.ofSeconds(time[1]),Duration.ofSeconds(time[2]));
    }

    @Override
    public <T> @UnknownNullability T part(@NotNull TitlePart<T> part) {
        return null;
    }
}

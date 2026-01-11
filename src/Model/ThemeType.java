package Model;

public enum ThemeType {

    FOREST(new ThemeAssets(
            "assets/forest/bg.png",
            "assets/forest/tile_grass.png",
            "assets/forest/tile_grass_hover.png",
            "assets/forest/tile_brown.png",
            "assets/forest/icon_flag.png",
            "assets/forest/icon_mine.png",
            "assets/forest/icon_question.png",
            "assets/forest/icon_surprise.png",
            "assets/forest/heart_full.png",
            "assets/forest/heart_empty.png",
            "assets/forest/refresh.png",
            "assets/forest/music.wav"   // 🎵
    )),

    ICE(new ThemeAssets(
            "assets/ice/bg.png",
            "assets/ice/tile_ice.png",
            "assets/ice/tile_ice_hover.png",
            "assets/ice/tile_frost.png",
            "assets/ice/icon_flag.png",
            "assets/ice/icon_mine.png",
            "assets/ice/icon_question.png",
            "assets/ice/icon_surprise.png",
            "assets/ice/heart_full.png",
            "assets/ice/heart_empty.png",
            "assets/ice/refresh.png",
            "assets/ice/music.wav"          // 🎵
    )),

    LAVA(new ThemeAssets(
            "assets/lava/bg.png",
            "assets/lava/tile_lava.png",
            "assets/lava/tile_lava_hover.png",
            "assets/lava/tile_rock.png",
            "assets/lava/icon_flag.png",
            "assets/lava/icon_mine.png",
            "assets/lava/icon_question.png",
            "assets/lava/icon_surprise.png",
            "assets/lava/heart_full.png",
            "assets/lava/heart_empty.png",
            "assets/lava/refresh.png",
            "assets/lava/music.wav"        // 🎵
    )),

    BEACH(new ThemeAssets(
            "assets/beach/bg.png",
            "assets/beach/tile_sand.png",
            "assets/beach/tile_sand_hover.png",
            "assets/beach/tile_water.png",
            "assets/beach/icon_flag.png",
            "assets/beach/icon_mine.png",
            "assets/beach/icon_question.png",
            "assets/beach/icon_surprise.png",
            "assets/beach/heart_full.png",
            "assets/beach/heart_empty.png",
            "assets/beach/refresh.png",
            "assets/beach/music.wav"      // 🎵
    )),

    SPACE(new ThemeAssets(
            "assets/space/bg.png",
            "assets/space/tile_space.png",
            "assets/space/tile_space_hover.png",
            "assets/space/tile_dark.png",
            "assets/space/icon_flag.png",
            "assets/space/icon_mine.png",
            "assets/space/icon_question.png",
            "assets/space/icon_surprise.png",
            "assets/space/heart_full.png",
            "assets/space/heart_empty.png",
            "assets/space/refresh.png",
            "assets/space/music.wav"      // 🎵
    ));

    public final ThemeAssets assets;

    ThemeType(ThemeAssets assets) {
        this.assets = assets;
    }
    @Override
    public String toString() {
        return SysData.getI18n().t("theme." + name().toLowerCase());
    }
}

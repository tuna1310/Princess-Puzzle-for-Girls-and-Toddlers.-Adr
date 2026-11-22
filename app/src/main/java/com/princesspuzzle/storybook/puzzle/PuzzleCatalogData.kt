@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.puzzle

import com.princesspuzzle.storybook.R

internal object PuzzleCatalogData {
    fun buildAllLevels(): List<LevelDefinition> =
        buildStarterLevels() + buildPrincessLevels() + buildFantasyLevels() + buildRoyalFinaleLevels()

    private fun part(
        prefix: String,
        index: Int,
        name: String,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        zIndex: Int
    ): PuzzlePieceDefinition = PuzzlePieceDefinition(
        id = "${prefix}_p$index",
        partIndex = index,
        name = name,
        normalizedCenterX = cx,
        normalizedCenterY = cy,
        normalizedWidth = w,
        normalizedHeight = h,
        zIndex = zIndex
    )

    private fun buildStarterLevels(): List<LevelDefinition> = listOf(
        LevelDefinition(
            id = 1,
            slugId = "castle_pink",
            index = 0,
            titleRes = R.string.level_1_title,
            subtitleRes = R.string.level_1_sub,
            free = true,
            theme = SceneThemeId.PINK_CASTLE,
            difficulty = PuzzleDifficulty.EASY,
            pieces = listOf(
                part("castle_pink", 0, "Royal Sun & Butterfly Crest", 0.50f, 0.14f, 0.58f, 0.22f, 1),
                part("castle_pink", 1, "Central Spire & Flag", 0.50f, 0.34f, 0.30f, 0.34f, 3),
                part("castle_pink", 2, "Left Castle Tower", 0.25f, 0.56f, 0.26f, 0.52f, 2),
                part("castle_pink", 3, "Right Castle Tower", 0.75f, 0.56f, 0.26f, 0.52f, 2),
                part("castle_pink", 4, "Main Keep & Royal Gate", 0.50f, 0.70f, 0.44f, 0.42f, 4)
            )
        ),
        LevelDefinition(
            id = 2,
            slugId = "princess_brunette",
            index = 1,
            titleRes = R.string.level_2_title,
            subtitleRes = R.string.level_2_sub,
            free = true,
            theme = SceneThemeId.BRUNETTE_PRINCESS,
            difficulty = PuzzleDifficulty.EASY,
            pieces = listOf(
                part("princess_brunette", 0, "Golden Crown & Hair Flower", 0.50f, 0.12f, 0.36f, 0.18f, 5),
                part("princess_brunette", 1, "Chestnut Hair & Smiling Face", 0.48f, 0.28f, 0.52f, 0.32f, 3),
                part("princess_brunette", 2, "Outstretched Princess Arms", 0.48f, 0.48f, 0.72f, 0.22f, 2),
                part("princess_brunette", 3, "Pink Crown-Patterned Dress", 0.46f, 0.55f, 0.44f, 0.36f, 4),
                part("princess_brunette", 4, "Pink Handbag & Pencils", 0.79f, 0.52f, 0.28f, 0.28f, 6),
                part("princess_brunette", 5, "Princess Legs & Slippers", 0.50f, 0.83f, 0.42f, 0.28f, 1)
            )
        ),
        LevelDefinition(
            id = 3,
            slugId = "royal_pony",
            index = 2,
            titleRes = R.string.level_3_title,
            subtitleRes = R.string.level_3_sub,
            free = true,
            theme = SceneThemeId.ROYAL_PONY,
            difficulty = PuzzleDifficulty.EASY,
            pieces = listOf(
                part("royal_pony", 0, "Pony Head, Mane & Flower", 0.70f, 0.24f, 0.46f, 0.38f, 5),
                part("royal_pony", 1, "Front Chest & Forelegs", 0.66f, 0.58f, 0.30f, 0.46f, 4),
                part("royal_pony", 2, "Turquoise Saddle & Stirrup", 0.49f, 0.47f, 0.26f, 0.34f, 6),
                part("royal_pony", 3, "Pony Back & Rump", 0.38f, 0.44f, 0.32f, 0.28f, 3),
                part("royal_pony", 4, "Pink Hind Legs & Hooves", 0.36f, 0.70f, 0.28f, 0.42f, 2),
                part("royal_pony", 5, "Flowing Hot-Pink Tail", 0.16f, 0.52f, 0.26f, 0.42f, 1)
            )
        ),
        LevelDefinition(
            id = 4,
            slugId = "cherry_icecream",
            index = 3,
            titleRes = R.string.level_4_title,
            subtitleRes = R.string.level_4_sub,
            free = true,
            theme = SceneThemeId.CHERRY_ICECREAM,
            difficulty = PuzzleDifficulty.EASY,
            pieces = listOf(
                part("cherry_icecream", 0, "Twin Red Cherries & Stems", 0.56f, 0.17f, 0.40f, 0.24f, 6),
                part("cherry_icecream", 1, "Pink & Cream Swirled Scoop", 0.50f, 0.32f, 0.44f, 0.34f, 4),
                part("cherry_icecream", 2, "Left Mint Leaf Fan", 0.27f, 0.34f, 0.26f, 0.26f, 2),
                part("cherry_icecream", 3, "Right Mint Leaf Fan", 0.73f, 0.36f, 0.26f, 0.26f, 2),
                part("cherry_icecream", 4, "Upper Waffle Cone Wrapper", 0.48f, 0.58f, 0.28f, 0.26f, 3),
                part("cherry_icecream", 5, "Lower Waffle Cone Tip", 0.46f, 0.80f, 0.20f, 0.28f, 1)
            )
        )
    )

    private fun buildPrincessLevels(): List<LevelDefinition> = listOf(
        LevelDefinition(
            id = 5,
            slugId = "princess_blonde",
            index = 4,
            titleRes = R.string.level_5_title,
            subtitleRes = R.string.level_5_sub,
            free = false,
            theme = SceneThemeId.BLONDE_PRINCESS,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("princess_blonde", 0, "Pink Jeweled Crown", 0.52f, 0.11f, 0.28f, 0.16f, 6),
                part("princess_blonde", 1, "Golden Blonde Hair & Face", 0.50f, 0.26f, 0.42f, 0.26f, 4),
                part("princess_blonde", 2, "Striped Bodice & Arms", 0.50f, 0.43f, 0.72f, 0.20f, 3),
                part("princess_blonde", 3, "Blue Waist Flower Belt", 0.52f, 0.49f, 0.22f, 0.15f, 7),
                part("princess_blonde", 4, "Hot-Pink Wavy Overskirt", 0.49f, 0.62f, 0.62f, 0.24f, 5),
                part("princess_blonde", 5, "Light-Pink Underskirt Ruffle", 0.49f, 0.68f, 0.64f, 0.20f, 2),
                part("princess_blonde", 6, "Legs & Blue Flower Shoes", 0.51f, 0.84f, 0.42f, 0.24f, 1)
            )
        ),
        LevelDefinition(
            id = 6,
            slugId = "princess_flower",
            index = 5,
            titleRes = R.string.level_6_title,
            subtitleRes = R.string.level_6_sub,
            free = false,
            theme = SceneThemeId.FLOWER_PRINCESS,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("princess_flower", 0, "Pearl Royal Tiara", 0.47f, 0.11f, 0.28f, 0.14f, 6),
                part("princess_flower", 1, "Wavy Golden Hair", 0.47f, 0.26f, 0.48f, 0.30f, 2),
                part("princess_flower", 2, "Smiling Princess Face", 0.47f, 0.25f, 0.26f, 0.22f, 5),
                part("princess_flower", 3, "Daisy-Belted Bodice", 0.47f, 0.43f, 0.26f, 0.18f, 4),
                part("princess_flower", 4, "Left Magenta Skirt Panel", 0.38f, 0.62f, 0.28f, 0.28f, 3),
                part("princess_flower", 5, "Right Magenta Skirt Panel", 0.56f, 0.62f, 0.28f, 0.28f, 3),
                part("princess_flower", 6, "Right Arm & Flower Basket", 0.72f, 0.48f, 0.34f, 0.26f, 7),
                part("princess_flower", 7, "Left Arm & Dancing Slippers", 0.44f, 0.78f, 0.48f, 0.34f, 1)
            )
        ),
        LevelDefinition(
            id = 7,
            slugId = "royal_kitty",
            index = 6,
            titleRes = R.string.level_7_title,
            subtitleRes = R.string.level_7_sub,
            free = false,
            theme = SceneThemeId.ROYAL_KITTY,
            difficulty = PuzzleDifficulty.EASY,
            pieces = listOf(
                part("royal_kitty", 0, "Golden Kitty Crown", 0.50f, 0.15f, 0.30f, 0.16f, 5),
                part("royal_kitty", 1, "White Kitty Head & Whiskers", 0.50f, 0.33f, 0.44f, 0.28f, 4),
                part("royal_kitty", 2, "White Fairy Gown & Daisy", 0.50f, 0.60f, 0.46f, 0.34f, 3),
                part("royal_kitty", 3, "Pink Royal Kitty Purse", 0.77f, 0.56f, 0.24f, 0.24f, 6),
                part("royal_kitty", 4, "Fluffy White Tail & Paws", 0.46f, 0.82f, 0.50f, 0.24f, 1)
            )
        ),
        LevelDefinition(
            id = 8,
            slugId = "friendly_dragon",
            index = 7,
            titleRes = R.string.level_8_title,
            subtitleRes = R.string.level_8_sub,
            free = false,
            theme = SceneThemeId.FRIENDLY_DRAGON,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("friendly_dragon", 0, "Smiling Dragon Head & Horns", 0.28f, 0.26f, 0.34f, 0.26f, 6),
                part("friendly_dragon", 1, "Left Orange Dragon Wing", 0.36f, 0.40f, 0.34f, 0.26f, 2),
                part("friendly_dragon", 2, "Right Orange Dragon Wing", 0.68f, 0.34f, 0.36f, 0.28f, 2),
                part("friendly_dragon", 3, "Scaled Golden Dragon Body", 0.48f, 0.52f, 0.38f, 0.32f, 4),
                part("friendly_dragon", 4, "Curled Spiral Dragon Tail", 0.76f, 0.62f, 0.34f, 0.34f, 3),
                part("friendly_dragon", 5, "Front Dragon Claws", 0.34f, 0.74f, 0.26f, 0.24f, 5),
                part("friendly_dragon", 6, "Hind Dragon Legs", 0.58f, 0.78f, 0.28f, 0.24f, 1)
            )
        )
    )

    private fun buildFantasyLevels(): List<LevelDefinition> = listOf(
        LevelDefinition(
            id = 9,
            slugId = "rainbow_palace",
            index = 8,
            titleRes = R.string.level_9_title,
            subtitleRes = R.string.level_9_sub,
            free = false,
            theme = SceneThemeId.RAINBOW_PALACE,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("rainbow_palace", 0, "Golden Butterfly & Banner", 0.50f, 0.13f, 0.48f, 0.18f, 7),
                part("rainbow_palace", 1, "Left Crimson Spire & Flag", 0.24f, 0.30f, 0.22f, 0.26f, 5),
                part("rainbow_palace", 2, "Center Golden Spire & Flag", 0.50f, 0.27f, 0.24f, 0.28f, 6),
                part("rainbow_palace", 3, "Right Crimson Spire & Flag", 0.76f, 0.30f, 0.22f, 0.26f, 5),
                part("rainbow_palace", 4, "Central Turquoise Tower", 0.50f, 0.56f, 0.32f, 0.42f, 4),
                part("rainbow_palace", 5, "Left Patterned Gold Wing", 0.23f, 0.62f, 0.28f, 0.36f, 2),
                part("rainbow_palace", 6, "Right Patterned Gold Wing", 0.77f, 0.62f, 0.28f, 0.36f, 2),
                part("rainbow_palace", 7, "Crimson Striped Royal Gate", 0.50f, 0.76f, 0.34f, 0.26f, 8)
            )
        ),
        LevelDefinition(
            id = 10,
            slugId = "butterfly_tree",
            index = 9,
            titleRes = R.string.level_10_title,
            subtitleRes = R.string.level_10_sub,
            free = false,
            theme = SceneThemeId.BUTTERFLY_TREE,
            difficulty = PuzzleDifficulty.EASY,
            pieces = listOf(
                part("butterfly_tree", 0, "Pink Royal Butterfly", 0.66f, 0.16f, 0.30f, 0.22f, 6),
                part("butterfly_tree", 1, "Center Spiral Green Canopy", 0.50f, 0.33f, 0.34f, 0.30f, 4),
                part("butterfly_tree", 2, "Left Spiral Green Canopy", 0.24f, 0.42f, 0.30f, 0.28f, 3),
                part("butterfly_tree", 3, "Right Spiral Green Canopy", 0.76f, 0.42f, 0.30f, 0.28f, 3),
                part("butterfly_tree", 4, "Red Fairy Tree Trunk", 0.50f, 0.64f, 0.34f, 0.38f, 2),
                part("butterfly_tree", 5, "Meadow Blossom Base", 0.50f, 0.84f, 0.56f, 0.20f, 5)
            )
        ),
        LevelDefinition(
            id = 11,
            slugId = "pumpkin_carriage",
            index = 10,
            titleRes = R.string.level_11_title,
            subtitleRes = R.string.level_11_sub,
            free = false,
            theme = SceneThemeId.PUMPKIN_CARRIAGE,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("pumpkin_carriage", 0, "Golden Crown Coach Roof", 0.50f, 0.18f, 0.36f, 0.20f, 5),
                part("pumpkin_carriage", 1, "Pink Pumpkin Coach Body", 0.50f, 0.46f, 0.58f, 0.38f, 3),
                part("pumpkin_carriage", 2, "Royal Window & Curtains", 0.50f, 0.44f, 0.28f, 0.22f, 6),
                part("pumpkin_carriage", 3, "Coachman Golden Lantern", 0.80f, 0.36f, 0.20f, 0.22f, 7),
                part("pumpkin_carriage", 4, "Velvet Entry Steps", 0.50f, 0.70f, 0.24f, 0.18f, 4),
                part("pumpkin_carriage", 5, "Left Golden Spoke Wheel", 0.26f, 0.76f, 0.30f, 0.30f, 2),
                part("pumpkin_carriage", 6, "Right Golden Spoke Wheel", 0.74f, 0.76f, 0.30f, 0.30f, 2)
            )
        ),
        LevelDefinition(
            id = 12,
            slugId = "meadow_unicorn",
            index = 11,
            titleRes = R.string.level_12_title,
            subtitleRes = R.string.level_12_sub,
            free = false,
            theme = SceneThemeId.MEADOW_UNICORN,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("meadow_unicorn", 0, "Golden Spiral Horn & Crown", 0.74f, 0.14f, 0.28f, 0.22f, 7),
                part("meadow_unicorn", 1, "Unicorn Head & Lavender Mane", 0.68f, 0.30f, 0.38f, 0.28f, 5),
                part("meadow_unicorn", 2, "Floral Neck Garland", 0.60f, 0.44f, 0.24f, 0.20f, 6),
                part("meadow_unicorn", 3, "White Body & Star Flank", 0.44f, 0.48f, 0.40f, 0.26f, 3),
                part("meadow_unicorn", 4, "Front Legs & Golden Hooves", 0.64f, 0.72f, 0.26f, 0.36f, 4),
                part("meadow_unicorn", 5, "Hind Legs & Golden Hooves", 0.36f, 0.72f, 0.26f, 0.36f, 2),
                part("meadow_unicorn", 6, "Rainbow Flowing Tail", 0.16f, 0.54f, 0.26f, 0.38f, 1)
            )
        )
    )

    private fun buildRoyalFinaleLevels(): List<LevelDefinition> = listOf(
        LevelDefinition(
            id = 13,
            slugId = "royal_treasure",
            index = 12,
            titleRes = R.string.level_13_title,
            subtitleRes = R.string.level_13_sub,
            free = false,
            theme = SceneThemeId.ROYAL_TREASURE,
            difficulty = PuzzleDifficulty.MEDIUM,
            pieces = listOf(
                part("royal_treasure", 0, "Pink Diamond Center Jewel", 0.50f, 0.18f, 0.26f, 0.22f, 6),
                part("royal_treasure", 1, "Golden Royal Tiara Base", 0.50f, 0.30f, 0.56f, 0.24f, 4),
                part("royal_treasure", 2, "Magic Star Fairy Wand", 0.28f, 0.56f, 0.34f, 0.36f, 5),
                part("royal_treasure", 3, "Ruby Heart Royal Locket", 0.72f, 0.54f, 0.30f, 0.28f, 5),
                part("royal_treasure", 4, "Left Crystal Glass Slipper", 0.35f, 0.80f, 0.30f, 0.22f, 2),
                part("royal_treasure", 5, "Right Crystal Glass Slipper", 0.66f, 0.80f, 0.30f, 0.22f, 2)
            )
        ),
        LevelDefinition(
            id = 14,
            slugId = "mermaid_princess",
            index = 13,
            titleRes = R.string.level_14_title,
            subtitleRes = R.string.level_14_sub,
            free = false,
            theme = SceneThemeId.MERMAID_PRINCESS,
            difficulty = PuzzleDifficulty.HARD,
            pieces = listOf(
                part("mermaid_princess", 0, "Golden Seashell Tiara", 0.50f, 0.11f, 0.28f, 0.15f, 7),
                part("mermaid_princess", 1, "Flowing Coral Hair", 0.50f, 0.26f, 0.52f, 0.30f, 2),
                part("mermaid_princess", 2, "Smiling Mermaid Face", 0.50f, 0.25f, 0.28f, 0.22f, 6),
                part("mermaid_princess", 3, "Pearl Seashell Bodice", 0.50f, 0.43f, 0.32f, 0.18f, 5),
                part("mermaid_princess", 4, "Waving Mermaid Arms", 0.50f, 0.43f, 0.68f, 0.20f, 3),
                part("mermaid_princess", 5, "Upper Turquoise Scaled Tail", 0.48f, 0.62f, 0.34f, 0.26f, 4),
                part("mermaid_princess", 6, "Lower Shimmering Tail Fin", 0.58f, 0.82f, 0.42f, 0.24f, 3),
                part("mermaid_princess", 7, "Pink Starfish Friend", 0.24f, 0.74f, 0.24f, 0.22f, 8)
            )
        ),
        LevelDefinition(
            id = 15,
            slugId = "rose_princess",
            index = 14,
            titleRes = R.string.level_15_title,
            subtitleRes = R.string.level_15_sub,
            free = false,
            theme = SceneThemeId.ROSE_PRINCESS,
            difficulty = PuzzleDifficulty.HARD,
            pieces = listOf(
                part("rose_princess", 0, "Golden Rose Crown", 0.50f, 0.11f, 0.28f, 0.15f, 7),
                part("rose_princess", 1, "Long Auburn Curls", 0.50f, 0.26f, 0.48f, 0.28f, 2),
                part("rose_princess", 2, "Smiling Princess Face", 0.50f, 0.25f, 0.26f, 0.22f, 6),
                part("rose_princess", 3, "Rose Corset Bodice", 0.50f, 0.43f, 0.30f, 0.20f, 5),
                part("rose_princess", 4, "Flowing Velvet Royal Cape", 0.50f, 0.52f, 0.66f, 0.36f, 1),
                part("rose_princess", 5, "Layered Rose Gown Skirt", 0.50f, 0.66f, 0.52f, 0.28f, 4),
                part("rose_princess", 6, "Blooming Rose Bouquet", 0.76f, 0.48f, 0.26f, 0.24f, 8),
                part("rose_princess", 7, "Golden Rose Slippers", 0.50f, 0.85f, 0.36f, 0.18f, 3)
            )
        ),
        LevelDefinition(
            id = 16,
            slugId = "grand_ball_princess",
            index = 15,
            titleRes = R.string.level_16_title,
            subtitleRes = R.string.level_16_sub,
            free = false,
            theme = SceneThemeId.GRAND_BALL_PRINCESS,
            difficulty = PuzzleDifficulty.HARD,
            pieces = listOf(
                part("grand_ball_princess", 0, "Diamond Star Tiara", 0.50f, 0.10f, 0.30f, 0.15f, 8),
                part("grand_ball_princess", 1, "Royal Updo Hairstyle", 0.50f, 0.24f, 0.44f, 0.24f, 3),
                part("grand_ball_princess", 2, "Princess Face & Ruby Necklace", 0.50f, 0.27f, 0.28f, 0.22f, 7),
                part("grand_ball_princess", 3, "Embroidered Gold Bodice", 0.50f, 0.43f, 0.28f, 0.18f, 6),
                part("grand_ball_princess", 4, "Left Arm & Royal Feather Fan", 0.25f, 0.44f, 0.30f, 0.22f, 9),
                part("grand_ball_princess", 5, "Right Gloved Arm & Scepter", 0.75f, 0.44f, 0.30f, 0.22f, 9),
                part("grand_ball_princess", 6, "Magenta Ballgown Peplum", 0.50f, 0.57f, 0.54f, 0.20f, 5),
                part("grand_ball_princess", 7, "Golden Layered Ballgown Skirt", 0.50f, 0.72f, 0.64f, 0.26f, 4),
                part("grand_ball_princess", 8, "Crystal Dancing Shoes", 0.50f, 0.88f, 0.38f, 0.16f, 2)
            )
        )
    )
}

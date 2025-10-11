gecko player anim.bbmodel:
    You can use this Blockbench project to make player animations. Make sure you open it in the GeckoLib Animated Model format (from the GeckoLib Animation Utils plugin).

clothes_example.jar:
    A data pack + resource pack for data-driven clothes sets, packed together as a single .jar mod for the convenience of installation. You can unpack this file like a .zip archive, and use it as a template to make new clothes sets.
    - data: Data pack files - they register the clothes sets in the mod, and optinally you can also register a new JoJo character that will be associated with specific clothes set(s), as there might be features implemented in the future which make this relevant.
    - assets: Resource pack files - clothes models and textures, item models and names.
        You can create any model in Blockbench in the GeckoLib Animated Model format, as long as it follows the model parts naming convention:
            Main model parts (head, body, right_arm, right_arm_slim, left_arm, left_arm_slim, right_leg, left_leg) must keep these names to work correctly with the player model animations.
            "slotX_" prefixes define which clothes piece each model part belongs to. slot0 - head slot, slot1 - chest slot, slot2 - legs slot, slot3 - feet slot.
    If you keep it packed as the .jar file, DO NOT FORGET to edit the neoforge.mods.toml file in the META-INF directory. Change the modId there, and optionally you can edit the mod name and description, mention yourself as the author, give credit to people who have contributed, etc.
    Also, "jojo_ripples" is the main mod's namespace, so you might want to rename the directories with this name to something else of your choice, preferrably your modId.
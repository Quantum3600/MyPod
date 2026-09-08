package com.trishit.mypod.data.theme

import androidx.compose.ui.graphics.Color

enum class ChassisStyle {
    CLAYMORPHIC_3D,
    CLASSIC_METALLIC
}

enum class ThemePreset(
    val id: String,
    val displayName: String,
    val bodyPrimary: Color,
    val bodySecondary: Color,
    val wheelColor: Color,
    val wheelButtonTextColor: Color,
    val centerButtonColor: Color,
    val centerButtonIconColor: Color,
    val bezelColor: Color = Color(0xFF101012),
    val screenBackground: Color = Color(0xFFF8F9FA),
    val chassisStyle: ChassisStyle = ChassisStyle.CLAYMORPHIC_3D
) {
    SPACE_GRAY(
        id = "space_gray",
        displayName = "Space Gray (3D Clay)",
        bodyPrimary = Color(0xFF3B3E43),
        bodySecondary = Color(0xFF232528),
        wheelColor = Color(0xFF1C1C1E),
        wheelButtonTextColor = Color(0xFFFFFFFF),
        centerButtonColor = Color(0xFF3B3E43),
        centerButtonIconColor = Color(0xFFFFFFFF),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    ),
    SILVER(
        id = "silver",
        displayName = "Silver (3D Clay)",
        bodyPrimary = Color(0xFFDCDFE3),
        bodySecondary = Color(0xFFB8BDC4),
        wheelColor = Color(0xFFF8F9FA),
        wheelButtonTextColor = Color(0xFF888888),
        centerButtonColor = Color(0xFFECEFF1),
        centerButtonIconColor = Color(0xFF666666),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    ),
    CLASSIC_SPACE_GRAY(
        id = "classic_space_gray",
        displayName = "Classic Space Gray",
        bodyPrimary = Color(0xFF3B3E43),
        bodySecondary = Color(0xFF232528),
        wheelColor = Color(0xFF1C1C1E),
        wheelButtonTextColor = Color(0xFFFFFFFF),
        centerButtonColor = Color(0xFF3B3E43),
        centerButtonIconColor = Color(0xFFFFFFFF),
        chassisStyle = ChassisStyle.CLASSIC_METALLIC
    ),
    CLASSIC_SILVER(
        id = "classic_silver",
        displayName = "Classic Silver",
        bodyPrimary = Color(0xFFDCDFE3),
        bodySecondary = Color(0xFFB8BDC4),
        wheelColor = Color(0xFFF8F9FA),
        wheelButtonTextColor = Color(0xFF888888),
        centerButtonColor = Color(0xFFECEFF1),
        centerButtonIconColor = Color(0xFF666666),
        chassisStyle = ChassisStyle.CLASSIC_METALLIC
    ),
    BLACK(
        id = "black",
        displayName = "Black (3D Clay)",
        bodyPrimary = Color(0xFF1C1C1E),
        bodySecondary = Color(0xFF0F0F10),
        wheelColor = Color(0xFF2C2C2E),
        wheelButtonTextColor = Color(0xFFFFFFFF),
        centerButtonColor = Color(0xFF3A3A3C),
        centerButtonIconColor = Color(0xFFCCCCCC),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    ),
    CLASSIC_BLACK(
        id = "classic_black",
        displayName = "Classic Black",
        bodyPrimary = Color(0xFF1C1C1E),
        bodySecondary = Color(0xFF0F0F10),
        wheelColor = Color(0xFF2C2C2E),
        wheelButtonTextColor = Color(0xFFFFFFFF),
        centerButtonColor = Color(0xFF3A3A3C),
        centerButtonIconColor = Color(0xFFCCCCCC),
        chassisStyle = ChassisStyle.CLASSIC_METALLIC
    ),
    U2_EDITION(
        id = "u2_edition",
        displayName = "U2 Special Edition",
        bodyPrimary = Color(0xFF141415),
        bodySecondary = Color(0xFF09090A),
        wheelColor = Color(0xFFD32F2F), // Iconic red wheel
        wheelButtonTextColor = Color(0xFFFFFFFF),
        centerButtonColor = Color(0xFFB71C1C), // Darker red center
        centerButtonIconColor = Color(0xFFFFFFFF),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    ),
    PINK(
        id = "pink",
        displayName = "Pink",
        bodyPrimary = Color(0xFFE91E63),
        bodySecondary = Color(0xFFB00020),
        wheelColor = Color(0xFFFCE4EC),
        wheelButtonTextColor = Color(0xFFD81B60),
        centerButtonColor = Color(0xFFF8BBD0),
        centerButtonIconColor = Color(0xFFC2185B),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    ),
    BLUE(
        id = "blue",
        displayName = "Blue",
        bodyPrimary = Color(0xFF1E88E5),
        bodySecondary = Color(0xFF1565C0),
        wheelColor = Color(0xFFE3F2FD),
        wheelButtonTextColor = Color(0xFF1976D2),
        centerButtonColor = Color(0xFFBBDEFB),
        centerButtonIconColor = Color(0xFF0D47A1),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    ),
    GREEN(
        id = "green",
        displayName = "Green",
        bodyPrimary = Color(0xFF4CAF50),
        bodySecondary = Color(0xFF2E7D32),
        wheelColor = Color(0xFFE8F5E9),
        wheelButtonTextColor = Color(0xFF388E3C),
        centerButtonColor = Color(0xFFC8E6C9),
        centerButtonIconColor = Color(0xFF1B5E20),
        chassisStyle = ChassisStyle.CLAYMORPHIC_3D
    );

    companion object {
        fun fromId(id: String): ThemePreset {
            return entries.find { it.id == id } ?: SPACE_GRAY
        }
    }
}

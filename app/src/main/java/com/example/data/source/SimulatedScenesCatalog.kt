package com.example.data.source

import com.example.data.local.GlossaryEntryEntity
import com.example.data.model.ContextDomain
import com.example.data.model.OcrTextBox
import com.example.data.model.SimulatedGameScene

object SimulatedScenesCatalog {

    val defaultGlossaryEntries: List<GlossaryEntryEntity> = listOf(
        GlossaryEntryEntity(chineseTerm = "本座", vietnameseTerm = "Bản tọa", domainCategory = "WUXIA_XIANXIA", notes = "Đại từ bậc đại năng/chưởng môn tự xưng"),
        GlossaryEntryEntity(chineseTerm = "师尊", vietnameseTerm = "Sư tôn", domainCategory = "WUXIA_XIANXIA", notes = "Đệ tử tôn xưng sư phụ"),
        GlossaryEntryEntity(chineseTerm = "道友", vietnameseTerm = "Đạo hữu", domainCategory = "WUXIA_XIANXIA", notes = "Người tu tiên gọi nhau"),
        GlossaryEntryEntity(chineseTerm = "筑基期", vietnameseTerm = "Trúc Cơ Kỳ", domainCategory = "WUXIA_XIANXIA", notes = "Cảnh giới tu tiên thứ 2"),
        GlossaryEntryEntity(chineseTerm = "元婴期", vietnameseTerm = "Nguyên Anh Kỳ", domainCategory = "WUXIA_XIANXIA", notes = "Cảnh giới đại năng tu tiên"),
        GlossaryEntryEntity(chineseTerm = "天剑诀", vietnameseTerm = "Thiên Kiếm Quyết", domainCategory = "WUXIA_XIANXIA", notes = "Bí kíp kiếm pháp trấn phái"),
        GlossaryEntryEntity(chineseTerm = "九天玄雷剑", vietnameseTerm = "Cửu Thiên Huyền Lôi Kiếm", domainCategory = "WUXIA_XIANXIA", notes = "Thần binh hệ Lôi/Hỏa"),
        GlossaryEntryEntity(chineseTerm = "上品灵石", vietnameseTerm = "Thượng phẩm Linh thạch", domainCategory = "WUXIA_XIANXIA", notes = "Đơn vị tiền tệ tu tiên cao cấp"),
        GlossaryEntryEntity(chineseTerm = "血魔老祖", vietnameseTerm = "Huyết Ma Lão Tổ", domainCategory = "WUXIA_XIANXIA", notes = "Trùm phản diện Ma giáo"),
        GlossaryEntryEntity(chineseTerm = "开拓者", vietnameseTerm = "Khai Phá Giả", domainCategory = "SCIFI_ANIME_RPG", notes = "Nhân vật chính Honkai: Star Rail"),
        GlossaryEntryEntity(chineseTerm = "星穹列车", vietnameseTerm = "Đoàn Tàu Astral", domainCategory = "SCIFI_ANIME_RPG", notes = "Tàu vũ trụ xuyên ngân hà"),
        GlossaryEntryEntity(chineseTerm = "跃迁引擎", vietnameseTerm = "Động cơ Bước Nhảy", domainCategory = "SCIFI_ANIME_RPG", notes = "Hệ thống dịch chuyển không gian"),
        GlossaryEntryEntity(chineseTerm = "星核猎手", vietnameseTerm = "Thợ Săn Stellaron", domainCategory = "SCIFI_ANIME_RPG", notes = "Tổ chức bí ẩn trong vũ trụ")
    )

    val scenes: List<SimulatedGameScene> = listOf(
        // Scene 1: Multi-Box Tiên Hiệp Dialogue + 3 Player Choices
        SimulatedGameScene(
            id = "scene_wuxia_hall",
            title = "1. Tiên Hiệp: Thanh Vân Đại Điện",
            subtitle = "Multi-Box Batching [1..4] + Ngữ cảnh Sư tôn / Bản tọa",
            domain = ContextDomain.WUXIA_XIANXIA,
            backgroundGradientStart = 0xFF0A192FL,
            backgroundGradientEnd = 0xFF064E3BL,
            accentColor = 0xFF10B981L,
            description = "Mô phỏng 4 khung hội thoại xuất hiện cùng lúc (NPC Chưởng môn + 3 lựa chọn trả lời có số liệu biến thiên {0}). Gom thành 1 request duy nhất gửi Gemini.",
            boxes = listOf(
                OcrTextBox(
                    id = 1,
                    rawChinese = "本座观你气息浑厚，这便传你《天剑诀》第三式！",
                    normLeft = 0.05f,
                    normTop = 0.56f,
                    normRight = 0.95f,
                    normBottom = 0.72f,
                    sampledBgHex = 0xEB0F172AL,
                    textColorHex = 0xFFF8FAFCL,
                    speakerHint = "Thanh Vân Chưởng Môn"
                ),
                OcrTextBox(
                    id = 2,
                    rawChinese = "呈上500枚上品灵石并叩谢师恩",
                    normLeft = 0.36f,
                    normTop = 0.14f,
                    normRight = 0.95f,
                    normBottom = 0.24f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFF10B981L,
                    speakerHint = "Lựa chọn [1]"
                ),
                OcrTextBox(
                    id = 3,
                    rawChinese = "请师尊允许弟子立刻下山历练",
                    normLeft = 0.36f,
                    normTop = 0.27f,
                    normRight = 0.95f,
                    normBottom = 0.37f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFF06B6D4L,
                    speakerHint = "Lựa chọn [2]"
                ),
                OcrTextBox(
                    id = 4,
                    rawChinese = "继续留在藏经阁闭关修炼30日",
                    normLeft = 0.36f,
                    normTop = 0.40f,
                    normRight = 0.95f,
                    normBottom = 0.50f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFFF59E0BL,
                    speakerHint = "Lựa chọn [3]"
                )
            )
        ),

        // Scene 2: Feature 3 - Vertical Chinese Text Detection (height / width > 2.2)
        SimulatedGameScene(
            id = "scene_vertical_scroll",
            title = "2. Cổ Trang Chữ Dọc: Tàng Kinh Các",
            subtitle = "Vertical Detection (H/W > 2.2) • Ghép cột Phải ➔ Trái",
            domain = ContextDomain.WUXIA_XIANXIA,
            backgroundGradientStart = 0xFF1E1B4BL,
            backgroundGradientEnd = 0xFF3B0764L,
            accentColor = 0xFFF59E0BL,
            description = "Phát hiện các khung câu đối/thẻ bài chữ dọc cổ trang có tỷ lệ Height/Width > 2.2 và tự động sắp xếp thứ tự đọc từ Phải sang Trái trước khi dịch.",
            boxes = listOf(
                // Rightmost vertical column (Read 1st in Classical Chinese)
                OcrTextBox(
                    id = 1,
                    rawChinese = "剑气纵横三万里",
                    normLeft = 0.76f,
                    normTop = 0.12f,
                    normRight = 0.90f,
                    normBottom = 0.68f, // H/W ratio > 2.2 -> Vertical!
                    sampledBgHex = 0xEB27173AL,
                    textColorHex = 0xFFFBBF24L,
                    speakerHint = "Liễn Dọc Phải (Cột 1)"
                ),
                // Middle-Right vertical column (Read 2nd)
                OcrTextBox(
                    id = 2,
                    rawChinese = "一剑光寒十九洲",
                    normLeft = 0.54f,
                    normTop = 0.12f,
                    normRight = 0.68f,
                    normBottom = 0.68f, // H/W ratio > 2.2 -> Vertical!
                    sampledBgHex = 0xEB27173AL,
                    textColorHex = 0xFFFBBF24L,
                    speakerHint = "Liễn Dọc Giữa (Cột 2)"
                ),
                // Left vertical plaque (Read 3rd)
                OcrTextBox(
                    id = 3,
                    rawChinese = "天道无亲唯剑永恒",
                    normLeft = 0.12f,
                    normTop = 0.14f,
                    normRight = 0.27f,
                    normBottom = 0.66f, // H/W ratio > 2.2 -> Vertical!
                    sampledBgHex = 0xEB1E1B4BL,
                    textColorHex = 0xFF38BDF8L,
                    speakerHint = "Bia Đá Trái (Cột 3)"
                ),
                // Bottom horizontal warning plaque
                OcrTextBox(
                    id = 4,
                    rawChinese = "擅入藏经阁者格杀勿论",
                    normLeft = 0.08f,
                    normTop = 0.75f,
                    normRight = 0.92f,
                    normBottom = 0.90f,
                    sampledBgHex = 0xEB31102FL,
                    textColorHex = 0xFFF87171L,
                    speakerHint = "Lệnh Bài Ngang"
                )
            )
        ),

        // Scene 3: Feature 1 - Parameterized Template Cache ({0}, {1}, {2})
        SimulatedGameScene(
            id = "scene_combat_template",
            title = "3. RPG Sát Thương Biến Thiên ({0}, {1})",
            subtitle = "Parameterized Template Cache • Đổi số dịch < 0.8ms",
            domain = ContextDomain.SYSTEM_UI,
            backgroundGradientStart = 0xFF1F122BL,
            backgroundGradientEnd = 0xFF3F1D38L,
            accentColor = 0xFF06B6D4L,
            description = "Tự động tách các con số sát thương/hồi chiêu thành biến {0}, {1}, {2}. Nhấn nút 'Random Số Sát Thương Mới' để kiểm chứng tốc độ điền số < 1ms không cần gọi lại Gemini API!",
            boxes = listOf(
                OcrTextBox(
                    id = 1,
                    rawChinese = "九天玄雷剑：对前方敌人造成1250点火焰伤害，并附加35%暴击率提升，冷却时间8秒。",
                    normLeft = 0.05f,
                    normTop = 0.14f,
                    normRight = 0.95f,
                    normBottom = 0.36f,
                    sampledBgHex = 0xEB0F172AL,
                    textColorHex = 0xFF38BDF8L,
                    speakerHint = "Tooltip Kỹ Năng {0},{1},{2}"
                ),
                OcrTextBox(
                    id = 2,
                    rawChinese = "消耗180点法力值，瞬间恢复自身3400点生命值。",
                    normLeft = 0.05f,
                    normTop = 0.42f,
                    normRight = 0.95f,
                    normBottom = 0.60f,
                    sampledBgHex = 0xEB111827L,
                    textColorHex = 0xFF10B981L,
                    speakerHint = "Hiệu Ứng Hồi Phục {0},{1}"
                ),
                OcrTextBox(
                    id = 3,
                    rawChinese = "击败敌方首领获得12枚上品灵石与45000点经验值！",
                    normLeft = 0.05f,
                    normTop = 0.66f,
                    normRight = 0.95f,
                    normBottom = 0.86f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFFFBBF24L,
                    speakerHint = "Chiến Lợi Phẩm {0},{1}"
                )
            )
        ),

        // Scene 4: Anime / Sci-Fi RPG (Honkai / Genshin)
        SimulatedGameScene(
            id = "scene_scifi_rpg",
            title = "4. Sci-Fi RPG: Đoàn Tàu Astral",
            subtitle = "Auto-Context Profiler • Văn phong Anime/Sci-Fi hiện đại",
            domain = ContextDomain.SCIFI_ANIME_RPG,
            backgroundGradientStart = 0xFF090D16L,
            backgroundGradientEnd = 0xFF1E3A8AL,
            accentColor = 0xFF38BDF8L,
            description = "Tự động nhận diện ngữ cảnh Sci-Fi RPG qua từ khóa (Khai Phá Giả, Đoàn Tàu Astral, Động cơ Bước Nhảy) và điều chỉnh đại từ nhân xưng phù hợp.",
            boxes = listOf(
                OcrTextBox(
                    id = 1,
                    rawChinese = "开拓者，星穹列车的跃迁引擎已充能98%，是否立即启动折跃？",
                    normLeft = 0.05f,
                    normTop = 0.56f,
                    normRight = 0.95f,
                    normBottom = 0.74f,
                    sampledBgHex = 0xEB0F172AL,
                    textColorHex = 0xFFF8FAFCL,
                    speakerHint = "Pom-Pom / Hệ Thống Tàu"
                ),
                OcrTextBox(
                    id = 2,
                    rawChinese = "空间站护盾剩余42%，星核猎手正在接近坐标！",
                    normLeft = 0.05f,
                    normTop = 0.14f,
                    normRight = 0.95f,
                    normBottom = 0.28f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFFF87171L,
                    speakerHint = "Cảnh Báo Radar"
                ),
                OcrTextBox(
                    id = 3,
                    rawChinese = "立即出发前往罗浮仙舟",
                    normLeft = 0.34f,
                    normTop = 0.34f,
                    normRight = 0.95f,
                    normBottom = 0.43f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFF10B981L,
                    speakerHint = "Quyết định [1]"
                ),
                OcrTextBox(
                    id = 4,
                    rawChinese = "先联系姬子领航员确认坐标",
                    normLeft = 0.34f,
                    normTop = 0.45f,
                    normRight = 0.95f,
                    normBottom = 0.54f,
                    sampledBgHex = 0xEB1E293BL,
                    textColorHex = 0xFF38BDF8L,
                    speakerHint = "Quyết định [2]"
                )
            )
        )
    )
}

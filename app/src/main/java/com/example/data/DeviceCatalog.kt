package com.example.data

import com.example.model.DeviceCatalogEntry

object DeviceCatalog {
    val supportedBrands = listOf(
        "Samsung", "Xiaomi", "Redmi", "POCO", "Realme",
        "Oppo", "Vivo", "iQOO", "Tecno", "Infinix", "Itel", "OnePlus", "Other"
    )

    val devices: List<DeviceCatalogEntry> = listOf(
        // Samsung (14 models)
        DeviceCatalogEntry("sam_a04", "Samsung", "Galaxy A04s", 4, 90, 6.5, "Exynos 850"),
        DeviceCatalogEntry("sam_a05", "Samsung", "Galaxy A05s", 6, 90, 6.7, "Snapdragon 680"),
        DeviceCatalogEntry("sam_a14", "Samsung", "Galaxy A14 5G", 4, 90, 6.6, "Exynos 1330 / Dimensity 700"),
        DeviceCatalogEntry("sam_a15", "Samsung", "Galaxy A15 5G", 6, 90, 6.5, "Dimensity 6100+"),
        DeviceCatalogEntry("sam_a24", "Samsung", "Galaxy A24", 6, 90, 6.5, "Helio G99"),
        DeviceCatalogEntry("sam_a25", "Samsung", "Galaxy A25 5G", 8, 120, 6.5, "Exynos 1280"),
        DeviceCatalogEntry("sam_a34", "Samsung", "Galaxy A34 5G", 8, 120, 6.6, "Dimensity 1080"),
        DeviceCatalogEntry("sam_a35", "Samsung", "Galaxy A35 5G", 8, 120, 6.6, "Exynos 1380"),
        DeviceCatalogEntry("sam_a54", "Samsung", "Galaxy A54 5G", 8, 120, 6.4, "Exynos 1380"),
        DeviceCatalogEntry("sam_a55", "Samsung", "Galaxy A55 5G", 8, 120, 6.6, "Exynos 1480"),
        DeviceCatalogEntry("sam_m14", "Samsung", "Galaxy M14 5G", 6, 90, 6.6, "Exynos 1330"),
        DeviceCatalogEntry("sam_m34", "Samsung", "Galaxy M34 5G", 8, 120, 6.5, "Exynos 1280"),
        DeviceCatalogEntry("sam_s23", "Samsung", "Galaxy S23 Ultra", 12, 120, 6.8, "Snapdragon 8 Gen 2"),
        DeviceCatalogEntry("sam_s24", "Samsung", "Galaxy S24 Ultra", 12, 120, 6.8, "Snapdragon 8 Gen 3"),

        // Xiaomi (8 models)
        DeviceCatalogEntry("xia_11lite", "Xiaomi", "Xiaomi 11 Lite NE 5G", 8, 90, 6.55, "Snapdragon 778G"),
        DeviceCatalogEntry("xia_12pro", "Xiaomi", "Xiaomi 12 Pro", 12, 120, 6.73, "Snapdragon 8 Gen 1"),
        DeviceCatalogEntry("xia_12t", "Xiaomi", "Xiaomi 12T Pro", 12, 120, 6.67, "Snapdragon 8+ Gen 1"),
        DeviceCatalogEntry("xia_13", "Xiaomi", "Xiaomi 13", 12, 120, 6.36, "Snapdragon 8 Gen 2"),
        DeviceCatalogEntry("xia_13t", "Xiaomi", "Xiaomi 13T", 12, 144, 6.67, "Dimensity 8200 Ultra"),
        DeviceCatalogEntry("xia_13tpro", "Xiaomi", "Xiaomi 13T Pro", 12, 144, 6.67, "Dimensity 9200+"),
        DeviceCatalogEntry("xia_14", "Xiaomi", "Xiaomi 14", 12, 120, 6.36, "Snapdragon 8 Gen 3"),
        DeviceCatalogEntry("xia_14ultra", "Xiaomi", "Xiaomi 14 Ultra", 16, 120, 6.73, "Snapdragon 8 Gen 3"),

        // Redmi (13 models)
        DeviceCatalogEntry("red_9a", "Redmi", "Redmi 9A", 2, 60, 6.53, "Helio G25"),
        DeviceCatalogEntry("red_10", "Redmi", "Redmi 10", 4, 90, 6.5, "Helio G88"),
        DeviceCatalogEntry("red_12", "Redmi", "Redmi 12", 6, 90, 6.79, "Helio G88"),
        DeviceCatalogEntry("red_12c", "Redmi", "Redmi 12C", 4, 60, 6.71, "Helio G85"),
        DeviceCatalogEntry("red_13c", "Redmi", "Redmi 13C", 6, 90, 6.74, "Helio G85"),
        DeviceCatalogEntry("red_a3", "Redmi", "Redmi A3", 4, 90, 6.71, "Helio G36"),
        DeviceCatalogEntry("red_note10", "Redmi", "Redmi Note 10 Pro", 6, 120, 6.67, "Snapdragon 732G"),
        DeviceCatalogEntry("red_note11", "Redmi", "Redmi Note 11", 6, 90, 6.43, "Snapdragon 680"),
        DeviceCatalogEntry("red_note12", "Redmi", "Redmi Note 12", 6, 120, 6.67, "Snapdragon 685"),
        DeviceCatalogEntry("red_note12pro", "Redmi", "Redmi Note 12 Pro 5G", 8, 120, 6.67, "Dimensity 1080"),
        DeviceCatalogEntry("red_note13", "Redmi", "Redmi Note 13 5G", 8, 120, 6.67, "Dimensity 6080"),
        DeviceCatalogEntry("red_note13pro", "Redmi", "Redmi Note 13 Pro 5G", 8, 120, 6.67, "Snapdragon 7s Gen 2"),
        DeviceCatalogEntry("red_note13prop", "Redmi", "Redmi Note 13 Pro+", 12, 120, 6.67, "Dimensity 7200 Ultra"),

        // POCO (8 models)
        DeviceCatalogEntry("poco_c55", "POCO", "POCO C55", 6, 60, 6.71, "Helio G85"),
        DeviceCatalogEntry("poco_c65", "POCO", "POCO C65", 6, 90, 6.74, "Helio G85"),
        DeviceCatalogEntry("poco_m5", "POCO", "POCO M5", 6, 90, 6.58, "Helio G99"),
        DeviceCatalogEntry("poco_m6pro", "POCO", "POCO M6 Pro", 8, 120, 6.67, "Helio G99 Ultra"),
        DeviceCatalogEntry("poco_x5pro", "POCO", "POCO X5 Pro 5G", 8, 120, 6.67, "Snapdragon 778G"),
        DeviceCatalogEntry("poco_x6", "POCO", "POCO X6 5G", 8, 120, 6.67, "Snapdragon 7s Gen 2"),
        DeviceCatalogEntry("poco_x6pro", "POCO", "POCO X6 Pro", 12, 120, 6.67, "Dimensity 8300 Ultra"),
        DeviceCatalogEntry("poco_f6", "POCO", "POCO F6", 12, 120, 6.67, "Snapdragon 8s Gen 3"),

        // Realme (10 models)
        DeviceCatalogEntry("rlm_c33", "Realme", "Realme C33", 4, 60, 6.5, "Unisoc T612"),
        DeviceCatalogEntry("rlm_c53", "Realme", "Realme C53", 6, 90, 6.74, "Unisoc T612"),
        DeviceCatalogEntry("rlm_c55", "Realme", "Realme C55", 6, 90, 6.72, "Helio G88"),
        DeviceCatalogEntry("rlm_c65", "Realme", "Realme C65", 8, 90, 6.67, "Helio G85"),
        DeviceCatalogEntry("rlm_c67", "Realme", "Realme C67", 8, 90, 6.72, "Snapdragon 685"),
        DeviceCatalogEntry("rlm_11", "Realme", "Realme 11 5G", 8, 120, 6.72, "Dimensity 6100+"),
        DeviceCatalogEntry("rlm_12pro", "Realme", "Realme 12 Pro+", 8, 120, 6.7, "Snapdragon 7s Gen 2"),
        DeviceCatalogEntry("rlm_narzo60", "Realme", "Realme Narzo 60", 8, 90, 6.43, "Dimensity 6020"),
        DeviceCatalogEntry("rlm_narzo70", "Realme", "Realme Narzo 70 Pro", 8, 120, 6.67, "Dimensity 7050"),
        DeviceCatalogEntry("rlm_gt6t", "Realme", "Realme GT 6T", 12, 120, 6.78, "Snapdragon 7+ Gen 3"),

        // Oppo (9 models)
        DeviceCatalogEntry("opp_a17", "Oppo", "OPPO A17", 4, 60, 6.56, "Helio G35"),
        DeviceCatalogEntry("opp_a18", "Oppo", "OPPO A18", 4, 90, 6.56, "Helio G85"),
        DeviceCatalogEntry("opp_a38", "Oppo", "OPPO A38", 4, 90, 6.56, "Helio G85"),
        DeviceCatalogEntry("opp_a58", "Oppo", "OPPO A58", 6, 60, 6.72, "Helio G85"),
        DeviceCatalogEntry("opp_a78", "Oppo", "OPPO A78", 8, 90, 6.43, "Snapdragon 680"),
        DeviceCatalogEntry("opp_a79", "Oppo", "OPPO A79 5G", 8, 90, 6.72, "Dimensity 6020"),
        DeviceCatalogEntry("opp_f25", "Oppo", "OPPO F25 Pro 5G", 8, 120, 6.7, "Dimensity 7050"),
        DeviceCatalogEntry("opp_reno10", "Oppo", "OPPO Reno 10 5G", 8, 120, 6.7, "Dimensity 7050"),
        DeviceCatalogEntry("opp_reno11", "Oppo", "OPPO Reno 11 5G", 8, 120, 6.7, "Dimensity 7050"),

        // Vivo (9 models)
        DeviceCatalogEntry("viv_y16", "Vivo", "Vivo Y16", 4, 60, 6.51, "Helio P35"),
        DeviceCatalogEntry("viv_y17s", "Vivo", "Vivo Y17s", 4, 60, 6.56, "Helio G85"),
        DeviceCatalogEntry("viv_y27", "Vivo", "Vivo Y27", 6, 60, 6.64, "Helio G85"),
        DeviceCatalogEntry("viv_y28", "Vivo", "Vivo Y28 5G", 6, 90, 6.56, "Dimensity 6020"),
        DeviceCatalogEntry("viv_y36", "Vivo", "Vivo Y36", 8, 90, 6.64, "Snapdragon 680"),
        DeviceCatalogEntry("viv_y100", "Vivo", "Vivo Y100 5G", 8, 120, 6.67, "Snapdragon 4 Gen 2"),
        DeviceCatalogEntry("viv_v27", "Vivo", "Vivo V27 5G", 8, 120, 6.78, "Dimensity 7200"),
        DeviceCatalogEntry("viv_v29", "Vivo", "Vivo V29 5G", 12, 120, 6.78, "Snapdragon 778G"),
        DeviceCatalogEntry("viv_v30", "Vivo", "Vivo V30 5G", 12, 120, 6.78, "Snapdragon 7 Gen 3"),

        // iQOO (6 models)
        DeviceCatalogEntry("iqo_z6lite", "iQOO", "iQOO Z6 Lite 5G", 6, 120, 6.58, "Snapdragon 4 Gen 1"),
        DeviceCatalogEntry("iqo_z7pro", "iQOO", "iQOO Z7 Pro 5G", 8, 120, 6.78, "Dimensity 7200"),
        DeviceCatalogEntry("iqo_z9", "iQOO", "iQOO Z9 5G", 8, 120, 6.67, "Dimensity 7200"),
        DeviceCatalogEntry("iqo_z9x", "iQOO", "iQOO Z9x 5G", 6, 120, 6.72, "Snapdragon 6 Gen 1"),
        DeviceCatalogEntry("iqo_neo7", "iQOO", "iQOO Neo 7 Pro", 12, 120, 6.78, "Snapdragon 8+ Gen 1"),
        DeviceCatalogEntry("iqo_neo9", "iQOO", "iQOO Neo 9 Pro", 12, 144, 6.78, "Snapdragon 8 Gen 2"),

        // Tecno (9 models)
        DeviceCatalogEntry("tec_spark10", "Tecno", "Tecno Spark 10 Pro", 8, 90, 6.8, "Helio G88"),
        DeviceCatalogEntry("tec_spark20", "Tecno", "Tecno Spark 20", 8, 90, 6.56, "Helio G85"),
        DeviceCatalogEntry("tec_spark20pro", "Tecno", "Tecno Spark 20 Pro+", 8, 120, 6.78, "Helio G99 Ultimate"),
        DeviceCatalogEntry("tec_sparkgo24", "Tecno", "Tecno Spark Go 2024", 4, 90, 6.6, "Unisoc T606"),
        DeviceCatalogEntry("tec_camon20", "Tecno", "Tecno Camon 20 Pro", 8, 120, 6.67, "Helio G99"),
        DeviceCatalogEntry("tec_camon30", "Tecno", "Tecno Camon 30 5G", 8, 120, 6.78, "Dimensity 7020"),
        DeviceCatalogEntry("tec_pova4", "Tecno", "Tecno POVA 4", 8, 90, 6.82, "Helio G99"),
        DeviceCatalogEntry("tec_pova5", "Tecno", "Tecno POVA 5 Pro", 8, 120, 6.78, "Dimensity 6080"),
        DeviceCatalogEntry("tec_pova6", "Tecno", "Tecno POVA 6 Pro 5G", 12, 120, 6.78, "Dimensity 6080"),

        // Infinix (10 models)
        DeviceCatalogEntry("inf_smart8", "Infinix", "Infinix Smart 8", 4, 90, 6.6, "Unisoc T606"),
        DeviceCatalogEntry("inf_hot30", "Infinix", "Infinix Hot 30", 8, 90, 6.78, "Helio G88"),
        DeviceCatalogEntry("inf_hot30i", "Infinix", "Infinix Hot 30i", 4, 90, 6.56, "Unisoc T606"),
        DeviceCatalogEntry("inf_hot40i", "Infinix", "Infinix Hot 40i", 8, 90, 6.56, "Unisoc T606"),
        DeviceCatalogEntry("inf_hot40", "Infinix", "Infinix Hot 40 Pro", 8, 120, 6.78, "Helio G99"),
        DeviceCatalogEntry("inf_note12", "Infinix", "Infinix Note 12 G96", 8, 60, 6.7, "Helio G96"),
        DeviceCatalogEntry("inf_note30", "Infinix", "Infinix Note 30", 8, 120, 6.78, "Helio G99"),
        DeviceCatalogEntry("inf_note40", "Infinix", "Infinix Note 40 Pro", 8, 120, 6.78, "Helio G99 Ultimate"),
        DeviceCatalogEntry("inf_gt10", "Infinix", "Infinix GT 10 Pro", 8, 120, 6.67, "Dimensity 8050"),
        DeviceCatalogEntry("inf_gt20", "Infinix", "Infinix GT 20 Pro", 12, 144, 6.78, "Dimensity 8200 Ultimate"),

        // Itel (7 models)
        DeviceCatalogEntry("itl_a60", "Itel", "Itel A60s", 4, 60, 6.6, "Unisoc SC9863A1"),
        DeviceCatalogEntry("itl_a70", "Itel", "Itel A70", 4, 60, 6.6, "Unisoc T603"),
        DeviceCatalogEntry("itl_p40", "Itel", "Itel P40", 4, 60, 6.6, "Unisoc SC9863A"),
        DeviceCatalogEntry("itl_p55", "Itel", "Itel P55 5G", 6, 90, 6.6, "Dimensity 6080"),
        DeviceCatalogEntry("itl_s23", "Itel", "Itel S23", 8, 90, 6.6, "Unisoc T606"),
        DeviceCatalogEntry("itl_s23p", "Itel", "Itel S23+", 8, 60, 6.78, "Unisoc T616"),
        DeviceCatalogEntry("itl_rs4", "Itel", "Itel RS4", 12, 120, 6.56, "Helio G99 Ultimate"),

        // OnePlus (8 models)
        DeviceCatalogEntry("onp_nord_ce2", "OnePlus", "OnePlus Nord CE 2 Lite", 6, 120, 6.59, "Snapdragon 695"),
        DeviceCatalogEntry("onp_nord_ce3", "OnePlus", "OnePlus Nord CE 3 Lite", 8, 120, 6.72, "Snapdragon 695"),
        DeviceCatalogEntry("onp_nord_ce4", "OnePlus", "OnePlus Nord CE 4", 8, 120, 6.7, "Snapdragon 7 Gen 3"),
        DeviceCatalogEntry("onp_nord3", "OnePlus", "OnePlus Nord 3 5G", 16, 120, 6.74, "Dimensity 9000"),
        DeviceCatalogEntry("onp_10r", "OnePlus", "OnePlus 10R 5G", 8, 120, 6.7, "Dimensity 8100-Max"),
        DeviceCatalogEntry("onp_11r", "OnePlus", "OnePlus 11R 5G", 16, 120, 6.74, "Snapdragon 8+ Gen 1"),
        DeviceCatalogEntry("onp_12r", "OnePlus", "OnePlus 12R", 16, 120, 6.78, "Snapdragon 8 Gen 2"),
        DeviceCatalogEntry("onp_12", "OnePlus", "OnePlus 12", 16, 120, 6.82, "Snapdragon 8 Gen 3"),

        // Other Popular Android Devices (7 models)
        DeviceCatalogEntry("oth_sym_z70", "Other", "Symphony Z70", 4, 90, 6.56, "Unisoc T606"),
        DeviceCatalogEntry("oth_wal_nexg", "Other", "Walton NEXG N72", 6, 90, 6.6, "Helio G85"),
        DeviceCatalogEntry("oth_moto_g54", "Other", "Motorola Moto G54 5G", 8, 120, 6.5, "Dimensity 7020"),
        DeviceCatalogEntry("oth_noth_2a", "Other", "Nothing Phone (2a)", 8, 120, 6.7, "Dimensity 7200 Pro"),
        DeviceCatalogEntry("oth_pix_7a", "Other", "Google Pixel 7a", 8, 90, 6.1, "Google Tensor G2"),
        DeviceCatalogEntry("oth_low_3gb", "Other", "Budget 3GB Android (60Hz)", 3, 60, 6.5, "Entry Quad/Octa Core"),
        DeviceCatalogEntry("oth_mid_6gb", "Other", "Mid-Range 6GB Android (90Hz)", 6, 90, 6.6, "Mid-Tier Octa Core")
    )
}

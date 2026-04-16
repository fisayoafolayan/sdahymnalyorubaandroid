package com.sdahymnal.yoruba.data

data class HymnCategory(
    val id: String,
    val name: String,
    val englishTitle: String,
    val icon: String,
    val hymnRange: IntRange,
)

object HymnCategoryStore {

    val categories: List<HymnCategory> = listOf(
        // WORSHIP
        HymnCategory("adoration",     "Ìbà Àti Ìyìn",                          "Adoration And Praise",                      "praise",         1..22),
        HymnCategory("morning",       "Ìsìn Òwúrọ̀",                            "Morning Worship",                           "sunrise",        23..33),
        HymnCategory("evening",       "Ìsìn Aṣalẹ́",                             "Evening Worship",                           "evening",        34..46),
        HymnCategory("opening",       "Ìbẹ̀rẹ̀ Ìsìn",                            "Beginning Of Worship",                      "music",          47..49),
        HymnCategory("closing",       "Ìparí Ìsìn",                             "End Of Worship",                            "closing",        50..56),

        // TRINITY
        HymnCategory("trinity",       "Mẹ́talọ́kan",                              "Trinity",                                   "trinity",        57..57),

        // GOD THE FATHER
        HymnCategory("lovegod",       "Ìfẹ́ Ọlọ́run",                             "Love Of God",                               "love_god",       58..61),
        HymnCategory("majesty",       "Ọlá Ńlá Àti Agbára Ọlọ́run",             "Majesty And Power Of God",                  "majesty",        62..68),
        HymnCategory("nature",        "Agbára Ọlọ́run Lórí Ẹ̀dá",                "God's Power Over Creation",                 "nature",         69..76),
        HymnCategory("faithfulness",  "Òdodo Ọlọ́run",                           "Righteousness Of God",                      "faithful",       77..80),
        HymnCategory("grace",         "Oore Ọ̀fẹ́ Àti Àánú Ọlọ́run",             "Grace And Mercy Of God",                    "grace",          81..86),

        // JESUS CHRIST
        HymnCategory("birth",         "Ìbí Krístì",                             "Birth Of Christ",                           "birth",          87..99),
        HymnCategory("lifemin",       "Ìgbé Ayé Àti Iṣẹ́",                      "Life And Work",                             "ministry",       100..102),
        HymnCategory("suffering",     "Ìjìyà Àti Ikú",                          "Suffering And Death",                       "suffering",      103..115),
        HymnCategory("resurrection",  "Àjíǹdé Àti Ìgòkè Re Ọ̀run",             "Resurrection And Ascension",                "resurrection",   116..119),
        HymnCategory("priesthood",    "Iṣẹ́ Alúfáà",                             "Priestly Work",                             "priesthood",     120..120),
        HymnCategory("loveChrist",    "Ìfẹ́ Rẹ Fún Wa",                          "His Love For Us",                           "love_christ",    121..134),
        HymnCategory("secondadvent",  "Bíbọ̀ Lẹ́ẹ̀kejì",                          "Second Coming",                             "second_advent",  135..153),
        HymnCategory("kingdom",       "Ìjọba",                                  "Kingdom",                                   "kingdom",        154..160),
        HymnCategory("glorypraise",   "Ògó Àti Ìyìn",                           "Glory And Praise",                          "glory",          161..183),

        // HOLY SPIRIT
        HymnCategory("holyspirit",    "Ẹ̀mí Mímọ́",                              "Holy Spirit",                               "spirit",         184..195),

        // HOLY SCRIPTURES
        HymnCategory("scripture",     "Ìwé Mímọ́",                               "Holy Scripture",                            "scripture",      196..202),

        // GOSPEL
        HymnCategory("invitation",    "Ìpè Ìhìnrere",                           "Gospel Call",                               "invitation",     203..218),
        HymnCategory("repentance",    "Ìrònúpìwàdà",                            "Repentance",                                "repentance",     219..223),
        HymnCategory("forgiveness",   "Ìdáríjì",                                "Forgiveness",                               "forgiveness",    224..227),
        HymnCategory("consecration",  "Ìyàsí Mímọ́",                             "Sanctification",                            "consecration",   228..262),
        HymnCategory("baptism",       "Ìríbọmí",                                "Baptism",                                   "baptism",        263..267),
        HymnCategory("salvation",     "Ìgbàlà Àti Ìràpadà",                     "Salvation And Redemption",                  "salvation",      268..279),

        // CHRISTIAN CHURCH
        HymnCategory("community",     "Àjùmọ̀ṣepọ̀ Nínú Krístì",                "Fellowship In Christ",                      "community",      280..285),
        HymnCategory("mission",       "Iṣẹ́ Ìjọ",                                "Church Work",                               "mission",        286..301),
        HymnCategory("dedication",    "Ìyàsí Mímọ́ Ilé Ọlọ́run",                 "Consecration Of The House Of God",          "dedication",     302..303),
        HymnCategory("ordination",    "Ìgbọ́wọ́ Lé Lórí",                         "Laying On Of Hands",                        "ordination",     304..307),

        // DOCTRINES
        HymnCategory("sabbath",       "Ọjọ́ Ìsimi",                              "Day Of Rest",                               "sabbath",        308..322),
        HymnCategory("communion",     "Oúnjẹ Alẹ́ Olúwa",                        "Lord's Supper",                             "communion",      323..326),
        HymnCategory("lawgrace",      "Ọ̀fín Àti Oore Ọ̀fẹ́",                    "Law And Grace",                             "law",            327..327),
        HymnCategory("eternallife",   "Ìyè Àìnípẹ̀kun",                          "Eternal Life",                              "eternal",        328..341),

        // EARLY ADVENT
        HymnCategory("earlyadvent",   "Àwọn Oníìrètí Àkọkọ́",                   "First Expectations",                        "early_advent",   342..347),

        // CHRISTIAN LIFE
        HymnCategory("ourlove",       "Ìfẹ́ Wa Fún Ọlọ́run",                     "Our Love For God",                          "our_love",       348..353),
        HymnCategory("joy",           "Ayọ̀ Àti Àlàáfíà",                        "Joy And Peace",                             "joy",            354..361),
        HymnCategory("hope",          "Ìrètí Àti Ìtùnú",                         "Hope And Comfort",                          "hope",           362..366),
        HymnCategory("meditation",    "Àsàrò Àti Àdúrà",                        "Meditation And Prayer",                     "prayer",         367..380),
        HymnCategory("faith",         "Ìgbàgbọ́ Àti Ìgbékèlé",                  "Faith And Trust",                           "faith",          381..403),
        HymnCategory("guidance",      "Ìtọ́sọ́nà",                                "Guidance",                                  "guidance",       404..422),
        HymnCategory("thankful",      "Ọpẹ́ Ìkórè",                              "Harvest Thanksgiving",                      "thankful",       423..433),
        HymnCategory("humility",      "Ìrelè",                                   "Humility",                                  "humility",       434..435),
        HymnCategory("service",       "Iṣẹ́ Ìfẹ́",                                "Work Of Love",                              "service",        436..443),
        HymnCategory("obedience",     "Ìgbòràn",                                 "Obedience",                                 "obedience",      443..444),
        HymnCategory("watchful",      "Ìṣọ̀nà",                                   "Watchfulness",                              "watchful",       445..450),
        HymnCategory("warfare",       "Ogun Kristẹni",                           "Christian Warfare",                         "warfare",        451..464),
        HymnCategory("pilgrimage",    "Ìrìn Àjò Mímọ́",                          "Holy Pilgrimage",                           "pilgrimage",     465..474),

        // CHRISTIAN HOME
        HymnCategory("marriage",      "Ìgbéyàwó",                               "Marriage",                                  "marriage",       475..480),

        // OFFERING & PRAYER
        HymnCategory("offering",      "Ọrẹ, Ọpẹ́ Àti Àdúrà",                   "Offering, Thanks And Prayer",               "offering",       481..493),

        // CHILDREN
        HymnCategory("children",      "Orin Ọmọdé",                             "Children's Songs",                          "children",       494..509),

        // MISCELLANEOUS
        HymnCategory("misc",          "Oníirúurú",                              "Miscellaneous",                             "misc",           510..510),

        // CHOIR
        HymnCategory("choir",         "Orin Ògó Àti Àṣàyàn Ẹgbẹ́",             "Songs Of Glory And Choir Selections",       "choir",          511..563),

        // INDIGENOUS
        HymnCategory("indigenous",    "Orin Ìbílẹ̀",                              "Indigenous Songs",                          "indigenous",     564..621),
    )

    fun hymnsIn(category: HymnCategory, hymns: List<Hymn>): List<Hymn> =
        hymns.filter { it.number in category.hymnRange }
}

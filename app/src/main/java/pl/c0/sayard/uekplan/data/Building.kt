package pl.c0.sayard.uekplan.data

import android.content.Context
import pl.c0.sayard.uekplan.R

/**
 * Created by karol on 27.02.18.
 */
class Building(val context: Context){
    val LIBRARY_BUILDING = "Budynek Biblioteki"
    val MAIN_BUILDING = "Budynek Główny"
    val KSIEZOWKA_BUILDING = "Księżówka"
    val PAVILON_A = "Pawilon A"
    val PAVILON_B = "Pawilon B"
    val PAVILON_C = "Pawilon C"
    val PAVILON_D = "Pawilon D"
    val PAVILON_E = "Pawilon E"
    val PAVILON_F = "Pawilon F"
    val PAVILON_G = "Pawilon G"
    val SPORT_TEACHING_COMPLEX = "Pawilon Sportowo-dydaktyczny"
    val USTRONIE_BUILDING = "Pawilon Ustronie"
    val PONE = "PONE Nowy Targ"
    val RAKOWICKA_16 = "Rakowicka 16"
    val SIENKIEWICZA_4 = "Sienkiewicza 4"
    val SIENKIEWICZA_5 = "Sienkiewicza 5"

    val LIBRARY_BUILDING_ABBREVATION = "Bibl. "
    val MAIN_BUILDING_ABBREVATION = "Bud. gł. "
    val KSIEZOWKA_BUILDING_ABBREVATION = "Księżówka"
    val PAVILON_A_ABBREVATION = "Paw.A "
    val PAVILON_B_ABBREVATION = "Paw.B "
    val PAVILON_C_ABBREVATION = "Paw.C "
    val PAVILON_D_ABBREVATION = "Paw.D "
    val PAVILON_E_ABBREVATION = "Paw.E "
    val PAVILON_F_ABBREVATION = "Paw.F "
    val PAVILON_G_ABBREVATION = "Paw.G "
    val SPORT_TEACHING_COMPLEX_ABBREVATION = "Paw.S "
    val USTRONIE_BUILDING_ABBREVATION = "Paw.U "
    val PONE_ABBREVATION = "PONE "
    val RAKOWICKA_16_ABBREVATION = "Rakowicka 16"
    val SIENKIEWICZA_4_ABBREVATION = "Sienk. 4 "
    val SIENKIEWICZA_5_ABBREVATION = "Sienk. 5 "

    fun getBuildingList(): List<String>{
        return listOf<String>(
                context.getString(R.string.building),
                LIBRARY_BUILDING,
                MAIN_BUILDING,
                KSIEZOWKA_BUILDING,
                PAVILON_A,
                PAVILON_B,
                PAVILON_C,
                PAVILON_D,
                PAVILON_E,
                PAVILON_F,
                PAVILON_G,
                SPORT_TEACHING_COMPLEX,
                USTRONIE_BUILDING,
                PONE,
                RAKOWICKA_16,
                SIENKIEWICZA_4,
                SIENKIEWICZA_5
        )
    }

    fun getBuildingAbbreviation(building: String):String{
        return when(building){
            LIBRARY_BUILDING -> LIBRARY_BUILDING_ABBREVATION
            MAIN_BUILDING -> MAIN_BUILDING_ABBREVATION
            KSIEZOWKA_BUILDING -> KSIEZOWKA_BUILDING_ABBREVATION
            PAVILON_A -> PAVILON_A_ABBREVATION
            PAVILON_B -> PAVILON_B_ABBREVATION
            PAVILON_C -> PAVILON_C_ABBREVATION
            PAVILON_D -> PAVILON_D_ABBREVATION
            PAVILON_E -> PAVILON_E_ABBREVATION
            PAVILON_F -> PAVILON_F_ABBREVATION
            PAVILON_G -> PAVILON_G_ABBREVATION
            SPORT_TEACHING_COMPLEX -> SPORT_TEACHING_COMPLEX_ABBREVATION
            USTRONIE_BUILDING -> USTRONIE_BUILDING_ABBREVATION
            PONE -> PONE_ABBREVATION
            RAKOWICKA_16 -> RAKOWICKA_16_ABBREVATION
            SIENKIEWICZA_4 -> SIENKIEWICZA_4_ABBREVATION
            SIENKIEWICZA_5 -> SIENKIEWICZA_5_ABBREVATION
            else -> ""
        }
    }
}
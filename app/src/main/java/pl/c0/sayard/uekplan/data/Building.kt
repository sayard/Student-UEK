package pl.c0.sayard.uekplan.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng
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
    val MAIN_BUILDING_ABBREVATION = "Bud.gł. "
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

    val MAIN_BUILDING_30 = "30 kóło kortów"
    val SJO = "SJO"
    val HALL = "HALA SPORTOWA"
    val POOL = "PŁYWALNIA"
    val USTRONIE_BUILDING_SHORT = "paw.U"
    val USTRONIE_BUILDING_SHORT_2 = "Ust.s."
    val USTRONIE_BUILDING_LONG = "Paw.Ustronie"
    val PONE_FULL = "Pdhalański Ośrodek Nauk Ekon."

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

    fun getBuildingLatLng(classroom: String?): LatLng?{
        if(classroom == null){
            return null
        }else{
            return when{
                classroom.startsWith(LIBRARY_BUILDING_ABBREVATION) -> LatLng(50.068521, 19.955813)
                classroom.startsWith(MAIN_BUILDING_ABBREVATION) -> LatLng(50.068516, 19.953870)
                classroom == MAIN_BUILDING_30 -> LatLng(50.068304, 19.954247)
                classroom.contains(KSIEZOWKA_BUILDING_ABBREVATION) -> LatLng(50.069154, 19.954054)
                classroom.startsWith(PAVILON_A_ABBREVATION) -> LatLng(50.069194, 19.954689)
                classroom.startsWith(PAVILON_B_ABBREVATION) -> LatLng(50.068950, 19.955482)
                classroom.startsWith(PAVILON_C_ABBREVATION) -> LatLng(50.069234, 19.955258)
                classroom.startsWith(PAVILON_D_ABBREVATION) -> LatLng(50.069434, 19.954303)
                classroom.startsWith(PAVILON_E_ABBREVATION) -> LatLng(50.069060, 19.955864)
                classroom.startsWith(PAVILON_F_ABBREVATION) -> LatLng(50.068508, 19.956644)
                classroom.startsWith(SJO) -> LatLng(50.068508, 19.956644)
                classroom.startsWith(PAVILON_G_ABBREVATION) -> LatLng(50.069514, 19.953841)
                classroom.startsWith(SPORT_TEACHING_COMPLEX_ABBREVATION) -> LatLng(50.067933, 19.956759)
                classroom == HALL -> LatLng(50.067933, 19.956759)
                classroom == POOL -> LatLng(50.067757, 19.956807)
                classroom.startsWith(USTRONIE_BUILDING_ABBREVATION) -> LatLng(50.068172, 19.955624)
                classroom.contains(USTRONIE_BUILDING_SHORT) -> LatLng(50.068172, 19.955624)
                classroom.startsWith(USTRONIE_BUILDING_SHORT_2) -> LatLng(50.068172, 19.955624)
                classroom.startsWith(USTRONIE_BUILDING_LONG) -> LatLng(50.068172, 19.955624)
                classroom.startsWith(PONE_ABBREVATION) -> LatLng(49.489873, 20.045916)
                classroom == PONE_FULL -> LatLng(49.489873, 20.045916)
                classroom.startsWith(RAKOWICKA_16_ABBREVATION) -> LatLng(50.067708, 19.952025)
                classroom.startsWith(SIENKIEWICZA_4_ABBREVATION) -> LatLng(50.070477, 19.925854)
                classroom.startsWith(SIENKIEWICZA_5_ABBREVATION) -> LatLng(50.070420, 19.926146)
                else -> null
            }
        }
    }

    fun getBuildingFromAbbreviation(classroom: String?): String?{
        if(classroom == null){
            return null
        }else{
            return when{
                classroom.startsWith(LIBRARY_BUILDING_ABBREVATION) -> LIBRARY_BUILDING
                classroom.startsWith(MAIN_BUILDING_ABBREVATION) -> MAIN_BUILDING
                classroom.startsWith(KSIEZOWKA_BUILDING_ABBREVATION) -> KSIEZOWKA_BUILDING
                classroom.startsWith(PAVILON_A_ABBREVATION) -> PAVILON_A
                classroom.startsWith(PAVILON_B_ABBREVATION) -> PAVILON_B
                classroom.startsWith(PAVILON_C_ABBREVATION) -> PAVILON_C
                classroom.startsWith(PAVILON_D_ABBREVATION) -> PAVILON_D
                classroom.startsWith(PAVILON_E_ABBREVATION) -> PAVILON_E
                classroom.startsWith(PAVILON_F_ABBREVATION) -> PAVILON_F
                classroom.startsWith(PAVILON_G_ABBREVATION) -> PAVILON_G
                classroom.startsWith(SPORT_TEACHING_COMPLEX_ABBREVATION) -> SPORT_TEACHING_COMPLEX
                classroom.startsWith(USTRONIE_BUILDING_ABBREVATION) -> USTRONIE_BUILDING
                classroom.startsWith(PONE_ABBREVATION) -> PONE
                classroom.startsWith(RAKOWICKA_16_ABBREVATION) -> RAKOWICKA_16
                classroom.startsWith(SIENKIEWICZA_4_ABBREVATION) -> SIENKIEWICZA_4
                classroom.startsWith(SIENKIEWICZA_5_ABBREVATION) -> SIENKIEWICZA_5
                else -> null
            }
        }
    }

    fun getClassroomWithoutBuilding(classroom: String): String{
        return when{
            classroom.startsWith(LIBRARY_BUILDING_ABBREVATION) -> classroom.replace(LIBRARY_BUILDING_ABBREVATION, "")
            classroom.startsWith(MAIN_BUILDING_ABBREVATION) -> classroom.replace(MAIN_BUILDING_ABBREVATION, "")
            classroom.startsWith(KSIEZOWKA_BUILDING_ABBREVATION) -> classroom.replace(KSIEZOWKA_BUILDING_ABBREVATION, "")
            classroom.startsWith(PAVILON_A_ABBREVATION) -> classroom.replace(PAVILON_A_ABBREVATION, "")
            classroom.startsWith(PAVILON_B_ABBREVATION) -> classroom.replace(PAVILON_B_ABBREVATION, "")
            classroom.startsWith(PAVILON_C_ABBREVATION) -> classroom.replace(PAVILON_C_ABBREVATION, "")
            classroom.startsWith(PAVILON_D_ABBREVATION) -> classroom.replace(PAVILON_D_ABBREVATION, "")
            classroom.startsWith(PAVILON_E_ABBREVATION) -> classroom.replace(PAVILON_E_ABBREVATION, "")
            classroom.startsWith(PAVILON_F_ABBREVATION) -> classroom.replace(PAVILON_F_ABBREVATION, "")
            classroom.startsWith(PAVILON_G_ABBREVATION) -> classroom.replace(PAVILON_G_ABBREVATION, "")
            classroom.startsWith(SPORT_TEACHING_COMPLEX_ABBREVATION) -> classroom.replace(SPORT_TEACHING_COMPLEX_ABBREVATION, "")
            classroom.startsWith(USTRONIE_BUILDING_ABBREVATION) -> classroom.replace(USTRONIE_BUILDING_ABBREVATION, "")
            classroom.startsWith(PONE_ABBREVATION) -> classroom.replace(PONE_ABBREVATION, "")
            classroom.startsWith(RAKOWICKA_16_ABBREVATION) -> classroom.replace(RAKOWICKA_16_ABBREVATION, "")
            classroom.startsWith(SIENKIEWICZA_4_ABBREVATION) -> classroom.replace(SIENKIEWICZA_4_ABBREVATION, "")
            classroom.startsWith(SIENKIEWICZA_5_ABBREVATION) -> classroom.replace(SIENKIEWICZA_5_ABBREVATION, "")
            else -> ""
        }
    }
}

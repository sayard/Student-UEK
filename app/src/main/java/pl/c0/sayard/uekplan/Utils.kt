package pl.c0.sayard.uekplan

/**
 * Created by Karol on 1/1/2018.
 */
class Utils {
    companion object {
        fun getGroupURL(group: Group): String{
            return "http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=" +
                    group.id.toString() +
                    "&okres=1"
        }
    }
}

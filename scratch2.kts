import java.text.SimpleDateFormat
import java.util.Locale

try {
    val alphaFormatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
    val date = alphaFormatter.parse("16-AUG-2027")
    println("Parsed: " + date)
} catch (e: Exception) {
    println("Error: " + e.message)
}

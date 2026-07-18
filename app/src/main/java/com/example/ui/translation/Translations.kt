package com.example.ui.translation

enum class AppLanguage {
    ENGLISH,
    SINHALA
}

object Translations {
    private val translations = mapOf(
        "app_title" to mapOf(
            AppLanguage.ENGLISH to "Daily Trip Logs",
            AppLanguage.SINHALA to "දිනපතා ගමන් සටහන්"
        ),
        "trips_tab" to mapOf(
            AppLanguage.ENGLISH to "Trip Logs",
            AppLanguage.SINHALA to "ගිය ගමන්"
        ),
        "planner_tab" to mapOf(
            AppLanguage.ENGLISH to "Reminders",
            AppLanguage.SINHALA to "මතක් කිරීම් / එලාම්"
        ),
        "stats_tab" to mapOf(
            AppLanguage.ENGLISH to "Analytics",
            AppLanguage.SINHALA to "වාර්තා සහ විස්තර"
        ),
        "calculator_tab" to mapOf(
            AppLanguage.ENGLISH to "Calculator",
            AppLanguage.SINHALA to "මීටර ගණකය"
        ),
        "add_trip" to mapOf(
            AppLanguage.ENGLISH to "Log New Trip",
            AppLanguage.SINHALA to "අලුත් ගමනක් ලියන්න"
        ),
        "add_planned" to mapOf(
            AppLanguage.ENGLISH to "Plan Future Journey",
            AppLanguage.SINHALA to "ඉදිරි ගමනක් සැලසුම් කරන්න"
        ),
        "destination" to mapOf(
            AppLanguage.ENGLISH to "Destination / Place",
            AppLanguage.SINHALA to "යන තැන / ස්ථානය"
        ),
        "destination_placeholder" to mapOf(
            AppLanguage.ENGLISH to "e.g. Colombo, Kandy",
            AppLanguage.SINHALA to "උදා: කොළඹ, මහනුවර"
        ),
        "reason" to mapOf(
            AppLanguage.ENGLISH to "Reason / Purpose",
            AppLanguage.SINHALA to "ගමනේ හේතුව"
        ),
        "reason_placeholder" to mapOf(
            AppLanguage.ENGLISH to "e.g. Special Meeting, Delivery",
            AppLanguage.SINHALA to "උදා: රැස්වීමක්, බඩු ගෙනයන්න"
        ),
        "vehicle" to mapOf(
            AppLanguage.ENGLISH to "Vehicle Used",
            AppLanguage.SINHALA to "ගිය වාහනය"
        ),
        "vehicle_a" to mapOf(
            AppLanguage.ENGLISH to "Vehicle A (Primary)",
            AppLanguage.SINHALA to "A වාහනය (ප්‍රධාන)"
        ),
        "vehicle_b" to mapOf(
            AppLanguage.ENGLISH to "Vehicle B (Secondary)",
            AppLanguage.SINHALA to "B වාහනය (අමතර)"
        ),
        "driver" to mapOf(
            AppLanguage.ENGLISH to "Driver Name",
            AppLanguage.SINHALA to "ඩ්‍රයිවර්ගේ නම"
        ),
        "driver_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Enter driver name",
            AppLanguage.SINHALA to "ඩ්‍රයිවර්ගේ නම ලියන්න"
        ),
        "assistant" to mapOf(
            AppLanguage.ENGLISH to "Assistant Name",
            AppLanguage.SINHALA to "හෙල්පර්ගේ නම"
        ),
        "assistant_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Enter assistant name",
            AppLanguage.SINHALA to "හෙල්පර්ගේ නම ලියන්න"
        ),
        "distance" to mapOf(
            AppLanguage.ENGLISH to "Distance (km)",
            AppLanguage.SINHALA to "ගිය දුර (කි.මී.)"
        ),
        "distance_placeholder" to mapOf(
            AppLanguage.ENGLISH to "e.g. 45.5",
            AppLanguage.SINHALA to "උදා: 45.5"
        ),
        "date_time" to mapOf(
            AppLanguage.ENGLISH to "Date & Time",
            AppLanguage.SINHALA to "දිනය සහ වෙලාව"
        ),
        "save" to mapOf(
            AppLanguage.ENGLISH to "Save",
            AppLanguage.SINHALA to "සේව් කරන්න"
        ),
        "cancel" to mapOf(
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.SINHALA to "කැන්සල් කරන්න"
        ),
        "no_trips" to mapOf(
            AppLanguage.ENGLISH to "No trip logs found. Start by adding your first trip!",
            AppLanguage.SINHALA to "තවම එක ගමනක්වත් ලියලා නැහැ. අලුත් ගමනක් ලියන්න!"
        ),
        "no_planned" to mapOf(
            AppLanguage.ENGLISH to "No upcoming journeys planned. Set reminders to trigger alarms!",
            AppLanguage.SINHALA to "ඉදිරියට සැලසුම් කරපු ගමන් මොකුත් නැහැ. වෙලාවට මතක් කරන්න එලාම් එකක් දාන්න!"
        ),
        "alarms_header" to mapOf(
            AppLanguage.ENGLISH to "Trip Alarms Configured",
            AppLanguage.SINHALA to "දැනට දාලා තියෙන එලාම්"
        ),
        "total_distance" to mapOf(
            AppLanguage.ENGLISH to "Total Distance",
            AppLanguage.SINHALA to "මුළු ගිය දුර"
        ),
        "trips_logged" to mapOf(
            AppLanguage.ENGLISH to "Trips Logged",
            AppLanguage.SINHALA to "ලියපු ගමන් ගණන"
        ),
        "active_reminders" to mapOf(
            AppLanguage.ENGLISH to "Active Reminders",
            AppLanguage.SINHALA to "දැනට දාලා තියෙන එලාම් ගණන"
        ),
        "trip_details" to mapOf(
            AppLanguage.ENGLISH to "Trip Log Details",
            AppLanguage.SINHALA to "ගමනේ විස්තර"
        ),
        "delete_confirm" to mapOf(
            AppLanguage.ENGLISH to "Delete Entry?",
            AppLanguage.SINHALA to "මේ සටහන මකලා දාන්නද?"
        ),
        "delete" to mapOf(
            AppLanguage.ENGLISH to "Delete",
            AppLanguage.SINHALA to "මකන්න"
        ),
        "notification_permission_required" to mapOf(
            AppLanguage.ENGLISH to "Notifications permission is required to trigger alarms.",
            AppLanguage.SINHALA to "එලාම් සහ මතක් කිරීම් ලැබෙන්න නම් නොටිෆිකේෂන් (Notification) ඔන් කරන්න ඕනේ."
        ),
        "grant_permission" to mapOf(
            AppLanguage.ENGLISH to "Grant Permission",
            AppLanguage.SINHALA to "අවසර දෙන්න"
        ),
        "alarm_intervals" to mapOf(
            AppLanguage.ENGLISH to "Alerts will ring at: 1 day before, 3 hours before, and 1 hour before scheduled time.",
            AppLanguage.SINHALA to "මතක් කිරීම්: ගමනට දිනකට කලින්, පැය 3කට කලින් සහ පැයකට කලින් එලාම් වදිනු ඇත."
        ),
        "stats_vehicle_a" to mapOf(
            AppLanguage.ENGLISH to "Vehicle A (Primary) Mileage",
            AppLanguage.SINHALA to "A වාහනය ගිය මුළු දුර"
        ),
        "stats_vehicle_b" to mapOf(
            AppLanguage.ENGLISH to "Vehicle B (Secondary) Mileage",
            AppLanguage.SINHALA to "B වාහනය ගිය මුළු දුර"
        ),
        "frequent_drivers" to mapOf(
            AppLanguage.ENGLISH to "Frequent Drivers",
            AppLanguage.SINHALA to "වැඩියෙන්ම ගිය ඩ්‍රයිවර්ලා"
        ),
        "frequent_assistants" to mapOf(
            AppLanguage.ENGLISH to "Frequent Assistants",
            AppLanguage.SINHALA to "වැඩියෙන්ම ගිය හෙල්පර්ලා"
        ),
        "settings_tab" to mapOf(
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.SINHALA to "සෙටින්ග්ස්"
        ),
        "vehicles_title" to mapOf(
            AppLanguage.ENGLISH to "Configure Vehicles",
            AppLanguage.SINHALA to "වාහන විස්තර ඇතුළත් කරන්න"
        ),
        "drivers_title" to mapOf(
            AppLanguage.ENGLISH to "Configure Drivers",
            AppLanguage.SINHALA to "ඩ්‍රයිවර්ලා ඇතුළත් කරන්න"
        ),
        "assistants_title" to mapOf(
            AppLanguage.ENGLISH to "Configure Assistants / Helpers",
            AppLanguage.SINHALA to "හෙල්පර්ලා ඇතුළත් කරන්න"
        ),
        "add_vehicle" to mapOf(
            AppLanguage.ENGLISH to "Add New Vehicle",
            AppLanguage.SINHALA to "අලුත් වාහනයක් එක් කරන්න"
        ),
        "add_driver" to mapOf(
            AppLanguage.ENGLISH to "Add New Driver",
            AppLanguage.SINHALA to "අලුත් ඩ්‍රයිවර් කෙනෙක් එක් කරන්න"
        ),
        "add_assistant" to mapOf(
            AppLanguage.ENGLISH to "Add New Assistant",
            AppLanguage.SINHALA to "අලුත් හෙල්පර් කෙනෙක් එක් කරන්න"
        ),
        "plate_number" to mapOf(
            AppLanguage.ENGLISH to "Vehicle Number (e.g. WP LF-7290)",
            AppLanguage.SINHALA to "වාහනයේ අංකය (උදා: WP LF-7290)"
        ),
        "phone_number" to mapOf(
            AppLanguage.ENGLISH to "Phone Number (e.g. 071-2345678)",
            AppLanguage.SINHALA to "දුරකථන අංකය (උදා: 071-2345678)"
        ),
        "description" to mapOf(
            AppLanguage.ENGLISH to "Description (e.g. Toyota TownAce)",
            AppLanguage.SINHALA to "වාහන වර්ගය / විස්තරය (උදා: Toyota TownAce)"
        ),
        "primary_driver" to mapOf(
            AppLanguage.ENGLISH to "Primary Driver",
            AppLanguage.SINHALA to "ප්‍රධාන ඩ්‍රයිවර්"
        ),
        "assistant_helper" to mapOf(
            AppLanguage.ENGLISH to "Assistant / Helper",
            AppLanguage.SINHALA to "හෙල්පර් (සහායක)"
        ),
        "setting_deleted" to mapOf(
            AppLanguage.ENGLISH to "Deleted successfully!",
            AppLanguage.SINHALA to "සාර්ථකව මකා දැමුණා!"
        ),
        "sync_title" to mapOf(
            AppLanguage.ENGLISH to "Google Sheets Cloud Sync",
            AppLanguage.SINHALA to "ක්ලවුඩ් සමකාලීනය (Sync)"
        ),
        "sync_url_label" to mapOf(
            AppLanguage.ENGLISH to "Google Sheets Web App URL",
            AppLanguage.SINHALA to "ගූගල් ෂීට් ලින්ක් එක"
        ),
        "sync_url_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Paste web app URL here...",
            AppLanguage.SINHALA to "ලින්ක් එක මෙතනට පේස්ට් කරන්න..."
        ),
        "sync_button" to mapOf(
            AppLanguage.ENGLISH to "Sync Now",
            AppLanguage.SINHALA to "සමමුහුර්ත"
        ),
        "syncing" to mapOf(
            AppLanguage.ENGLISH to "Syncing...",
            AppLanguage.SINHALA to "යාවත්කාලීන වේ..."
        ),
        "sync_success" to mapOf(
            AppLanguage.ENGLISH to "Synced successfully with Google Sheets!",
            AppLanguage.SINHALA to "සාර්ථකව සමමුහුර්ත විය!"
        ),
        "sync_failed" to mapOf(
            AppLanguage.ENGLISH to "Sync failed!",
            AppLanguage.SINHALA to "සමකාලීන කිරීම අසාර්ථකයි!"
        ),
        "sync_not_configured" to mapOf(
            AppLanguage.ENGLISH to "Please configure Google Sheets Web App URL in Settings first!",
            AppLanguage.SINHALA to "කරුණාකර මුලින්ම සෙටින්ග්ස් වලින් ලින්ක් එක සකසන්න!"
        ),
        "last_synced" to mapOf(
            AppLanguage.ENGLISH to "Last Synced",
            AppLanguage.SINHALA to "අවසන් යාවත්කාලීනය"
        ),
        "add_stop" to mapOf(
            AppLanguage.ENGLISH to "+ Add Stop / Place",
            AppLanguage.SINHALA to "+ තව යන තැනක්/නැවතුමක් එකතු කරන්න"
        ),
        "stop_label" to mapOf(
            AppLanguage.ENGLISH to "Stop / Destination",
            AppLanguage.SINHALA to "යන තැන / නැවතුම"
        ),
        "quick_log_title" to mapOf(
            AppLanguage.ENGLISH to "⚡ Instant Ad-hoc Quick Log",
            AppLanguage.SINHALA to "⚡ ඉක්මනින් ගමනක් ලියන්න (Quick Log)"
        ),
        "quick_log_btn" to mapOf(
            AppLanguage.ENGLISH to "Instant Log",
            AppLanguage.SINHALA to "ඉක්මනින් ලියන්න"
        ),
        "quick_log_success" to mapOf(
            AppLanguage.ENGLISH to "Instant trip logged successfully!",
            AppLanguage.SINHALA to "ඉක්මන් ගමන් සටහන සාර්ථකව ඇතුළත් කළා!"
        ),
        "first_destination_label" to mapOf(
            AppLanguage.ENGLISH to "First Destination / Start Place",
            AppLanguage.SINHALA to "පළමුවෙන්ම යන තැන / ආරම්භක ස්ථානය"
        ),
        "additional_stops_header" to mapOf(
            AppLanguage.ENGLISH to "Additional Stops in this Trip",
            AppLanguage.SINHALA to "ගමන අතරතුර නවතින තැන්"
        ),
        "enter_password" to mapOf(
            AppLanguage.ENGLISH to "Enter Password to proceed",
            AppLanguage.SINHALA to "ඉදිරියට යාමට මුරපදය ඇතුළත් කරන්න"
        ),
        "password_label" to mapOf(
            AppLanguage.ENGLISH to "Password",
            AppLanguage.SINHALA to "මුරපදය"
        ),
        "password_incorrect" to mapOf(
            AppLanguage.ENGLISH to "Incorrect password! Access denied.",
            AppLanguage.SINHALA to "වැරදි මුරපදයක්! ප්‍රවේශය ප්‍රතික්ෂේප විය."
        ),
        "password_prompt" to mapOf(
            AppLanguage.ENGLISH to "Authentication Required",
            AppLanguage.SINHALA to "අවසරය අවශ්‍යයි"
        ),
        "incomplete_details" to mapOf(
            AppLanguage.ENGLISH to "Details Incomplete",
            AppLanguage.SINHALA to "විස්තර අසම්පූර්ණයි"
        ),
        "close" to mapOf(
            AppLanguage.ENGLISH to "Close",
            AppLanguage.SINHALA to "වසන්න"
        ),
        "fuel_order_number" to mapOf(
            AppLanguage.ENGLISH to "Fuel Order Number (Optional)",
            AppLanguage.SINHALA to "ඉන්ධන ඇනවුම් අංකය (අත්‍යවශ්‍ය නොවේ)"
        ),
        "fuel_liters" to mapOf(
            AppLanguage.ENGLISH to "Fuel Quantity in Liters (Optional)",
            AppLanguage.SINHALA to "ඉන්ධන ලීටර් ප්‍රමාණය (අත්‍යවශ්‍ය නොවේ)"
        ),
        "fuel_order_placeholder" to mapOf(
            AppLanguage.ENGLISH to "e.g. F-98745",
            AppLanguage.SINHALA to "උදා: F-98745"
        ),
        "fuel_liters_placeholder" to mapOf(
            AppLanguage.ENGLISH to "e.g. 25.5",
            AppLanguage.SINHALA to "උදා: 25.5"
        )
    )

    fun getString(key: String, language: AppLanguage): String {
        return translations[key]?.get(language) ?: key
    }
}

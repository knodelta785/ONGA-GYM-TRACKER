package com.ongarand.gymtracker.data

object SeedData {
    val muscles = listOf("Dada", "Punggung", "Bahu", "Bisep", "Trisep", "Kaki", "Core")

    val exercises: List<Exercise> = listOf(
        // Dada
        Exercise(name = "Bench Press", muscle = "Dada", equipment = "Barbell"),
        Exercise(name = "Incline Dumbbell Press", muscle = "Dada", equipment = "Dumbbell"),
        Exercise(name = "Chest Fly", muscle = "Dada", equipment = "Mesin/Cable"),
        Exercise(name = "Push Up", muscle = "Dada", equipment = "Bodyweight"),
        Exercise(name = "Dips", muscle = "Dada", equipment = "Bodyweight"),
        // Punggung
        Exercise(name = "Deadlift", muscle = "Punggung", equipment = "Barbell"),
        Exercise(name = "Pull Up", muscle = "Punggung", equipment = "Bodyweight"),
        Exercise(name = "Lat Pulldown", muscle = "Punggung", equipment = "Cable"),
        Exercise(name = "Barbell Row", muscle = "Punggung", equipment = "Barbell"),
        Exercise(name = "Seated Cable Row", muscle = "Punggung", equipment = "Cable"),
        // Bahu
        Exercise(name = "Overhead Press", muscle = "Bahu", equipment = "Barbell"),
        Exercise(name = "Lateral Raise", muscle = "Bahu", equipment = "Dumbbell"),
        Exercise(name = "Face Pull", muscle = "Bahu", equipment = "Cable"),
        // Bisep
        Exercise(name = "Barbell Curl", muscle = "Bisep", equipment = "Barbell"),
        Exercise(name = "Dumbbell Hammer Curl", muscle = "Bisep", equipment = "Dumbbell"),
        // Trisep
        Exercise(name = "Triceps Pushdown", muscle = "Trisep", equipment = "Cable"),
        Exercise(name = "Skull Crusher", muscle = "Trisep", equipment = "Barbell/EZ Bar"),
        // Kaki
        Exercise(name = "Squat", muscle = "Kaki", equipment = "Barbell"),
        Exercise(name = "Leg Press", muscle = "Kaki", equipment = "Mesin"),
        Exercise(name = "Romanian Deadlift", muscle = "Kaki", equipment = "Barbell"),
        Exercise(name = "Leg Extension", muscle = "Kaki", equipment = "Mesin"),
        Exercise(name = "Leg Curl", muscle = "Kaki", equipment = "Mesin"),
        Exercise(name = "Calf Raise", muscle = "Kaki", equipment = "Mesin/Dumbbell"),
        // Core
        Exercise(name = "Plank", muscle = "Core", equipment = "Bodyweight"),
        Exercise(name = "Hanging Leg Raise", muscle = "Core", equipment = "Bodyweight"),
        Exercise(name = "Cable Crunch", muscle = "Core", equipment = "Cable")
    )

    /** Template latihan: nama template -> daftar nama latihan. */
    val templates: Map<String, List<String>> = mapOf(
        "Push" to listOf("Bench Press", "Overhead Press", "Incline Dumbbell Press", "Lateral Raise", "Triceps Pushdown"),
        "Pull" to listOf("Deadlift", "Pull Up", "Barbell Row", "Face Pull", "Barbell Curl"),
        "Legs" to listOf("Squat", "Romanian Deadlift", "Leg Press", "Leg Curl", "Calf Raise"),
        "Full Body" to listOf("Squat", "Bench Press", "Barbell Row", "Overhead Press", "Plank")
    )

    val quotes = listOf(
        "Disiplin itu memilih apa yang kamu mau paling dalam, bukan yang kamu mau sekarang.",
        "Satu sesi hari ini lebih baik dari sesi sempurna yang tidak pernah terjadi.",
        "Progres kecil yang konsisten mengalahkan lonjakan sesaat.",
        "Tubuhmu mengikuti kebiasaanmu. Pilih kebiasaan yang kamu banggakan.",
        "Beban naik sedikit demi sedikit, itulah progressive overload.",
        "Datang saja dulu. Semangat biasanya menyusul setelah set pertama.",
        "Istirahat dan makan cukup adalah bagian dari latihan."
    )

    val motivationalReminders = listOf(
        "Waktunya latihan! Satu set pertama itu yang tersulit, sisanya mengalir.",
        "Belum ada sesi hari ini. Buka aplikasi dan catat set pertamamu.",
        "Streak-mu menunggu. Ayo gym hari ini!",
        "Tubuhmu berterima kasih untuk setiap sesi. Yuk mulai."
    )
}

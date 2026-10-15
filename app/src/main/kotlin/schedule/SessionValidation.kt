package schedule

import java.time.LocalDate


internal fun validateDate(date : LocalDate) {
    require(date.year in 1..9999) {
        "year must be between 1 and 9999"
    }
}

internal fun validateTopic(topic: String) {
    require(topic.isNotBlank()) {
        "topic must be not blank"
    }
    validateTextSafety(topic, "topic")
}

internal fun validateLocation(location: String) {
    require(location.isNotBlank()) {
        "location must not be blank"
    }
    require(location != "-") {
        "location must not be '-'"
    }
    validateTextSafety(location, "location")
}

internal fun validateTeachers(teachers: List<String>, minimumTeacherCount: Int) {
    require(teachers.size >= minimumTeacherCount) {
        "expected at least $minimumTeacherCount teacher(s), got ${teachers.size}"
    }
    for (t in teachers) {
        require(t.isNotBlank()){
            "teacher must not be blank"
        }
        require(t != "-") {
            "teacher must not be '-'"
        }
        validateTextSafety(t, "teacher")
    }
    require(teachers.size == teachers.toSet().size) {
        "teacher names must be unique"
    }
}

internal fun validateTextSafety(value: String, fieldName: String) {
    require(value.none { Character.isISOControl(it) }) {
        "$fieldName must not contain control characters"
    }
    require(value == value.trim()) {
        "$fieldName must not have leading or trailing whitespace"
    }
}


package com.pwfb.domain.entity

/**
 * 저장되는 문자열 설정 항목.
 * keyName은 DataStore에 저장된 기존 키 이름과 동일해야 한다 (변경 시 기존 사용자 데이터 유실).
 */
enum class PrefKey(val keyName: String, val default: String = "") {
    NAME("name", "NameNull"), // 이름
    WEIGHT("weight", "WeightNull"), // 체중(공복 체중)
    D_DAY("dDay"), // d-day
    TRAINING_PROGRAM("trainingProgram"), // 트레이닝 기록
    CARBOHYDRATE("carbohydrate"), // 탄수화물
    PROTEIN("protein"), // 단백질
    FAT("fat"), // 지방
    WATER("water"), // 수분량
    SODIUM("sodium"), // 나트륨
    POTASSIUM("potassium"), // 칼륨
    CREATINE("creatine"), // 크레아틴
    DIETARY_FIBER("dietaryFiber") // 식이 섬유
}

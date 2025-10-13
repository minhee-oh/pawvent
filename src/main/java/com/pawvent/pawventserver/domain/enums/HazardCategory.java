package com.pawvent.pawventserver.domain.enums;

//신고 카테고리
public enum HazardCategory {
    LEASH,           // 목줄 미착용
    MUZZLE,         // 입마개 미착용
    AGGRESSIVE_DOG, // 공격적인 개
    HAZARDOUS_MATERIAL, // 위험물질
    WILDLIFE,       // 야생동물 출몰
    LOW_LIGHT,      // 조명 부족
    BIKE_CAR,       // 자전거·차량 위험
    POOP_LEFT       // 배변 미수거
}

# PWFB (Peak Week For Bodybuilder)

보디빌딩 대회 D-Day를 기준으로 피크위크(대회 전 7일) 동안의 영양 섭취량과 트레이닝 전략을 안내하는 Android 앱입니다.

![전체](https://github.com/OhGyong/PWFB/assets/52282493/5052858e-67d0-4e89-a6aa-6326898c9aec)

## 기능

| 탭 | 내용 |
|---|---|
| 홈 | D-Day, 공복 체중 기준 영양소별 권장 섭취량, 당일 트레이닝·전략 요약 |
| 영양 | 일자별 영양 전략 설명과 실제 섭취량 기록 (탄수화물·단백질·지방·수분·나트륨·칼륨·크레아틴·식이섬유) |
| 트레이닝 | 일자별 트레이닝 안내와 운동 계획 기록 |
| 프로필 | 이름, 공복 체중, 대회 날짜·시간 수정, 당일 컨디션 전략, 오류 로그 전송 |

첫 실행 시 이름 → 체중 → 대회 날짜 순으로 입력받고(온보딩), 이후에는 메인 화면으로 바로 진입합니다.

## 동작 규칙

- **D-Day**는 달력 날짜 기준으로 계산합니다. 대회 3일 전이면 시각과 관계없이 `D-3`입니다.
- 영양·트레이닝 전략은 `D-7 ~ D-1` 구간에서 날짜별로 바뀌고, 그 외 기간은 평소 섭취를 안내합니다.
- Play 스토어에서 설치된 앱은 시작 시 **즉시 업데이트**를 요구하며, 사용자가 업데이트를 취소하면 앱이 종료됩니다. 로컬 빌드는 이 검사를 건너뜁니다.
- UI는 Material 3 다크 테마로 고정이며(시스템 라이트/다크 설정과 무관), 서체는 Pretendard Std를 번들합니다(`licenses/Pretendard-OFL.txt`).
- 온보딩에서 뒤로 가기는 이전 단계로 돌아가고, D-Day까지 저장하면 온보딩 화면은 백스택에서 제거됩니다.
- 영양 섭취량과 운동 계획은 입력칸에서 포커스가 빠질 때(입력칸 밖 터치, 탭 이동, 완료 키) 자동 저장됩니다.

## 기술 구성

- Kotlin, 단일 모듈(`app`)
- UI 전체가 Jetpack Compose + Material 3 (XML 레이아웃 없음). 화면은 `ui/screen/*Screen.kt`, 테마는 `theme/`, ViewModel은 `ui/viewmodel/`
- 단일 `MainActivity`가 `PwfbNavHost`로 온보딩(이름 → 체중 → D-Day)과 메인(탭 4개)을 모두 그린다
- Hilt(KSP) 의존성 주입, Preferences DataStore 저장
- 구조: `ui`(Compose 화면/ViewModel) → `domain`(UseCase/Repository 인터페이스/Entity) → `data`(Repository 구현, Hilt 모듈)
- Firebase: Analytics, Crashlytics, Storage(오류 로그 업로드)
- AdMob 배너, Play In-App Update
- 날짜·시간 선택은 Compose Material 3 `DatePickerDialog` / `TimePicker`

버전은 `gradle/libs.versions.toml`에서 관리합니다. AGP 9 내장 Kotlin을 사용하므로 `org.jetbrains.kotlin.android` 플러그인은 모듈에 적용하지 않고 루트에 버전만 선언합니다.

## 빌드 준비

1. **`app/google-services.json`** — Firebase 콘솔에서 받아 둡니다. 없으면 빌드가 실패합니다.
2. **`local.properties`** (저장소에 올리지 않음)

   ```properties
   sdk.dir=<Android SDK 경로>

   # 디버그 빌드에는 없어도 됩니다. 없으면 Google 테스트 광고 ID로 대체합니다.
   AdMobAPI_KEY=<AdMob 앱 ID>

   # 릴리즈 빌드에만 필요합니다.
   storeFile=<keystore 경로>
   keyAlias=<키 별칭>
   keyPassword=<키 비밀번호>
   storePassword=<keystore 비밀번호>
   ```

   릴리즈 빌드(`assembleRelease`, `bundleRelease`)는 서명 정보나 `AdMobAPI_KEY`가 없으면 안내 메시지와 함께 실패합니다. 디버그 빌드와 단위 테스트는 서명 정보 없이도 실행됩니다.

3. 빌드 및 테스트

   ```bash
   ./gradlew assembleDebug        # 디버그 APK
   ./gradlew testDebugUnitTest    # 단위 테스트
   ./gradlew lint                 # 린트
   ./gradlew assembleRelease      # 릴리즈 APK (서명 정보 필요)
   ```

   JDK 17 이상이 필요합니다.

## CI

`.github/workflows/android-ci.yml`

- PR: lint + 단위 테스트 (서명 키 등 릴리즈 시크릿은 사용하지 않음)
- `main` push: 추가로 디버그·릴리즈 APK를 빌드해 7일간 artifact로 보관

필요한 Repository Secret: `GOOGLE_SERVICES_JSON`, `STORE_FILE`(base64), `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD`, `ADMOB_API_KEY`

## 저장 데이터

- 사용자 설정은 Preferences DataStore(`pref_pwfb`)에 저장되며 Android 자동 백업에 포함됩니다.
- 저장 실패 등 오류 로그는 `files/logs/<날짜>.txt`에 기록되고 7일이 지나면 삭제됩니다. 백업에는 포함되지 않습니다.
- 프로필의 "로그 전송"은 오늘 로그를 Firebase Storage `logs/<날짜>/<앱 설치 ID>.txt`로 업로드합니다.

## 디자인 토큰

- 색: `theme/Color.kt`(Material 3 역할 색 + 영양소별 포인트 색) → `theme/Theme.kt`의 `PwfbTheme`가 `ColorScheme`으로 묶습니다. 화면에서는 `MaterialTheme.colorScheme.primary`처럼 역할 색으로만 참조합니다.
- 타이포: `theme/Typo.kt`가 Material 3 type scale 전부를 Pretendard로 덮어씁니다(Display·Headline 700, Title·Label 600, Body 400).
- 공통 컴포넌트(`ui/screen/CommonScreen.kt`): `PwfbCard`(기본/히어로 카드), `PrimaryButton`(56dp 알약 버튼), `TonalButton`, `Eyebrow`(섹션 머리말), `NutrientAvatar`, `DdayHeaderCard`, `OnboardingStep`, 날짜·시간 피커.
- 일자별 문구 규칙은 `ui/model/`의 `NutritionIntake`(홈 권장량), `NutritionGuide`(영양 탭 가이드), `PeakWeekText`(전략·트레이닝)에 순수 함수로 있어 단위 테스트로 검증합니다.

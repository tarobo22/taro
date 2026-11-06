# 1.4.0 로컬 preview OTA 호환성 근거

세 번째 로컬 preview 번들의 생성·해시·DB 정책 검증이 완료됐다. 기준은 root가 최종 승인하고 커밋한 RN `d8576628bce18f4d5c38d45b4813b80c44b55ca9`이며, 빌드 전후 제품 소스 144개 해시가 동일하다. 첫째와 둘째는 superseded/non-final로 보존하고 CAS로 비활성화했다. 이 검증은 JS/Hermes 번들·정적 자산 생성, 로컬 파일 해시 대조, 격리 DB의 배포 정책 확인까지 수행한다. 기기 다운로드·재시작·실제 적용은 수행하지 않았다. 서버 시작은 자동 승인 검토가 거절했으며 재시도하지 않았다. 운영·스토어 배포와 실제 AI/음성 호출은 없다.

## 1.4.0과 bootstrap

| 확인 대상 | 확인한 코드와 값 | 의미 |
|---|---|---|
| 네이티브 버전 | `android/app/build.gradle`: versionName 1.4.0, versionCode 5 | JS 표시 버전과 별개로 APK가 제공해야 하는 버전 |
| 테스트 패키지 | root가 보고한 `com.yeoun.app.nonai14`; Gradle의 yeounApplicationId override 사용 | 실제 설치·실행 확인은 root 범위 |
| JS 표시 버전 | `src/domain/content.ts`: release.version 1.4.0 | 표시용이며 SDK의 네이티브 버전 판정을 대체하지 않음 |
| SDK와 도구 | SDK·CLI·bare·server·protocol·plugin-core 모두 1.1.7 | `native-package-inventory.json`에 선언/설치 버전 기록 |
| JS/네이티브 연결 | `src/services/updates.native.ts`의 모듈 범위 HotUpdater.init, MainApplication.kt의 release HotUpdater.getJSBundleFile | 기본 APK에 HotUpdater 네이티브 연결이 있어야 함 |
| 전략 | CLI config와 client check 모두 appVersion | 실제 배포 정책은 APP_VERSION / 정확히 1.4.0 / Android |
| 채널 | AndroidManifest.xml의 preview, bootstrap 정책에서 전달한 채널로 check | 로컬 배포 채널 preview와 일치 |
| bootstrap DTO | backend http/bootstrap.py OTAPolicy, client P0Api.ts/model.ts | channel / min_native_version / updates_enabled 3필드 계약 일치 |
| 버전·중단 판정 | GovernedUpdates.ts | SDK getAppVersion()을 최소 버전과 비교하고, 정책 미확인·비활성·정책 변경 시 check/download/reload를 중단 |

bootstrap 호환성은 코드의 DTO·호출 계약 확인이다. 이번 작업에서 실제 서버 bootstrap HTTP 요청이나 기기 OTA 적용은 검증하지 않았다. 실제 운영 최소 버전과 활성 상태는 서버 정책이 결정한다. 관리 설정을 바꿨다고 APK의 네이티브 모듈이 추가되지는 않는다.

## 기본 APK에 필요한 패키지와 네이티브 구성

| 구성 | 설치 버전/위치 | 기본 APK에 필요한 이유 |
|---|---|---|
| React / React Native | 19.2.3 / 0.85.3 | UI 런타임과 네이티브 bridge/renderer |
| Hermes | native RN 엔진; bare 컴파일 로그의 HBC bytecode 98 | OTA HBC와 APK 엔진의 호환 필요 |
| HotUpdater | @hot-updater/react-native 1.1.7 | 번들 로더·다운로드·앱 준비/복구 수명 주기 |
| AsyncStorage | 2.2.0 | owner별 로컬 캐시와 복구 상태 |
| Keychain | 10.0.0 (선언 ^10.0.0) | 세션 보관에 쓰는 네이티브 보안 저장소 연결 |
| Safe Area | 5.7.0 | 네이티브 안전 영역 UI |
| SVG | 15.15.4 | 네이티브 SVG renderer |
| Google Play Billing | com.android.billingclient:billing:9.1.0 | YeounBilling bridge의 구매/복원 연결 |
| 커스텀 bridge | MainApplication.kt에 YeounAuth/YeounBilling/YeounFeedback/YeounFiles/YeounReminders/YeounSpeech Package 등록 | PKCE·브라우저 연결, 결제, 감각 피드백, 파일 내보내기, 알림, 기존 음성 bridge |
| Manifest 구성 | INTERNET, RECORD_AUDIO, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, 알림 receiver, yeoun://auth/complete deep link | 해당 bridge가 요구하는 Android 구성 |

네이티브 파일 전체 목록과 패키지 선언/설치 비교는 `native-package-inventory.json`에 있다. 실제 OAuth·구매·음성 사용 가능 여부를 이번 번들 생성으로 확인한 것은 아니다. iOS 네이티브 구현·설치·동작도 이번 Android 번들 검증 대상에 포함하지 않았다.

## OTA에 실을 수 있는 범위

화면의 JS/TS 로직·문구·레이아웃, JS 데이터 처리, Metro가 포함하는 정적 이미지를 배포할 수 있다. 이번 최종본의 Home.tsx Panel 두 곳 `height:'100%'` → `flex:1` 변경은 그 범위다.

새 NativeModule, Kotlin/Java/Swift/Objective-C 변경, Gradle/Pod 의존성, 네이티브 SDK 버전·Hermes 엔진, AndroidManifest 권한/receiver/deep link, 패키지 ID·서명·versionCode/versionName, 네이티브에 내장한 기본 채널·서명 공개키는 OTA JS 번들로 추가하거나 교체할 수 없다. 이런 변경은 새 기본 앱 빌드가 필요하다. appVersion 전략은 지정한 버전을 대상으로 할 뿐, 누락된 네이티브 의존성을 스스로 설치하거나 APK의 ABI를 증명하지 않는다.

첫 두 CLI 실행의 Node는 v24.15.0이고 package.json의 engines는 >=24.22.2다. 첫 두 번들 생성 자체는 성공했지만 그 실행 환경이 저장소의 선언 최소 버전을 충족한다고 보고하지 않는다. 세 번째 최종 생성·검증은 root가 지정한 `C:/Users/chang/.cache/yeoun-tools/node-v24.22.2-win-x64/node.exe`를 명시 실행하고 프로세스 PATH 앞에 같은 디렉터리를 추가했다. 실제 실행 버전 v24.22.2는 선언된 최소 버전과 일치한다. 패키지나 전역 런타임 설치 설정은 변경하지 않았다. 최종 인벤토리는 `native-package-inventory-final3.json`이다.

## 보존과 검증 상태

첫 공개 Bundle ID `01a1b02d-b2ff-79b1-bfc2-2d35667483c3`는 Home 수정 전 산출물이므로 superseded/non-final이다. 원본 `bundle/`, immutable artifact `01a1b02c-5b38-7b93-b0c0-90d5336194c3`, 매니페스트·자산·해시는 그대로 보관했다. 허용된 CLI `bundle disable --expected-revision 1 --yes --json`로 그 release 한 건만 revision 2 / enabled=false로 변경했다. 삭제 명령은 실행하지 않았다. 최초 81개 archive/storage 자산과 archive hash 검증 결과는 `first-verification.json`, 변경 결과는 `first-disable.json`에 있다.

둘째 후보는 별도 `bundle-final/`에 생성하여 첫 archive를 덮어쓰지 않았다. 공개 ID는 `01a1b033-e784-7c2a-9855-15478c7f65d6`, artifact는 `01a1b032-c076-78ef-a8c0-e1a01d5b937b`이며 81개 자산 전체 해시 검증을 통과했다. root의 추가 문서 대조와 stagger/큰 글자/타로 연출 수정 수집 때문에 둘째도 non-final이다. 셋째 생성·검증 성공을 확인한 뒤 root가 허용한 둘째 release 한 건에만 expected-revision 1 CAS를 적용해 revision 2 / enabled=false로 바꿨다. 두 이전 archive와 immutable artifact 세 건은 모두 삭제 없이 보존했다.

셋째 최종본은 `bundle-final3/`다. 공개 Bundle ID는 `01a1b049-aa7a-7820-8160-923800a5fe5b`, 내부 artifact ID는 `01a1b047-c5aa-7512-a9c5-4b25bbe6ca67`다. Android / preview / APP_VERSION / 정확히 1.4.0 / enabled=true / revision 1 / force=false / rollout 100%를 SQL로 확인했다. 81개 archive·storage 자산의 해시와 바이트 수, 매니페스트 DB 해시, 압축 archive를 대조했다. SDK가 준비한 부분 업데이트 경로 한 건도 저장 파일의 SHA256·크기와 DB 메타데이터·base 해시가 일치한다. 실제 부분 업데이트 적용은 검증하지 않았다.

매니페스트 SHA256은 `8247b6d4e2a6077d650aca3a3c948bf0270c8c49ff83c8f8dfbb808a0670463c`, archive SHA256은 `92c48929ed710b699069e7879abc503d00efa36a1c189dd9fb0c1da21a0c01a5`이며 archive는 16,843,784바이트다. 전체 결과는 `final-result.json`, SQL은 `final-policies.json`/`retained-bundles-final.json`/`patches-final3.json`, 파일 검증은 `third-verification.json`, 소스 대조는 `third-source-before.json`/`third-source-after.json`에 있다.

native bundle build adapter의 기본 중간 출력 디렉터리는 저장소 `dist/`이며 재빌드마다 다시 생성된다. 이 디렉터리는 첫 immutable archive 보존 위치가 아니다. 검증에 더 필요하지 않음을 root에 알렸으며, root가 정확한 절대 경로를 확인하여 전용 output 아래로 이동한다. 이 agent는 임의 이동·삭제하지 않았다.

CLI가 `.gitignore`에 `# hot-updater`와 `.hot-updater/output` 두 줄을 자동 추가한 것은 직접 소스 편집 0과 구분하는 추적 파일 변경 예외다. root가 이 설정을 추적·커밋했다. `dist/`, bundle/bundle-final, data-root/.local/ota-files는 생성 산출물이며 앱 코드 변경으로 보고하지 않는다. 기존 사용자 변경을 복구하거나 덮어쓰지 않았다.

신규 검증 스크립트 첫 실행은 URL 경로의 공백 `%20` 처리로 ENOENT가 발생했다. root 승인대로 node:url의 fileURLToPath로 경로 해석만 수정했고 해시/바이트 수 assertions는 유지했다. 재실행은 81개 자산 전체를 통과했다. 기존 제품 테스트는 변경하지 않았다.

## 공식 문서와 설치 CLI 근거

- [Connect your app](https://hot-updater.dev/docs/get-started/app-setup): init/네이티브 로더/동일 전략과 기본 채널 재빌드 조건.
- [Test an OTA update](https://hot-updater.dev/docs/get-started/test-ota): 네이티브 의존성을 유지한 JS·이미지 변경과 실제 기기 적용 검증을 구분.
- [Kysely schema](https://hot-updater.dev/docs/custom/database/kysely): 별도 서버 정의로 db migrate 실행.
- [Deploy](https://hot-updater.dev/docs/guides/deploy): Android/채널/target-app-version, immutable artifact와 공개 Bundle ID.
- [Console bundle commands](https://hot-updater.dev/docs/guides/console): disable와 expected-revision CAS.
- [Bundle model](https://hot-updater.dev/docs/concepts/how-it-works): delivery policy 변경과 저장 artifact 보존 구분.

설치된 1.1.7 CLI의 --help도 확인했다. disable은 core.updateReleasePolicy에 `{enabled:false}`와 expectedRevision을 전달하고 delete와 다른 명령이다. 서버 실행 정책거절 원문 발췌는 `server-start-policy-rejection.txt`, 스키마·초기 빈 DB는 prepared.json/tables.txt/schema-settings.json/initial-counts.json에 있다.

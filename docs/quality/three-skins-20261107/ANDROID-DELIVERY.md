# 세 공간 Android 납품 기록

[설치 파일과 체크섬](https://github.com/tarobo22/taro/releases/tag/untagged-e3f0a7ff0b87244ccae8) · GitHub 초안 릴리스

| 설치본 | 검증 |
|---|---|
| yeoun-preview-arm64-v8a.apk | 서명·ARM64 ABI·번들·9개 포즈와 야옹 확인 |
| yeoun-preview-x86_64.apk | 동일 패키징 검증 및 Android 35 에뮬레이터 설치·실행 |
| SHA256SUMS | 내려받은 두 파일의 체크섬 |

에뮬레이터에서 카페 → 진료실 → 놀이방을 직접 눌렀다. 세 공간의 전체 배경·테두리·메뉴·버튼과 캐릭터가 바뀌었고 앱을 종료하고 다시 열자 놀이방이 유지됐다. 카페의 손, 의사의 옆가르마, 고양이의 눈·입·귀·앞발·꼬리를 실제 화면에서 확인했다.

- [카페](native/android-cafe.png) · [진료실](native/android-clinic.png) · [놀이방](native/android-playroom.png) · [재시작 뒤](native/android-restored-playroom.png)
- [Android 동작 기록](native/android-device-proof.json)
- [ARM64 서명·에셋 기록](native/android-arm64.json) · [x86_64 서명·에셋 기록](native/android-x86.json)
- [JSON만 바꿔도 번들이 갱신되는지 확인한 기록](native/android-bundle-input-proof.json)

## 해결한 문제

| 현상 | 수정 | 검증 |
|---|---|---|
| 새 작업 폴더에는 앱 폴더의 debug.keystore가 없어 조립 실패 | Android SDK가 관리하는 기본 테스트 서명을 사용 | 두 ABI에서 assembleRelease와 apksigner verify 통과 |
| 기존 앱과 서명이 달라 덮어쓰기 불가 | 미리보기 패키지 com.yeoun.preview.skins를 별도 설치 | 기존 앱을 지우지 않고 미리보기 설치·업데이트 |
| JSON·이미지만 바꾸면 Gradle이 이전 번들 재사용 | Android 번들 작업의 입력에 src JSON과 assets 추가 | JS 코드를 그대로 두고 API 포트만 바꿨을 때 새 값이 번들에 들어감 |
| Android에서 중첩 SVG 위치가 달라 고양이 눈·입이 몸 밖에 렌더링 | 몸과 같은 좌표계의 SVG 그룹으로 얼굴 위치 지정 | 수정 APK의 실제 놀이방 화면에서 정상 표시 |

앱 코드: 115cc48995e5d0daa52bc2b9aa2a236f16bdfaa9. 기존 기능 테스트는 수정·삭제·완화하지 않았다. [최종 코드의 RN 검사](https://github.com/tarobo22/taro/actions/runs/42340678125)에서 타입·기능·번들 검증 통과.

## 미리보기 실행과 재현

미리보기의 이름은 **여운 미리보기**, 패키지는 com.yeoun.preview.skins다. APK는 기기의 http://127.0.0.1:8808로 연결한다. PC의 로컬 QA FastAPI가 8868에서 실행 중이면 연결한 기기에서 다음 포트 전달을 설정한다.

~~~sh
adb reverse tcp:8808 tcp:8868
~~~

Android 빌드 복사본에서 src/config/endpoints.json의 apiBaseUrl을 위 기기 주소로 지정하고, 앱 이름을 미리보기로 지정했다. 제품 저장소의 기본 API 주소와 이름은 바꾸지 않았다. 빌드 경로가 긴 Windows에서는 짧은 경로의 복사본을 사용한다. 의존성 설치 후 Android SDK 및 JAVA_HOME을 설정한다.

~~~powershell
cd android
.\gradlew.bat :app:assembleRelease '-PreactNativeArchitectures=arm64-v8a' '-PyeounApplicationId=com.yeoun.preview.skins'
.\gradlew.bat :app:assembleRelease '-PreactNativeArchitectures=x86_64' '-PyeounApplicationId=com.yeoun.preview.skins'
~~~

각 빌드 직후 app/build/outputs/apk/release/app-release.apk를 ABI별 파일명으로 보관한다. SDK의 기본 테스트 서명과 빌드 방법은 [Android 공식 서명 안내](https://developer.android.com/studio/publish/app-signing)를 참고했다. ARM64 실제 휴대폰과 스토어 배포는 이 검증에 포함되지 않는다.

## 초기 연결이 끊긴 경우

기존 APK는 저장된 테스트 세션의 연결을 확인하는 중에 Store가 필요한 재시도 버튼을 렌더링해 예외가 발생했다. 수정 APK에서 QA API 연결을 끊은 채 실행하고 재시도 버튼을 눌렀다. 앱 프로세스가 유지됐고, 재시도 실패 메시지도 표시됐다. 연결을 복원하고 앱을 다시 열자 선택한 놀이방이 유지됐다. [실제 재시도 화면](native/android-startup-recovery.png)

iOS의 [실패 실행](https://github.com/tarobo22/taro/actions/runs/42337300637)에서 같은 예외가 확인됐다. [수정 커밋의 시뮬레이터 실행](https://github.com/tarobo22/taro/actions/runs/42340677998)에서 재검증 중이다. Swift 규칙 테스트는 유지했고, 실행 후 PID와 설치된 실행 파일을 대조하도록 승인받은 CI 설정을 적용했다.

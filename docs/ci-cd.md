# 앱 자동 검증과 시연 APK 배포

PR·작업 브랜치는 타입, 기존 동작 테스트, Android JS 번들, APK 빌드와 네이티브 단위 테스트를 검증한다. `main`에서는 성공한 APK를 전용 서명키로 서명·검증해 GitHub Release에 올린다. 실패한 빌드는 릴리스하지 않는다.

- [최신 APK](https://github.com/tarobo22/taro/releases/latest/download/yeoun-demo.apk)
- [Android 자동화](https://github.com/tarobo22/taro/actions/workflows/checks.yml)
- [iOS 자동화](https://github.com/tarobo22/taro/actions/workflows/ios.yml)
- 시연 API: `https://yeoun-43-201-209-249.sslip.io/api`

`scripts/configure-demo.mjs`가 CI 작업 공간에만 공개 API 주소를 넣는다. 로컬 개발 기본 주소는 유지한다. GPT 키는 앱·GitHub에 포함하지 않는다.

GitHub Secret `YEOUN_ANDROID_KEYSTORE_B64`와 `YEOUN_ANDROID_STORE_PASSWORD`는 사용자가 허용한 전용 시연 서명키다. 같은 키를 계속 사용해야 앱 업데이트가 가능하다. 이전 debug 서명 앱에서는 설치 충돌이 날 수 있으므로 기존 기록 보존 여부를 확인한 뒤 별도 설치 전환이 필요하다.

iOS는 Swift 테스트 → Release 시뮬레이터 빌드 → 공개 API·시뮬레이터용 서명과 키체인 entitlement 확인 → 설치·실행·딥링크 확인 → 화면·로그·시뮬레이터 앱 7일 보관을 수행한다. 시뮬레이터 전용 ad-hoc 서명은 Apple 배포 인증서가 아니며 TestFlight/App Store 배포는 하지 않는다. Android도 Play Store 업로드가 아닌 APK 배포다.

이번 자동화는 APK 배포이며 OTA(CodePush 대체) 서버는 활성화하지 않는다. 운영 API의 OTA 기능은 기존대로 꺼져 있다. 앱 업데이트는 새 APK 설치로 반영한다.

`main`에서 실행 중인 릴리스 작업은 자동 취소하지 않는다. PR에는 서명 Secret과 릴리스 쓰기 권한을 주지 않는다. 실행 실패 시 Actions 로그를 확인하고 수정 커밋을 push하거나 같은 커밋의 워크플로를 다시 실행한다. 이력 강제 변경은 하지 않는다.

# 여운 · React Native 앱

여운의 RN Community CLI 앱입니다. 좌우 카드 선택, 뒤집기, 소리와 햅틱을 연결하고, 같은 상담 API를 웹과 공유합니다. [공개 웹 체험](https://yeoun-43-201-209-249.sslip.io/) · [포트폴리오 자료실](https://app.notion.com/p/411b37c6fe72812b9723cf911343c5d1)

## 현재 사용자 흐름

- 오늘의 한 장에서 카드 뜻을 바로 읽고, 고민 타로에서 1·3·5장 중 고릅니다. 3장 설정에 적은 질문은 시작 버튼으로 전송됩니다.
- 마음 대화는 왼쪽·오른쪽 밀기로 답합니다. **초기화**를 눌러 다시 시작할 수 있고, 날짜가 바뀌면 당일 선택과 기기 초안이 초기화됩니다.
- 작은 쉼표 결말은 한 화면에서 스크롤합니다. 설정의 호흡 원은 들숨과 날숨에 맞춰 움직이며, 캐릭터 표정 목록에는 고정된 뒤로가기 버튼이 있습니다.

## 로컬 실행

Node 24.22.2 이상과 React Native 0.85.3을 사용합니다. Android는 SDK가 필요하며, iOS 빌드에는 macOS와 Xcode가 필요합니다. [로컬 FastAPI](https://github.com/tarobo22/taro-back)를 먼저 시작하세요.

```powershell
. ./tools/use-env.ps1
npm ci
npm run typecheck
npm test
npm start
# Android 에뮬레이터를 연 별도 터미널에서
npm run android
```

Android 에뮬레이터의 로컬 API는 `10.0.2.2:8808`입니다. 개발용 OTA 도구는 `npm run ota:server`로 시작합니다. 네이티브 모듈이나 권한을 바꾼 경우에는 새 네이티브 빌드가 필요합니다.

## 빌드·검증

2026-11-08 기준 제품 코드 `0d07cd8`이 main에 올라갔습니다. 타입 검사와 기존 앱 테스트 173개가 통과했습니다. 공개 웹 체험과 네이티브 설치 파일의 버전은 각각 확인하세요. [Android 서명 APK 릴리스](https://github.com/tarobo22/taro/releases) · [Android 검증](https://github.com/tarobo22/taro/actions/workflows/checks.yml) · [iOS 시뮬레이터 검증](https://github.com/tarobo22/taro/actions/workflows/ios.yml)

## 설계 기록

[기획 PDF](docs/planning/타로기획1안.pdf) · [2026-11-06 구현 현황](docs/planning/implementation-status-20261106.md) · [요구사항 추적표](docs/planning/requirements-traceability-20261106.csv) · [남은 결정안](docs/planning/remaining-decisions-20261106.md) · [당시 소개·사용 가이드](docs/guides/yeoun-introduction-20261106/START-HERE.md) · [TDD 평가](docs/quality/tdd-audit.md)

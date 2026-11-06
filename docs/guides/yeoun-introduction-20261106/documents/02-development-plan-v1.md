# 개발안 1
RN·React·FastAPI·PostgreSQL 개발 구조
상태: 1안 · 현재 구조와 개발 기준

## 화면과 데이터의 책임을 나눈다.
RN CLI와 React 웹을 유지하고, 프레임 표현과 API 유스케이스를 분리한다.

RN·React 화면 → 기능 hook → repository·fetch → FastAPI → PostgreSQL

| 계층 | 담당 | 의존 기준 |
| --- | --- | --- |
| 화면 | 선택·입력·로딩·오류 표시 | 직접 SQL·GPT 호출 없음 |
| 클라이언트 기능 | 초안·멱등 요청·응답 디코딩 | fetch transport와 화면 분리 |
| 표현 모션 | SharedValue / Canvas2D·RAF | 프레임마다 React 상태 갱신 회피 |
| FastAPI 응용 | 유스케이스·UoW·소유권·작업 처리 | 도메인과 외부 adapter 연결 |
| 도메인 | 카드·기록·동의·상태 규칙 | HTTP·DB·SDK에 독립 |
| 어댑터 | PostgreSQL·OpenAI·스토어 | port 계약 구현 |

서버는 DDD·헥사고날 방향으로 구성한다. 모든 기능이 동일한 성숙도로 검증됐다는 뜻은 아니다. 기존 App Store 미완료 작업과 테스트는 이번 문서 제작에서도 보존한다.

## 데이터는 사용자의 흐름을 연결한다.
아래는 관계를 설명하는 개념 ERD다. 실제 물리 테이블 전체를 대체하지 않는다.

사용자 → 리딩·대화 → AI 작업 → 결과·기록

| 관계 | 역할 |
| --- | --- |
| 사용자 → 기록/리딩/대화 | 본인 소유 자료만 조회·수정 |
| 리딩 → 확정 카드·자리 | 확정 ID·방향·순서를 결과와 연결 |
| 대화 → 사용자 turn·결과 | 이어가기의 문맥과 처리 상태 보존 |
| AI 작업 → 동의·참고 기록 version | 전송 당시 동의·자료 버전을 검증 |
| 마음기록 → 원본 리딩/대화 | 결과에서 기록, 기록에서 원본으로 이동 |

| 현재 API 예 | 역할 |
| --- | --- |
| POST /v2/conversations/{id}/turns | 대화 입력 접수 |
| POST /v2/readings/{id}/interpretations | 확정 카드 AI 해석 접수 |
| GET /v2/ai/jobs/{id} | 처리 상태·결과 확인 |
| POST /v2/ai/jobs/{id}/cancel / retry | 취소·명시적 재시도 |
| POST /v2/transcriptions / /v2/journal-insights | 음성 전사·기록 요약 |

## 기능 단위로 검증하고 push한다.
기존 테스트는 보존한다. 사용자 요청대로 새 디자인 스냅샷 테스트는 추가하지 않는다.

핵심 실패 조건 → 구현 → 관련 검증 → 커밋·push → 원격 SHA 확인

| 검증 | 확인할 실제 문제 |
| --- | --- |
| 중복 요청 | 연속 전송·재시도 시 중복 처리 방지 |
| 소유권·동의 | 타인 자료·철회 동의로 AI 호출하지 않음 |
| 카드 계약 | 확정 카드 ID·순서·장수 불변 |
| 실패·취소 | 실패를 성공으로 표시하지 않고 입력 유지 |
| 실제 조작 | 키보드·뒤로가기·드래그·줄인 모션 |

새 기능 개발 때 의미 있는 도메인/API 경계는 RED → GREEN으로 진행한다. 이전의 모든 UI 변경이 TDD였던 것으로 소급해 표시하지 않는다. 문서·영상 제작 작업에서는 제품 테스트를 수정하거나 재실행하지 않았다.

Git은 codex/ 작업 브랜치에서 기능별로 나눈다. 제품·검증·문서 범위를 분명히 하고, 기존 미완료 파일을 섞어 커밋하지 않는다. main 병합·운영 배포는 별도 단계다.

## 개발 실행과 출시 준비를 구분한다.
로컬 실행·빌드·CI 통과와 외부 서비스 활성화는 서로 다른 완료 조건이다.

| 항목 | 현재 근거 | 남은 확인 |
| --- | --- | --- |
| RN / 웹 | 타입·웹 build·Android QA APK·핵심 동작 | 저사양 실기기·OS별 확인 |
| 백엔드 | FastAPI·PostgreSQL·작업 처리 코드 | 운영 HTTPS·백업·복구·관측 |
| AI | Responses·결과 검증·작업 큐 코드 | 실제 모델·품질·비용·지연 |
| iOS | 프로젝트·네이티브 코드 | Mac/Xcode·서명·기기 실행 |
| OTA | 기존 SDK·배포 체계 | 현재1.5 대상 실제 적용 검증 |

새 Skia·Reanimated·Gesture Handler가 들어간 기본 앱은1.5.0/build6이다. 새 네이티브 라이브러리는 JS 코드푸시만으로 설치할 수 없다. 같은 네이티브 계약 안의 UI 변경도 호환성·복구를 확인한 뒤 배포한다.

```
Client: screen -> use case -> repository -> fetch
Server: router -> application / UoW -> domain / ports
Adapters: PostgreSQL / OpenAI / stores
Media task: source hashes unchanged; no app deployment
```

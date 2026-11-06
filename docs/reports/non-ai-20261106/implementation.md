# 비AI 구현 현황과 개발자 인계 · 최신 타로 반영

## 최신 갱신 · 2026-11-06

18쪽 통합 PDF에 최신 설정 사진·화면 명세·모션·검증표를 반영했다. 자세한 변경과 사진은 [최신 타로 설정 보고서](compact-setup/README.md)를 우선 확인한다.

설정은 주제·1/3/5장·선택 질문·음성/파일·덱·시작을 모았고, 시작 한 번으로 카드 선택으로 이동한다. 선택 자체로 리딩을 생성하지 않는다. 3개 viewport에서 시작 bottom717px, 신규1+기존 관련4개 브라우저 테스트 통과, RN/Web 타입·Web/Android 빌드와 실제 에뮬레이터 진입을 확인했다. 이 수치를 이전 전체39와 더하지 않는다.

RN PR#4 / Web PR#4의 소스 커밋33f0ce0d / 031709cd 기준이다. 이번 보고서 갱신은 문서·사진·영상만 변경하며 앱 코드·테스트·Backend는 그대로다. 최신 설정 FPS·실기기 촉감·새 OTA 다운로드/적용 완료를 주장하지 않는다.

## 이전 비AI 구현 감사 스냅샷

아래의 경로 수·source cut·16쪽 PDF 언급·로그 값은 최신 화면 수정 전의 보존된 이력이다. 현재 PDF와 최신 설정의 상태는 위 갱신 및 compact-setup/evidence.json을 따른다.

# 비AI 구현 현황과 개발자 인계

2026-11-06 · 여운 Android/Web의 비AI 기능과 사용자 흐름을 구현하고 서버·클라이언트·브라우저·Android 검증을 마쳤다. 제품 구현에 관련된 285개 경로, 이후 CI와 자동 생성 계약을 포함한 전달 기준 289개 경로가 바뀌었다. 이 근거 수집 단계에서는 실제 API·DB 구조·diff를 읽고 report-data만 작성했으며 제품 코드와 기존 테스트를 추가 변경하지 않았다. iOS 코드 부재, 실제 외부 활성화와 운영 정책은 구현 완료와 구분한다. 통합 담당자는 최종 16쪽 PDF를 검증하고 Notion 비공개 초안에 저장했다.

## 결과와 집계 기준

기존 P0/P1에 초안·기록 복귀/원본 연결, 감각 수명주기, 계정 전환, Google Play 구매 경계, 한 편의 이야기, 비AI 운영관리와 다이얼·마음 확인·카드 공개를 연결했다. 기능 코드가 있다는 사실, 검증 통과, 외부 활성화를 구분한다. iOS 소셜·결제 코드는 미구현이며 실제 OAuth/스토어 등록도 하지 않았다.

| 단위 | 수집값 | 해석 |
|---|---:|---|
| 기본 요구 | 68 | 비AI46·AI동결14·혼합8이라는 이번 감사 분류 |
| API paths / operations | 81 / 99 | path와 method/path를 따로 집계; 기능 수 아님 |
| OpenAPI schema | 119 | 인증/422 실행 계약은 보충 MD와 함께 읽음 |
| operation 버전 구분 | v2 83 / v1 15 / root health 1 | 합계 99; /v2/health는 v2에 포함 |
| DB table / column | 46 / 323 | 사용자 행을 조회하지 않은 구조 메타데이터 |
| DB constraint / FK | 176 / 53 | 실제 FK만 ERD 간선으로 표현 |
| 변경 경로 | 289 | RN/Web 대응 파일도 저장소별 경로로 집계 |
| 기존 테스트 M/D | 0 | baseline에 존재하던 테스트 수정/삭제 없음 |
| 최초 문서 hash | 387개 before 기록 보존 | 386개 동일·통합 담당자의 생성 계약 갱신 1건을 별도 표시; 전체 정독을 의미하지 않음 |

원본30p·상세54p·이전 현황9p·개발해설14p, 과거 Flutter/market/감각 자료의 출처·구절·읽기 범위는 상위 document-reconciliation.json에 있다. 과거 인벤토리372파일/307unique는 metadata·prototype·복사본도 포함한다. 이를 독립 확정 요구372개 또는 전체 정독으로 표시하지 않는다.

## 소스 기준과 변경

| 저장소 | 최종 제품 source cut | CI/계약을 포함한 전달 head |
|---|---|---|
| taro | d8576628bce18f4d5c38d45b4813b80c44b55ca9 | e24133de5657f15b2bca0e962b0092ef5333d7e5 |
| taro-raect | 00ab7343ba6645b4191d19cd82098cbf64f58993 | a1ddc2983c3f97aebc51b109b7c07d8169449133 |
| taro-back | b152d5644cac57340fb2f05eadfc3328f2b29648 | 65fce63ae3bd556393f047ec0a76db14f67a6ccb |

`changed-files-product-cut.csv`는 baseline부터 최종 제품 cut까지 285개 경로, `changed-files.csv`는 전달 head까지 289개 경로다. 전달 증분은 신규 .github/workflows/checks.yml 3개와 backend 생성 OpenAPI 1건이다. 이 289개는 아래 SHA의 스냅샷이며, 이후 보고서·Git 문서 추가 경로는 통합 담당자의 별도 all-changed-files.csv에 기록한다. working tree 상태는 test-preservation.json에 따로 남긴다.

| 저장소 | 변경 경로 | 추가 | 수정 | 기존 테스트 M/D |
|---|---:|---:|---:|---:|
| taro | 108 | 66 | 42 | 0 |
| taro-raect | 111 | 75 | 36 | 0 |
| taro-back | 70 | 57 | 13 | 0 |

AI 알고리즘·모델·프롬프트·제공자·전사 개발은 이번 범위에서 제외됐다. 다만 P0Api.ts, useP1.ts, P0.tsx/P0Tarot.tsx/P1.tsx, App.tsx, composition.ts, backend main.py 같은 혼합 파일은 계정 격리·member kind·조립·기록/시각 수명주기 때문에 실제로 변경됐다. “AI 파일은 전혀 수정하지 않았다”라고 표현하지 않는다. 파일 변경과 알고리즘 변경을 구분한다.

## 기본 경험·기록

새 게스트 → 실제 일일 카드/basic → source-linked 마음기록 저장 → 기록 목록을 실제 로컬 API에서 확인했다. basic을 GPT 결과로 표시하지 않았다. 현재 v2 서버가 일일1장/고민3장 슬롯·버전·순서를 검증하고 일기 편집은 optimistic version을 사용한다. 전체 앱에서5장 기능이 없다는 뜻은 아니다. 기존 Topics의 legacy1/3/5장 chips와 호환 route는 baseline UI로 남아 있으며 이번 범위 밖 삭제/변경하지 않았다.

기록은 종류/날짜 필터·paging/scroll memory를 보전한다. inactive Records DOM은 unmount하고 PageMemory를 별도로 유지한다. owner 또는 원본 membership 변경은 이전 selection/dashboard/cache를 재사용하지 않는다. 서버 소유 source를 열고 삭제된 원본은 없음 안내를 한다.

근거: api/application/reading_v2.py, journal_v2.py, src/screens/Journal.tsx, features/navigation/RecordViewState.ts, useP0Pages.ts, tests/record-cache.test.ts, p0-journal-source.test.ts.

## 감각·카드·다이얼

Character/BreathingDial은 active에서만 반복 자원을 실행한다. background/inactive/unmount는 자원을 멈추고 이전 callback을 무효화한다. capture마다 독립 audio lease를 발급해 중첩/늦은 종료가 새 capture mute를 해제하지 못한다. 새 native는 이미 재생 중인 stream도 중단한다. OS 음성 인식기 자체의 시작/종료음은 앱 제어 밖이다.

실제 기본값 발췌: src/features/audio/preferences.ts.

```typescript
export const defaultSensory:Readonly<SensoryPreferences>=Object.freeze({sound:false,haptics:true,music:false});
```

기존 저장 선택은 보존한다. 효과음/BGM은 별도 fixed gain이며 사용자 슬라이더 계약은 없다. 서버 preferences에 없는 sound/music/haptics를 원격 동기화했다고 주장하지 않는다.

카드 이미지 decode 후 앞/뒷 Animated face를 각각 회전시킨다. 과거 flat backface가 녹색 거울 뒷면으로 남는 현상은 root pixel RED→GREEN과 실제 재촬영으로 해결을 확인했다. 이전 qa/card-geometry.json과 새 card-geometry-verified.json을 구분해 보존한다. 원본 그림·색을 바꿔 문제를 숨기지 않았다.

주제/슬롯은270도 clamp·키보드/버튼 대안을 제공한다. 슬롯 탐색만으로 고른 카드가 바뀌지 않고 별도 CTA가 기존 server slot ID를 전달한다. Mood 탐색/취소는 일기 초안을 바꾸지 않으며 확인 시트 후 반영한다. 최종 저장은 폼의 저장 CTA다.

실제 순수 규칙 발췌: src/features/motion/dial.ts.

```typescript
export function dialAngle(index:number,count:number,circular=false){return circular?Math.max(0,index)/Math.max(1,count)*360:-135+clamp(index,0,Math.max(0,count-1))/Math.max(1,count-1)*270;}
```

## 계정과 기기 연결

Google/Apple provider 상태·PKCE start/callback/exchange·세션 목록/기기별 해제/전체 로그아웃을 연결했다. 최초 게스트 연결은 같은 owner를 유지한다. 기존 계정으로 이동하면 원래 guest 서버행을 보전하고 로컬 초안/pending은 owner별 opaque archive에 둔다. 자동 email merge나 초안 합치기는 없다.

전환은 Store/navigation unmount → HTTP 중단/세대 검사 → 진행 중 local 작업/storage drain → owner archive/restore → generation-safe 연결 교체 → 새 Store mount 순서다. 이미 서버에 도달한 mutation의 결과를 취소 성공으로 가정하거나 자동 재전송하지 않는다.

실제 정리 순서 발췌: AccountRuntime.prepare.

```typescript
await this.http.pause();await Promise.allSettled([...this.localTasks]);await this.cache.drain();
```

Android PKCE는 SecureRandom/SHA256, Web은 WebCrypto다. 이전 앱/미지원 환경은 비활성 안내다. iOS 동등 모듈은 아직 없다. Web guest 보관은 기존 browser localStorage 신뢰 경계, Android는 별도 Keychain 서비스다. 서버 기록의 같은 owner 접근과 기기 로컬 초안/감각 설정의 원격 동기화를 혼동하지 않는다.

## 구매·유료 덱

Google Play 상품/offer/가격은 스토어가 관리한다. 서버는 검증된 receipt·owner/member·기간·교체·취소/환불 상태에서 권한을 계산하고 유료 덱은 최신 권한을 확인한다. 결제창 취소·구독 해지·환불·계정 데이터 삭제를 각각 안내한다.

실제 상품/제공자 미설정 화면을 확인했다. 임의 가격이나 모의 성공 구매를 최종 실결제 증거로 쓰지 않는다. Web은 권한 조회만 제공하고 구매/복원은 지원 Android 스토어 앱에서 한다. iOS AppStore adapter/구매·복원 verifier는 없다.

구조를 설명하는 응답 일부(인증 값 생략): api/adapters/http/billing.py.

```json
{"store":"google_play","available":false,"reason":"catalog_not_configured","products":[]}
```

## 한 편의 이야기

window-pause@1.0.0 자작 에피소드는3선택 후3결말 중 하나로 끝난다. node·aggregate version·owner·멱등 요청을 확인하고 재시작 후 서버 진행을 조회한다. 다시 보기는 새 진행을 만들지 않으며 다시 시작은 명시 확인으로만 한다. 점수·재화·랜덤 보상·확률 결제는 없다.

실제 계약 예시. 현재 owner 인증값과 요청식별 값은 싣지 않았다.

```http
POST /v2/story-runs/<run_id>/choices
Content-Type: application/json

{"expected_version":1,"node_id":"<현재 node id>","answer":true}
```

근거: api/application/story.py, domain/story.py, adapters/story_catalog.py, migrations/0023_story.sql, src/screens/Story.tsx, tests/story.test.ts.

## 비AI 운영관리

별도 짧은 운영 세션·viewer/publisher·권리 검토와 명시적 게시/실제 publication rollback·피드백 triage/CAS·감사·OTA policy metadata를 구현했다. 기본 역할/후보 publication을 생성하지 않는다.79자산의 근거와 immutable snapshot을 확인한다. 실제 운영 역할 grant/후보 게시를 실행하지 않았다.

동일 개인정보 트랜잭션에서 admin export는 본인 역할·세션·본인 actor 감사의 공개 metadata만 제공한다. erase는 역할/세션 제거와 감사 owner 익명화를 한다. 타인 feedback 본문이나 credential을 export하지 않는다. audit_retention_days=None은 자동삭제 비활성 기본이며 운영 정책 응답 대기다.

근거: api/application/admin.py, admin_ports.py, adapters/admin_postgres.py, migrations/0024_admin.sql, src/screens/Admin.tsx, features/admin, api/tests/integration/test_admin_postgres.py.

## 실제 API와 runtime 보충

원본 actual-openapi.json, operation/auth/error CSV와 runtime-auth-error-contract.md를 한 쌍으로 읽는다. 자동 schema의 optional Header/securitySchemes 부재를 공개 API 근거로 쓰지 않는다. GET admin/eligibility는 의도적으로 미인증/무효 연결에 false를 반환하지만 admin 세션 발급과 이후 API는 별도 권한을 검사한다.

422 기본 HTTPValidationError 선언과 실제 /v2/ 중앙 envelope가 다르고 callback 기본200 선언과 정상303 redirect도 구분한다. 이번에는 API 동작을 더 바꾸지 않고 원본과 보충을 함께 납품한다.

## DB와 ERD

read-only transaction에서 information_schema와 pg_constraint 관련 시스템 metadata만 조회했다. application table의 사용자 행·row count·원문·결제 값을 읽지 않았고 접속 비밀값은 산출물에 넣지 않았다.

| 영역 | 실제 테이블 |
|---|---|
| social_ | social_accounts, social_auth_flows, social_device_sessions, social_identities |
| billing_ | billing_receipts |
| story_ | story_requests, story_runs |
| admin_ | admin_audit, admin_catalog_heads, admin_catalog_publications, admin_catalog_versions, admin_feedback_triage, admin_ota_policy, admin_roles, admin_sessions |

전체46테이블/53FK는 schema-erd.mmd에 모두 있다. PK/UK/FK·nullable을 표기하고 FK 없는 source ID/JSON 참조를 임의 간선으로 추가하지 않는다. nullable/unique 기반 최대 관계가 도메인의 최소 cardinality 보장은 아니다.

## 최근 확인된 검증

다음은 root 제공 suite 수치다. 이 수집기가 제품 테스트를 재실행한 값이 아니다. 같은 파일을46요구에서 반복 참조해도 테스트를 중복 합산하지 않는다.

| 검증 | 최근 제공 결과 | 근거/잔여 |
|---|---|---|
| backend |532 PASS|backend-final.log, isolated final test DB|
| RN/Web 단위 |133/130 PASS|taro-verified-unit.log / taro-raect-verified-unit.log|
| 양쪽 typecheck/Web build |PASS/PASS|root 제공|
| 전체 browser |39 PASS|browser-delivery.log|
| native JVM |6 PASS|root 제공|
| Android |APK1.4.0/code5 build PASS|android-delivery-build.log SUCCESS54s|
| 실제 화면 |사진8·18초 영상|qa/summary.json, 별도 테스트 수로 추가하지 않음|
| text2x |360/430×4화면8조건|필수CTA clipping/가로넘침0, ±ink 관찰은 별도|

computed px×2 주입은 OS/native 글자 확대나 모든 스크린리더/기기 접근성 인증이 아니다. 이전 storageState context timeout과 backface 문제도 first-attempt에 남겨 성공 증거와 구분했다.

### 원격 CI 확인

기존 workflow scope를 확인했고 재인증 없이 활성 push했다. 세 대표 push 실행이 모두 SUCCESS다. 원격·로컬 테스트 수를 합산하지 않는다.

| 저장소 | 정확한 head | 결과 | 링크 |
|---|---|---|---|
| tarobo22/taro | e24133de5657f15b2bca0e962b0092ef5333d7e5 | success | [실행](https://github.com/tarobo22/taro/actions/runs/42199149778) |
| tarobo22/taro-raect | a1ddc2983c3f97aebc51b109b7c07d8169449133 | success | [실행](https://github.com/tarobo22/taro-raect/actions/runs/42199157893) |
| tarobo22/taro-back | 65fce63ae3bd556393f047ec0a76db14f67a6ccb | success | [실행](https://github.com/tarobo22/taro-back/actions/runs/42199165757) |

### 소표본 API 왕복 지연

GET별 warmup1+순차20표본, concurrency1, full-response round trip, nearest-rank p95다. 부하시험·운영 p95 달성 선언이 아니다. qa/api-latency.json 원본을 참조한다.

| GET | path | 표본 p95 ms | max ms |
|---|---|---:|---:|
| bootstrap | /v2/bootstrap | 49.154 | 82.106 |
| catalog | /v2/decks?limit=20 | 46.68 | 76.31 |
| story_list | /v2/story-episodes | 20.817 | 26.697 |
| journal_first_page | /v2/journal-entries?limit=20 | 19.11 | 20.096 |

## 0.2 설계값과 root 범위 해석

root는 현재 요청의 범위 해석으로 D03/D09/D10을 보강해 병합했고 GREEN으로 확인했다. 이 판단은 별도의 새 사용자 승인이 아니다. 이 근거 수집 단계에서는 제품 코드를 바꾸지 않는다. 아래 관측값은 최종 제품 cut의 구현값이며 이전 관측은 JSON before_batch_value로 보존한다.

| 항목 | 문서값 | 관측값 | 상태 |
|---|---|---|---|
| D03 세 장 stagger | 0/220/440ms 시작·총860ms | ReadingV2 delay=(position-1)*220 →0/220/440ms·총860ms | root 범위해석 보강 완료·최종 source cut GREEN |
| D05 캐릭터 idle | 3.8초 주기·최대1.5px | motion.breath2800ms를 왕복2회=5.6초; 0→-1.5px | 주기 차이·진폭 일치; 초기 수치 채택 해석 필요 |
| D06 NO/YES 확정 거리 | 56dp 수평 확정 | width*.18: 360/390/430뷰에서56.16/61.56/68.76dp | 기능은 있음·값 차이; 반응형 거리 채택 해석 필요 |
| D08 press 토큰 | scale0.98/release120ms | 실제Button .975, spring speed35/bounciness2; motion.press120 토큰은 별도 선언 | 초기 디자인 토큰과 실제 연출 차이; 기능 없음 아님 |
| D09 카드 선택 강조 | 선택강조180ms | TarotSelectionMotion의 선택offset 0/-4를180ms로 전이, reduced/background는 정적·이전animation중단 | root 범위해석 보강 완료·최종 source cut GREEN |
| D10 재섞기 opacity | 짧은 opacity(정확숫자없음) | TarotShuffleMotion은 서버version변경때 .55→1/180ms; 초기mount/reentry/background/reduced는정적 | root 범위해석 보강 완료·최종 source cut GREEN |
| D11 화면 전환 | 160ms opacity/8px | App opacity200ms; 추가8px 이동없음 | 최신 사용자 보존 지시가 우선·변경안함 |
| D12 dial feedback 최소간격 | 값 변경시만 tick·최소60ms 초기제안 | 동일값 tick 차단·공통 feedback guard55ms | 중복방지 충족, 간격55vs60 초기값 차이 |
| D16 audio loop crossfade | 플레이어100ms crossfade 지원 권장 | MediaPlayer/Audio pause/resume, crossfade 구현 없음 | 권장사항·승인 필수기능으로 자동확대안함 |
| D17 Canvas/입자 | 장면Canvas1·입자최대10·배경루프중단 | 해당 장식Canvas/particle시스템 미도입; 실제 반복자원은AppStategate | 사용시상한·새장식도입요구 아님 |

18항목의 구절·코드 줄은 design-values-reconciliation.csv/json에 있다. D05/06/08/12는 초기/제안값의 차이를 보고에 유지하고 큰 속도/터치 경계를 추가 변경하지 않는 root 해석이다. D11은 사용자 App opacity 보존 지시 우선이다. crossfade/Canvas는 권장·사용시 제약으로 유보한다.

## Android 실화면·짧은 HWUI 표본

root가 새 com.yeoun.app.nonai14 검증 앱에서 확인한 PNG를 근거로 기록한다. 기존 앱과 com.yeoun.app.p1verify는 그대로 두었다. artifact 위치는 output/taro-non-ai-20261106/native/이며 hash/크기는 native-evidence-summary.json에 있다.

| PNG | root의 직접 시각 확인 |
|---|---|
| home-final.png | Home flex correction visually normal |
| daily-front.png | Actual Knight of Cups original front |
| story-resumed-ready.png | left NO swipe then back→server resume at1 choice |
| story-two-ready.png | right YES then2 choices |
| story-ending.png | 3 choices→고요한 한 걸음 |
| account-ready.png | Actual providers disabled |
| billing-ready.png | Actual catalog0 and no fabricated price |
| final-font2-dial.png | OSfontScale2 actual daily: center4/6 plus full-width 오늘 하루, no circle/label overlap |
| final-font2-next.png | Direct plus input: center5/6 plus full-width 마음 돌봄 |

Home HWUI 약19.596초/1166frames에서 janky7(0.60%), p95 18ms, meminfo TOTAL PSS103795KiB(약101.36MiB) 표본이 있다. gfx-home.txt의 요약/Activity에 같은1166 값이 반복되므로 두 번 더하지 않는다. 모든 화면/실기기·배터리·물리 display FPS·프레임워크 비교 결과가 아니다.

실제 OS fontScale 2에서 발견한 원 테두리 겹침은 fontScale>1.3일 때 인덱스를 원 안에, 문구를 아래 전체 폭 영역에 분리해 수정했다. final-font2-dial.png의 4/6과 ‘오늘 하루’, final-font2-next.png의 직접 + 입력 후 5/6과 ‘마음 돌봄’을 시각 확인했다. 기존 실패 PNG를 보존하고 font_scale 1.0으로 복원했다. 브라우저 computed px×2 주입과 실제 OS 확대는 다른 검증이며 모든 접근성 인증은 아니다.

최종 Android APK는 1.4.0/code 5, 41,238,969 bytes이며 SHA256은 8bd17873a66ff0221e46663293f0ed2da95a6e9af08c54648d6e07f3fa85191c이다. android-delivery-build.log에서 BUILD SUCCESSFUL(54초)을 확인했다.

native helper에서 uiautomator idle실패뒤구XML을읽는문제를발견해이제stale XML을거절한다. 최종 실화면 단계는 위 PNG 시각 확인을 근거로 삼으며 아무 오류도 없는 자동화 전체 통과라고 표현하지 않는다.

## 미활성·코드 부재·운영 유보

- 실제 Google/Apple 등록·credential·allowlist·HTTPS/SPA fallback은 미설정이며 실제 OAuth를 실행하지 않았다.
- 실제 Google Play 상품/offer/국가·서비스권한/package 서명·스토어 설치 sandbox 구매/복원/환불은 남았다.
- iOS 소셜·결제는 제품 코드가 없고 macOS/Xcode·서명 환경도 없다. 설정만 하면 끝나는 항목이 아니다.
- 최종 외부 덱 그림체·국가별 권리·역할 grant/권리 승인/publication은 운영 단계다.
- root 보고상 OTA server 실행이 자동승인 검토에서 거절됐다. 이 수집에서 재시도/우회하지 않았다. hosting/HTTPS/서명/채널과 운영 배포도 별도다.
- 초기CI권한우려는기존scope재확인후해소됐다. 재인증없이신규workflow활성push를했고RN/Web/Back대표push실행3개모두SUCCESS다. 정확한SHA/URL은ci-evidence-summary.json과ci-runs.json에있다.
- audit retention 정책 응답/결정이 없어 None 기본을 유지한다. 임의 기간을 정하지 않았다.
- 실제 native FPS/열/배터리/터치·광범위 접근성·운영 부하는 소표본/빌드로 대체하지 않는다.

## 요구68 분류와 비AI46 매핑

AI전용14는 이전 상태를 유지했다. 혼합8은 비AI 보전/계정/운영 변경과 AI 의미를 나눴다. 전체68행은 requirements-68-classification.csv, 비AI46행의 코드·검증·활성화·한계는 non-ai-requirement-evidence.csv/json이다.

AI동결14: FR-TAR-09, FR-CON-05, FR-CON-08, FR-VOI-03, FR-VOI-04, FR-JOU-07, FR-AI-01, FR-AI-02, FR-AI-03, FR-AI-04, FR-AI-05, FR-AI-06, FR-AI-07, FR-AI-08.

혼합8: FR-CON-04, FR-CON-06, FR-VOI-02, FR-PRI-01, FR-PRI-02, FR-PRI-03, NFR-02, P1-03. P1-03은 비AI 운영 부분을 구현했고 AI 프롬프트/모델/사용량 운영 화면은 제외했다.

| ID | 요구 | 구현/검증 분류 | 활성화 |
|---|---|---|---|
| FR-ID-01 | 게스트 연결 | 구현·검증 근거 연결 | 현재 게스트 이용 가능 |
| FR-ID-02 | 시간대·언어 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-ID-03 | 세션 연속성 | 구현·검증 근거 연결 | 게스트 세션은 활성. 실제 소셜 로그인은 공급자 등록 필요 |
| FR-TAR-01 | 오늘의 한 장 | 구현·검증 근거 연결 | 후보 덱으로 현재 기본 기능 가능 |
| FR-TAR-02 | 고민 주제·질문 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-03 | 외부 78장 덱 | 후보 구현·권리/발행 미활성 | 후보 사용은 가능; 최종 그림체·국가별 권리 검토·게시 승인 필요 |
| FR-TAR-04 | 선택 전 비공개 슬롯 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-05 | 세 장 선택·순서 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-06 | 다시 섞기 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-07 | 리딩 위치 정의 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-08 | 기본 해석 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-10 | 카드 상세 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-11 | 리딩 기록·삭제 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-TAR-12 | 정역방향 정책 | 현재 기본 정책 구현; 역방향 확장 제외 | 현재 정방향1장/3장 사용 |
| FR-CON-01 | 대화 시작 | 구현·검증 근거 연결 | 기본 로컬 가이드 사용 가능; AI 제출 경로는 이번 개발에서 동결 |
| FR-CON-02 | 좌 아니요·우 예 | 구현·검증 근거 연결 | 현재 조작 가능 |
| FR-CON-03 | 선택 질문 3개 | 구현·검증 근거 연결 | 기본 로컬 가이드 사용 가능; AI 제출 경로는 이번 개발에서 동결 |
| FR-CON-07 | 말투 두 가지 | 구현·검증 근거 연결 | 기본 말투/설정 활성 |
| FR-VOI-01 | 사용자 시작·중지 | Android/Web 코드·생명주기 검증; 실기기 품질 대기 | Android 인식서비스/권한과 지원 브라우저 SpeechRecognition 필요 |
| FR-JOU-01 | 기분과 글 작성 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-JOU-02 | 초안 보존 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-JOU-03 | 조회·수정·삭제 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-JOU-04 | 통합 기록 목록 | 구현·검증 근거 연결 | 현재 사용 가능 |
| FR-JOU-05 | 결과에서 기록 연결 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-JOU-06 | 기분 통계·달력 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-JOU-08 | 즐겨찾기 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-PRI-04 | 내보내기·전체 삭제 | 구현·검증 근거 연결 | 현재 데이터 기능 사용 가능 |
| FR-PRI-05 | 소유권·로그 최소화 | 구현·검증 근거 연결 | 현재 owner 격리 사용 |
| FR-DES-01 | 파스텔·적은 색 | 구현·검증 근거 연결 | 현재 표현 사용 가능 |
| FR-DES-02 | 얼굴 코드·몸 이미지 | 구현·검증 근거 연결 | 현재 표현 사용 가능 |
| FR-DES-03 | 7개 표정 | 구현·검증 근거 연결 | 현재 표현 사용 가능 |
| FR-DES-04 | 잘림 없는 프로필 | 구현·부분 시각/접근성 검증 | 현재 기본 폭 동작 |
| FR-DES-05 | 모션·줄이기 | 타로0.2 보강·lifecycle/시각 구현 검증; 초기값 차이 별도 | 새 native binary에서 active stream mute·captureId 지원 |
| FR-DES-06 | 뒤로가기·스크롤 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| FR-OPS-01 | 코드푸시 | preview·호환 구현; 운영 활성화 미완료 | 운영 hosting/HTTPS/서명·채널 필요; OTA server 실행은 root 보고상 자동 승인 거절 |
| FR-OPS-02 | 호환·복구 정책 | preview·호환 구현; 운영 활성화 미완료 | 운영 hosting/HTTPS/서명·채널 필요; OTA server 실행은 root 보고상 자동 승인 거절 |
| FR-OPS-03 | 외부 에셋 추적 | 후보/권리 운영 코드; 실제 승인 미실행 | 실제 권리 검토·운영자 역할·최종 덱 publish 필요 |
| FR-OPS-04 | 피드백 | 구현·검증 근거 연결 | 사용자 피드백 활성; 실제 운영자 계정은 명시 역할 발급 필요 |
| NFR-01 | 접근성 | 부분 검증·플랫폼 최종 QA 대기 | 기본 접근성 도구 사용 가능 |
| NFR-03 | 오프라인·빈 상태 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| NFR-04 | 동시성·데이터 보전 | 구현·검증 근거 연결 | 외부 활성화 없이 현재 서버/앱에서 사용 가능 |
| NFR-05 | 문서 추적성 | 자료 수집·현재 추적; 계약 표현 차이 명시 | 원본 PDF는 root 별도 제작 |
| P1-01 | 기기 알림 | Android 구현·기기 정책 제한 | Android 권한·정확알람/배터리 정책에 따름 |
| P1-02 | 계정·기기 동기화 | Android/Web 구현·기능 검증; 실제OAuth 미활성 | 실제 Google/Apple 등록/return allowlist/credential 주입·HTTPS/SPA fallback 필요 |
| P2-01 | 결제·유료 덱 | GooglePlay 구현·기능 검증; 실제상품 미활성 | 실제 product/offer/판매국/서비스권한/package-signature·Play설치 sandbox검증 필요 |
| P2-02 | 분기 스토리 게임 | 1편·이어하기 구현/검증 | 현재 에피소드 사용 가능 |

비AI 46개 요구의 구현 경로가 있어도 모든 플랫폼과 실측이 완료됐다고 추정하지 않는다. 알려진 iOS 코드 부재 외에 새 승인 클릭 흐름이나 기능의 코드 부재는 확인하지 못했다. 타로 0.2 보강, 최종 Home 코드와 실제 OS fontScale 2 확인을 마쳤다. 실제 공급자·스토어 활성화, iOS 구현과 운영 정책은 별도이며 모든 접근성·운영 성능 보장을 의미하지 않는다.

옛 R14공유·R15analytics·S08검색,4캐릭터/123자산/32모션 양산안, 가격 실험, Flutter 전환은 현재 승인 기능에 자동 편입하지 않았다. 역방향/추가spread, Canvas·crossfade 권장도 분리했다. 아키텍처 개선 제안은 기능 누락과 다르다.

## 인계와 보전

원본 OpenAPI와 runtime 보충, schema metadata와 전체 FK ERD, baseline diff와 테스트 보전 검사, 요구 68개와 비AI 46개 매핑, 디자인값·범위 해석, 활성화 한계와 이 현황 보고서가 인계 자료다. 최초 387개 문서의 before hash 파일을 그대로 보존했다. 386개는 동일하고 backend 생성 OpenAPI 1건은 root가 50 paths/66 operations에서 81 paths/99 operations로 의도적으로 갱신했다. 이 근거 수집 단계에서는 기존 문서를 덮어쓰지 않고 report-data 아래에만 작성했다. 실제 인증정보·키·테스트 비밀번호값과 사용자 행은 보고서에 없다.

통합 담당자가 현재 보고서를 [Notion 비공개 초안](https://app.notion.com/p/410b37c6fe7281249a4dfa62b85a3c46?pvs=204)에 실제 저장하고 readback 및 16쪽 PDF embed를 확인했다. 최종 PDF는 output/pdf/yeoun-non-ai-implementation-20261106.pdf이며 SHA256은 `6d516a6a7055177eff04a4eb8c3c8a89a6941e3db0d2543def6a9e63051a7b76`이다. root가 전체 페이지 렌더·시각 QA를 완료했다. 기존 Notion 자료와 원본 기획 PDF 4개는 보존했다. 이 수집 단계에서는 Notion 쓰기나 PDF 제작을 별도로 반복하지 않았다.

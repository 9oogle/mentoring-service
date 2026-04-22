# Mentoring Service

## 로컬 개발 환경 설정

### GitHub Packages 인증

이 프로젝트는 `com.goggles:common-library`를 GitHub Packages에서 가져옵니다.
로컬에서 빌드하려면 GitHub Personal Access Token(PAT)이 필요합니다.

**1. PAT 발급**

GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)

권한: `read:packages`

**2. gradle.properties 설정**

```bash
cp gradle.properties.template gradle.properties
```

`gradle.properties`를 열어 발급받은 값으로 채웁니다.

```properties
GitHubPackagesUsername=깃허브_유저네임
GitHubPackagesPassword=발급받은_PAT
```

> `gradle.properties`는 `.gitignore`에 등록되어 있으므로 커밋되지 않습니다.
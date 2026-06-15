# ANDPAD Kotlin 版 (`andpad_kot`)

[`andpad_j`](../andpad_j) の Java バックエンドを **Kotlin** に移植したリポジトリです。  
フロントエンドは Next.js 15 のまま、**スマートフォン向け viewport / レスポンシブ UI** を有効にしています。

## 構成

| パス | 内容 |
|------|------|
| `backend/` | Spring Boot 3.5 · **Kotlin** · JDBC · Spring GraphQL |
| `frontend/` | Next.js 15（`andpad_j` と同一 UI、API を Kotlin API に向ける） |
| `graphql/schema.graphql` | GraphQL スキーマ（共有） |

## クイックスタート

### 1. PostgreSQL

```powershell
cd C:\devlop\andpad_kot
docker compose up -d db
```

Postgres: `localhost:5434` / user `andpad` / password `andpad` / db `andpad`

### 2. API（Spring Boot + Kotlin）

```powershell
cd backend
$env:DATABASE_URL="jdbc:postgresql://localhost:5434/andpad"
$env:DB_USER="andpad"
$env:DB_PASSWORD="andpad"
$env:JWT_SECRET="dev-local-secret-minimum-32-characters"
.\gradlew.bat bootRun
```

- GraphQL: http://localhost:8080/graphql
- Health: http://localhost:8080/health

### 3. Web（Next.js）

```powershell
cd frontend
npm install
npm run dev
```

http://localhost:3000 — デモ: `demo@sakura-dental.jp` / `demo1234`

スマートフォンから同一 Wi‑Fi 内の PC にアクセスする場合は `http://<PCのIP>:3000` を開いてください。

### まとめて起動

```powershell
npm install
npm run install:all
npm run dev
```

## テスト

```powershell
cd backend
.\gradlew.bat test
```

99 件の統合テスト（Testcontainers PostgreSQL）が Kotlin で動作します。

## `andpad_j` との違い

| 項目 | andpad_j | andpad_kot |
|------|----------|------------|
| バックエンド言語 | Java 21 | **Kotlin** (JVM 23 toolchain) |
| ビルド | `build.gradle` | `build.gradle.kts` |
| Lombok | 使用 | **不使用**（data class / 主コンストラクタ DI） |
| フロント | PC 中心 | **viewport + タッチ向け CSS** |

## Railway デプロイ

`railway.toml` / `Dockerfile.unified` を利用（`build.gradle.kts` 対応済み）。

## ライセンス

社内デモ用途（`andpad_j` と同様）

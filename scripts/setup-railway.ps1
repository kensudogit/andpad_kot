# ANDPAD Kotlin 版 — Railway 初回セットアップ
# 使い方: cd C:\devlop\andpad_kot && .\scripts\setup-railway.ps1

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "=== Railway link (discerning-transformation / production) ===" -ForegroundColor Cyan
railway link -p discerning-transformation -e production

Write-Host "`n=== Create service andpad_kot (skip if exists) ===" -ForegroundColor Cyan
railway add --service andpad_kot

Write-Host "`n=== Link this directory to andpad_kot service ===" -ForegroundColor Cyan
railway link -p discerning-transformation -e production -s andpad_kot

Write-Host @"

=== Variables (Railway Dashboard → andpad_kot → Variables) ===
  DATABASE_URL  → Reference → Postgres plugin
  JWT_SECRET    → 32+ char random string (NOT OpenAI key)
  OPENAI_API_KEY → optional, for AI chatbot

Do NOT set API_URL on unified deploy.

=== Deploy ===
  railway up

=== Verify ===
  curl https://<your-domain>/health
  curl https://<your-domain>/api/status

"@ -ForegroundColor Yellow

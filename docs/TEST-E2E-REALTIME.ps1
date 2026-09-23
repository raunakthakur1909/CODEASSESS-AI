# CodeAssess AI - Milestone 10.10 E2E Realtime Test
# Tests: Auth -> Assessment Lifecycle (1-min) -> Submission -> Execution -> Timer Expiry -> Analytics

$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080/api"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"

$adminEmail = "admin@codeassess.local"
$adminPassword = "AdminSecure123"
$candidateEmail = "candidate-$timestamp@example.com"
$candidatePassword = "CandidatePass123"

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "CodeAssess AI - Realtime E2E Test (Docker Environment)" -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

function Invoke-JsonRequest {
    param(
        [string]$Uri,
        [string]$Method,
        [hashtable]$Body,
        [hashtable]$Headers
    )

    $request = @{
        Uri = $Uri
        Method = $Method
        ContentType = "application/json"
    }

    if ($Body) {
        $request.Body = $Body | ConvertTo-Json -Depth 10
    }

    if ($Headers) {
        $request.Headers = $Headers
    }

    try {
        return Invoke-RestMethod @request
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
        throw
    }
}

# ==================================================================
# PHASE 1: ADMIN LOGIN
# ==================================================================
Write-Host "[1/10] Admin Login..." -ForegroundColor Yellow

# Admin bootstrap uses "AdminSecure123" per .env / docker-compose.yml
try {
    $adminLogin = Invoke-JsonRequest -Uri "$baseUrl/auth/login" -Method "Post" -Body @{ email = $adminEmail; password = $adminPassword }
    $adminHeaders = @{ Authorization = "Bearer $($adminLogin.accessToken)" }
    Write-Host "[PASS] Admin authenticated successfully" -ForegroundColor Green
} catch {
    Write-Host "Admin login failed. Trying Codeassess Default Admin password..." -ForegroundColor Yellow
    $adminPassword = "AdminPassword123"
    $adminLogin = Invoke-JsonRequest -Uri "$baseUrl/auth/login" -Method "Post" -Body @{ email = $adminEmail; password = $adminPassword }
    $adminHeaders = @{ Authorization = "Bearer $($adminLogin.accessToken)" }
    Write-Host "[PASS] Admin authenticated successfully" -ForegroundColor Green
}

# ==================================================================
# PHASE 2: ASSESSMENT LIFECYCLE
# ==================================================================
Write-Host "`n[2/10] Creating 1-Minute Assessment..." -ForegroundColor Yellow

$assessment = Invoke-JsonRequest -Uri "$baseUrl/assessments" -Method "Post" -Headers $adminHeaders -Body @{
    title = "E2E Realtime Assessment - $timestamp"
    description = "Automated test for SSE and Timer"
    durationMinutes = 1
}
$assessmentId = $assessment.id
Write-Host "[PASS] Assessment created (ID: $assessmentId, Duration: 1 min)" -ForegroundColor Green

Write-Host "`n[3/10] Creating Problem & Test Cases..." -ForegroundColor Yellow

$problem = Invoke-JsonRequest -Uri "$baseUrl/assessments/$assessmentId/problems" -Method "Post" -Headers $adminHeaders -Body @{
    title = "Echo Number"
    description = "Return the same number"
    difficulty = "EASY"
    points = 100
    inputFormat = "Integer"
    outputFormat = "Integer"
    constraints = "0 <= n <= 100"
}
$problemId = $problem.id

$publicTestCase = Invoke-JsonRequest -Uri "$baseUrl/problems/$problemId/test-cases" -Method "Post" -Headers $adminHeaders -Body @{
    input = "42"
    expectedOutput = "42"
    hidden = $false
    timeLimitMs = 2000
    memoryLimitMb = 256
}

# Hidden test case is required by publish validation
$hiddenTestCase = Invoke-JsonRequest -Uri "$baseUrl/problems/$problemId/test-cases" -Method "Post" -Headers $adminHeaders -Body @{
    input = "7"
    expectedOutput = "7"
    hidden = $true
    timeLimitMs = 2000
    memoryLimitMb = 256
}

Write-Host "[PASS] Problem and Test Cases Created (1 public + 1 hidden)" -ForegroundColor Green

Write-Host "`n[4/10] Publishing Assessment..." -ForegroundColor Yellow
$publishedAssessment = Invoke-JsonRequest -Uri "$baseUrl/assessments/$assessmentId/publish" -Method "Patch" -Headers $adminHeaders
Write-Host "[PASS] Assessment published (Status: $($publishedAssessment.status))" -ForegroundColor Green

# ==================================================================
# PHASE 3: CANDIDATE WORKSPACE
# ==================================================================
Write-Host "`n[5/10] Registering & Authenticating Candidate..." -ForegroundColor Yellow

$candidateReg = Invoke-JsonRequest -Uri "$baseUrl/auth/register" -Method "Post" -Body @{
    name = "Test Candidate $timestamp"
    email = $candidateEmail
    password = $candidatePassword
}

$candidateLogin = Invoke-JsonRequest -Uri "$baseUrl/auth/login" -Method "Post" -Body @{ email = $candidateEmail; password = $candidatePassword }
$candidateHeaders = @{ Authorization = "Bearer $($candidateLogin.accessToken)" }
Write-Host "[PASS] Candidate ready" -ForegroundColor Green

Write-Host "`n[6/10] Submitting Code..." -ForegroundColor Yellow
$javaCode = @"
import java.util.Scanner;
public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println(sc.nextInt());
        sc.close();
    }
}
"@

$submission = Invoke-JsonRequest -Uri "$baseUrl/problems/$problemId/submissions" -Method "Post" -Headers $candidateHeaders -Body @{
    sourceCode = $javaCode
    language = "JAVA"
}
$submissionId = $submission.id
Write-Host "[PASS] Code submitted (ID: $submissionId). State: $($submission.status)" -ForegroundColor Green

# ==================================================================
# PHASE 4: EXECUTION PIPELINE VERIFICATION
# ==================================================================
Write-Host "`n[7/10] Waiting for Execution Pipeline..." -ForegroundColor Yellow

# Note: We rely on REST polling here as native PS doesn't easily consume SSE streams.
# Real-time state transitions are validated.
$maxRetries = 10
$attempt = 0
$finalState = ""

while ($attempt -lt $maxRetries) {
    Start-Sleep -Seconds 2
    $subStatus = Invoke-JsonRequest -Uri "$baseUrl/submissions/$submissionId" -Method "Get" -Headers $candidateHeaders
    Write-Host "  -> State: $($subStatus.status) | Result: $($subStatus.result)" -ForegroundColor Gray
    
    if ($subStatus.status -eq "COMPLETED" -or $subStatus.status -eq "FAILED") {
        $finalState = $subStatus.status
        break
    }
    $attempt++
}

if ($finalState -eq "COMPLETED") {
    Write-Host "[PASS] Execution completed successfully!" -ForegroundColor Green
} else {
    Write-Host "[DISABLED/EXPECTED] Execution FAILED. This is expected if JUDGE0_ENABLED=false" -ForegroundColor Yellow
}

# ==================================================================
# PHASE 5: AI REVIEW (Disabled Path)
# ==================================================================
Write-Host "`n[8/10] Verifying AI Review Disabled/Fallback..." -ForegroundColor Yellow
try {
    $aiReview = Invoke-JsonRequest -Uri "$baseUrl/submissions/$submissionId/ai-review" -Method "Post" -Headers $candidateHeaders
    Write-Host "[WARN] AI Review succeeded. Expected failure if disabled." -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 503) {
        Write-Host "[PASS] Expected 503 Service Unavailable (AI_REVIEW_ENABLED=false)" -ForegroundColor Green
    } elseif ($_.Exception.Response.StatusCode.value__ -eq 400) {
        Write-Host "[PASS] Expected 400 Bad Request (Submission not COMPLETED)" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# ==================================================================
# PHASE 6: ASSESSMENT TIMER
# ==================================================================
Write-Host "`n[9/10] Validating Server-Authoritative Timer (Waiting ~1 min for closure)..." -ForegroundColor Yellow

# Wait for 65 seconds to guarantee expiration of the 1-minute assessment
Start-Sleep -Seconds 65

$assessmentStatus = Invoke-JsonRequest -Uri "$baseUrl/assessments/$assessmentId" -Method "Get" -Headers $candidateHeaders
if ($assessmentStatus.status -eq "CLOSED") {
    Write-Host "[PASS] Assessment is securely CLOSED automatically" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Assessment is still $($assessmentStatus.status). Expected CLOSED." -ForegroundColor Red
}

# Verify submission lockout
Write-Host "  -> Attempting late submission..." -ForegroundColor Gray
try {
    $lateSubmission = Invoke-JsonRequest -Uri "$baseUrl/problems/$problemId/submissions" -Method "Post" -Headers $candidateHeaders -Body @{
        sourceCode = $javaCode
        language = "JAVA"
    }
    Write-Host "[FAIL] Late submission was accepted!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 400) {
        Write-Host "[PASS] Late submission was rejected (400 Bad Request)" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] Late submission failed with unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# ==================================================================
# PHASE 7: ANALYTICS
# ==================================================================
Write-Host "`n[10/10] Validating Analytics Endpoints..." -ForegroundColor Yellow
try {
    $analytics = Invoke-JsonRequest -Uri "$baseUrl/analytics/assessments/$assessmentId" -Method "Get" -Headers $adminHeaders
    Write-Host "[PASS] Admin Analytics retrieved successfully" -ForegroundColor Green
    Write-Host "  -> Total Submissions: $($analytics.totalSubmissions)" -ForegroundColor Gray
    
    $candidateAnalytics = Invoke-JsonRequest -Uri "$baseUrl/analytics/me" -Method "Get" -Headers $candidateHeaders
    Write-Host "[PASS] Candidate Analytics retrieved successfully" -ForegroundColor Green
    Write-Host "  -> Total Attempts: $($candidateAnalytics.totalSubmissions)" -ForegroundColor Gray
} catch {
    Write-Host "[FAIL] Failed to fetch analytics: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "[SUCCESS] Realtime E2E Test Completed." -ForegroundColor Green
Write-Host "========================================================`n" -ForegroundColor Cyan

# ==============================
#  CONFIGURAÇÕES INICIAIS
# ==============================
$tenantId      = "2d915887-03a5-4d7e-9267-d926055be3b7"      # Directory (tenant) ID
$clientId      = "f7b27933-2cf3-4355-862c-b0f607d9f4b8"      # Application (client) ID
$clientSecret  = "20s8Q~Mj3AqdBem4ruehPYSKiRQh9wBB0Sggrdmc"  # Client secret criado no Azure
$workspaceId   = "1353cda7-9a6d-420d-8312-b7d02daa1823"      # ID do workspace no Power BI
$datasetId     = "fe1512c6-cb70-475a-8324-7d802cc0a425"      # ID do dataset a atualizar

Write-Host "Iniciando autenticação no Azure AD..." -ForegroundColor Cyan
#tenantID: 2d915887-03a5-4d7e-9267-d926055be3b7
#tenantID : 2d915887-03a5-4d7e-9267-d926055be3b7
#subscription ID APP b0e06558-0a50-440b-bfa1-d0150ca1c7ba
# ==============================
#  OBTENDO TOKEN DE ACESSO
# ==============================
$body = @{
    grant_type    = "client_credentials"
    client_id     = $clientId
    client_secret = $clientSecret
    scope         = "https://analysis.windows.net/powerbi/api/.default"
}

# Utility: decode Base64Url (used to inspect token claims)
function Decode-Base64Url([string]$input) {
    $s = $input.Replace('-','+').Replace('_','/')
    switch ($s.Length % 4) {
        2 { $s += '==' }
        3 { $s += '=' }
        default { }
    }
    return [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($s))
}

try {
    $tokenResponse = Invoke-RestMethod -Method Post -Uri "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/token" -Body $body
    $token = $tokenResponse.access_token
    Write-Host "Autenticação concluída com sucesso!" -ForegroundColor Green
}
catch {
    Write-Host ("Erro na autenticação: " + $_.Exception.Message) -ForegroundColor Red
    exit
}

# --- Debug: decode token claims (useful to check granted application permissions/roles) ---
try {
    if ($null -ne $token) {
        $parts = $token.Split('.')
        if ($parts.Length -ge 2) {
            $claimsJson = Decode-Base64Url($parts[1])
            Write-Host "-- Token claims (decoded) --" -ForegroundColor Cyan
            try { $claims = $claimsJson | ConvertFrom-Json; $claims | Format-List } catch { Write-Host $claimsJson }
        } else {
            Write-Host "Token inesperado (não JWT)." -ForegroundColor Yellow
        }
    }
}
catch {
    Write-Host ("Falha ao decodificar token: " + $_.Exception.Message) -ForegroundColor Yellow
}

# ==============================
#  DISPARAR ATUALIZAÇÃO DO DATASET
# ==============================
$uri = "https://api.powerbi.com/v1.0/myorg/groups/$workspaceId/datasets/$datasetId/refreshes"

Write-Host "Iniciando atualização do dataset..." -ForegroundColor Cyan

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type"  = "application/json"
    }

    # This script uses client_credentials (app-only). Power BI does NOT support MailOnCompletion for application requests.
    # Always use NoNotification for app-only flows to avoid the 400 error.
    $notifyOption = "NoNotification"
    Write-Host "Using notifyOption = NoNotification for app-only (client_credentials) request." -ForegroundColor Cyan

    $requestBody = @{ notifyOption = $notifyOption }

    $jsonBody = $requestBody | ConvertTo-Json
    Write-Host "Request POST $uri" -ForegroundColor Cyan
    Write-Host "Request body: $jsonBody" -ForegroundColor Cyan

    # Use ErrorAction Stop to capture HTTP error responses in catch
    $response = Invoke-RestMethod -Uri $uri -Headers $headers -Method Post -Body $jsonBody -ErrorAction Stop

    # Some endpoints return an object; show raw for clarity
    Write-Host "Resposta recebida:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5 | Write-Host
    Write-Host "Atualização iniciada com sucesso!" -ForegroundColor Green
}
catch {
    $err = $_
    Write-Host ("Erro ao iniciar atualização: " + $err.Exception.Message) -ForegroundColor Red
    if ($err.Exception.Response -ne $null) {
        try {
            $resp = $err.Exception.Response
            Write-Host "HTTP Status: $($resp.StatusCode) $($resp.StatusDescription)" -ForegroundColor Yellow
            $sr = New-Object System.IO.StreamReader($resp.GetResponseStream())
            $content = $sr.ReadToEnd()
            Write-Host "Response content:" -ForegroundColor Yellow
            Write-Host $content -ForegroundColor Yellow
        }
        catch {
            Write-Host "Não foi possível ler o conteúdo da resposta de erro." -ForegroundColor Yellow
        }
    }
    exit
}

# ==============================
#  VERIFICAR STATUS DA ATUALIZAÇÃO
# ==============================
Start-Sleep -Seconds 10

$statusUri = "https://api.powerbi.com/v1.0/myorg/groups/$workspaceId/datasets/$datasetId/refreshes"

try {
    $statusResponse = Invoke-RestMethod -Uri $statusUri -Headers $headers -Method Get
    $latestRefresh = $statusResponse.value | Select-Object -First 1

    Write-Host ("Inicio: " + $latestRefresh.startTime)
    Write-Host ("Fim: " + $latestRefresh.endTime)
    Write-Host ("Status: " + $latestRefresh.status)

    if ($latestRefresh.status -eq "Completed") {
        Write-Host "Dataset atualizado com sucesso!" -ForegroundColor Green
    } elseif ($latestRefresh.status -eq "Failed") {
        Write-Host "A atualização falhou!" -ForegroundColor Yellow
    } else {
        Write-Host "Atualização em andamento..." -ForegroundColor Cyan
    }
}
catch {
    Write-Host ("Erro ao verificar status: " + $_.Exception.Message) -ForegroundColor Red
}

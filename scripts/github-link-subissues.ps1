# ================================================================
# 기존 이슈(47~108)에 GraphQL addSubIssue로 계층 연결
#
# 사전 조건: github-projects-setup.ps1 으로 이슈가 이미 생성된 상태
#            이슈 번호: Epic 47,66,90 / Week 48,52,56,61,67,72,77,81,85,91,95,98,103
#
# 실행:
#   .\scripts\github-link-subissues.ps1
# ================================================================

$Owner = "yooyongbeom"
$RepoName = "bx-cf-be"

# 이슈 번호 → GraphQL node ID 캐시
$IdCache = @{}

function Get-NodeId($number) {
    if ($IdCache.ContainsKey($number)) { return $IdCache[$number] }
    $q = 'query($owner:String!,$repo:String!,$num:Int!){repository(owner:$owner,name:$repo){issue(number:$num){id}}}'
    $result = gh api graphql --field "query=$q" --field "owner=$Owner" --field "repo=$RepoName" --field "num=$number" | ConvertFrom-Json
    $nodeId = $result.data.repository.issue.id
    $IdCache[$number] = $nodeId
    return $nodeId
}

function Link($parentNum, $childNum) {
    Write-Host "  #$parentNum ← #$childNum" -ForegroundColor Cyan
    $parentId = Get-NodeId $parentNum
    $childId  = Get-NodeId $childNum
    $m = 'mutation($p:ID!,$c:ID){addSubIssue(input:{issueId:$p,subIssueId:$c}){issue{number}subIssue{number}}}'
    gh api graphql --field "query=$m" --field "p=$parentId" --field "c=$childId" | Out-Null
}

Write-Host "`n[1단계] Epic #47 계층 연결..." -ForegroundColor Yellow

# Epic #47 ← Week
# #48은 이미 연결됨 (테스트에서)
Link 47 52
Link 47 56
Link 47 61

Write-Host "`n  [W1] #48 ← Tasks" -ForegroundColor DarkCyan
Link 48 49
Link 48 50
Link 48 51

Write-Host "`n  [W2] #52 ← Tasks" -ForegroundColor DarkCyan
Link 52 53
Link 52 54
Link 52 55

Write-Host "`n  [W3] #56 ← Tasks" -ForegroundColor DarkCyan
Link 56 57
Link 56 58
Link 56 59
Link 56 60

Write-Host "`n  [W4] #61 ← Tasks" -ForegroundColor DarkCyan
Link 61 62
Link 61 63
Link 61 64
Link 61 65

Write-Host "`n[2단계] Epic #66 계층 연결..." -ForegroundColor Yellow

Link 66 67
Link 66 72
Link 66 77
Link 66 81
Link 66 85

Write-Host "`n  [W5] #67 ← Tasks" -ForegroundColor DarkCyan
Link 67 68
Link 67 69
Link 67 70
Link 67 71

Write-Host "`n  [W6] #72 ← Tasks" -ForegroundColor DarkCyan
Link 72 73
Link 72 74
Link 72 75
Link 72 76

Write-Host "`n  [W7] #77 ← Tasks" -ForegroundColor DarkCyan
Link 77 78
Link 77 79
Link 77 80

Write-Host "`n  [W8] #81 ← Tasks" -ForegroundColor DarkCyan
Link 81 82
Link 81 83
Link 81 84

Write-Host "`n  [W9] #85 ← Tasks" -ForegroundColor DarkCyan
Link 85 86
Link 85 87
Link 85 88
Link 85 89

Write-Host "`n[3단계] Epic #90 계층 연결..." -ForegroundColor Yellow

Link 90 91
Link 90 95
Link 90 98
Link 90 103

Write-Host "`n  [W10] #91 ← Tasks" -ForegroundColor DarkCyan
Link 91 92
Link 91 93
Link 91 94

Write-Host "`n  [W11] #95 ← Tasks" -ForegroundColor DarkCyan
Link 95 96
Link 95 97

Write-Host "`n  [W12] #98 ← Tasks" -ForegroundColor DarkCyan
Link 98 99
Link 98 100
Link 98 101
Link 98 102

Write-Host "`n  [W13] #103 ← Tasks" -ForegroundColor DarkCyan
Link 103 104
Link 103 105
Link 103 106
Link 103 107
Link 103 108

Write-Host "`n✅ 완료! https://github.com/$Owner/$RepoName/issues/47 에서 확인하세요." -ForegroundColor Green
